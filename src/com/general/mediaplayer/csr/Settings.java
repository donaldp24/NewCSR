package com.general.mediaplayer.csr;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.app.Instrumentation;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
//import android.os.ServiceManager;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceActivity.Header;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
//import android.view.IWindowManager;
//import android.view.IWindowManager.Stub;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
//import com.general.mediaplayer.csr.accounts.AccountSyncSettings;
//import com.general.mediaplayer.csr.applications.ManageApplications;
//import com.general.mediaplayer.csr.bluetooth.BluetoothEnabler;
//import com.general.mediaplayer.csr.fuelgauge.PowerUsageSummary;
//import com.general.mediaplayer.csr.services.CsrManagerService;
//import com.general.mediaplayer.csr.wifi.WifiEnabler;
import com.hklt.hidusb.Hidusb;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Settings extends PreferenceActivity
  implements ButtonBarHandler
{
  private static final String DIR_SUPER_MANAGER_KEY = "/mnt/usbhost1/AdministratorPassword";
  private static final String EXTRA_CLEAR_UI_OPTIONS = "settings:remove_ui_options";
  private static final String IN_SUPER_MODE_KEY = "inSuperModeKey";
  private static final String LOG_TAG = "Settings";
  private static final String META_DATA_KEY_FRAGMENT_CLASS = "com.general.mediaplayer.csr.FRAGMENT_CLASS";
  private static final String META_DATA_KEY_HEADER_ID = "com.general.mediaplayer.csr.TOP_LEVEL_HEADER_ID";
  private static final String META_DATA_KEY_PARENT_FRAGMENT_CLASS = "com.general.mediaplayer.csr.PARENT_FRAGMENT_CLASS";
  private static final String META_DATA_KEY_PARENT_TITLE = "com.general.mediaplayer.csr.PARENT_FRAGMENT_TITLE";
  private static final int MSG_BASE = 200;
  public static final int MSG_IDSCAN_INIT_OK = 210;
  public static final int MSG_OPEN_DEVICE_FAILED = 205;
  public static final int MSG_OPEN_DEVICE_SUCCESS = 204;
  public static final int MSG_RESET_USB_END = 207;
  private static final int MSG_RESTART_APP_AGAIN = 201;
  public static final int MSG_SET_LED = 206;
  public static final int MSG_TIMER = 202;
  public static final int MSG_UPDATE_IDBUFFER = 203;
  public static final int MSG_UPDATE_PLAYERFILE = 211;
  public static final int MSG_UPDATE_VIDEO_SCREEN = 212;
  private static final String MediaPlayer_START_ACTIVITY = ".ScanMediaActivity";
  private static final String RECEIVER_APP_RUN_ACTION = "com.general.mediaplayer.receiverapprun";
  private static final String SAVE_KEY_CURRENT_HEADER = "com.general.mediaplayer.csr.CURRENT_HEADER";
  private static final String SAVE_KEY_PARENT_HEADER = "com.general.mediaplayer.csr.PARENT_HEADER";
  private static final String SEND_APP_RUN_ACTION = "com.general.mediaplayer.sendapprun";
  private static final String SUPER_MANAGER_MODE_IN_ACTION = "com.general.mediaplayer.startsupermode";
  public static final String TAG = "CSR Settings";
  private final long EXIT_CSR_TIME_OUT = 120000L;
  private int KeyStateCurr = 0;
  private int KeyStateFlag1 = 0;
  private int KeyStatePrev = 0;
  private boolean MCU_Key_State_clear = false;
  public String MCU_VersionStr = "";
  private Toast ToastOPEN_DEVICE_FAILED = null;
  private Toast ToastUPDATE_VIDEO_SCREEN = null;
  private int USBHIDScanCode = 0;
  private boolean USBHID_USE_FLAG = false;
  private int mActiveSlaveNum = 0;
  private int mBlockDataSizeAll = 0;
  private int mBlockDataSizeReaded = 0;
  private int mBlockDataSum = 0;
  private Context mContext;
  private PreferenceActivity.Header mCurrentHeader;
  private boolean mDebug = false;
  public boolean mFileTransferThreadRun = false;
  private PreferenceActivity.Header mFirstHeader;
  private boolean mFirstPlayVideo = false;
  private String mFragmentClass;
  public Handler mHandler = new Handler()
  {
    public void handleMessage(Message paramMessage)
    {
      switch (paramMessage.what)
      {
      case 201:
        if (Settings.this.mRestartAppAgain)
        {
            Settings.this.exitCsr();
            return;
        }
      case 202:
          Settings.this.onTimerDo();
          return;
      }
    }
  };
  protected HashMap<Integer, Integer> mHeaderIndexMap = new HashMap();
  private List<PreferenceActivity.Header> mHeaders;
  private Hidusb mHidusb;
  private boolean mInLocalHeaderSwitch;
  private boolean mIsActiveSlaveDo = false;
  private boolean mIsUidPlayed = false;
  private boolean mIsVideoPlaying = false;
  private int mLastSelPos = 0;
  private int[] mLedCmdResponse = null;
  private int[] mLedCmdResponseByte = null;
  private boolean mOnPause = false;
  private PreferenceActivity.Header mParentHeader;
  private boolean mQuitUsbHidThread = false;
  private int mResetUsbEndCount = 0;
  private int mResponseId = 0;
  public boolean mRestartAppAgain = false;
  private Runnable mRunnable = new Runnable()
  {
    public void run()
    {
      if (Settings.this.mFileTransferThreadRun)
      {
        Settings.this.mHandler.removeCallbacks(Settings.this.mRunnable);
        Settings.this.mHandler.postDelayed(Settings.this.mRunnable, 120000L);
      }
        else
      {
          Settings.this.mHandler.removeCallbacks(Settings.this.mRunnable);
          Settings.this.exitCsr();
      }
    }
  };
  private boolean mScanFlagDo = false;
  private int[] mSendDataResponse = null;
  private SharedPreferences mSharedPreferences;
  public boolean mSuperManagerMode = false;
  private BroadcastReceiver mSuperModeReceiver = new BroadcastReceiver()
  {
    public void onReceive(Context paramContext, Intent paramIntent)
    {
      if (paramIntent.getAction().equalsIgnoreCase("com.general.mediaplayer.startsupermode"))
      {
        Log.v("Settings", "==Settings===mSuperModeReceiver==");
        Settings.this.finish();
      }
      else if (paramIntent.getAction().equalsIgnoreCase("com.general.mediaplayer.sendapprun"))
      {
          if (!Settings.this.mSuperManagerMode)
            Settings.this.mSuperManagerMode = paramIntent.getBooleanExtra("inSuperModeKey", false);

          Intent localIntent = new Intent();
          localIntent.setAction("com.general.mediaplayer.receiverapprun");
          localIntent.putExtra("Csr_app_run", true);
          Settings.this.sendBroadcast(localIntent);
        }
    }
  };

  private Timer mTimer;
  private TimerTask mTimerTask;
  private int mTopLevelHeaderId;
  private int mUidBuffer = 0;
  private int mUidBuffer1 = 0;
  private int mUidBufferPre = 0;
  private boolean mUsbDebug = true;
  private int[] mUsbHidBlockDataBuffer = new int['?'];
  private char[] mUsbHidBlockDataCharBuffer = new char['?'];
  private UsbHidCmd mUsbHidCmd;
  private int[] mUsbHidCmdResponse = null;
  private int[] mUsbHidCmdResponseByte = null;
  private boolean mUsbHidDeviceReady = false;
  private UsbHidPlayerData mUsbHidPlayerData;
  private int mUsbHidReadErrorCount = 0;
  private int[] mUsbHidUidBuffer = { 0, 0, 0, 0, 0, 0, 0 };
  PowerManager.WakeLock mWakeLock = null;
  private int showToastOPEN_DEVICE_FAILED = 0;

  private void Keyscan()
  {
      this.KeyStatePrev = this.KeyStateCurr;
      this.KeyStateCurr = this.USBHIDScanCode;
      if(this.KeyStateCurr == 0) {
          this.KeyStateFlag1 = 0;
      } else {
          if(this.KeyStateCurr != this.KeyStatePrev) {
              this.KeyStateFlag1 = 0;
              return;
          }
          ++this.KeyStateFlag1;
          if(this.KeyStateFlag1 == 2 || this.KeyStateFlag1 == 190) {
              if(this.KeyStateFlag1 == 190) {
                  this.KeyStateFlag1 = 120;
              }

              switch(this.KeyStatePrev) {
                  case 1:
                      this.sendKeyIntent(19);
                      return;
                  case 2:
                      this.sendKeyIntent(20);
                      return;
                  case 3:
                      this.sendKeyIntent(21);
                      return;
                  case 4:
                      this.sendKeyIntent(22);
                      return;
                  case 5:
                      this.sendKeyIntent(66);
                      return;
                  case 6:
                      this.sendKeyIntent(4);
                      return;
                  default:
                      return;
              }
          }
      }
  }
    // $FF: synthetic method
    static int access$808(Settings var0) {
        int var1 = var0.mUsbHidReadErrorCount;
        var0.mUsbHidReadErrorCount = var1 + 1;
        return var1;
    }



    private boolean checkSuperManagerKey()
  {
    File localFile = new File("/mnt/usbhost1/AdministratorPassword");
    boolean bool1 = localFile.exists();
    boolean i = false;
    if (bool1)
    {
      boolean bool2 = localFile.isDirectory();
      i = false;
      if (bool2)
        i = true;
    }
    return i;
  }

  private String getMCUVersionStr()
  {
    String str1 = getResources().getString(R.string.mediaplayer_setting_sp);
    String str2 = getResources().getString(R.string.mediaplayer_MCU_version_key);
    if (this.mSharedPreferences == null)
      this.mSharedPreferences = getApplicationContext().getSharedPreferences(str1, 2);
    return this.mSharedPreferences.getString(str2, "");
  }

  private void getMetaData()
  {
    try
    {
      ActivityInfo localActivityInfo = getPackageManager().getActivityInfo(getComponentName(), 128);
      if (localActivityInfo != null)
      {
        if (localActivityInfo.metaData == null)
          return;
        this.mTopLevelHeaderId = localActivityInfo.metaData.getInt("com.general.mediaplayer.csr.TOP_LEVEL_HEADER_ID");
        this.mFragmentClass = localActivityInfo.metaData.getString("com.general.mediaplayer.csr.FRAGMENT_CLASS");
        int i = localActivityInfo.metaData.getInt("com.general.mediaplayer.csr.PARENT_FRAGMENT_TITLE");
        String str = localActivityInfo.metaData.getString("com.general.mediaplayer.csr.PARENT_FRAGMENT_CLASS");
        if (str != null)
        {
          this.mParentHeader = new PreferenceActivity.Header();
          this.mParentHeader.fragment = str;
          if (i != 0)
          {
            this.mParentHeader.title = getResources().getString(i);
            return;
          }
        }
      }
    }
    catch (PackageManager.NameNotFoundException localNameNotFoundException)
    {
    }
  }

  private void highlightHeader()
  {
    if (this.mTopLevelHeaderId != 0)
    {
      Integer localInteger = (Integer)this.mHeaderIndexMap.get(Integer.valueOf(this.mTopLevelHeaderId));
      if (localInteger != null)
      {
        getListView().setItemChecked(localInteger.intValue(), true);
        getListView().smoothScrollToPosition(localInteger.intValue());
      }
    }
  }

  private boolean needsDockSettings()
  {
    return getResources().getBoolean(R.bool.has_dock_settings);
  }

  private void openApp(String paramString)
  {
    try
    {
      PackageInfo localPackageInfo2 = getPackageManager().getPackageInfo(paramString, 0);
      PackageManager localPackageManager = getPackageManager();
      Intent localIntent1 = new Intent("android.intent.action.MAIN", null);
      localIntent1.addCategory("android.intent.category.LAUNCHER");
      localIntent1.setPackage(localPackageInfo2.packageName);
      ResolveInfo localResolveInfo = (ResolveInfo)localPackageManager.queryIntentActivities(localIntent1, 0).iterator().next();
      if (localResolveInfo != null)
      {
        String str1 = localResolveInfo.activityInfo.packageName;
        String str2 = localResolveInfo.activityInfo.name;
        Intent localIntent2 = new Intent("android.intent.action.MAIN");
        localIntent2.addCategory("android.intent.category.LAUNCHER");
        ComponentName localComponentName = new ComponentName(str1, str2);
        localIntent2.addFlags(0x10000000);
        localIntent2.setComponent(localComponentName);
        startActivity(localIntent2);
      }
      return;
    }
    catch (PackageManager.NameNotFoundException localNameNotFoundException)
    {
      while (true)
      {
        localNameNotFoundException.printStackTrace();
        PackageInfo localPackageInfo1 = null;
      }
    }
  }

  private void sendKeyIntent(int paramInt)
  {
      /*
    ClearAppAutoRunTimerOut();
    new Thread(paramInt)
    {
      public void run()
      {
        int i = 1;
        try
        {
          if (this.val$keyCode == 4)
            i = 2;
          IWindowManager localIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.getService("window"));
          for (int j = 0; j < i; j++)
          {
            Thread.sleep(20L);
            long l = SystemClock.uptimeMillis();
            localIWindowManager.injectKeyEvent(new KeyEvent(l, l, 0, this.val$keyCode, 0), false);
            localIWindowManager.injectKeyEvent(new KeyEvent(l, l, 1, this.val$keyCode, 0), false);
          }
        }
        catch (InterruptedException localInterruptedException)
        {
          localInterruptedException.printStackTrace();
          return;
        }
        catch (RemoteException localRemoteException)
        {
          localRemoteException.printStackTrace();
        }
      }
    }
    .start();
    */
  }

  private void setSavedMCUValue(String paramString)
  {
    String str1 = getResources().getString(R.string.mediaplayer_setting_sp);
    String str2 = getResources().getString(R.string.mediaplayer_MCU_version_key);
    this.mSharedPreferences = getApplicationContext().getSharedPreferences(str1, 2);
    SharedPreferences.Editor localEditor = this.mSharedPreferences.edit();
    localEditor.putString(str2, paramString);
    localEditor.commit();
  }

  private void showExitDialog()
  {
    new AlertDialog.Builder(this).setMessage(R.string.exit_dialog_title_message).setTitle(R.string.exit_dialog_title).setPositiveButton(0x104000A, new DialogInterface.OnClickListener()
    {
      public void onClick(DialogInterface paramDialogInterface, int paramInt)
      {
        Settings.this.exitCsr_Exitmenu();
      }
    }).setNegativeButton(0x1040000, null).create().show();
  }

  private void startCsrManagerService(Context paramContext)
  {
    Log.v(" ", "===Settings start startCsrManagerService==");
      /*
    paramContext.startService(new Intent(this, CsrManagerService.class));
    */
  }

  private void switchToHeaderLocal(PreferenceActivity.Header paramHeader)
  {
    this.mInLocalHeaderSwitch = true;
    switchToHeader(paramHeader);
    this.mInLocalHeaderSwitch = false;
  }

  private void switchToParent(String paramString)
  {
    ComponentName localComponentName = new ComponentName(this, paramString);
    try
    {
      PackageManager localPackageManager = getPackageManager();
      ActivityInfo localActivityInfo = localPackageManager.getActivityInfo(localComponentName, 128);
      if ((localActivityInfo != null) && (localActivityInfo.metaData != null))
      {
        String str = localActivityInfo.metaData.getString("com.general.mediaplayer.csr.FRAGMENT_CLASS");
        CharSequence localCharSequence = localActivityInfo.loadLabel(localPackageManager);
        PreferenceActivity.Header localHeader = new PreferenceActivity.Header();
        localHeader.fragment = str;
        localHeader.title = localCharSequence;
        this.mCurrentHeader = localHeader;
        switchToHeaderLocal(localHeader);
        highlightHeader();
        this.mParentHeader = new PreferenceActivity.Header();
        this.mParentHeader.fragment = localActivityInfo.metaData.getString("com.general.mediaplayer.csr.PARENT_FRAGMENT_CLASS");
        this.mParentHeader.title = localActivityInfo.metaData.getString("com.general.mediaplayer.csr.PARENT_FRAGMENT_TITLE");
      }
      return;
    }
    catch (PackageManager.NameNotFoundException localNameNotFoundException)
    {
      Log.w("Settings", "Could not find parent activity : " + paramString);
    }
  }

  private void updateHeaderList(List<PreferenceActivity.Header> paramList)
  {
    int i = 0;
    if (i < paramList.size())
    {
      PreferenceActivity.Header localHeader = (PreferenceActivity.Header)paramList.get(i);
      int j = (int)localHeader.id;
      if (j == R.id.wifi_settings)
        if (!getPackageManager().hasSystemFeature("android.hardware.wifi"))
          paramList.remove(localHeader);
      while (true)
      {
        if (paramList.get(i) == localHeader)
        {
          if ((this.mFirstHeader == null) && (HeaderAdapter.getHeaderType(localHeader) != 0))
            this.mFirstHeader = localHeader;
          this.mHeaderIndexMap.put(Integer.valueOf(j), Integer.valueOf(i));
          i++;
        }
        if (j == R.id.battery_settings)
        {
        paramList.remove(localHeader);
        break;
        }
        if (j == R.id.bluetooth_settings)
        {
          if (getPackageManager().hasSystemFeature("android.hardware.bluetooth"))
            continue;
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.data_usage_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.operator_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.storage_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.storage_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.battery_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.application_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.manufacturer_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.location_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.security_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.language_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.privacy_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.dock_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.date_time_settings)
          continue;
        if (j == R.id.accessibility_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j == R.id.development_settings)
        {
          paramList.remove(localHeader);
          continue;
        }
        if (j != R.id.sync_settings)
          continue;
        paramList.remove(localHeader);
      }
    }
  }

  public void ClearAppAutoRunTimerOut()
  {
    if (this.mHandler != null)
    {
      this.mHandler.removeCallbacks(this.mRunnable);
      this.mHandler.removeMessages(201);
      this.mHandler.postDelayed(this.mRunnable, 120000L);
    }
  }

  public void DisableScreenOff()
  {
    if (this.mWakeLock == null)
    {
      Log.d("CSR Settings", "Acquiring wakelock.");
      this.mWakeLock = ((PowerManager)getSystemService("power")).newWakeLock(26, "CSR Settings");
      this.mWakeLock.acquire();
    }
  }

  public void EnableScreenOff()
  {
    if (this.mWakeLock != null)
    {
      Log.d("CSR Settings", "Releasing and destroying wakelock");
      this.mWakeLock.release();
      this.mWakeLock = null;
    }
  }

  public boolean dispatchKeyEvent(KeyEvent paramKeyEvent)
  {
    Log.v("CSR Settings", "=======Settings dispatchKeyEvent===event.Action=" + paramKeyEvent.getAction());
    ClearAppAutoRunTimerOut();
    return super.dispatchKeyEvent(paramKeyEvent);
  }

  public boolean dispatchKeyShortcutEvent(KeyEvent paramKeyEvent)
  {
    ClearAppAutoRunTimerOut();
    return super.dispatchKeyShortcutEvent(paramKeyEvent);
  }

  public boolean dispatchTouchEvent(MotionEvent paramMotionEvent)
  {
    ClearAppAutoRunTimerOut();
    return super.dispatchTouchEvent(paramMotionEvent);
  }

  public void exitCsr()
  {
    String str = getAutoRunAppPackage(this);
    if (str != null)
    {
      this.mRestartAppAgain = false;
      openApp(str);
      finish();
    }
      else
        this.mHandler.sendEmptyMessageDelayed(201, 1000L);
  }

  public void exitCsr_Exitmenu()
  {
    if (this.mSuperManagerMode)
      finish();
      else
    {
        String str;

        str = getAutoRunAppPackage(this);

        if (str != null)
        {
            this.mRestartAppAgain = false;
            openApp(str);
            finish();
        }
    }
  }

  public void finish()
  {
    Log.v("Settings", "======settings======finish=========");
    this.mHandler.removeCallbacks(this.mRunnable);
    this.mQuitUsbHidThread = true;
    if (this.mTimer != null)
    {
      this.mTimer.cancel();
      this.mTimer = null;
    }
    EnableScreenOff();
    super.finish();
  }

  public String getAutoRunAppPackage(Context paramContext)
  {
    String str1 = getResources().getString(R.string.mediaplayer_setting_sp);
    String str2 = getResources().getString(R.string.mediaplayer_autorun_app_key);
    String str3 = paramContext.getString(R.string.mediaplayer_packagename);
    String str4 = paramContext.getSharedPreferences(str1, 1).getString(str2, "");
    String str5 = null;
    if (str4 != null)
    {
      int i = str4.lastIndexOf('.');
      str5 = null;
      if (i > 0)
      {
        boolean bool1 = str3.equalsIgnoreCase(str4.substring(0, i));
        str5 = null;
        if (bool1)
        {
          boolean bool2 = isAppInstalled(paramContext, str4);
          str5 = null;
          if (bool2)
            str5 = str4;
        }
      }
    }
    return str5;
  }

  public Intent getIntent()
  {
    Intent localIntent1 = super.getIntent();
    String str = getStartingFragmentClass(localIntent1);
    if ((str != null) && (!onIsMultiPane()))
    {
      Intent localIntent2 = new Intent(localIntent1);
      localIntent2.putExtra(":android:show_fragment", str);
      Bundle localBundle1 = localIntent1.getExtras();
      if (localBundle1 != null);
      for (Bundle localBundle2 = new Bundle(localBundle1); ; localBundle2 = new Bundle())
      {
        localBundle2.putParcelable("intent", localIntent1);
        localIntent2.putExtra(":android:show_fragment_args", localIntent1.getExtras());
        return localIntent2;
      }
    }
    return localIntent1;
  }

  public boolean getMcuI2CData(int paramInt1, int paramInt2)
  {
    int[] arrayOfInt1 = new int[UsbHidCmd.mGetMcuI2CDataBase.length];
    this.mResponseId = (1 + this.mResponseId);
    this.mResponseId = (0xFF & this.mResponseId);
    for (int i = 0; i < UsbHidCmd.mGetMcuI2CDataBase.length; i++)
    {
      arrayOfInt1[i] = UsbHidCmd.mGetMcuI2CDataBase[i];
      if (i == 3)
        arrayOfInt1[i] = this.mResponseId;
      if (i == 4)
        arrayOfInt1[i] = (paramInt1 + arrayOfInt1[i]);
      if (i == 5)
        arrayOfInt1[i] = (paramInt2 + arrayOfInt1[i]);
      if (i == 6)
        arrayOfInt1[i] = 0;
      if (i != 7)
        continue;
      arrayOfInt1[i] = 0;
      for (int i10 = 0; i10 < i; i10++)
        arrayOfInt1[i] = (0xFF & arrayOfInt1[i] + arrayOfInt1[i10]);
    }
    String str1 = "";
    if (this.mDebug)
    {
      for (int i9 = 0; i9 < arrayOfInt1.length; i9++)
        str1 = str1 + Integer.toHexString(arrayOfInt1[i9]) + " ";
      Log.v("CSR Settings", "senduidcmd_send= " + str1);
    }
    this.mUsbHidReadErrorCount = (1 + this.mUsbHidReadErrorCount);
    this.mUsbHidCmdResponse = usbHidSendData(arrayOfInt1);
    this.mUsbHidCmdResponse = usbHidSendData(arrayOfInt1);
    this.mUsbHidCmdResponseByte = intArrayToByteArray(this.mUsbHidCmdResponse);
    int[] arrayOfInt2 = this.mUsbHidCmdResponseByte;
    int j = 0;
    if (arrayOfInt2 != null)
    {
      this.mUsbHidReadErrorCount = 0;
      if (this.mDebug)
      {
        String str3 = "";
        for (int i8 = 0; i8 < this.mUsbHidCmdResponseByte.length; i8++)
          str3 = str3 + Integer.toHexString(this.mUsbHidCmdResponseByte[i8]) + " ";
      }
      int k = this.mUsbHidCmdResponseByte[0];
      j = 0;
      if (k == 170)
      {
        int m = this.mUsbHidCmdResponseByte[1];
        j = 0;
        if (m == 18)
        {
          int n = this.mUsbHidCmdResponseByte[2];
          int i1 = this.mResponseId;
          j = 0;
          if (n == i1)
          {
            int i2 = 0;
              int i3 = 0;
            for (i3 = 0; i3 < 2 + this.mUsbHidCmdResponseByte[1]; i3++)
              i2 += (0xFF & this.mUsbHidCmdResponseByte[i3]);
            int i4 = i2 & 0xFF;
            int i5 = 0xFF & this.mUsbHidCmdResponseByte[i3];
            j = 0;
            if (i4 == i5)
            {
              for (int i6 = 0; i6 < 7; i6++)
                this.mUsbHidUidBuffer[i6] = this.mUsbHidCmdResponseByte[(i6 + 4)];
              if (this.mDebug)
              {
                String str2 = "";
                for (int i7 = 0; i7 < this.mUsbHidUidBuffer.length; i7++)
                  str2 = str2 + Integer.toHexString(this.mUsbHidUidBuffer[i7]) + " ";
                Log.v("CSR Settings", "uid= " + str2);
              }
              j = 1;
            }
          }
        }
      }
    }
    return j == 1;
  }

    @Override
  public Button getNextButton()
  {
    //return super.getNextButton();
      return null;
  }

  protected String getStartingFragmentClass(Intent paramIntent)
  {
      /*
    String str;
    if (this.mFragmentClass != null)
      str = this.mFragmentClass;
    do
    {
      //return str;
      str = paramIntent.getComponent().getClassName();
      if (str.equals(getClass().getName()))
        return null;
    }
    while ((!"com.general.mediaplayer.csr.ManageApplications".equals(str)) && (!"com.general.mediaplayer.csr.RunningServices".equals(str)) && (!"com.general.mediaplayer.csr.applications.StorageUse".equals(str)));
    return ManageApplications.class.getName();
    */
      return "";
  }

  public boolean getUiddata(int paramInt1, int paramInt2)
  {
    int[] arrayOfInt1 = new int[UsbHidCmd.mGetUidBase.length];
    this.mResponseId = (1 + this.mResponseId);
    this.mResponseId = (0xFF & this.mResponseId);
    for (int i = 0; i < UsbHidCmd.mGetUidBase.length; i++)
    {
      arrayOfInt1[i] = UsbHidCmd.mGetUidBase[i];
      if (i == 3)
        arrayOfInt1[i] = this.mResponseId;
      if (i == 4)
        arrayOfInt1[i] = (paramInt2 + arrayOfInt1[i]);
      if (i != 5)
        continue;
      arrayOfInt1[i] = 0;
      for (int i10 = 0; i10 < i; i10++)
        arrayOfInt1[i] = (0xFF & arrayOfInt1[i] + arrayOfInt1[i10]);
    }
    String str1 = "";
    if (this.mDebug)
    {
      for (int i9 = 0; i9 < arrayOfInt1.length; i9++)
        str1 = str1 + Integer.toHexString(arrayOfInt1[i9]) + " ";
      Log.v("CSR Settings", "senduidcmd_send= " + str1);
    }
    this.mUsbHidReadErrorCount = (1 + this.mUsbHidReadErrorCount);
    this.mUsbHidCmdResponse = usbHidSendData(arrayOfInt1);
    this.mUsbHidCmdResponse = usbHidSendData(arrayOfInt1);
    this.mUsbHidCmdResponseByte = intArrayToByteArray(this.mUsbHidCmdResponse);
    int[] arrayOfInt2 = this.mUsbHidCmdResponseByte;
    int j = 0;
    if (arrayOfInt2 != null)
    {
      this.mUsbHidReadErrorCount = 0;
      if (this.mDebug)
      {
        String str3 = "";
        for (int i8 = 0; i8 < this.mUsbHidCmdResponseByte.length; i8++)
          str3 = str3 + Integer.toHexString(this.mUsbHidCmdResponseByte[i8]) + " ";
      }
      int k = this.mUsbHidCmdResponseByte[0];
      j = 0;
      if (k == 170)
      {
        int m = this.mUsbHidCmdResponseByte[1];
        j = 0;
        if (m == 9)
        {
          int n = this.mUsbHidCmdResponseByte[2];
          int i1 = this.mResponseId;
          j = 0;
          if (n == i1)
          {
            int i2 = 0;
              int i3 = 0;
            for (i3 = 0; i3 < 2 + this.mUsbHidCmdResponseByte[1]; i3++)
              i2 += (0xFF & this.mUsbHidCmdResponseByte[i3]);
            int i4 = i2 & 0xFF;
            int i5 = 0xFF & this.mUsbHidCmdResponseByte[i3];
            j = 0;
            if (i4 == i5)
            {
              for (int i6 = 0; i6 < 7; i6++)
                this.mUsbHidUidBuffer[i6] = this.mUsbHidCmdResponseByte[(i6 + 4)];
              if (this.mDebug)
              {
                String str2 = "";
                for (int i7 = 0; i7 < this.mUsbHidUidBuffer.length; i7++)
                  str2 = str2 + Integer.toHexString(this.mUsbHidUidBuffer[i7]) + " ";
                Log.v("CSR Settings", "uid= " + str2);
              }
              j = 1;
            }
          }
        }
      }
    }
    return j == 1;
  }

    @Override
  public boolean hasNextButton()
  {
    //return super.hasNextButton();
      return false;
  }

  public int[] intArrayToByteArray(int[] paramArrayOfInt)
  {

    int[] arrayOfInt = null;
    if (paramArrayOfInt != null);
      int i = paramArrayOfInt.length;
      arrayOfInt = null;
      if (i > 0)
      {
        arrayOfInt = new int[4 * paramArrayOfInt.length];
        for (int j = 0; j < paramArrayOfInt.length; j++)
        {
          arrayOfInt[(j * 4)] = (0xFF & paramArrayOfInt[j]);
          arrayOfInt[(1 + j * 4)] = (0xFF & paramArrayOfInt[j] >> 8);
          arrayOfInt[(2 + j * 4)] = (0xFF & paramArrayOfInt[j] >> 16);
          arrayOfInt[(3 + j * 4)] = (0xFF & paramArrayOfInt[j] >> 24);
        }
      }
      return arrayOfInt;

  }

  public boolean isAppInstalled(Context paramContext, String paramString)
  {
    if ((paramString == null) || (paramContext == null))
      return false;
    List localList = paramContext.getPackageManager().getInstalledPackages(0);
    for (int i = 0; ; i++)
    {
      int j = localList.size();
      int k = 0;
      if (i < j)
      {
        if (!((PackageInfo)localList.get(i)).packageName.equalsIgnoreCase(paramString))
          continue;
        k = 1;
      }
      return k == 1;
    }
  }

  public void onBackPressed()
  {
    Log.v("Csr", "===csr settings ==onBackPressed");
    getFragmentManager().popBackStackImmediate();
  }

  public void onBuildHeaders(List<PreferenceActivity.Header> paramList)
  {
    loadHeadersFromResource(R.xml.settings_headers, paramList);
    updateHeaderList(paramList);
    this.mHeaders = paramList;
  }

  public Intent onBuildStartFragmentIntent(String paramString, Bundle paramBundle, int paramInt1, int paramInt2)
  {
    Intent localIntent = super.onBuildStartFragmentIntent(paramString, paramBundle, paramInt1, paramInt2);
      /*
    if ((DataUsageSummary.class.getName().equals(paramString))
            || (PowerUsageSummary.class.getName().equals(paramString))
            || (AccountSyncSettings.class.getName().equals(paramString))
            || (UserDictionarySettings.class.getName().equals(paramString)))
      localIntent.putExtra("settings:remove_ui_options", true);
      */
    localIntent.setClass(this, SubSettings.class);
    return localIntent;
  }

  protected void onCreate(Bundle paramBundle)
  {
    if (getIntent().getBooleanExtra("restart_app_again", false))
    {
      Log.v("", "===settings  onCreate===");
      this.mRestartAppAgain = true;
      this.mHandler.sendEmptyMessageDelayed(201, 1000L);
    }
    if (getIntent().getBooleanExtra("settings:remove_ui_options", false))
      getWindow().setUiOptions(0);
    getMetaData();
    if (true == this.USBHID_USE_FLAG)
      setTheme(R.style.SettingsTheme);
    this.mInLocalHeaderSwitch = true;
    super.onCreate(paramBundle);
    this.mInLocalHeaderSwitch = false;
    DisableScreenOff();
    this.mUsbHidCmd = new UsbHidCmd();
    try
    {
      this.mHidusb = Hidusb.newInstance();
      startUsbHidScanThread();
      if ((!onIsHidingHeaders()) && (onIsMultiPane()))
      {
        highlightHeader();
        setTitle(R.string.settings_label);
      }
      if (paramBundle != null)
      {
        this.mCurrentHeader = ((PreferenceActivity.Header)paramBundle.getParcelable("com.general.mediaplayer.csr.CURRENT_HEADER"));
        this.mParentHeader = ((PreferenceActivity.Header)paramBundle.getParcelable("com.general.mediaplayer.csr.PARENT_HEADER"));
      }
      if ((paramBundle != null) && (this.mCurrentHeader != null))
        showBreadCrumbs(this.mCurrentHeader.title, null);
      if (this.mParentHeader != null)
        setParentTitle(this.mParentHeader.title, null, new View.OnClickListener()
        {
          public void onClick(View paramView)
          {
            Settings.this.switchToParent(Settings.this.mParentHeader.fragment);
          }
        });
      startCsrManagerService(this);
      Log.v("==", "======settings======onCreate=========");
      return;
    }
    catch (Exception localException)
    {
      while (true)
        finish();
    }
  }

  protected void onDestroy()
  {
    Log.v("Settings", "======settings======onDestroy=========");
    this.mHandler.removeCallbacks(this.mRunnable);
    this.mQuitUsbHidThread = true;
    if (this.mTimer != null)
    {
      this.mTimer.cancel();
      this.mTimer = null;
    }
    EnableScreenOff();
    super.onDestroy();
  }

  public PreferenceActivity.Header onGetInitialHeader()
  {
    String str = getStartingFragmentClass(super.getIntent());
    if (str != null)
    {
      PreferenceActivity.Header localHeader = new PreferenceActivity.Header();
      localHeader.fragment = str;
      localHeader.title = getTitle();
      localHeader.fragmentArguments = getIntent().getExtras();
      this.mCurrentHeader = localHeader;
      return localHeader;
    }
    return this.mFirstHeader;
  }

  public void onListItemClick(ListView paramListView, View paramView, int paramInt, long paramLong)
  {
    if (true == this.USBHID_USE_FLAG)
    {
      if (this.mLastSelPos == paramInt)
        if (((HeaderAdapter)paramListView.getAdapter()).getItemViewType(paramInt) == 2)
          ((Settings.HeaderAdapter.HeaderViewHolder)paramView.getTag()).switch_.performClick();
      while (true)
      {
        this.mLastSelPos = paramInt;
      }
    }
    if (((PreferenceActivity.Header)((HeaderAdapter)paramListView.getAdapter()).getItem(paramInt)).id == R.id.exit_settings)
    {
      super.onListItemClick(paramListView, paramView, paramInt, paramLong);
      showExitDialog();
    }
    while (true)
    {
      Log.v("", "onListItemClick==");
      this.mLastSelPos = paramInt;
    }
  }

  public void onNewIntent(Intent paramIntent)
  {
    super.onNewIntent(paramIntent);
    if (((0x100000 & paramIntent.getFlags()) == 0) && (this.mFirstHeader != null) && (!onIsHidingHeaders()) && (onIsMultiPane()))
      switchToHeaderLocal(this.mFirstHeader);
  }

  public void onPause()
  {
    super.onPause();
    Log.v("==", "======settings======onPause=========");
    unregisterReceiver(this.mSuperModeReceiver);
    ListAdapter localListAdapter = getListAdapter();
    if ((localListAdapter instanceof HeaderAdapter))
      ((HeaderAdapter)localListAdapter).pause();
    this.mOnPause = true;
  }

  public boolean onPreferenceStartFragment(PreferenceFragment paramPreferenceFragment, Preference paramPreference)
  {
    int i = paramPreference.getTitleRes();
    //if (paramPreference.getFragment().equals(WallpaperTypeSettings.class.getName()))
      i = R.string.wallpaper_settings_fragment_title;
    startPreferencePanel(paramPreference.getFragment(), paramPreference.getExtras(), i, null, null, 0);
    return true;
  }

  public void onResume()
  {
    super.onResume();
    if ((!this.mSuperManagerMode) && (checkSuperManagerKey()))
      this.mSuperManagerMode = true;
    this.mHandler.postDelayed(this.mRunnable, 120000L);
    Log.v("==", "======settings======onResume=========");
    IntentFilter localIntentFilter = new IntentFilter();
    localIntentFilter.addAction("com.general.mediaplayer.startsupermode");
    localIntentFilter.addAction("com.general.mediaplayer.sendapprun");
    registerReceiver(this.mSuperModeReceiver, localIntentFilter);
    ListAdapter localListAdapter = getListAdapter();
    if ((localListAdapter instanceof HeaderAdapter))
      ((HeaderAdapter)localListAdapter).resume();
    setTitle(R.string.settings_label);
    this.mOnPause = false;
  }

  protected void onSaveInstanceState(Bundle paramBundle)
  {
    super.onSaveInstanceState(paramBundle);
    if (this.mCurrentHeader != null)
      paramBundle.putParcelable("com.general.mediaplayer.csr.CURRENT_HEADER", this.mCurrentHeader);
    if (this.mParentHeader != null)
      paramBundle.putParcelable("com.general.mediaplayer.csr.PARENT_HEADER", this.mParentHeader);
  }

  public void onTimerDo()
  {
    Keyscan();
  }

  public void sendDpadLeftKey()
  {
    new Thread()
    {
      public void run()
      {
        try
        {
          new Instrumentation().sendKeyDownUpSync(21);
          return;
        }
        catch (Exception localException)
        {
          Log.e("Exception when sendDpadLeftKey", localException.toString());
        }
      }
    }
    .start();
  }

  public void sendDpadRightKey()
  {
    new Thread()
    {
      public void run()
      {
        try
        {
          new Instrumentation().sendKeyDownUpSync(22);
          return;
        }
        catch (Exception localException)
        {
          Log.e("Exception when sendDpadRightKey", localException.toString());
        }
      }
    }
    .start();
  }

  public void setFileTransferThreadRun(boolean paramBoolean)
  {
    this.mFileTransferThreadRun = paramBoolean;
  }

  public void setListAdapter(ListAdapter paramListAdapter)
  {
    if (this.mHeaders == null)
    {
      this.mHeaders = new ArrayList();
      for (int i = 0; i < paramListAdapter.getCount(); i++)
        this.mHeaders.add((PreferenceActivity.Header)paramListAdapter.getItem(i));
    }
    super.setListAdapter(new HeaderAdapter(this, this.mHeaders));
    this.mLastSelPos = getListView().getSelectedItemPosition();
  }

  public boolean setMcuI2CData(int paramInt, int[] paramArrayOfInt)
  {
    this.mResponseId = (1 + this.mResponseId);
    this.mResponseId = (0xFF & this.mResponseId);
    if ((paramArrayOfInt == null) || (paramArrayOfInt.length > 16))
      return false;
    int i = paramArrayOfInt.length;
    int j = 0;
    for (int k = 0; k < i; k++)
      j = 0xFF & j + paramArrayOfInt[k];
    int[] arrayOfInt1 = new int[UsbHidCmd.mSetMcuI2CDataBase.length];
    for (int m = 0; m < UsbHidCmd.mSetMcuI2CDataBase.length; m++)
    {
      arrayOfInt1[m] = UsbHidCmd.mSetMcuI2CDataBase[m];
      if (m == 3)
        arrayOfInt1[m] = this.mResponseId;
      if (m == 4)
        arrayOfInt1[m] = paramInt;
      if (m == 5)
        arrayOfInt1[m] = i;
      if (m != 6)
        continue;
      arrayOfInt1[m] = j;
    }
    for (int n = 0; n < i; n++)
      arrayOfInt1[(n + 7)] = paramArrayOfInt[n];
    int i1 = UsbHidCmd.mSetMcuI2CDataBase.length;
    arrayOfInt1[(i1 - 1)] = 0;
    for (int i2 = 0; i2 < i1 - 1; i2++)
      arrayOfInt1[(i1 - 1)] = (0xFF & arrayOfInt1[(i1 - 1)] + arrayOfInt1[i2]);
    String str1 = "";
    if (this.mDebug)
    {
      for (int i15 = 0; i15 < arrayOfInt1.length; i15++)
        str1 = str1 + Integer.toHexString(arrayOfInt1[i15]) + " ";
      Log.v("CSR Settings", "setMcuI2CData_send= " + str1);
    }
    usbHidSendData(arrayOfInt1);
    int[] arrayOfInt2 = intArrayToByteArray(usbHidSendData(arrayOfInt1));
    int i3 = 0;
    if (arrayOfInt2 != null)
    {
      this.mUsbHidReadErrorCount = 0;
      String str2 = "";
      if (this.mDebug)
      {
        for (int i14 = 0; i14 < arrayOfInt2.length; i14++)
          str2 = str2 + Integer.toHexString(arrayOfInt2[i14]) + " ";
        Log.v("CSR Settings", "setMcuI2CData_return= " + str2);
      }
      int i4 = arrayOfInt2[0];
      i3 = 0;
      if (i4 == 170)
      {
        int i5 = arrayOfInt2[1];
        i3 = 0;
        if (i5 == 2)
        {
          int i6 = arrayOfInt2[2];
          int i7 = this.mResponseId;
          i3 = 0;
          if (i6 == i7)
          {
            int i8 = arrayOfInt2[3];
            i3 = 0;
            if (i8 == 0)
            {
              if (this.mDebug)
              {
                String str3 = "";
                for (int i13 = 0; i13 < arrayOfInt2.length; i13++)
                  str3 = str3 + Integer.toHexString(arrayOfInt2[i13]) + " ";
                Log.v("CSR Settings", "setMcuI2CData_return= " + str3);
              }
              int i9 = 0;
                int i10 = 0;
              for (i10 = 0; i10 < 2 + arrayOfInt2[1]; i10++)
                i9 += arrayOfInt2[i10];
              int i11 = i9 & 0xFF;
              int i12 = 0xFF & arrayOfInt2[i10];
              i3 = 0;
              if (i11 == i12)
                i3 = 1;
            }
          }
        }
      }
    }
    return i3 == 1;
  }

  public void startUsbHidScanThread()
  {
      /*
    this.mUsbHidPlayerData = new UsbHidPlayerData();
    this.mTimerTask = new TimerTask()
    {
      public void run()
      {
        Message localMessage = new Message();
        localMessage.what = 202;
        Settings.this.mHandler.sendMessage(localMessage);
      }
    };
    this.mTimer = new Timer();
    this.mTimer.schedule(this.mTimerTask, 1000L, 2L);
    new Thread()
    {
      public void run()
      {
        Log.v("CSR Settings", "startUsbHidScanThread start");
        try
        {
          Thread.sleep(3000L);
          i = 0;
          if (!Settings.this.mQuitUsbHidThread)
            if (!Settings.this.mUsbHidDeviceReady)
            {
              Settings localSettings1 = Settings.this;
              Settings localSettings2 = Settings.this;
              Settings.access$502(localSettings1, localSettings2.usbHidSendData(UsbHidCmd.mWakeCmd));
              Settings.access$702(Settings.this, Settings.this.intArrayToByteArray(Settings.this.mUsbHidCmdResponse));
              Settings.access$808(Settings.this);
              str = "";
              if (Settings.this.mUsbHidCmdResponseByte != null)
                for (int j = 0; j < Settings.this.mUsbHidCmdResponseByte.length; j++)
                  str = str + Integer.toHexString(Settings.this.mUsbHidCmdResponseByte[j]) + " ";
            }
        }
        catch (InterruptedException localInterruptedException1)
        {
          while (true)
          {
            int i;
            String str;
            localInterruptedException1.printStackTrace();
            continue;
            Settings.this.MCU_VersionStr = "";
            byte[] arrayOfByte = new byte[1];
            int k = 9;
            label235: int m;
            int n;
            while (true)
            {
              if (k < Settings.this.mUsbHidCmdResponseByte.length)
              {
                arrayOfByte[0] = (byte)Settings.access$700(Settings.this)[k];
                if (arrayOfByte[0] != 0);
              }
              else
              {
                Log.v("CSR Settings", "mWakeCmd_return length=" + Settings.this.mUsbHidCmdResponse.length + "data= " + str);
                Log.v("CSR Settings", "MCU_VersionStr= " + Settings.this.MCU_VersionStr);
                if (!Settings.this.MCU_VersionStr.equals(Settings.this.getMCUVersionStr()))
                {
                  Settings.this.setSavedMCUValue(Settings.this.MCU_VersionStr);
                  Log.v("CSR Settings", "Save MCU_VersionStr= " + Settings.this.MCU_VersionStr);
                }
                if ((Settings.this.mUsbHidCmdResponseByte[0] != 170) || (Settings.this.mUsbHidCmdResponseByte[1] != 21) || (Settings.this.mUsbHidCmdResponseByte[2] != 6))
                  break label657;
                m = 0;
                for (n = 0; n < 2 + Settings.this.mUsbHidCmdResponseByte[1]; n++)
                  m += Settings.this.mUsbHidCmdResponseByte[n];
              }
              try
              {
                Settings.this.MCU_VersionStr += new String(arrayOfByte, "utf-8");
                if (k >= 22)
                  break label235;
                k++;
              }
              catch (UnsupportedEncodingException localUnsupportedEncodingException)
              {
                while (true)
                  localUnsupportedEncodingException.printStackTrace();
              }
            }
            new StringBuilder().append(" sum=").append(Integer.toHexString(m & 0xFF)).append(" mUsbHidCmdResponseByte[").append(n).append("]=").append(Integer.toHexString(Settings.this.mUsbHidCmdResponseByte[n])).toString();
            if ((m & 0xFF) == Settings.this.mUsbHidCmdResponseByte[n])
            {
              Settings.access$802(Settings.this, 0);
              Settings.access$402(Settings.this, true);
              Log.v("CSR Settings", "Open Device success");
              Message localMessage2 = new Message();
              localMessage2.what = 204;
              Settings.this.mHandler.sendMessage(localMessage2);
            }
            label657: if (Settings.this.mUsbHidReadErrorCount > 10)
            {
              Settings.access$802(Settings.this, 0);
              Log.v("CSR Settings", "Open Device failed");
              Message localMessage1 = new Message();
              localMessage1.what = 205;
              Settings.this.mHandler.sendMessage(localMessage1);
              Settings.access$1102(Settings.this, 1);
              Settings.this.MCU_VersionStr = "";
              if (!Settings.this.MCU_VersionStr.equals(Settings.this.getMCUVersionStr()))
              {
                Settings.this.setSavedMCUValue(Settings.this.MCU_VersionStr);
                Log.v("CSR Settings", "Save MCU_VersionStr= NULL " + Settings.this.MCU_VersionStr);
              }
            }
            try
            {
              Thread.sleep(1500L);
              try
              {
                Thread.sleep(200L);
              }
              catch (InterruptedException localInterruptedException4)
              {
                localInterruptedException4.printStackTrace();
              }
            }
            catch (InterruptedException localInterruptedException5)
            {
              while (true)
                localInterruptedException5.printStackTrace();
            }
            if (true != Settings.this.USBHID_USE_FLAG)
            {
              try
              {
                Thread.sleep(1500L);
              }
              catch (InterruptedException localInterruptedException3)
              {
                localInterruptedException3.printStackTrace();
              }
              continue;
            }
            Settings.access$1102(Settings.this, 0);
            if (Settings.this.MCU_Key_State_clear)
            {
              int[] arrayOfInt = { 1 };
              if (Settings.this.setMcuI2CData(100, arrayOfInt))
                Settings.access$1302(Settings.this, false);
            }
            if (Settings.this.getMcuI2CData(0, 8))
            {
              Settings.access$1402(Settings.this, Settings.this.mUsbHidUidBuffer[3]);
              if ((Settings.this.mUidBuffer > 0) && (Settings.this.mUidBuffer < 22))
              {
                if (Settings.this.USBHIDScanCode != Settings.this.mUidBuffer)
                  Settings.access$1302(Settings.this, true);
                Settings.access$1602(Settings.this, Settings.this.mUidBuffer);
                label1008: i++;
                if (i > 100)
                  i = 0;
              }
            }
            try
            {
              while (true)
              {
                Thread.sleep(50L);
                if (Settings.this.mUsbHidReadErrorCount <= 10)
                  break;
                Settings.access$402(Settings.this, false);
                Settings.access$802(Settings.this, 0);
                break;
                Settings.access$1702(Settings.this, false);
                Settings.access$1602(Settings.this, 0);
                Settings.access$1802(Settings.this, 0);
                break label1008;
                Settings.access$1602(Settings.this, 0);
                Settings.access$1802(Settings.this, 0);
              }
            }
            catch (InterruptedException localInterruptedException2)
            {
              while (true)
                localInterruptedException2.printStackTrace();
            }
          }
          Log.v("CSR Settings", "startUsbHidScanThread end");
        }
      }
    }
    .start();
    */
  }

  public void switchToHeader(PreferenceActivity.Header paramHeader)
  {
    if (!this.mInLocalHeaderSwitch)
    {
      this.mCurrentHeader = null;
      this.mParentHeader = null;
    }
    super.switchToHeader(paramHeader);
  }

  public void testSdcardExist()
  {
    File localFile1 = new File("/mnt/sdcard");
    if ((localFile1.exists()) && (localFile1.isDirectory()))
    {
      File[] arrayOfFile2 = localFile1.listFiles();
      int k = arrayOfFile2.length;
//      for (int m = 0; m < k; m++)
//        arrayOfFile2[m];
    }
    File localFile2 = new File("/mnt/asec");
    if ((localFile2.exists()) && (localFile2.isDirectory()))
    {
      File[] arrayOfFile1 = localFile2.listFiles();
      int i = arrayOfFile1.length;
//      for (int j = 0; j < i; j++)
//        arrayOfFile1[j];
    }
  }

  public int[] usbHidSendData(int[] paramArrayOfInt)
  {

      this.mSendDataResponse = this.mHidusb.sendData(paramArrayOfInt);
      int[] arrayOfInt = this.mSendDataResponse;

      return arrayOfInt;

  }

  public static class AccessibilitySettingsActivity extends Settings
  {
  }

  public static class AccountSyncSettingsActivity extends Settings
  {
  }

  public static class AccountSyncSettingsInAddAccountActivity extends Settings
  {
  }

  public static class AdvancedWifiSettingsActivity extends Settings
  {
  }

  public static class AndroidBeamSettingsActivity extends Settings
  {
  }

  public static class ApplicationSettingsActivity extends Settings
  {
  }

  public static class BluetoothSettingsActivity extends Settings
  {
  }

  public static class CryptKeeperSettingsActivity extends Settings
  {
  }

  public static class DataUsageSummaryActivity extends Settings
  {
  }

  public static class DateTimeSettingsActivity extends Settings
  {
  }

  public static class DevelopmentSettingsActivity extends Settings
  {
  }

  public static class DeviceAdminSettingsActivity extends Settings
  {
  }

  public static class DeviceInfoSettingsActivity extends Settings
  {
  }

  public static class DisplaySettingsActivity extends Settings
  {
  }

  public static class DockSettingsActivity extends Settings
  {
  }

  public static class EthernetSettingsActivity extends Settings
  {
  }

  private static class HeaderAdapter extends ArrayAdapter<PreferenceActivity.Header>
  {
    static final int HEADER_TYPE_CATEGORY = 0;
    private static final int HEADER_TYPE_COUNT = 3;
    static final int HEADER_TYPE_NORMAL = 1;
    static final int HEADER_TYPE_SWITCH = 2;
    //private final BluetoothEnabler mBluetoothEnabler;
    private LayoutInflater mInflater;
    //private final WifiEnabler mWifiEnabler;

    public HeaderAdapter(Context paramContext, List<PreferenceActivity.Header> paramList)
    {
      super(paramContext, 0, paramList);
      this.mInflater = ((LayoutInflater)paramContext.getSystemService("layout_inflater"));
      //this.mWifiEnabler = new WifiEnabler(paramContext, new Switch(paramContext));
      //this.mBluetoothEnabler = new BluetoothEnabler(paramContext, new Switch(paramContext));
    }

    static int getHeaderType(PreferenceActivity.Header paramHeader)
    {
      if ((paramHeader.fragment == null) && (paramHeader.intent == null))
        return 0;
      if ((paramHeader.id == R.id.wifi_settings) || (paramHeader.id == R.id.bluetooth_settings))
        return 2;
      return 1;
    }

    public boolean areAllItemsEnabled()
    {
      return false;
    }

    public int getItemViewType(int paramInt)
    {
      return getHeaderType((PreferenceActivity.Header)getItem(paramInt));
    }

    public View getView(int paramInt, View paramView, ViewGroup paramViewGroup)
    {
        Header var4 = (Header)this.getItem(paramInt);
        int var5 = getHeaderType(var4);
        HeaderViewHolder var6;
        Object var7;
        if(paramView == null) {
            var6 = new HeaderViewHolder();
            var7 = null;
            switch(var5) {
                case 0:
                    var7 = new TextView(this.getContext(), (AttributeSet)null, 16843272);
                    var6.title = (TextView)var7;
                    ((View)var7).setVisibility(8);
                    break;
                case 1:
                    var7 = this.mInflater.inflate(17367151, paramViewGroup, false);
                    var6.icon = (ImageView)((View)var7).findViewById(R.id.icon);
                    var6.title = (TextView)((View)var7).findViewById(R.id.title);
                    var6.summary = (TextView)((View)var7).findViewById(R.id.summary);
                    break;
                case 2:
                    var7 = this.mInflater.inflate(R.layout.preference_header_switch_item, paramViewGroup, false);
                    var6.icon = (ImageView)((View)var7).findViewById(R.id.icon);
                    var6.title = (TextView)((View)var7).findViewById(R.id.title);
                    var6.summary = (TextView)((View)var7).findViewById(R.id.summary);
                    var6.switch_ = (Switch)((View)var7).findViewById(R.id.switchWidget);
            }

            ((View)var7).setTag(var6);
        } else {
            var7 = paramView;
            var6 = (HeaderViewHolder)paramView.getTag();
        }

        switch(var5) {
            case 0:
                var6.title.setText(var4.getTitle(this.getContext().getResources()));
                return (View)var7;
            case 2:
                if(var4.id == R.id.wifi_settings) {
                    //this.mWifiEnabler.setSwitch(var6.switch_);
                } else {
                    //this.mBluetoothEnabler.setSwitch(var6.switch_);
                }
            case 1:
                var6.icon.setImageResource(var4.iconRes);
                var6.title.setText(var4.getTitle(this.getContext().getResources()));
                CharSequence var8 = var4.getSummary(this.getContext().getResources());
                if(!TextUtils.isEmpty(var8)) {
                    var6.summary.setVisibility(0);
                    var6.summary.setText(var8);
                    return (View)var7;
                }

                var6.summary.setVisibility(8);
                return (View)var7;
            default:
                return (View)var7;
        }
    }

    public int getViewTypeCount()
    {
      return 3;
    }

    public boolean hasStableIds()
    {
      return true;
    }

    public boolean isEnabled(int paramInt)
    {
      return getItemViewType(paramInt) != 0;
    }

    public void pause()
    {
      //this.mWifiEnabler.pause();
      //this.mBluetoothEnabler.pause();
    }

    public void resume()
    {
      //this.mWifiEnabler.resume();
      //this.mBluetoothEnabler.resume();
    }

    private static class HeaderViewHolder
    {
      ImageView icon;
      TextView summary;
      Switch switch_;
      TextView title;
    }
  }

  public static class InputMethodAndLanguageSettingsActivity extends Settings
  {
  }

  public static class InputMethodAndSubtypeEnablerActivity extends Settings
  {
  }

  public static class LocalePickerActivity extends Settings
  {
  }

  public static class LocationSettingsActivity extends Settings
  {
  }

  public static class ManageAccountsSettingsActivity extends Settings
  {
  }

  public static class ManageApplicationsActivity extends Settings
  {
  }

  public static class PowerUsageSummaryActivity extends Settings
  {
  }

  public static class PrivacySettingsActivity extends Settings
  {
  }

  public static class RunningServicesActivity extends Settings
  {
  }

  public static class SecuritySettingsActivity extends Settings
  {
  }

  public static class SoundSettingsActivity extends Settings
  {
  }

  public static class SpellCheckersSettingsActivity extends Settings
  {
  }

  public static class StorageSettingsActivity extends Settings
  {
  }

  public static class StorageUseActivity extends Settings
  {
  }

  public static class TetherSettingsActivity extends Settings
  {
  }

  public static class TextToSpeechSettingsActivity extends Settings
  {
  }

  public static class UserDictionarySettingsActivity extends Settings
  {
  }

  public static class VpnSettingsActivity extends Settings
  {
  }

  public static class WifiP2pSettingsActivity extends Settings
  {
  }

  public static class WifiSettingsActivity extends Settings
  {
  }

  public static class WirelessSettingsActivity extends Settings
  {
  }
}

/* Location:           D:\Elance\works\41_MatthewDemos\CSR\CSR_unzipped\classes_dex2jar.jar
 * Qualified Name:     com.general.mediaplayer.csr.Settings
 * JD-Core Version:    0.6.0
 */