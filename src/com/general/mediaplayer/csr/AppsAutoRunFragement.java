package com.general.mediaplayer.csr;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import com.general.mediaplayer.csr.Settings;
import com.general.mediaplayer.csr.app.AppInfo;
import com.general.mediaplayer.csr.app.PakageInfoProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppsAutoRunFragement extends DialogFragment {

   private static final int LOAD_INSTALLED_APP_FINISH = 1002;
   private static final int MSG_BACK = 1001;
   private static final int MSG_BASE = 1000;
   private ButtonOnClick buttonOnClick = new ButtonOnClick(0);
   private Handler handler = new Handler() {
      public void handleMessage(Message var1) {
         switch(var1.what) {
         case 1001:
            AppsAutoRunFragement.this.doBack();
            return;
         case 1002:
            AppsAutoRunFragement.this.initCilentAppInfo();
            AppsAutoRunFragement.this.mIsInstalledAppViewprefer = true;
            AppsAutoRunFragement.this.showSetAutoRunAppDialog();
            return;
         default:
         }
      }
   };
   private List mAllInstallAppInfos;
   private AppInfo mAppInfo;
   private String mAutoRunAppPackageName;
   private List mClientAppInfos;
   Editor mEditor;
   private String[] mInstalledCilentAppNames = new String[0];
   private boolean mIsInstalledAppViewprefer = false;
   private PakageInfoProvider mPakageInfoProvider;
   private SharedPreferences mSharedPreferences;


   private void LoadInstalledAppInfo() {
      (new Thread() {
         public void run() {
            AppsAutoRunFragement.this.mAllInstallAppInfos = AppsAutoRunFragement.this.mPakageInfoProvider.getAppInfo();
            Message var2 = Message.obtain();
            var2.what = 1002;
            AppsAutoRunFragement.this.handler.sendMessage(var2);
         }
      }).start();
   }

   private void initCilentAppInfo() {
      this.mClientAppInfos = new ArrayList();
      Iterator var1 = this.mAllInstallAppInfos.iterator();

      while(var1.hasNext()) {
         AppInfo var2 = (AppInfo)var1.next();
         if(var2.getIsCilentApp().booleanValue()) {
            this.mClientAppInfos.add(var2);
         }
      }

   }

   private void initView() {
      this.mPakageInfoProvider = new PakageInfoProvider(this.getActivity().getApplicationContext());
      this.LoadInstalledAppInfo();
      String var1 = this.getResources().getString(R.string.mediaplayer_setting_sp);
      String var2 = this.getResources().getString(R.string.mediaplayer_autorun_app_key);
      this.mSharedPreferences = this.getActivity().getApplicationContext().getSharedPreferences(var1, 2);
      this.mEditor = this.mSharedPreferences.edit();
      this.mAutoRunAppPackageName = this.mSharedPreferences.getString(var2, "");
      Log.v(this.getTag(), "====initView====mAutoRunAppPackageName=" + this.mAutoRunAppPackageName);
   }

   private void setAutoRunClientApp(int var1) {
      if(this.mClientAppInfos != null && this.mClientAppInfos.size() != 0) {
         ((Settings)this.getActivity()).mRestartAppAgain = false;
         this.mAutoRunAppPackageName = ((AppInfo)this.mClientAppInfos.get(var1)).getPackageName();
         Log.v("test", "mAutoRunAppPackageName=" + this.mAutoRunAppPackageName);
         String strAutorunAppKey = this.getResources().getString(R.string.mediaplayer_autorun_app_key);
         Log.v("test", "strAutoRunAppKey=" + strAutorunAppKey);
         this.mEditor.putString(strAutorunAppKey, this.mAutoRunAppPackageName);
         this.mEditor.commit();
         Log.v(this.getTag(), "====setAutoRunClientApp====mAutoRunAppPackageName=" + this.mAutoRunAppPackageName);
      }
   }

   private void showSetAutoRunAppDialog() {
      if(!this.isAdded()) {
         Log.v("AppsAutoRunFragement", "===AppsAutoRunFragement===showSetAutoRunAppDialog==not addad=");
      } else if(this.mIsInstalledAppViewprefer) {
         int var1 = this.mClientAppInfos.size();
         int var2 = 0;
         this.mInstalledCilentAppNames = new String[var1];
         String[] var3 = new String[var1];

         for(int var4 = 0; var4 < var1; ++var4) {
            this.mInstalledCilentAppNames[var4] = ((AppInfo)this.mClientAppInfos.get(var4)).getAppName();
            var3[var4] = ((AppInfo)this.mClientAppInfos.get(var4)).getPackageName();
            if(this.mAutoRunAppPackageName.equalsIgnoreCase(var3[var4])) {
               var2 = var4;
            }
         }

         this.buttonOnClick.setPosition(var2);
         String var5 = this.getResources().getString(R.string.set_auto_run_dialog_title);
         String var6 = this.getResources().getString(R.string.set_auto_run_dialog_sure);
         String var7 = this.getResources().getString(R.string.set_auto_run_dialog_cancle);
         Builder var8 = new Builder(this.getActivity());
         var8.setTitle(var5);
         var8.setSingleChoiceItems(this.mInstalledCilentAppNames, var2, this.buttonOnClick);
         var8.setPositiveButton(var6, this.buttonOnClick);
         var8.setNegativeButton(var7, this.buttonOnClick);
         var8.setCancelable(false);
         var8.show();
         var8.setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface var1, int var2, KeyEvent var3) {
               Log.v(AppsAutoRunFragement.this.getTag(), "====setAutoRunClientApp====OnKeyListener keyCode=" + var2 + "=KeyEvent=" + var3.getAction());
               return false;
            }
         });
         return;
      }

   }

   public void doBack() {
      (new Thread() {
         public void run() {
            Log.v(AppsAutoRunFragement.this.getTag(), "ResetSystemDialogFragement==doBack");

            try {
               Runtime.getRuntime().exec("input keyevent 4");
            } catch (IOException var3) {
               Log.e("Exception when doBack", var3.toString());
            }
         }
      }).start();
   }

   public void onActivityCreated(Bundle var1) {
      super.onActivityCreated(var1);
      this.initView();
   }

   public Dialog onCreateDialog(Bundle var1) {
      return super.onCreateDialog(var1);
   }

   public void onStart() {
      super.onStart();
   }

   private class ButtonOnClick implements OnClickListener {

      private int index;


      public ButtonOnClick(int var2) {
         this.index = var2;
      }

      public void onClick(DialogInterface var1, int var2) {
         if(var2 >= 0) {
            this.index = var2;
         } else {
            if(var2 == -1) {
               AppsAutoRunFragement.this.setAutoRunClientApp(this.index);
               Message var5 = new Message();
               var5.what = 1001;
               AppsAutoRunFragement.this.handler.sendMessage(var5);
               return;
            }

            if(var2 == -2) {
               Message var3 = new Message();
               var3.what = 1001;
               AppsAutoRunFragement.this.handler.sendMessage(var3);
               return;
            }
         }

      }

      public void setPosition(int var1) {
         this.index = var1;
      }
   }
}
