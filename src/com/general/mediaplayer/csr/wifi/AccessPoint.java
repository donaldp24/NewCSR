package com.general.mediaplayer.csr.wifi;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.Preference;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import com.general.mediaplayer.csr.wifi.Summary;

import com.general.mediaplayer.csr.R;

class AccessPoint extends Preference {

   private static final String KEY_CONFIG = "key_config";
   private static final String KEY_DETAILEDSTATE = "key_detailedstate";
   private static final String KEY_SCANRESULT = "key_scanresult";
   private static final String KEY_WIFIINFO = "key_wifiinfo";
   static final int SECURITY_EAP = 3;
   static final int SECURITY_NONE = 0;
   static final int SECURITY_PSK = 2;
   static final int SECURITY_WEP = 1;
   private static final int[] STATE_NONE = new int[0];
   private static final int[] STATE_SECURED = new int[]{R.attr.state_encrypted};
   static final String TAG = "Settings.AccessPoint";
   String bssid;
   private WifiConfiguration mConfig;
   private android.net.wifi.WifiInfo mInfo;
   private int mRssi;
   ScanResult mScanResult;
   private DetailedState mState;
   int networkId;
   PskType pskType;
   int security;
   String ssid;
   boolean wpsAvailable = false;


   AccessPoint(Context var1, ScanResult var2) {
      super(var1);
      this.pskType = PskType.UNKNOWN;
      this.setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
      this.loadResult(var2);
      this.refresh();
   }

   AccessPoint(Context var1, WifiConfiguration var2) {
      super(var1);
      this.pskType = PskType.UNKNOWN;
      this.setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
      this.loadConfig(var2);
      this.refresh();
   }

   AccessPoint(Context var1, Bundle var2) {
      super(var1);
      this.pskType = PskType.UNKNOWN;
      this.setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
      this.mConfig = (WifiConfiguration)var2.getParcelable("key_config");
      if(this.mConfig != null) {
         this.loadConfig(this.mConfig);
      }

      this.mScanResult = (ScanResult)var2.getParcelable("key_scanresult");
      if(this.mScanResult != null) {
         this.loadResult(this.mScanResult);
      }

      this.mInfo = (android.net.wifi.WifiInfo)var2.getParcelable("key_wifiinfo");
      if(var2.containsKey("key_detailedstate")) {
         this.mState = DetailedState.valueOf(var2.getString("key_detailedstate"));
      }

      this.update(this.mInfo, this.mState);
   }

   static String convertToQuotedString(String var0) {
      return "\"" + var0 + "\"";
   }

   private static PskType getPskType(ScanResult var0) {
      boolean var1 = var0.capabilities.contains("WPA-PSK");
      boolean var2 = var0.capabilities.contains("WPA2-PSK");
      if(var2 && var1) {
         return PskType.WPA_WPA2;
      } else if(var2) {
         return PskType.WPA2;
      } else if(var1) {
         return PskType.WPA;
      } else {
         Log.w("Settings.AccessPoint", "Received abnormal flag string: " + var0.capabilities);
         return PskType.UNKNOWN;
      }
   }

   private static int getSecurity(ScanResult var0) {
      return var0.capabilities.contains("WEP")?1:(var0.capabilities.contains("PSK")?2:(var0.capabilities.contains("EAP")?3:0));
   }

   static int getSecurity(WifiConfiguration var0) {
      byte var1 = 1;
      if(var0.allowedKeyManagement.get(var1)) {
         var1 = 2;
      } else {
         if(var0.allowedKeyManagement.get(2) || var0.allowedKeyManagement.get(3)) {
            return 3;
         }

         if(var0.wepKeys[0] == null) {
            return 0;
         }
      }

      return var1;
   }

   private void loadConfig(WifiConfiguration var1) {
      String var2;
      if(var1.SSID == null) {
         var2 = "";
      } else {
         var2 = removeDoubleQuotes(var1.SSID);
      }

      this.ssid = var2;
      this.bssid = var1.BSSID;
      this.security = getSecurity(var1);
      this.networkId = var1.networkId;
      this.mRssi = Integer.MAX_VALUE;
      this.mConfig = var1;
   }

