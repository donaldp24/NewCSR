package com.general.mediaplayer.csr.wifi;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.general.mediaplayer.csr.wifi.Summary;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import com.general.mediaplayer.csr.R;

public class WifiStatusTest extends Activity {

   private static final String TAG = "WifiStatusTest";
   private TextView mBSSID;
   private TextView mHiddenSSID;
   private TextView mHttpClientTest;
   private String mHttpClientTestResult;
   private TextView mIPAddr;
   private TextView mLinkSpeed;
   private TextView mMACAddr;
   private TextView mNetworkId;
   private TextView mNetworkState;
   OnClickListener mPingButtonHandler = new OnClickListener() {
      public void onClick(View var1) {
         WifiStatusTest.this.updatePingState();
      }
   };
   private TextView mPingHostname;
   private String mPingHostnameResult;
   private TextView mPingIpAddr;
   private String mPingIpAddrResult;
   private TextView mRSSI;
   private TextView mSSID;
   private TextView mScanList;
   private TextView mSupplicantState;
   private WifiManager mWifiManager;
   private TextView mWifiState;
   private IntentFilter mWifiStateFilter;
   private final BroadcastReceiver mWifiStateReceiver = new BroadcastReceiver() {
      public void onReceive(Context var1, Intent var2) {
         if(var2.getAction().equals("android.net.wifi.WIFI_STATE_CHANGED")) {
            WifiStatusTest.this.handleWifiStateChanged(var2.getIntExtra("wifi_state", 4));
         } else {
            if(var2.getAction().equals("android.net.wifi.STATE_CHANGE")) {
               WifiStatusTest.this.handleNetworkStateChanged((NetworkInfo)var2.getParcelableExtra("networkInfo"));
               return;
            }

            if(var2.getAction().equals("android.net.wifi.SCAN_RESULTS")) {
               WifiStatusTest.this.handleScanResultsAvailable();
               return;
            }

            if(!var2.getAction().equals("android.net.wifi.supplicant.CONNECTION_CHANGE")) {
               if(var2.getAction().equals("android.net.wifi.supplicant.STATE_CHANGE")) {
                  WifiStatusTest.this.handleSupplicantStateChanged((SupplicantState)var2.getParcelableExtra("newState"), var2.hasExtra("supplicantError"), var2.getIntExtra("supplicantError", 0));
                  return;
               }

               if(var2.getAction().equals("android.net.wifi.RSSI_CHANGED")) {
                  WifiStatusTest.this.handleSignalChanged(var2.getIntExtra("newRssi", 0));
                  return;
               }

               if(!var2.getAction().equals("android.net.wifi.NETWORK_IDS_CHANGED")) {
                  Log.e("WifiStatusTest", "Received an unknown Wifi Intent");
                  return;
               }
            }
         }

      }
   };
   private Button pingTestButton;
   private Button updateButton;
   OnClickListener updateButtonHandler = new OnClickListener() {
      public void onClick(View var1) {
         android.net.wifi.WifiInfo var2 = WifiStatusTest.this.mWifiManager.getConnectionInfo();
         WifiStatusTest.this.setWifiStateText(WifiStatusTest.this.mWifiManager.getWifiState());
         WifiStatusTest.this.mBSSID.setText(var2.getBSSID());
         WifiStatusTest.this.mHiddenSSID.setText(String.valueOf(var2.getHiddenSSID()));
         int var3 = var2.getIpAddress();
         StringBuffer var4 = new StringBuffer();
         StringBuffer var5 = var4.append(var3 & 255).append('.');
         int var6 = var3 >>> 8;
         StringBuffer var7 = var5.append(var6 & 255).append('.');
         int var8 = var6 >>> 8;
         var7.append(var8 & 255).append('.').append(255 & var8 >>> 8);
         WifiStatusTest.this.mIPAddr.setText(var4);
         WifiStatusTest.this.mLinkSpeed.setText(var2.getLinkSpeed() + " Mbps");
         WifiStatusTest.this.mMACAddr.setText(var2.getMacAddress());
         WifiStatusTest.this.mNetworkId.setText(String.valueOf(var2.getNetworkId()));
         WifiStatusTest.this.mRSSI.setText(String.valueOf(var2.getRssi()));
         WifiStatusTest.this.mSSID.setText(var2.getSSID());
         SupplicantState var10 = var2.getSupplicantState();
         WifiStatusTest.this.setSupplicantStateText(var10);
      }
   };


