package com.general.mediaplayer.csr.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
    }
    //得到网络列表
    public List<ScanResult> GetWifiList()
    {
        return mWifiList;
    }
    public List<WifiSpotItem> GetWifiSpotList()
    {
        //得到扫描结果
        mWifiList = mWifiManager.getScanResults();
        if ( mWifiSpotList == null ) {
            mWifiSpotList = new ArrayList<WifiSpotItem>();
        } else {
            mWifiSpotList.clear();
        }

        //得到配置好的网络连接
        mWifiConfiguration = mWifiManager.getConfiguredNetworks();

        // Get active wifi spot info
        String szActiveSSID;
        NetworkInfo.DetailedState detailedState;
        {
            ConnectivityManager connMgr = (ConnectivityManager) m_ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfoWifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            detailedState = netInfoWifi.getDetailedState();
            szActiveSSID = netInfoWifi.getExtraInfo();
        }

        for ( int i=0; i<mWifiList.size(); i++ ) {
            WifiSpotItem newspot = new WifiSpotItem(mWifiList.get(i));

            newspot.nSignalLevel = mWifiManager.calculateSignalLevel(newspot.nLevel, 4);
            if ( newspot.szBSSID.equals(mWifiInfo.getBSSID()) ) {
                newspot.wifiInfo = mWifiInfo;
            } else {
                newspot.wifiInfo = null;
            }

            if ( String.format("\"%s\"", newspot.szSSID).equals(szActiveSSID) ) {
                //newspot.detailedState = WifiInfo.getDetailedStateOf(mWifiInfo.getSupplicantState());
                newspot.detailedState = detailedState;
            }else {
                newspot.detailedState = null;
            }

            newspot.wifiConfig = null; // will be determined in next block

            mWifiSpotList.add(newspot);
        }

        // Set WifiConfiguration of access point.
        if ( mWifiConfiguration != null ) {
            for (int j = 0; j < mWifiConfiguration.size(); j++) {
                WifiConfiguration config = mWifiConfiguration.get(j);

                int k;
                int nScannedSpotCount = mWifiSpotList.size();
                for (k = 0; k < nScannedSpotCount; k++) {
                    WifiSpotItem spot = mWifiSpotList.get(k);

                    if (config.SSID != null && spot.szSSID != null &&
                            config.SSID.equals(String.format("\"%s\"", spot.szSSID))) {
                        spot.wifiConfig = config;
                        break;
                    }
                }

                if (k == nScannedSpotCount) { // if there is no matched spot with this config,
                    // this is the spot not in range.
                    WifiSpotItem newspot = new WifiSpotItem(config);

                    mWifiSpotList.add(newspot);
                }
            }
        }

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
}