   private void loadResult(ScanResult var1) {
      this.ssid = var1.SSID;
      this.bssid = var1.BSSID;
      this.security = getSecurity(var1);
      boolean var2;
      if(this.security != 3 && var1.capabilities.contains("WPS")) {
         var2 = true;
      } else {
         var2 = false;
      }

      this.wpsAvailable = var2;
      if(this.security == 2) {
         this.pskType = getPskType(var1);
      }

      this.networkId = -1;
      this.mRssi = var1.level;
      this.mScanResult = var1;
   }

   private void refresh() {
      this.setTitle(this.ssid);
      Context var1 = this.getContext();
      if(this.mState != null) {
         this.setSummary(Summary.get(var1, this.mState));
      } else if(this.mRssi == Integer.MAX_VALUE) {
         this.setSummary(var1.getString(R.string.wifi_not_in_range));
      } else if(this.mConfig != null && this.mConfig.status == 1) {
         switch(this.mConfig.disableReason) {
         case 0:
            this.setSummary(var1.getString(R.string.wifi_disabled_generic));
            return;
         case 1:
         case 2:
            this.setSummary(var1.getString(R.string.wifi_disabled_network_failure));
            return;
         case 3:
            this.setSummary(var1.getString(R.string.wifi_disabled_password_failure));
            return;
         default:
         }
      } else {
         StringBuilder var2 = new StringBuilder();
         if(this.mConfig != null) {
            var2.append(var1.getString(R.string.wifi_remembered));
         }

         if(this.security != 0) {
            String var5;
            if(var2.length() == 0) {
               var5 = var1.getString(R.string.wifi_secured_first_item);
            } else {
               var5 = var1.getString(R.string.wifi_secured_second_item);
            }

            Object[] var6 = new Object[]{this.getSecurityString(true)};
            var2.append(String.format(var5, var6));
         }

         if(this.mConfig == null && this.wpsAvailable) {
            if(var2.length() == 0) {
               var2.append(var1.getString(R.string.wifi_wps_available_first_item));
            } else {
               var2.append(var1.getString(R.string.wifi_wps_available_second_item));
            }
         }

         this.setSummary(var2.toString());
      }
   }

   static String removeDoubleQuotes(String var0) {
      int var1 = var0.length();
      if(var1 > 1 && var0.charAt(0) == 34 && var0.charAt(var1 - 1) == 34) {
         var0 = var0.substring(1, var1 - 1);
      }

      return var0;
   }

   public int compareTo(Preference var1) {
      byte var2 = -1;
      if(!(var1 instanceof AccessPoint)) {
         var2 = 1;
      } else {
         AccessPoint var3 = (AccessPoint)var1;
         if(this.mInfo != var3.mInfo) {
            if(this.mInfo == null) {
               return 1;
            }
         } else if((this.mRssi ^ var3.mRssi) < 0) {
            if(this.mRssi == Integer.MAX_VALUE) {
               return 1;
            }
         } else {
            if((this.networkId ^ var3.networkId) >= 0) {
               int var4 = WifiManager.compareSignalLevel(var3.mRssi, this.mRssi);
               if(var4 != 0) {
                  return var4;
               }

               return this.ssid.compareToIgnoreCase(var3.ssid);
            }

            if(this.networkId == var2) {
               return 1;
            }
         }
      }

      return var2;
   }

   protected void generateOpenNetworkConfig() {
      if(this.security != 0) {
         throw new IllegalStateException();
      } else if(this.mConfig == null) {
         this.mConfig = new WifiConfiguration();
         this.mConfig.SSID = convertToQuotedString(this.ssid);
         this.mConfig.allowedKeyManagement.set(0);
      }
   }

   WifiConfiguration getConfig() {
      return this.mConfig;
   }

   android.net.wifi.WifiInfo getInfo() {
      return this.mInfo;
   }

   int getLevel() {
      return this.mRssi == Integer.MAX_VALUE?-1:WifiManager.calculateSignalLevel(this.mRssi, 4);
   }

