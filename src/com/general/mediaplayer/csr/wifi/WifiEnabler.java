package com.general.mediaplayer.csr.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import com.general.mediaplayer.csr.WirelessSettings;
import java.util.concurrent.atomic.AtomicBoolean;

import com.general.mediaplayer.csr.R;

public class WifiEnabler implements OnCheckedChangeListener {

   private AtomicBoolean mConnected = new AtomicBoolean(false);
   private final Context mContext;
   private final IntentFilter mIntentFilter;
   private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
      public void onReceive(Context var1, Intent var2) {
         String var3 = var2.getAction();
         if("android.net.wifi.WIFI_STATE_CHANGED".equals(var3)) {
            WifiEnabler.this.handleWifiStateChanged(var2.getIntExtra("wifi_state", 4));
         } else if("android.net.wifi.supplicant.STATE_CHANGE".equals(var3)) {
            if(!WifiEnabler.this.mConnected.get()) {
               WifiEnabler.this.handleStateChanged(android.net.wifi.WifiInfo.getDetailedStateOf((SupplicantState)var2.getParcelableExtra("newState")));
               return;
            }
         } else if("android.net.wifi.STATE_CHANGE".equals(var3)) {
            NetworkInfo var4 = (NetworkInfo)var2.getParcelableExtra("networkInfo");
            WifiEnabler.this.mConnected.set(var4.isConnected());
            WifiEnabler.this.handleStateChanged(var4.getDetailedState());
            return;
         }

      }
   };
   private boolean mStateMachineEvent;
   private Switch mSwitch;
   private final WifiManager mWifiManager;


   public WifiEnabler(Context var1, Switch var2) {
      this.mContext = var1;
      this.mSwitch = var2;
      this.mWifiManager = (WifiManager)var1.getSystemService("wifi");
      this.mIntentFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
      this.mIntentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
      this.mIntentFilter.addAction("android.net.wifi.STATE_CHANGE");
   }

   private void handleStateChanged(DetailedState var1) {}

   private void handleWifiStateChanged(int var1) {
      switch(var1) {
      case 0:
         this.mSwitch.setEnabled(true);
         return;
      case 1:
         this.setSwitchChecked(false);
         this.mSwitch.setEnabled(true);
         return;
      case 2:
         this.mSwitch.setEnabled(true);
         return;
      case 3:
         this.setSwitchChecked(true);
         this.mSwitch.setEnabled(true);
         return;
      default:
         this.setSwitchChecked(false);
         this.mSwitch.setEnabled(true);
      }
   }

   private void setSwitchChecked(boolean var1) {
      if(var1 != this.mSwitch.isChecked()) {
         this.mStateMachineEvent = true;
         this.mSwitch.setChecked(var1);
         this.mStateMachineEvent = false;
      }

   }

   public void onCheckedChanged(CompoundButton var1, boolean var2) {
      if(!this.mStateMachineEvent) {
         if(var2 && !WirelessSettings.isRadioAllowed(this.mContext, "wifi")) {
            Toast.makeText(this.mContext, R.string.wifi_in_airplane_mode, 0).show();
            var1.setChecked(false);
         }

         int var3 = this.mWifiManager.getWifiApState();
         if(var2 && (var3 == 12 || var3 == 13)) {
            this.mWifiManager.setWifiApEnabled((WifiConfiguration)null, false);
         }

         if(this.mWifiManager.setWifiEnabled(var2)) {
            this.mSwitch.setEnabled(false);
         } else {
            Toast.makeText(this.mContext, 2131427766, 0).show();
         }
      }
   }

   public void pause() {
      this.mContext.unregisterReceiver(this.mReceiver);
      this.mSwitch.setOnCheckedChangeListener((OnCheckedChangeListener)null);
   }

   public void resume() {
      this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
      this.mSwitch.setOnCheckedChangeListener(this);
   }

   public void setSwitch(Switch var1) {
      if(this.mSwitch != var1) {
         this.mSwitch.setOnCheckedChangeListener((OnCheckedChangeListener)null);
         this.mSwitch = var1;
         this.mSwitch.setOnCheckedChangeListener(this);
         int var2 = this.mWifiManager.getWifiState();
         boolean var3;
         if(var2 == 3) {
            var3 = true;
         } else {
            var3 = false;
         }

         boolean var4;
         if(var2 == 1) {
            var4 = true;
         } else {
            var4 = false;
         }

         this.mSwitch.setChecked(var3);
         if(!var3 && !var4) {
            this.mSwitch.setEnabled(true);
         } else {
            Switch var5;
            boolean var6;
            label28: {
               var5 = this.mSwitch;
               if(!var3) {
                  var6 = false;
                  if(!var4) {
                     break label28;
                  }
               }

               var6 = true;
            }

            var5.setEnabled(var6);
         }
      }
   }
}
