package com.general.mediaplayer.csr;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pManager;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.provider.Settings.System;
import com.general.mediaplayer.csr.AirplaneModeEnabler;
import com.general.mediaplayer.csr.SettingsPreferenceFragment;
import com.general.mediaplayer.csr.Utils;
import com.general.mediaplayer.csr.nfc.NfcEnabler;
import com.general.mediaplayer.csr.wifi.p2p.WifiP2pEnabler;

public class WirelessSettings extends SettingsPreferenceFragment {

   public static final String EXIT_ECM_RESULT = "exit_ecm_result";
   private static final String KEY_ANDROID_BEAM_SETTINGS = "android_beam_settings";
   private static final String KEY_ETHERNET_SETTINGS = "ethernet_settings";
   private static final String KEY_MOBILE_NETWORK_SETTINGS = "mobile_network_settings";
   private static final String KEY_PROXY_SETTINGS = "proxy_settings";
   private static final String KEY_TETHER_SETTINGS = "tether_settings";
   private static final String KEY_TOGGLE_AIRPLANE = "toggle_airplane";
   private static final String KEY_TOGGLE_NFC = "toggle_nfc";
   private static final String KEY_TOGGLE_WIFI_P2P = "toggle_wifi_p2p";
   private static final String KEY_VPN_SETTINGS = "vpn_settings";
   private static final String KEY_WIFI_P2P_SETTINGS = "wifi_p2p_settings";
   private static final String KEY_WIMAX_SETTINGS = "wimax_settings";
   public static final int REQUEST_CODE_EXIT_ECM = 1;
   private AirplaneModeEnabler mAirplaneModeEnabler;
   private CheckBoxPreference mAirplaneModePreference;
   private NfcAdapter mNfcAdapter;
   private NfcEnabler mNfcEnabler;
   private WifiP2pEnabler mWifiP2pEnabler;


   public static boolean isRadioAllowed(Context var0, String var1) {
      if(AirplaneModeEnabler.isAirplaneModeOn(var0)) {
         String var2 = System.getString(var0.getContentResolver(), "airplane_mode_toggleable_radios");
         if(var2 == null || !var2.contains(var1)) {
            return false;
         }
      }

      return true;
   }

   public void onActivityResult(int var1, int var2, Intent var3) {
      if(var1 == 1) {
         Boolean var4 = Boolean.valueOf(var3.getBooleanExtra("exit_ecm_result", false));
         this.mAirplaneModeEnabler.setAirplaneModeInECM(var4.booleanValue(), this.mAirplaneModePreference.isChecked());
      }

   }

   public void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.addPreferencesFromResource(2131034171);
      Activity var2 = this.getActivity();
      this.mAirplaneModePreference = (CheckBoxPreference)this.findPreference("toggle_airplane");
      CheckBoxPreference var3 = (CheckBoxPreference)this.findPreference("toggle_nfc");
      PreferenceScreen var4 = (PreferenceScreen)this.findPreference("android_beam_settings");
      CheckBoxPreference var5 = (CheckBoxPreference)this.findPreference("toggle_wifi_p2p");
      this.mAirplaneModeEnabler = new AirplaneModeEnabler(var2, this.mAirplaneModePreference);
      this.mNfcEnabler = new NfcEnabler(var2, var3, var4);
      String var6 = System.getString(var2.getContentResolver(), "airplane_mode_toggleable_radios");
      boolean var7 = this.getResources().getBoolean(17891377);
      if(!var7) {
         PreferenceScreen var23 = this.getPreferenceScreen();
         Preference var24 = this.findPreference("wimax_settings");
         if(var24 != null) {
            var23.removePreference(var24);
         }
      } else if(var6 == null || !var6.contains("wimax") && var7) {
         this.findPreference("wimax_settings").setDependency("toggle_airplane");
      }

      if(var6 == null || !var6.contains("wifi")) {
         this.findPreference("vpn_settings").setDependency("toggle_airplane");
      }

      if(var6 != null && !var6.contains("bluetooth")) {
         ;
      }

      if(var6 == null || !var6.contains("nfc")) {
         this.findPreference("toggle_nfc").setDependency("toggle_airplane");
         this.findPreference("android_beam_settings").setDependency("toggle_airplane");
      }

      this.getPreferenceScreen().removePreference(this.findPreference("toggle_airplane"));
      this.getPreferenceScreen().removePreference(this.findPreference("ethernet_settings"));
      this.getPreferenceScreen().removePreference(this.findPreference("vpn_settings"));
      this.mNfcAdapter = null;
      if(this.mNfcAdapter == null) {
         this.getPreferenceScreen().removePreference(var3);
         this.getPreferenceScreen().removePreference(var4);
         this.mNfcEnabler = null;
      }

      this.getPreferenceScreen().removePreference(this.findPreference("mobile_network_settings"));
      WifiP2pManager var10000 = (WifiP2pManager)var2.getSystemService("wifip2p");
      this.getPreferenceScreen().removePreference(var5);
      this.getPreferenceScreen().removePreference(this.findPreference("wifi_p2p_settings"));
      Preference var15 = this.findPreference("proxy_settings");
      DevicePolicyManager var16 = (DevicePolicyManager)var2.getSystemService("device_policy");
      this.getPreferenceScreen().removePreference(var15);
      boolean var18;
      if(var16.getGlobalProxyAdmin() == null) {
         var18 = true;
      } else {
         var18 = false;
      }

      var15.setEnabled(var18);
      ConnectivityManager var19 = (ConnectivityManager)var2.getSystemService("connectivity");
      if(!var19.isTetheringSupported()) {
         this.getPreferenceScreen().removePreference(this.findPreference("tether_settings"));
      } else {
         this.findPreference("tether_settings").setTitle(Utils.getTetheringLabel(var19));
      }
   }

   public void onPause() {
      super.onPause();
      this.mAirplaneModeEnabler.pause();
      if(this.mNfcEnabler != null) {
         this.mNfcEnabler.pause();
      }

      if(this.mWifiP2pEnabler != null) {
         this.mWifiP2pEnabler.pause();
      }

   }

   public boolean onPreferenceTreeClick(PreferenceScreen var1, Preference var2) {
      if(var2 == this.mAirplaneModePreference && Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
         this.startActivityForResult(new Intent("android.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", (Uri)null), 1);
         return true;
      } else {
         return super.onPreferenceTreeClick(var1, var2);
      }
   }

   public void onResume() {
      super.onResume();
      this.mAirplaneModeEnabler.resume();
      if(this.mNfcEnabler != null) {
         this.mNfcEnabler.resume();
      }

      if(this.mWifiP2pEnabler != null) {
         this.mWifiP2pEnabler.resume();
      }

   }
}
