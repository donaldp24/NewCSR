package com.general.mediaplayer.csr.wifi;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import com.general.mediaplayer.csr.R;

/**
 * Created by Jun7 on 2015/4/13.
 */
public class WifiSpotItem {
    public String szSSID;
    public String szBSSID;
    public String szCaps;
    public int nLevel;
    public int nFrequency;
    public long nTimestamp;

    public int nSignalLevel;
    public NetworkInfo.DetailedState detailedState;
    public WifiConfiguration wifiConfig;
    public WifiInfo wifiInfo;
    //public String szSecurityString;

    public WifiSpotItem ( String i_szSSID, String i_szBSSID, String i_szCaps, int i_nLevel, int i_nFrequency, int i_nTimestamp ) {
        this.szSSID = i_szSSID;
        this.szBSSID = i_szBSSID;
        this.szCaps = i_szCaps;
        this.nLevel = i_nLevel;
        this.nFrequency = i_nFrequency;
        this.nTimestamp = i_nTimestamp;
    }

    public WifiSpotItem ( ScanResult i_scanResult ) {
        this.szSSID = i_scanResult.SSID;
        this.szBSSID = i_scanResult.BSSID;
        this.szCaps = i_scanResult.capabilities;
        this.nLevel = i_scanResult.level;
        this.nFrequency = i_scanResult.frequency;
        this.nTimestamp = i_scanResult.timestamp;
    }

    public NetworkInfo.DetailedState getState() {
        return detailedState;
    }

    /*public WifiConfiguration getConfig() {
        return wifiConfig;
    }*/

    // TODO
    /*public String getSecurityString(boolean b) {
        return szSecurityString;
    }*/

    public int getSignalLevel() {
        return nSignalLevel;
    }

    public WifiInfo getInfo() {
        return wifiInfo;
    }

    public int getNetworkId() {
        if ( wifiConfig != null ) {
            return wifiConfig.networkId;
        }

        return -1;
    }

    public boolean getWpsAvailable() {
        int nSecurityLevel = getSecurityLevel();
        if ( nSecurityLevel==LEVEL_WPA ||
                nSecurityLevel==LEVEL_WPA2 ||
                nSecurityLevel==LEVEL_WPA_WPA2 ) {
            return true;
        }

        return false;
    }

    /*static int getSecurity(WifiConfiguration var0) {
        byte var1 = 1;

        if ( var0 != null ) { // FIXME
            if (var0.allowedKeyManagement.get(var1)) {
                var1 = 2;
            } else {
                if (var0.allowedKeyManagement.get(2) || var0.allowedKeyManagement.get(3)) {
                    return 3;
                }

                if (var0.wepKeys[0] == null) {
                    return 0;
                }
            }
        }

        return var1;
    }*/

    /*public String getSecurityString(Context i_ctx, boolean var1) {
        switch(getSecurity(this.wifiConfig)) {
            case 1:
                if(var1) {
                    return i_ctx.getString(R.string.wifi_security_short_wep);
                }

                return i_ctx.getString(R.string.wifi_security_wep);

            case 2: {
                String szPskType = this.wifiConfig.allowedKeyManagement.toString();
*/
                // FIXME
                /*switch() {
                    case 1:
                        if(var1) {
                            return i_ctx.getString(R.string.wifi_security_short_wpa);
                        }

                        return i_ctx.getString(R.string.wifi_security_wpa);
                    case 2:
                        if(var1) {
                            return i_ctx.getString(R.string.wifi_security_short_wpa2);
                        }

                        return i_ctx.getString(R.string.wifi_security_wpa2);
                    case 3:
                        if(var1) {
                            return i_ctx.getString(R.string.wifi_security_short_wpa_wpa2);
                        }

                        return i_ctx.getString(R.string.wifi_security_wpa_wpa2);
                    default:
                        if(var1) {
                            return i_ctx.getString(R.string.wifi_security_short_psk_generic);
                        }

                        return i_ctx.getString(R.string.wifi_security_psk_generic);
                }*/
            /*}

            case 3:
                if(var1) {
                    return i_ctx.getString(R.string.wifi_security_short_eap);
                }

                return i_ctx.getString(R.string.wifi_security_eap);
            default:
                return var1?"":i_ctx.getString(R.string.wifi_security_none);
        }
    }*/