   private void handleNetworkStateChanged(NetworkInfo var1) {
      if(this.mWifiManager.isWifiEnabled()) {
         String var2 = Summary.get(this, this.mWifiManager.getConnectionInfo().getSSID(), var1.getDetailedState());
         this.mNetworkState.setText(var2);
      }

   }

   private void handleScanResultsAvailable() {
      List var1 = this.mWifiManager.getScanResults();
      StringBuffer var2 = new StringBuffer();
      if(var1 != null) {
         for(int var3 = -1 + var1.size(); var3 >= 0; --var3) {
            ScanResult var4 = (ScanResult)var1.get(var3);
            if(var4 != null && !TextUtils.isEmpty(var4.SSID)) {
               var2.append(var4.SSID + " ");
            }
         }
      }

      this.mScanList.setText(var2);
   }

   private void handleSignalChanged(int var1) {
      this.mRSSI.setText(String.valueOf(var1));
   }

   private void handleSupplicantStateChanged(SupplicantState var1, boolean var2, int var3) {
      if(var2) {
         this.mSupplicantState.setText("ERROR AUTHENTICATING");
      } else {
         this.setSupplicantStateText(var1);
      }
   }

   private void handleWifiStateChanged(int var1) {
      this.setWifiStateText(var1);
   }

   private void httpClientTest() {
      DefaultHttpClient var1 = new DefaultHttpClient();

      try {
         HttpGet var2 = new HttpGet("http://www.google.com");
         HttpResponse var4 = var1.execute(var2);
         if(var4.getStatusLine().getStatusCode() == 200) {
            this.mHttpClientTestResult = "Pass";
         } else {
            this.mHttpClientTestResult = "Fail: Code: " + String.valueOf(var4);
         }

         var2.abort();
      } catch (IOException var5) {
         this.mHttpClientTestResult = "Fail: IOException";
      }
   }

   private final void pingHostname() {
      try {
         if(Runtime.getRuntime().exec("ping -c 1 -w 100 www.google.com").waitFor() == 0) {
            this.mPingHostnameResult = "Pass";
         } else {
            this.mPingHostnameResult = "Fail: Host unreachable";
         }
      } catch (UnknownHostException var4) {
         this.mPingHostnameResult = "Fail: Unknown Host";
      } catch (IOException var5) {
         this.mPingHostnameResult = "Fail: IOException";
      } catch (InterruptedException var6) {
         this.mPingHostnameResult = "Fail: InterruptedException";
      }
   }

   private final void pingIpAddr() {
      try {
         if(Runtime.getRuntime().exec("ping -c 1 -w 100 " + "74.125.47.104").waitFor() == 0) {
            this.mPingIpAddrResult = "Pass";
         } else {
            this.mPingIpAddrResult = "Fail: IP addr not reachable";
         }
      } catch (IOException var3) {
         this.mPingIpAddrResult = "Fail: IOException";
      } catch (InterruptedException var4) {
         this.mPingIpAddrResult = "Fail: InterruptedException";
      }
   }

   private void setSupplicantStateText(SupplicantState var1) {
      if(SupplicantState.FOUR_WAY_HANDSHAKE.equals(var1)) {
         this.mSupplicantState.setText("FOUR WAY HANDSHAKE");
      } else if(SupplicantState.ASSOCIATED.equals(var1)) {
         this.mSupplicantState.setText("ASSOCIATED");
      } else if(SupplicantState.ASSOCIATING.equals(var1)) {
         this.mSupplicantState.setText("ASSOCIATING");
      } else if(SupplicantState.COMPLETED.equals(var1)) {
         this.mSupplicantState.setText("COMPLETED");
      } else if(SupplicantState.DISCONNECTED.equals(var1)) {
         this.mSupplicantState.setText("DISCONNECTED");
      } else if(SupplicantState.DORMANT.equals(var1)) {
         this.mSupplicantState.setText("DORMANT");
      } else if(SupplicantState.GROUP_HANDSHAKE.equals(var1)) {
         this.mSupplicantState.setText("GROUP HANDSHAKE");
      } else if(SupplicantState.INACTIVE.equals(var1)) {
         this.mSupplicantState.setText("INACTIVE");
      } else if(SupplicantState.INVALID.equals(var1)) {
         this.mSupplicantState.setText("INVALID");
      } else if(SupplicantState.SCANNING.equals(var1)) {
         this.mSupplicantState.setText("SCANNING");
      } else if(SupplicantState.UNINITIALIZED.equals(var1)) {
         this.mSupplicantState.setText("UNINITIALIZED");
      } else {
         this.mSupplicantState.setText("BAD");
         Log.e("WifiStatusTest", "supplicant state is bad");
      }
   }

