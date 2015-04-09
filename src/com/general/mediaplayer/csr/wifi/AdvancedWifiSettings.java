package com.general.mediaplayer.csr.wifi;

import android.content.ContentResolver;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.general.mediaplayer.csr.SettingsPreferenceFragment;
import com.general.mediaplayer.csr.Utils;
import com.general.mediaplayer.csr.R;

public class AdvancedWifiSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

   private static final String KEY_CURRENT_IP_ADDRESS = "current_ip_address";
   private static final String KEY_ENABLE_WIFI_WATCHDOG = "wifi_enable_watchdog_service";
   private static final String KEY_FREQUENCY_BAND = "frequency_band";
   private static final String KEY_MAC_ADDRESS = "mac_address";
   private static final String KEY_NOTIFY_OPEN_NETWORKS = "notify_open_networks";
   private static final String KEY_SLEEP_POLICY = "sleep_policy";
   private static final String TAG = "AdvancedWifiSettings";
   private WifiManager mWifiManager;


   private void initPreferences() {
      byte var1 = 1;
      CheckBoxPreference var2 = (CheckBoxPreference)this.findPreference("notify_open_networks");
      byte var3;
      if(Secure.getInt(this.getContentResolver(), "wifi_networks_available_notification_on", 0) == var1) {
         var3 = var1;
      } else {
         var3 = 0;
      }

      var2.setChecked(var3 == 1);
      var2.setEnabled(this.mWifiManager.isWifiEnabled());
      CheckBoxPreference var4 = (CheckBoxPreference)this.findPreference("wifi_enable_watchdog_service");
      if(var4 != null) {
         if(Secure.getInt(this.getContentResolver(), "wifi_watchdog_on", var1) != var1) {
            var1 = 0;
         }

         var4.setChecked(var1 == 1);
         this.getPreferenceScreen().removePreference(var4);
      }

      ListPreference var5 = (ListPreference)this.findPreference("frequency_band");
      if(this.mWifiManager.isDualBandSupported()) {
         var5.setOnPreferenceChangeListener(this);
         int var9 = this.mWifiManager.getFrequencyBand();
         if(var9 != -1) {
            var5.setValue(String.valueOf(var9));
         } else {
            Log.e("AdvancedWifiSettings", "Failed to fetch frequency band");
         }
      } else if(var5 != null) {
         this.getPreferenceScreen().removePreference(var5);
      }

      ListPreference var7 = (ListPreference)this.findPreference("sleep_policy");
      if(var7 != null) {
         if(Utils.isWifiOnly(this.getActivity())) {
            var7.setEntries(R.array.wifi_sleep_policy_entries_wifi_only);
         }

         var7.setOnPreferenceChangeListener(this);
         String var8 = String.valueOf(System.getInt(this.getContentResolver(), "wifi_sleep_policy", 2));
         var7.setValue(var8);
         this.updateSleepPolicySummary(var7, var8);
      }

   }

   private void refreshWifiInfo() {
      android.net.wifi.WifiInfo var1 = this.mWifiManager.getConnectionInfo();
      Preference var2 = this.findPreference("mac_address");
      String var3;
      if(var1 == null) {
         var3 = null;
      } else {
         var3 = var1.getMacAddress();
      }

      if(TextUtils.isEmpty(var3)) {
         var3 = this.getActivity().getString(R.string.status_unavailable);
      }

      var2.setSummary(var3);
      Preference var4 = this.findPreference("current_ip_address");
      String var5 = Utils.getWifiIpAddresses(this.getActivity());
      if(var5 == null) {
         var5 = this.getActivity().getString(R.string.status_unavailable);
      }

      var4.setSummary(var5);
   }

   private void updateSleepPolicySummary(Preference var1, String var2) {
      if(var2 != null) {
         String[] var4 = this.getResources().getStringArray(R.array.wifi_sleep_policy_values);
         int var5;
         if(Utils.isWifiOnly(this.getActivity())) {
            var5 = R.array.wifi_sleep_policy_entries_wifi_only;
         } else {
            var5 = R.array.wifi_sleep_policy_entries;
         }

         String[] var6 = this.getResources().getStringArray(var5);

         for(int var7 = 0; var7 < var4.length; ++var7) {
            if(var2.equals(var4[var7]) && var7 < var6.length) {
               var1.setSummary(var6[var7]);
               return;
            }
         }
      }

      var1.setSummary("");
      Log.e("AdvancedWifiSettings", "Invalid sleep policy value: " + var2);
   }

   public void onActivityCreated(Bundle var1) {
      super.onActivityCreated(var1);
      this.mWifiManager = (WifiManager)this.getSystemService("wifi");
   }

   public void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.addPreferencesFromResource(R.xml.wifi_advanced_settings);
   }

   public boolean onPreferenceChange(Preference var1, Object var2) {
      String var3 = var1.getKey();
      if("frequency_band".equals(var3)) {
         try {
            this.mWifiManager.setFrequencyBand(Integer.parseInt((String)var2), true);
         } catch (NumberFormatException var9) {
            Toast.makeText(this.getActivity(), R.string.wifi_setting_frequency_band_error, 0).show();
            return false;
         }
      }

      if("sleep_policy".equals(var3)) {
         try {
            String var5 = (String)var2;
            System.putInt(this.getContentResolver(), "wifi_sleep_policy", Integer.parseInt(var5));
            this.updateSleepPolicySummary(var1, var5);
         } catch (NumberFormatException var8) {
            Toast.makeText(this.getActivity(), R.string.wifi_setting_sleep_policy_error, 0).show();
            return false;
         }
      }

      return true;
   }

   public boolean onPreferenceTreeClick(PreferenceScreen var1, Preference var2) {
      String var3 = var2.getKey();
      if("notify_open_networks".equals(var3)) {
         ContentResolver var8 = this.getContentResolver();
         boolean var9 = ((CheckBoxPreference)var2).isChecked();
         byte var10 = 0;
         if(var9) {
            var10 = 1;
         }

         Secure.putInt(var8, "wifi_networks_available_notification_on", var10);
         return true;
      } else if("wifi_enable_watchdog_service".equals(var3)) {
         ContentResolver var4 = this.getContentResolver();
         boolean var5 = ((CheckBoxPreference)var2).isChecked();
         byte var6 = 0;
         if(var5) {
            var6 = 1;
         }

         Secure.putInt(var4, "wifi_watchdog_on", var6);
         return true;
      } else {
         return super.onPreferenceTreeClick(var1, var2);
      }
   }

   public void onResume() {
      super.onResume();
      this.initPreferences();
      this.refreshWifiInfo();
   }
}