    public String getSummary(Context i_ctx) {
        String szSummary = "";

        if(this.detailedState != null) {
            szSummary = Summary.get(i_ctx, this.detailedState);
        } else if(this.nLevel == Integer.MAX_VALUE) {
            szSummary = i_ctx.getString(R.string.wifi_not_in_range);
        } else if(this.wifiConfig != null && this.wifiConfig.status == WifiConfiguration.Status.DISABLED) {
            /*switch(this.wifiConfig..disableReason) {
                case 0:
                    szSummary = i_ctx.getString(R.string.wifi_disabled_generic);
                    break;
                case 1:
                case 2:
                    szSummary = i_ctx.getString(R.string.wifi_disabled_network_failure);
                    break;
                case 3:
                    szSummary = i_ctx.getString(R.string.wifi_disabled_password_failure);
                    break;
                default:
                    break;
            }*/

            // FIXME
            szSummary = i_ctx.getString(R.string.wifi_disabled_password_failure);
        } else {
            StringBuilder var2 = new StringBuilder();
            if(this.wifiConfig != null) {
                var2.append(i_ctx.getString(R.string.wifi_remembered));
            }

            if(getSecurityLevel() != 0) {
                String var5;
                if(var2.length() == 0) {
                    var5 = i_ctx.getString(R.string.wifi_secured_first_item);
                } else {
                    var5 = i_ctx.getString(R.string.wifi_secured_second_item);
                }

                Object[] var6 = new Object[]{this.getSecurityString(i_ctx, true)};
                var2.append(String.format(var5, var6));
            }

            if(this.wifiConfig == null && this.getWpsAvailable()) {
                if(var2.length() == 0) {
                    var2.append(i_ctx.getString(R.string.wifi_wps_available_first_item));
                } else {
                    var2.append(i_ctx.getString(R.string.wifi_wps_available_second_item));
                }
            }

            szSummary = var2.toString();
        }

        return szSummary;
    }

    // https://code.google.com/p/pdn-slatedroid/source/browse/trunk/eclair/packages/apps/Settings/src/com/android/settings/wifi/AccessPointState.java?r=51#318
    // Constants used for different security types
    public static final String NONE = "None";
    public static final String WEP = "WEP";
    public static final String WPA = "WPA";
    public static final String WPA2 = "WPA2";
    public static final String WPA_WPA2 = "WPA/WPA2";
    public static final String EAP = "EAP";

    // getScanResultSecurity
    public String getSecurityString(Context i_ctx, boolean i_bShort) {
        switch(getSecurityLevel()) {
            case LEVEL_WEP:
                if(i_bShort) {
                    return i_ctx.getString(R.string.wifi_security_short_wep);
                }

                return i_ctx.getString(R.string.wifi_security_wep);

            case LEVEL_WPA:
                if(i_bShort) {
                    return i_ctx.getString(R.string.wifi_security_short_wpa);
                }

                return i_ctx.getString(R.string.wifi_security_wpa);

            case LEVEL_WPA2:
                if(i_bShort) {
                    return i_ctx.getString(R.string.wifi_security_short_wpa2);
                }

                return i_ctx.getString(R.string.wifi_security_wpa2);

            case LEVEL_WPA_WPA2:
                if(i_bShort) {
                    return i_ctx.getString(R.string.wifi_security_short_wpa_wpa2);
                }

                return i_ctx.getString(R.string.wifi_security_wpa_wpa2);

            case LEVEL_EAP:
                if(i_bShort) {
                    return i_ctx.getString(R.string.wifi_security_short_eap);
                }

                return i_ctx.getString(R.string.wifi_security_eap);
            default:
                return i_bShort?"":i_ctx.getString(R.string.wifi_security_none);
        }
    }

    public static final int LEVEL_NONE = 0;
    public static final int LEVEL_WEP = 2;
    public static final int LEVEL_WPA = 3;
    public static final int LEVEL_WPA2 = 4;
    public static final int LEVEL_WPA_WPA2 = 5;
    public static final int LEVEL_EAP = 1;
    public int getSecurityLevel() {
        if ( szCaps.contains(EAP) ) {
            return LEVEL_EAP;
        } else if ( szCaps.contains(WEP) ) {
            return LEVEL_WEP;
        } else if ( szCaps.contains(WPA) ) {
            if ( szCaps.contains(WPA2) ) {
                return LEVEL_WPA_WPA2;
            } else {
                return LEVEL_WPA;
            }
        } else if ( szCaps.contains(WPA2) ) {
            return LEVEL_WPA2;
        }

        return LEVEL_NONE;
    }

    public static String getHumanReadableSecurity(String i_szSecurity, Context i_ctx) {
        if ( i_szSecurity.equals(EAP) ) {
            return i_ctx.getString(R.string.wifi_security_eap);
        } else if ( i_szSecurity.equals(WEP) ) {
            return i_ctx.getString(R.string.wifi_security_wep);
        } else if ( i_szSecurity.equals(WPA) ) {
            return i_ctx.getString(R.string.wifi_security_wpa);
        } else if ( i_szSecurity.equals(WPA2) ) {
            return i_ctx.getString(R.string.wifi_security_wpa2);
        } else if ( i_szSecurity.equals(WPA_WPA2) ) {
            return i_ctx.getString(R.string.wifi_security_wpa_wpa2);
        }

        return "";
    }
}
