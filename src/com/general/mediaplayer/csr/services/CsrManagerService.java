package com.general.mediaplayer.csr.services;

import android.app.ActivityManager;
import android.app.Service;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import com.general.mediaplayer.csr.R;
import com.general.mediaplayer.csr.Settings;
import com.general.mediaplayer.csr.services.AlarmHelper;
import com.hklt.watchdog.wdg;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Iterator;

public class CsrManagerService extends Service {

   private static final String DIR_SUPER_MANAGER_KEY = "/mnt/external_sd/AdministratorPassword";
   private static final String IN_SUPER_MODE_KEY = "inSuperModeKey";
   private static final int MSG_BASE = 10000;
   private static final int MSG_TIMER = 10001;
   private static final String RECEIVER_APP_RUN_ACTION = "com.general.mediaplayer.receiverapprun";
   private static final String RESET_24HOUR_ACTION = "com.general.mediaplayer.24HourReset";
   private static final String RESTART_WATCHDOG_ACTION = "com.general.mediaplayer.RESTARTWATCHDOG";
   private static final String SEND_APP_RUN_ACTION = "com.general.mediaplayer.sendapprun";
   private static final String STOP_WATCHDOG_ACTION = "com.general.mediaplayer.STOPWATCHDOG";
   private static final int SUPER_MANAGER_KEY_SURE_CNT = 4;
   private static final String SUPER_MANAGER_MODE_IN_ACTION = "com.general.mediaplayer.startsupermode";
   private static final String SYS_CSR_RESTART_ACTION = "com.general.mediaplayer.csr.restart";
   static final String TAG = "CsrManagerService";
   private static final String stringAPK_CSR_Package_Spec = "com.general.mediaplayer.csr";
   private static final String stringAPK_Package_Spec = "com.general.mediaplayer";
   private final int APP_RUN_TIMEOUT_MAX = 60;
   private BroadcastReceiver m24HourResetReceiver = new BroadcastReceiver() {
      public void onReceive(Context context, Intent intent) {
         if(intent.getAction().equalsIgnoreCase(RESET_24HOUR_ACTION)) {
            Log.v("ResetTime", "============onReceive====m24HourResetReceiver====" + intent.getAction());
            CsrManagerService.this.set24HourReset();
         } else {
            if(intent.getAction().equalsIgnoreCase(RECEIVER_APP_RUN_ACTION)) {
               CsrManagerService.this.mAppRunTimeoutCnt = 0;
               if(intent.getBooleanExtra("Csr_app_run", false)) {
                  CsrManagerService.this.mAppRunTimeoutCntMax = 480;
                  return;
               }

               CsrManagerService.this.mAppRunTimeoutCntMax = APP_RUN_TIMEOUT_MAX;
               return;
            }

            if(intent.getAction().equalsIgnoreCase(SYS_CSR_RESTART_ACTION)) {
               Log.v(TAG, "====m24HourResetReceiver===com.general.mediaplayer.csr.restart");
               if(intent.getBooleanExtra("Csr_app_run", false)) {
                  CsrManagerService.this.mAppRunTimeoutCntMax = 480;
               } else {
                  CsrManagerService.this.mAppRunTimeoutCntMax = APP_RUN_TIMEOUT_MAX;
               }

               CsrManagerService.this.startRunCsrThread(context, intent);
               Log.v(TAG, "====m24HourResetReceiver=== end");
               return;
            }

            if(intent.getAction().equalsIgnoreCase(RESTART_WATCHDOG_ACTION)) {
               CsrManagerService.this.mIsStopWatchdog = false;
               return;
            }

            if(intent.getAction().equalsIgnoreCase(STOP_WATCHDOG_ACTION)) {
               CsrManagerService.this.mIsStopWatchdog = true;
               return;
            }
         }

      }
   };
   private AlarmHelper mAlarmHelper;
   private int mAppRunTimeoutCnt = 0;
   private int mAppRunTimeoutCntMax = APP_RUN_TIMEOUT_MAX;
   private Context mContext;
   private int mCurrentHour = 0;
   private int mCurrentMinute = 0;
   private Editor mEditor;
   private long mFristStartTimeMs = 0L;
   private Handler mHandler = new Handler() {
      public void handleMessage(Message msg) {
         switch(msg.what) {
         case MSG_TIMER:
         default:
         }
      }
   };
   private boolean mIsEnableReset = false;
   private boolean mIsStopWatchdog = false;
   private boolean mIsWdgOutHigh = false;
   private boolean mPowerOnFirst = false;
   private int mPowerOnFirstCnt = 0;
   private int mQuitRunCsrCnt = 0;
   private boolean mQuitRunCsrThread = false;
   private boolean mRebootIsDo = false;
   private int mResetHour = 2;
   private int mResetMinute = 0;
   private int mSendSuperManagerModeMessageCnt = 0;
   private SharedPreferences mSharedPreferences;
   private int mSuperManagerKeyIn = 0;
   private int mSuperManagerKeyout = 0;
   private boolean mSuperManagerMode = false;
   private Runnable mTimerRunable = new Runnable() {
      public void run() {
         CsrManagerService.this.onTimer();
         CsrManagerService.this.mHandler.postDelayed(this, 500L);
      }
   };
   private wdg mwdg;
   private int mwdgLogCnt = 0;


