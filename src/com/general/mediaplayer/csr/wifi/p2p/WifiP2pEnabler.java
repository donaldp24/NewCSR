package com.general.mediaplayer.csr.wifi.p2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;

import java.lang.reflect.Method;

public class WifiP2pEnabler implements OnPreferenceChangeListener {

   private static final String TAG = "WifiP2pEnabler";
   private Channel mChannel;
   private final CheckBoxPreference mCheckBox;
   private final Context mContext;
   private final IntentFilter mIntentFilter;
   private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
      public void onReceive(Context var1, Intent var2) {
         if("android.net.wifi.p2p.STATE_CHANGED".equals(var2.getAction())) {
            WifiP2pEnabler.this.handleP2pStateChanged(var2.getIntExtra("wifi_p2p_state", 1));
         }

      }
   };
   private WifiP2pManager mWifiP2pManager;


   public WifiP2pEnabler(Context var1, CheckBoxPreference var2) {
      this.mContext = var1;
      this.mCheckBox = var2;
      this.mWifiP2pManager = (WifiP2pManager)var1.getSystemService("wifip2p");
      if(this.mWifiP2pManager != null) {
         this.mChannel = this.mWifiP2pManager.initialize(this.mContext, this.mContext.getMainLooper(), (ChannelListener)null);
         if(this.mChannel == null) {
            Log.e("WifiP2pEnabler", "Failed to set up connection with wifi p2p service");
            this.mWifiP2pManager = null;
            this.mCheckBox.setEnabled(false);
         }
      } else {
         Log.e("WifiP2pEnabler", "mWifiP2pManager is null!");
      }

      this.mIntentFilter = new IntentFilter("android.net.wifi.p2p.STATE_CHANGED");
   }

   private void handleP2pStateChanged(int var1) {
      this.mCheckBox.setEnabled(true);
      switch(var1) {
      case 1:
         this.mCheckBox.setChecked(false);
         return;
      case 2:
         this.mCheckBox.setChecked(true);
         return;
      default:
         Log.e("WifiP2pEnabler", "Unhandled wifi state " + var1);
      }
   }

   public boolean onPreferenceChange(Preference var1, Object var2) {
      if(this.mWifiP2pManager == null) {
         return false;
      } else {
         this.mCheckBox.setEnabled(false);
         if(((Boolean)var2).booleanValue()) {

             // this.mWifiP2pManager.enableP2p(this.mChannel);
             try {
                 Method enableP2p = this.mWifiP2pManager.getClass().getDeclaredMethod("enableP2p", Channel.class);
                 enableP2p.setAccessible(true);
                 enableP2p.invoke(this.mWifiP2pManager, this.mChannel);
             } catch (Exception e) {
                 e.printStackTrace();
             }

            this.mCheckBox.setEnabled(true);
            return false;
         } else {

             //this.mWifiP2pManager.disableP2p(this.mChannel);
             try {
                 Method disableP2p = this.mWifiP2pManager.getClass().getDeclaredMethod("disableP2p", Channel.class);
                 disableP2p.setAccessible(true);
                 disableP2p.invoke(this.mWifiP2pManager, this.mChannel);
             } catch (Exception e) {
                 e.printStackTrace();
             }
             this.mCheckBox.setEnabled(true);
            return false;
         }
      }
   }

   public void pause() {
      if(this.mWifiP2pManager != null) {
         this.mContext.unregisterReceiver(this.mReceiver);
         this.mCheckBox.setOnPreferenceChangeListener((OnPreferenceChangeListener)null);
      }
   }

   public void resume() {
      if(this.mWifiP2pManager != null) {
         this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
         this.mCheckBox.setOnPreferenceChangeListener(this);
      }
   }
}
