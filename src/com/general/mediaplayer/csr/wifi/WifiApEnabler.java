package com.general.mediaplayer.csr.wifi;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.preference.CheckBoxPreference;
import android.provider.Settings.Secure;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import java.util.ArrayList;

import com.general.mediaplayer.csr.R;

public class WifiApEnabler {

   private final CheckBoxPreference mCheckBox;
   ConnectivityManager mCm;
   private final Context mContext;
   private final IntentFilter mIntentFilter;
   private final CharSequence mOriginalSummary;
   private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
      public void onReceive(Context var1, Intent var2) {
         String var3 = var2.getAction();
         if("android.net.wifi.WIFI_AP_STATE_CHANGED".equals(var3)) {
            WifiApEnabler.this.handleWifiApStateChanged(var2.getIntExtra("wifi_state", 14));
         } else {
            if("android.net.conn.TETHER_STATE_CHANGED".equals(var3)) {
               ArrayList var4 = var2.getStringArrayListExtra("availableArray");
               ArrayList var5 = var2.getStringArrayListExtra("activeArray");
               ArrayList var6 = var2.getStringArrayListExtra("erroredArray");
               WifiApEnabler.this.updateTetherState(var4.toArray(), var5.toArray(), var6.toArray());
               return;
            }

            if("android.intent.action.AIRPLANE_MODE".equals(var3)) {
               WifiApEnabler.this.enableWifiCheckBox();
               return;
            }
         }

      }
   };
   private WifiManager mWifiManager;
   private String[] mWifiRegexs;


   public WifiApEnabler(Context var1, CheckBoxPreference var2) {
      this.mContext = var1;
      this.mCheckBox = var2;
      this.mOriginalSummary = var2.getSummary();
      var2.setPersistent(false);
      this.mWifiManager = (WifiManager)var1.getSystemService("wifi");
      this.mCm = (ConnectivityManager)this.mContext.getSystemService("connectivity");
      this.mWifiRegexs = this.mCm.getTetherableWifiRegexs();
      this.mIntentFilter = new IntentFilter("android.net.wifi.WIFI_AP_STATE_CHANGED");
      this.mIntentFilter.addAction("android.net.conn.TETHER_STATE_CHANGED");
      this.mIntentFilter.addAction("android.intent.action.AIRPLANE_MODE");
   }

   private void enableWifiCheckBox() {
      boolean var1;
      if(System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 0) {
         var1 = true;
      } else {
         var1 = false;
      }

      if(!var1) {
         this.mCheckBox.setEnabled(true);
      } else {
         this.mCheckBox.setSummary(this.mOriginalSummary);
         this.mCheckBox.setEnabled(false);
      }
   }

   private void handleWifiApStateChanged(int var1) {
      switch(var1) {
      case 10:
         this.mCheckBox.setSummary(R.string.wifi_stopping);
         this.mCheckBox.setEnabled(false);
         return;
      case 11:
         this.mCheckBox.setChecked(false);
         this.mCheckBox.setSummary(this.mOriginalSummary);
         this.enableWifiCheckBox();
         return;
      case 12:
         this.mCheckBox.setSummary(R.string.wifi_starting);
         this.mCheckBox.setEnabled(false);
         return;
      case 13:
         this.mCheckBox.setChecked(true);
         this.mCheckBox.setEnabled(true);
         return;
      default:
         this.mCheckBox.setChecked(false);
         this.mCheckBox.setSummary(R.string.wifi_error);
         this.enableWifiCheckBox();
      }
   }

   private void updateTetherState(Object[] var1, Object[] var2, Object[] var3) {
      boolean var4 = false;
      boolean var5 = false;
      int var6 = var2.length;

      for(int var7 = 0; var7 < var6; ++var7) {
         String var14 = (String)var2[var7];
         String[] var15 = this.mWifiRegexs;
         int var16 = var15.length;

         for(int var17 = 0; var17 < var16; ++var17) {
            if(var14.matches(var15[var17])) {
               var4 = true;
            }
         }
      }

      int var8 = var3.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         String var10 = (String)var3[var9];
         String[] var11 = this.mWifiRegexs;
         int var12 = var11.length;

         for(int var13 = 0; var13 < var12; ++var13) {
            if(var10.matches(var11[var13])) {
               var5 = true;
            }
         }
      }

      if(var4) {
         this.updateConfigSummary(this.mWifiManager.getWifiApConfiguration());
      } else if(var5) {
         this.mCheckBox.setSummary(R.string.wifi_error);
         return;
      }

   }

   public void pause() {
      this.mContext.unregisterReceiver(this.mReceiver);
   }

   public void resume() {
      this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
      this.enableWifiCheckBox();
   }

   public void setSoftapEnabled(boolean var1) {
      ContentResolver var2 = this.mContext.getContentResolver();
      int var3 = this.mWifiManager.getWifiState();
      if(var1 && (var3 == 2 || var3 == 3)) {
         this.mWifiManager.setWifiEnabled(false);
         Secure.putInt(var2, "wifi_saved_state", 1);
      }

      if(this.mWifiManager.setWifiApEnabled((WifiConfiguration)null, var1)) {
         this.mCheckBox.setEnabled(false);
      } else {
         this.mCheckBox.setSummary(R.string.wifi_error);
      }

      if(!var1) {
         int var6;
         label26: {
            int var10;
            try {
               var10 = Secure.getInt(var2, "wifi_saved_state");
            } catch (SettingNotFoundException var11) {
               var6 = 0;
               break label26;
            }

            var6 = var10;
         }

         if(var6 == 1) {
            this.mWifiManager.setWifiEnabled(true);
            Secure.putInt(var2, "wifi_saved_state", 0);
         }

         Secure.putInt(var2, "wifi_tether_on", 0);
      } else {
         Secure.putInt(var2, "wifi_tether_on", 1);
      }
   }

   public void updateConfigSummary(WifiConfiguration var1) {
      String var2 = "17040348";//this.mContext.getString(17040348);
      CheckBoxPreference var3 = this.mCheckBox;
      String var4 = this.mContext.getString(R.string.wifi_tether_enabled_subtext);
      Object[] var5 = new Object[1];
      if(var1 != null) {
         var2 = var1.SSID;
      }

      var5[0] = var2;
      var3.setSummary(String.format(var4, var5));
   }
}
