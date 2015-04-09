package com.general.mediaplayer.csr.wifi;

import android.R;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceClickListener;
import android.text.Editable;
import android.widget.EditText;

public class WifiAPITest extends PreferenceActivity implements OnPreferenceClickListener {

   private static final String KEY_DISABLE_NETWORK = "disable_network";
   private static final String KEY_DISCONNECT = "disconnect";
   private static final String KEY_ENABLE_NETWORK = "enable_network";
   private static final String TAG = "WifiAPITest";
   private Preference mWifiDisableNetwork;
   private Preference mWifiDisconnect;
   private Preference mWifiEnableNetwork;
   private WifiManager mWifiManager;
   private int netid;


   private void onCreatePreferences() {
      this.addPreferencesFromResource(R.layout.wifi_api_test);
      PreferenceScreen var1 = this.getPreferenceScreen();
      this.mWifiDisconnect = var1.findPreference("disconnect");
      this.mWifiDisconnect.setOnPreferenceClickListener(this);
      this.mWifiDisableNetwork = var1.findPreference("disable_network");
      this.mWifiDisableNetwork.setOnPreferenceClickListener(this);
      this.mWifiEnableNetwork = var1.findPreference("enable_network");
      this.mWifiEnableNetwork.setOnPreferenceClickListener(this);
   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.onCreatePreferences();
      this.mWifiManager = (WifiManager)this.getSystemService("wifi");
   }

   public boolean onPreferenceClick(Preference var1) {
      if(var1 == this.mWifiDisconnect) {
         this.mWifiManager.disconnect();
      } else if(var1 == this.mWifiDisableNetwork) {
         Builder var2 = new Builder(this);
         var2.setTitle("Input");
         var2.setMessage("Enter Network ID");
         final EditText var5 = new EditText(this);
         var2.setView(var5);
         var2.setPositiveButton("Ok", new OnClickListener() {
            public void onClick(DialogInterface var1, int var2) {
               Editable var3 = var5.getText();
               WifiAPITest.this.netid = Integer.parseInt(var3.toString());
               WifiAPITest.this.mWifiManager.disableNetwork(WifiAPITest.this.netid);
            }
         });
         var2.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface var1, int var2) {}
         });
         var2.show();
      } else if(var1 == this.mWifiEnableNetwork) {
         Builder var10 = new Builder(this);
         var10.setTitle("Input");
         var10.setMessage("Enter Network ID");
         final EditText var13 = new EditText(this);
         var10.setView(var13);
         var10.setPositiveButton("Ok", new OnClickListener() {
            public void onClick(DialogInterface var1, int var2) {
               Editable var3 = var13.getText();
               WifiAPITest.this.netid = Integer.parseInt(var3.toString());
               WifiAPITest.this.mWifiManager.enableNetwork(WifiAPITest.this.netid, false);
            }
         });
         var10.setNegativeButton("Cancel", new OnClickListener() {
            public void onClick(DialogInterface var1, int var2) {}
         });
         var10.show();
      }

      return true;
   }

   public boolean onPreferenceTreeClick(PreferenceScreen var1, Preference var2) {
      super.onPreferenceTreeClick(var1, var2);
      return false;
   }
}
