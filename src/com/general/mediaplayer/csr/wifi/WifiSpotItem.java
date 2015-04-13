package com.general.mediaplayer.csr.wifi;

import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;

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
    NetworkInfo.DetailedState detailedState; // TODO
    WifiConfiguration wifiConfig; // TODO
    public WifiInfo wifiInfo;
    public String szSecurityString;

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

    public WifiConfiguration getConfig() {
        return wifiConfig;
    }

    // TODO
    public String getSecurityString(boolean b) {
        return szSecurityString;
    }

    public int getSignalLevel() {
        return nSignalLevel;
    }

    public WifiInfo getInfo() {
        return wifiInfo;
    }

    public int getNetworkId() {
        if ( wifiInfo != null ) {
            return wifiInfo.getNetworkId();
        }

        return -1;
    }

    public boolean getWpsAvailable() {
        int nSecurityLevel = WifiAdmin.getSecurityLevelFromCap(szCaps);
        if ( nSecurityLevel==WifiAdmin.LEVEL_WPA ||
                nSecurityLevel==WifiAdmin.LEVEL_WPA2 ||
                nSecurityLevel==WifiAdmin.LEVEL_WPA_WPA2 ) {
            return true;
        }

        return false;
    }
}