   // $FF: synthetic method
   static int access$708(CsrManagerService var0) {
      int var1 = var0.mQuitRunCsrCnt;
      var0.mQuitRunCsrCnt = var1 + 1;
      return var1;
   }

   private boolean checkSuperManagerKey() {
      File var1 = new File(DIR_SUPER_MANAGER_KEY);
      boolean var2 = var1.exists();
      boolean var3 = false;
      if(var2) {
         boolean var4 = var1.isDirectory();
         var3 = false;
         if(var4) {
            var3 = true;
         }
      }

      return var3;
   }

   private void get24HourResetInfo() {
      String strMediaplayerSettingSp = this.getResources().getString(R.string.mediaplayer_setting_sp);
      String strAlarmHourKey = this.getResources().getString(R.string.alarm_set_hour_sp_key);
      String strAlarmMinuteKey = this.getResources().getString(R.string.alarm_set_minute_sp_key);
      String strOpenAlarmKey = this.getResources().getString(R.string.open_alarm_sp_key);
      this.mSharedPreferences = this.getApplicationContext().getSharedPreferences(strMediaplayerSettingSp, 2);
      this.mResetHour = this.mSharedPreferences.getInt(strAlarmHourKey, 2);
      this.mResetMinute = this.mSharedPreferences.getInt(strAlarmMinuteKey, 0);
      this.mIsEnableReset = this.mSharedPreferences.getBoolean(strOpenAlarmKey, false);
      if(this.mResetHour >= 24) {
         this.mResetHour %= 24;
      }

      if(this.mResetMinute >= 60) {
         this.mResetMinute %= 60;
      }

      Log.v(" ", "==get24HourResetInfo==mResetHour=" + this.mResetHour + "==mResetMinute=" + this.mResetMinute);
      int var6 = Calendar.getInstance().getTimeZone().getRawOffset() / 1000 / 60;
      int var7 = 60 * this.mResetHour + this.mResetMinute;
      Log.v(" ", "==get24HourResetInfo==currentTimeOffSetMin=" + var6 + "==alarmTimeMin=" + var7);
      int var9 = var7 + var6;
      if(var9 >= 0) {
         if(var9 >= 1440) {
            var9 -= 1440;
         }
      } else {
         var9 += 1440;
      }

      int var10 = var9 / 60;
      int var11 = var9 % 60;
      Log.v(" ", "==get24HourResetInfo==mResetHour=" + var10 + "==mResetMinute=" + var11);
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      this.mCurrentHour = calendar.get(11);
      this.mCurrentMinute = calendar.get(12);
      if(var10 > this.mCurrentHour) {
         if(var11 > this.mCurrentMinute) {
            this.mFristStartTimeMs = (long)(1000 * 60 * 60 * (var10 - this.mCurrentHour) + 1000 * 60 * (var11 - this.mCurrentMinute));
         } else {
            this.mFristStartTimeMs = (long)(1000 * 60 * 60 * (-1 + (var10 - this.mCurrentHour)) + 1000 * 60 * (var11 + 60 - this.mCurrentMinute));
         }
      } else if(var10 == this.mCurrentHour) {
         if(var11 > this.mCurrentMinute) {
            this.mFristStartTimeMs = (long)(1000 * 60 * 60 * (var10 - this.mCurrentHour) + 1000 * 60 * (var11 - this.mCurrentMinute));
         } else {
            this.mFristStartTimeMs = (long)(1000 * 60 * 60 * (24 + -1 + (var10 - this.mCurrentHour)) + 1000 * 60 * (var11 + 60 - this.mCurrentMinute));
         }
      } else if(var11 > this.mCurrentMinute) {
         this.mFristStartTimeMs = (long)(1000 * 60 * 60 * (24 + (var10 - this.mCurrentHour)) + 1000 * 60 * (var11 - this.mCurrentMinute));
      } else {
         this.mFristStartTimeMs = (long)(1000 * 60 * 60 * (24 + -1 + (var10 - this.mCurrentHour)) + 1000 * 60 * (var11 + 60 - this.mCurrentMinute));
      }

      Log.v("ResetTime ", "mCurrentHour=" + this.mCurrentHour + "  mCurrentMinute=" + this.mCurrentMinute);
      Log.v("ResetTime ", "mResetHour=" + var10 + "  mResetMinute=" + var11);
      Log.v("ResetTime ", "mFristStartTimeMs=" + this.mFristStartTimeMs + " mIsEnableReset=" + this.mIsEnableReset);
   }

