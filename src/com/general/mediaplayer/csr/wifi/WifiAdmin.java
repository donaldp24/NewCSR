package com.general.mediaplayer.csr.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import com.general.mediaplayer.csr.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jun7 on 2015/4/12.
 */
// http://blog.csdn.net/jackzhang1226/article/details/5591934
public class WifiAdmin {
    //定义WifiManager对象
    public WifiManager mWifiManager;
    //定义WifiInfo对象
    private WifiInfo mWifiInfo;
    //扫描出的网络连接列表
    private List<ScanResult> mWifiList;
    private List<WifiSpotItem> mWifiSpotList;
    //网络连接列表
    private List<WifiConfiguration> mWifiConfiguration;
    //定义一个WifiLock
    WifiManager.WifiLock mWifiLock;

    Context m_ctx;


    //构造器
    public  WifiAdmin(Context context)
    {
        m_ctx = context;
        //取得WifiManager对象
        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        //取得WifiInfo对象
        mWifiInfo = mWifiManager.getConnectionInfo();
    }
    //打开WIFI
    public void OpenWifi()
    {
        if (!mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(true);

        }
    }
    //关闭WIFI
    public void CloseWifi()
    {
        if (!mWifiManager.isWifiEnabled())
        {
            mWifiManager.setWifiEnabled(false);
        }
    }
    //锁定WifiLock
    public void AcquireWifiLock()
    {
        mWifiLock.acquire();
    }
    //解锁WifiLock
    public void ReleaseWifiLock()
    {
        //判断时候锁定
        if (mWifiLock.isHeld())
        {
            mWifiLock.acquire();
        }
    }
    //创建一个WifiLock
    public void CreatWifiLock()
    {
        mWifiLock = mWifiManager.createWifiLock("Test");
    }
    //得到配置好的网络
    public List<WifiConfiguration> GetConfiguration()
    {
        return mWifiConfiguration;
    }
    //指定配置好的网络进行连接
    public void ConnectConfiguration(int index)
    {
        //索引大于配置好的网络索引返回
        if(index > mWifiConfiguration.size())
        {
            return;
        }
        //连接配置好的指定ID的网络
        mWifiManager.enableNetwork(mWifiConfiguration.get(index).networkId, true);
    }
    public void StartScan()
    {
        mWifiManager.startScan();
        //得到扫描结果
        mWifiList = mWifiManager.getScanResults();
        if ( mWifiSpotList == null ) {
            mWifiSpotList = new ArrayList<WifiSpotItem>();
        } else {
            mWifiSpotList.clear();
        }
        for ( int i=0; i<mWifiList.size(); i++ ) {
            WifiSpotItem newspot = new WifiSpotItem(mWifiList.get(i));

            newspot.nSignalLevel = mWifiManager.calculateSignalLevel(newspot.nLevel, 4);
            if ( String.format("\"%s\"",newspot.szSSID).equals(mWifiInfo.getSSID()) ) {
                newspot.wifiInfo = mWifiInfo;
            } else {
                newspot.wifiInfo = null;
            }
            newspot.detailedState = null; // TODO
            newspot.szSecurityString = getHumanReadableSecurity(getSecurityFromCap(newspot.szCaps), m_ctx);

            mWifiSpotList.add(newspot);
        }
        //得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();
    }
    //得到网络列表
    public List<ScanResult> GetWifiList()
    {
        return mWifiList;
    }
    public List<WifiSpotItem> GetWifiSpotList()
    {
        return mWifiSpotList;
    }
    //查看扫描结果
    public StringBuilder LookUpScan()
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mWifiList.size(); i++)
        {
            stringBuilder.append("Index_"+new Integer(i + 1).toString() + ":");
            //将ScanResult信息转换成一个字符串包
            //其中把包括：BSSID、SSID、capabilities、frequency、level
            stringBuilder.append((mWifiList.get(i)).toString());
            stringBuilder.append("/n");
        }
        return stringBuilder;
    }
    //得到MAC地址
    public String GetMacAddress()
    {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getMacAddress();
    }
    //得到接入点的BSSID
    public String GetBSSID()
    {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.getBSSID();
    }
    //得到IP地址
    public int GetIPAddress()
    {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getIpAddress();
    }
    //得到连接的ID
    public int GetNetworkId()
    {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }
    //得到WifiInfo的所有信息包
    public String GetWifiInfo()
    {
        return (mWifiInfo == null) ? "NULL" : mWifiInfo.toString();
    }
    //添加一个网络并连接
    public void AddNetwork(WifiConfiguration wcg)
    {
        int wcgID = mWifiManager.addNetwork(wcg);
        mWifiManager.enableNetwork(wcgID, true);
    }
    //断开指定ID的网络
    public void DisconnectWifi(int netId)
    {
        mWifiManager.disableNetwork(netId);
        mWifiManager.disconnect();
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
    public static String getSecurityFromCap(String i_szCap) {
        if ( i_szCap.contains(EAP) ) {
            return EAP;
        } else if ( i_szCap.contains(WEP) ) {
            return WEP;
        } else if ( i_szCap.contains(WPA) ) {
            if ( i_szCap.contains(WPA2) ) {
                return WPA_WPA2;
            } else {
                return WPA;
            }
        } else if ( i_szCap.contains(WPA2) ) {
            return WPA2;
        }

        return NONE;
    }

    public static final int LEVEL_NONE = 0;
    public static final int LEVEL_WEP = 2;
    public static final int LEVEL_WPA = 3;
    public static final int LEVEL_WPA2 = 4;
    public static final int LEVEL_WPA_WPA2 = 5;
    public static final int LEVEL_EAP = 1;
    public static int getSecurityLevelFromCap(String i_szCap) {
        if ( i_szCap.contains(EAP) ) {
            return LEVEL_EAP;
        } else if ( i_szCap.contains(WEP) ) {
            return LEVEL_WEP;
        } else if ( i_szCap.contains(WPA) ) {
            if ( i_szCap.contains(WPA2) ) {
                return LEVEL_WPA_WPA2;
            } else {
                return LEVEL_WPA;
            }
        } else if ( i_szCap.contains(WPA2) ) {
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
