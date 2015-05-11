package com.general.mediaplayer.csr;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.general.mediaplayer.csr.Settings;
import com.general.mediaplayer.csr.services.CsrManagerService;
import java.io.File;
import java.util.List;

public class MediaPlayerBroadcastReceiver extends BroadcastReceiver {

   static final String ACTION = "android.intent.action.BOOT_COMPLETED";
   private static final String DIR_SUPER_MANAGER_KEY = "/mnt/external_sd/AdministratorPassword";
   private static final String MEDIAPLAYER_START_ACTIVITY = ".ScanMediaActivity";
   private static final int MSG_BASE = 100;
   private static final int MSG_START_APP = 101;
   static final String START_CSR_ACTION = "com.android.intent.CSR";
   private static final String SYS_CSR_RESTART_ACTION = "com.general.mediaplayer.csr.restart";
   private Context mContext;
   private Handler mHandler = new Handler() {
      public void handleMessage(Message var1) {
         switch(var1.what) {
         case MSG_START_APP:
            MediaPlayerBroadcastReceiver.this.onBootCompleted(MediaPlayerBroadcastReceiver.this.mContext, (Intent)null);
            return;
         default:
         }
      }
   };


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

   private void openApp(Context paramContext, String packageName) {
       try
       {
           PackageInfo localPackageInfo2 = paramContext.getPackageManager().getPackageInfo(packageName, 0);
           PackageManager localPackageManager = paramContext.getPackageManager();
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
               localIntent2.addFlags(268435456);
               localIntent2.putExtra("PowerOnFirst", true);
               localIntent2.setComponent(localComponentName);
               paramContext.startActivity(localIntent2);
           }
           return;
       }
       catch (PackageManager.NameNotFoundException localNameNotFoundException)
       {
           //while (true)
           //{
               localNameNotFoundException.printStackTrace();
               PackageInfo localPackageInfo1 = null;
           //}
       }
   }

   private void startCsrManagerService(Context context) {
      Log.v(" ", "===onReceive start startCsrManagerService==");
      context.startService(new Intent(context, CsrManagerService.class));
   }

   public String getAutoRunAppPackage(Context context) {
      String strSettingSp = context.getString(R.string.mediaplayer_setting_sp);
      String strAutorunAppKey = context.getString(R.string.mediaplayer_autorun_app_key);
      String strPackageName = context.getString(R.string.mediaplayer_packagename);
      String strAutorunApp = context.getSharedPreferences(strSettingSp, 1).getString(strAutorunAppKey, "");
      Log.v("", "===broadcastReceiver getAutoRunAppPackage==strPackageName==" + strAutorunApp);
      String var7 = null;
      if(strAutorunApp != null) {
         int var8 = strAutorunApp.lastIndexOf('.');
         var7 = null;
         if(var8 > 0) {
            String var9 = strAutorunApp.substring(0, var8);
            Log.v("", "===broadcastReceiver getAutoRunAppPackage==substrPackName==" + var9);
            boolean var11 = strPackageName.equalsIgnoreCase(var9);
            var7 = null;
            if(var11) {
               boolean var12 = this.isAppInstalled(context, strAutorunApp);
               var7 = null;
               if(var12) {
                  var7 = strAutorunApp;
                  Log.v("", "===broadcastReceiver getAutoRunAppPackage==installed==" + strAutorunApp);
               }
            }
         }
      }

      return var7;
   }

   public boolean isAppInstalled(Context var1, String var2) {
      if(var2 != null && var1 != null) {
         List var3 = var1.getPackageManager().getInstalledPackages(0);
         int var4 = 0;

         boolean var6;
         while(true) {
            int var5 = var3.size();
            var6 = false;
            if(var4 >= var5) {
               break;
            }

            if(((PackageInfo)var3.get(var4)).packageName.equalsIgnoreCase(var2)) {
               var6 = true;
               break;
            }

            ++var4;
         }

         return var6;
      } else {
         return false;
      }
   }

   public void onBootCompleted(Context paramContext, Intent intent) {
      String var3 = this.getAutoRunAppPackage(paramContext);
      Log.v(" ", "========onBootCompleted=====strPackageName=" + var3);
      if(!this.checkSuperManagerKey()) {
         if(var3 != null) {
             //String str = new StringBuilder().append(var3).append(".ScanMediaActivity").toString();
             String str = var3;
             openApp(paramContext, str);
         } else {
            Intent var5 = new Intent(paramContext, Settings.class);
            var5.addFlags(268435456);
            var5.putExtra("restart_app_again", true);
            Log.v("", "===broadcastReceiver onBootCompleted===");
            paramContext.startActivity(var5);
         }
      }
   }

   public void onReceive(Context context, Intent intent) {
      if(intent.getAction().equals(ACTION)) {
         this.startCsrManagerService(context);
         this.onBootCompleted(context, intent);
      } else if(intent.getAction().equals(START_CSR_ACTION)) {
         Log.v("com.android.intent.CSR", "=======Start csr Receiver in Csr============");
         this.startCsrManagerService(context);
         (new Intent(context, Settings.class)).addFlags(268435456);
         Intent var5 = new Intent();
         var5.setAction(SYS_CSR_RESTART_ACTION);
         var5.putExtra("Csr_app_run", true);
         context.sendBroadcast(var5);
         return;
      }

   }

   public void testSdcardExist() {
      Log.v("", "testSdcardExist==start=");
      File var2 = new File("/mnt/sdcard");
      if(var2.exists() && var2.isDirectory()) {
         File[] var10 = var2.listFiles();
         int var11 = var10.length;

         for(int var12 = 0; var12 < var11; ++var12) {
            File var13 = var10[var12];
            Log.v("", "testSdcardExist1==path=" + var13.getAbsolutePath());
         }
      }

      File var3 = new File("/mnt/asec");
      if(var3.exists() && var3.isDirectory()) {
         File[] var5 = var3.listFiles();
         int var6 = var5.length;

         for(int var7 = 0; var7 < var6; ++var7) {
            File var8 = var5[var7];
            Log.v("", "testSdcardExist2==path=" + var8.getAbsolutePath());
         }
      }

      Log.v("", "testSdcardExist==end=");
   }
}