   public String getSecurityString(boolean var1) {
      Context var2 = this.getContext();
      switch(this.security) {
      case 1:
         if(var1) {
            return var2.getString(R.string.wifi_security_short_wep);
         }

         return var2.getString(R.string.wifi_security_wep);
      case 2:
         switch(null.$SwitchMap$com$general$mediaplayer$csr$wifi$AccessPoint$PskType[this.pskType.ordinal()]) {
         case 1:
            if(var1) {
               return var2.getString(R.string.wifi_security_short_wpa);
            }

            return var2.getString(R.string.wifi_security_wpa);
         case 2:
            if(var1) {
               return var2.getString(R.string.wifi_security_short_wpa2);
            }

            return var2.getString(R.string.wifi_security_wpa2);
         case 3:
            if(var1) {
               return var2.getString(R.string.wifi_security_short_wpa_wpa2);
            }

            return var2.getString(R.string.wifi_security_wpa_wpa2);
         default:
            if(var1) {
               return var2.getString(R.string.wifi_security_short_psk_generic);
            }

            return var2.getString(R.string.wifi_security_psk_generic);
         }
      case 3:
         if(var1) {
            return var2.getString(R.string.wifi_security_short_eap);
         }

         return var2.getString(R.string.wifi_security_eap);
      default:
         return var1?"":var2.getString(R.string.wifi_security_none);
      }
   }

   DetailedState getState() {
      return this.mState;
   }

   protected void onBindView(View var1) {
      super.onBindView(var1);
      ImageView var2 = (ImageView)var1.findViewById(R.id.signal);
      if(this.mRssi == Integer.MAX_VALUE) {
         var2.setImageDrawable((Drawable)null);
      } else {
         var2.setImageLevel(this.getLevel());
         var2.setImageResource(R.drawable.wifi_signal);
         int[] var3;
         if(this.security != 0) {
            var3 = STATE_SECURED;
         } else {
            var3 = STATE_NONE;
         }

         var2.setImageState(var3, true);
      }
   }

   public void saveWifiState(Bundle var1) {
      var1.putParcelable("key_config", this.mConfig);
      var1.putParcelable("key_scanresult", this.mScanResult);
      var1.putParcelable("key_wifiinfo", this.mInfo);
      if(this.mState != null) {
         var1.putString("key_detailedstate", this.mState.toString());
      }

   }

   void update(android.net.wifi.WifiInfo var1, DetailedState var2) {
      boolean var4;
      if(var1 != null && this.networkId != -1 && this.networkId == var1.getNetworkId()) {
         if(this.mInfo == null) {
            var4 = true;
         } else {
            var4 = false;
         }

         this.mInfo = var1;
         if(var1.getRssi() == 0) {
            this.mRssi = var1.getRssi();
         }

         this.mState = var2;
         this.refresh();
      } else {
         android.net.wifi.WifiInfo var3 = this.mInfo;
         var4 = false;
         if(var3 != null) {
            var4 = true;
            this.mInfo = null;
            this.mState = null;
            this.refresh();
         }
      }

      if(var4) {
         this.notifyHierarchyChanged();
      }

   }

   boolean update(ScanResult var1) {
      if(this.ssid.equals(var1.SSID) && this.security == getSecurity(var1)) {
         if(WifiManager.compareSignalLevel(var1.level, this.mRssi) > 0) {
            int var2 = this.getLevel();
            this.mRssi = var1.level;
            if(this.getLevel() != var2) {
               this.notifyChanged();
            }
         }

         if(this.security == 2) {
            this.pskType = getPskType(var1);
         }

         this.refresh();
         return true;
      } else {
         return false;
      }
   }

    static enum PskType {
        UNKNOWN("UNKNOWN", 0),
        WPA("WPA", 1),
        WPA2("WPA2", 2),
        WPA_WPA2("WPA_WPA2", 3);

        private int value = 0;
        private String strValue = "";
        private PskType(String strVal, int val) {
            value = val;
            strValue = strVal;
        }

        public int getValue() {
            return value;
        }
    }
}