   private void setWifiStateText(int var1) {
      String var2;
      switch(var1) {
      case 0:
         var2 = this.getString(R.string.wifi_state_disabling);
         break;
      case 1:
         var2 = this.getString(2131427897);
         break;
      case 2:
         var2 = this.getString(2131427898);
         break;
      case 3:
         var2 = this.getString(2131427899);
         break;
      case 4:
         var2 = this.getString(2131427900);
         break;
      default:
         var2 = "BAD";
         Log.e("WifiStatusTest", "wifi state is bad");
      }

      this.mWifiState.setText(var2);
   }

   private final void updatePingState() {
      final Handler var1 = new Handler();
      this.mPingIpAddrResult = this.getResources().getString(2131427366);
      this.mPingHostnameResult = this.getResources().getString(2131427366);
      this.mHttpClientTestResult = this.getResources().getString(2131427366);
      this.mPingIpAddr.setText(this.mPingIpAddrResult);
      this.mPingHostname.setText(this.mPingHostnameResult);
      this.mHttpClientTest.setText(this.mHttpClientTestResult);
      final Runnable var2 = new Runnable() {
         public void run() {
            WifiStatusTest.this.mPingIpAddr.setText(WifiStatusTest.this.mPingIpAddrResult);
            WifiStatusTest.this.mPingHostname.setText(WifiStatusTest.this.mPingHostnameResult);
            WifiStatusTest.this.mHttpClientTest.setText(WifiStatusTest.this.mHttpClientTestResult);
         }
      };
      (new Thread() {
         public void run() {
            WifiStatusTest.this.pingIpAddr();
            var1.post(var2);
         }
      }).start();
      (new Thread() {
         public void run() {
            WifiStatusTest.this.pingHostname();
            var1.post(var2);
         }
      }).start();
      (new Thread() {
         public void run() {
            WifiStatusTest.this.httpClientTest();
            var1.post(var2);
         }
      }).start();
   }

   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.mWifiManager = (WifiManager)this.getSystemService("wifi");
      this.mWifiStateFilter = new IntentFilter("android.net.wifi.WIFI_STATE_CHANGED");
      this.mWifiStateFilter.addAction("android.net.wifi.STATE_CHANGE");
      this.mWifiStateFilter.addAction("android.net.wifi.SCAN_RESULTS");
      this.mWifiStateFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
      this.mWifiStateFilter.addAction("android.net.wifi.RSSI_CHANGED");
      this.mWifiStateFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
      this.registerReceiver(this.mWifiStateReceiver, this.mWifiStateFilter);
      this.setContentView(2130968722);
      this.updateButton = (Button)this.findViewById(2131231221);
      this.updateButton.setOnClickListener(this.updateButtonHandler);
      this.mWifiState = (TextView)this.findViewById(2131231222);
      this.mNetworkState = (TextView)this.findViewById(2131231223);
      this.mSupplicantState = (TextView)this.findViewById(2131231224);
      this.mRSSI = (TextView)this.findViewById(2131231225);
      this.mBSSID = (TextView)this.findViewById(2131231226);
      this.mSSID = (TextView)this.findViewById(2131231150);
      this.mHiddenSSID = (TextView)this.findViewById(2131231227);
      this.mIPAddr = (TextView)this.findViewById(2131231228);
      this.mMACAddr = (TextView)this.findViewById(2131231229);
      this.mNetworkId = (TextView)this.findViewById(2131231230);
      this.mLinkSpeed = (TextView)this.findViewById(2131231231);
      this.mScanList = (TextView)this.findViewById(2131231232);
      this.mPingIpAddr = (TextView)this.findViewById(2131230988);
      this.mPingHostname = (TextView)this.findViewById(2131230989);
      this.mHttpClientTest = (TextView)this.findViewById(2131230990);
      this.pingTestButton = (Button)this.findViewById(2131230987);
      this.pingTestButton.setOnClickListener(this.mPingButtonHandler);
   }

   protected void onPause() {
      super.onPause();
      this.unregisterReceiver(this.mWifiStateReceiver);
   }

   protected void onResume() {
      super.onResume();
      this.registerReceiver(this.mWifiStateReceiver, this.mWifiStateFilter);
   }
}