   private void getTopTask(Context context) {
      RunningTaskInfo runningTaskInfo = (RunningTaskInfo)((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getRunningTasks(1).get(0);
      if(runningTaskInfo != null) {
         String strPackName = runningTaskInfo.topActivity.getPackageName();
         String strClassName = runningTaskInfo.topActivity.getClassName();
         if(!this.IsSpecAppPackage(strPackName, stringAPK_Package_Spec)) {
            if(strPackName.equalsIgnoreCase(stringAPK_CSR_Package_Spec)) {
               this.mAppRunTimeoutCnt = 0;
               return;
            }

            Log.v(TAG, "==getTopTask==strPackName=" + strPackName);
            Log.v(TAG, "==getTopTask==strClassName=" + strClassName);
            Log.v(TAG, "==getTopTask==IsSpecAppPackage=false=");
            return;
         }

         this.mAppRunTimeoutCnt = 0;
         this.mAppRunTimeoutCntMax = APP_RUN_TIMEOUT_MAX;
      }
   }

   private void onTimer() {
      this.testSuperManagerMode();
      this.sendWatchDogSign();
      this.mAppRunTimeoutCnt++;
      if(this.mAppRunTimeoutCnt >= this.mAppRunTimeoutCntMax && !this.mSuperManagerMode) {
         Log.v("onTimer", "============onTimer====APP_RUN_TIMEOUT_MAX====");
         this.startReboot();
      }

      Intent intent = new Intent();
      intent.setAction(SEND_APP_RUN_ACTION);
      intent.putExtra(IN_SUPER_MODE_KEY, this.mSuperManagerMode);
      this.sendBroadcast(intent);
      this.getTopTask(this.mContext);
   }

   private void save24HourResetInMillis(Long interval) {
      Log.v(TAG, "======save24HourResetInMillis==millis=" + interval);
      String strMediaplayerSettingSp = this.getResources().getString(R.string.mediaplayer_setting_sp);
      String strAlarmSetInMillisSp = this.getResources().getString(R.string.alarm_set_inmillis_sp_key);
      this.mSharedPreferences = this.getApplicationContext().getSharedPreferences(strMediaplayerSettingSp, 2);
      Editor editor = this.mSharedPreferences.edit();
      if(interval.longValue() > 0L) {
         editor.putLong(strAlarmSetInMillisSp, interval.longValue());
      }
      editor.commit();
   }

   private void sendSuperManagerKeyInMessage() {
      Log.v(TAG, "==CsrManagerService==sendSuperManagerKeyInMessage==com.general.mediaplayer.startsupermode");
      Intent var2 = new Intent();
      var2.setAction(SUPER_MANAGER_MODE_IN_ACTION);
      this.sendBroadcast(var2);
   }

   private void sendWatchDogSign() {
      if(!this.mIsStopWatchdog) {
         if(this.mIsWdgOutHigh) {
            this.mIsWdgOutHigh = false;
            if(this.mwdg != null) {
               this.mwdg.wdg_sw(0);
            }
         } else {
            this.mIsWdgOutHigh = true;
            if(this.mwdg != null) {
               this.mwdg.wdg_sw(1);
            }
         }

         int var2 = 1 + this.mwdgLogCnt;
         this.mwdgLogCnt = var2;
         if(var2 >= 29) {
            if(this.mIsWdgOutHigh) {
               if(this.mwdg != null) {
                  Log.v("", "==============mwdg.wdg_sw(1)============");
               }
            } else if(this.mwdg != null) {
               Log.v("", "==============mwdg.wdg_sw(0)============");
            }

            if(this.mwdgLogCnt >= 30) {
               this.mwdgLogCnt = 0;
               return;
            }
         }
      }

   }

   private void set24HourReset() {
      Log.v("ResetTime", "=============set24HourReset====");
      this.get24HourResetInfo();
      Calendar calendar = Calendar.getInstance();
      calendar.setTimeInMillis(System.currentTimeMillis());
      if(this.mIsEnableReset) {
         this.mAlarmHelper.openAlarm(0, calendar.getTimeInMillis() + this.mFristStartTimeMs, 86400000L);
         this.save24HourResetInMillis(Long.valueOf(calendar.getTimeInMillis() + this.mFristStartTimeMs));
      } else {
         this.mAlarmHelper.closeAlarm(0);
      }
   }

   private void startReboot() {
      Log.v(TAG, "==CsrManagerService==startReboot==");
      if(!this.mRebootIsDo) {
         Log.v(TAG, "==CsrManagerService==startReboot==do=========");
         this.mRebootIsDo = true;

         try {
            Runtime.getRuntime().exec("su -c reboot");
         } catch (IOException var4) {
            this.mRebootIsDo = false;
            var4.printStackTrace();
            return;
         }
      }

   }

   private void testSuperManagerMode() {
      if(this.mPowerOnFirst) {
         this.mPowerOnFirstCnt = this.mPowerOnFirstCnt + 1;
         if(this.mPowerOnFirstCnt >= 40) {
            Log.v(TAG, "mPowerOnFirstCnt=" + this.mPowerOnFirstCnt);
            this.mPowerOnFirstCnt = 0;
            this.mPowerOnFirst = false;
         }
      } else {
         if(!this.mSuperManagerMode) {
            if(this.checkSuperManagerKey()) {
               this.mSuperManagerKeyout = 0;
               int var4 = 1 + this.mSuperManagerKeyIn;
               this.mSuperManagerKeyIn = var4;
               if(var4 >= 4) {
                  this.mSuperManagerMode = true;
                  this.mSendSuperManagerModeMessageCnt = 0;
               }
            }
         } else if(!this.checkSuperManagerKey()) {
            this.mSuperManagerKeyIn = 0;
            int var1 = 1 + this.mSuperManagerKeyout;
            this.mSuperManagerKeyout = var1;
            if(var1 >= 4) {
               this.mSuperManagerMode = false;
               Log.v("onTimer", "============testSuperManagerMode====testSuperManagerMode====");
               this.startReboot();
            }
         }

         if(this.mSuperManagerMode) {
            int var3 = 1 + this.mSendSuperManagerModeMessageCnt;
            this.mSendSuperManagerModeMessageCnt = var3;
            if(var3 < 5) {
               this.sendSuperManagerKeyInMessage();
               return;
            }
         }
      }

   }

   public boolean IsSpecAppPackage(String var1, String var2) {
      if(var1 != null) {
         int var3 = var1.lastIndexOf('.');
         if(!stringAPK_CSR_Package_Spec.equalsIgnoreCase(var1) && var3 > 0 && var2.equalsIgnoreCase(var1.substring(0, var3))) {
            return true;
         }
      }

      return false;
   }

   public boolean getRunningProcess(Context context, Intent intent) {
      boolean var3 = false;
      ActivityManager activityManager = (ActivityManager)this.getSystemService(Context.ACTIVITY_SERVICE);
      Iterator appIterator = activityManager.getRunningAppProcesses().iterator();

      while(appIterator.hasNext()) {
         RunningAppProcessInfo appProcessInfo = (RunningAppProcessInfo)appIterator.next();
         if(!appProcessInfo.processName.equals("system")
                 && !appProcessInfo.processName.equals("com.android.phone")
                 && this.IsSpecAppPackage(appProcessInfo.processName, stringAPK_Package_Spec)) {
            Log.v(TAG, appProcessInfo.processName + " is Spec Package ");
            var3 = true;
            if(this.mQuitRunCsrCnt > 300) {
               //activityManager.forceStopPackage(appProcessInfo.processName);
                try {
                    Method forceStopPackage = activityManager.getClass().getDeclaredMethod("forceStopPackage", String.class);
                    forceStopPackage.setAccessible(true);
                    forceStopPackage.invoke(activityManager, appProcessInfo.processName);
                } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
         }
      }

      return var3;
   }

    @Override
   public IBinder onBind(Intent intent) {
      return null;
   }

    @Override
   public void onCreate() {
      super.onCreate();
      Log.v(TAG, "===onCreate==");

      try {
         this.mwdg = wdg.newInstance();
      } catch (Exception var8) {
         this.mwdg = null;
         Log.v(TAG, "===CsrManagerService load wdg failed===");
      }

      this.mContext = this.getApplicationContext();
      this.mAlarmHelper = new AlarmHelper(this.mContext);
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(RESET_24HOUR_ACTION);
      intentFilter.addAction(RECEIVER_APP_RUN_ACTION);
      intentFilter.addAction(SYS_CSR_RESTART_ACTION);
      this.registerReceiver(this.m24HourResetReceiver, intentFilter);
      this.mHandler.postDelayed(this.mTimerRunable, 500L);
      Intent sendIntent = new Intent();
      sendIntent.setAction(RESET_24HOUR_ACTION);
      this.sendBroadcast(sendIntent);
      this.mAppRunTimeoutCntMax = APP_RUN_TIMEOUT_MAX;
      this.mPowerOnFirst = true;
      this.mPowerOnFirstCnt = 0;
   }

    @Override
   public void onDestroy() {
      this.unregisterReceiver(this.m24HourResetReceiver);
      Log.v(TAG, "===onDestroy==");
      this.mHandler.removeCallbacks(this.mTimerRunable);
      super.onDestroy();
   }

   public void startRunCsr(Context context, Intent intent) {
      Intent newTaskIntent = new Intent(context, Settings.class);
      newTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(newTaskIntent);
   }

   public void startRunCsrThread(final Context context, final Intent intent) {
      Log.v(TAG, "startRunCsrThread Start");
      this.mQuitRunCsrThread = false;
      this.mQuitRunCsrCnt = 0;
      (new Thread() {
         public void run() {
            while(!CsrManagerService.this.mQuitRunCsrThread) {
               if(!CsrManagerService.this.getRunningProcess(context, intent)) {
                  CsrManagerService.this.mQuitRunCsrThread = true;
               } else {
                  try {
                     Thread.sleep(10L);
                  } catch (InterruptedException var3) {
                     var3.printStackTrace();
                  }

                  CsrManagerService.access$708(CsrManagerService.this);
                  if(CsrManagerService.this.mQuitRunCsrCnt > 500) {
                     CsrManagerService.this.mQuitRunCsrCnt = 0;
                     CsrManagerService.this.mQuitRunCsrThread = true;
                  }
               }
            }

            Log.v(TAG, "startRunCsrThread end");
            CsrManagerService.this.startRunCsr(context, intent);
         }
      }).start();
   }
}
