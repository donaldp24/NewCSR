package com.general.mediaplayer.csr.wifi.p2p;

import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.general.mediaplayer.csr.R;
import com.general.mediaplayer.csr.SettingsPreferenceFragment;
import com.general.mediaplayer.csr.wifi.p2p.WifiP2pDialog;
import com.general.mediaplayer.csr.wifi.p2p.WifiP2pPeer;
import java.util.Iterator;

public class WifiP2pSettings extends SettingsPreferenceFragment implements PeerListListener {

   private static final int DIALOG_CONNECT = 1;
   private static final int DIALOG_DISCONNECT = 2;
   private static final int MENU_ID_ADVANCED = 4;
   private static final int MENU_ID_CREATE_GROUP = 2;
   private static final int MENU_ID_REMOVE_GROUP = 3;
   private static final int MENU_ID_SEARCH = 1;
   private static final String TAG = "WifiP2pSettings";
   private Channel mChannel;
   private WifiP2pDialog mConnectDialog;
   private OnClickListener mConnectListener;
   private OnClickListener mDisconnectListener;
   private final IntentFilter mIntentFilter = new IntentFilter();
   private WifiP2pDeviceList mPeers = new WifiP2pDeviceList();
   private PreferenceGroup mPeersGroup;
   private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
      public void onReceive(Context var1, Intent var2) {
         String var3 = var2.getAction();
         if(!"android.net.wifi.p2p.STATE_CHANGED".equals(var3)) {
            if("android.net.wifi.p2p.PEERS_CHANGED".equals(var3)) {
               if(WifiP2pSettings.this.mWifiP2pManager != null) {
                  WifiP2pSettings.this.mWifiP2pManager.requestPeers(WifiP2pSettings.this.mChannel, WifiP2pSettings.this);
               }

               WifiP2pSettings.this.updateDevicePref();
               return;
            }

            if("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(var3)) {
               if(WifiP2pSettings.this.mWifiP2pManager != null && ((NetworkInfo)var2.getParcelableExtra("networkInfo")).isConnected()) {
                  Log.d("WifiP2pSettings", "Connected");
                  return;
               }
            } else if("android.net.wifi.p2p.THIS_DEVICE_CHANGED".equals(var3)) {
               WifiP2pSettings.this.mThisDevice = (WifiP2pDevice)var2.getParcelableExtra("wifiP2pDevice");
               Log.d("WifiP2pSettings", "Update device info: " + WifiP2pSettings.this.mThisDevice);
               WifiP2pSettings.this.updateDevicePref();
               return;
            }
         }

      }
   };
   private WifiP2pPeer mSelectedWifiPeer;
   private WifiP2pDevice mThisDevice;
   private Preference mThisDevicePref;
   private WifiP2pManager mWifiP2pManager;


   private void updateDevicePref() {
      this.mThisDevicePref = new Preference(this.getActivity());
      if(this.mThisDevice != null) {
         if(TextUtils.isEmpty(this.mThisDevice.deviceName)) {
            this.mThisDevicePref.setTitle(this.mThisDevice.deviceAddress);
         } else {
            this.mThisDevicePref.setTitle(this.mThisDevice.deviceName);
         }

         if(this.mThisDevice.status == 0) {
            String[] var1 = this.getActivity().getResources().getStringArray(R.array.wifi_p2p_status);
            this.mThisDevicePref.setSummary(var1[this.mThisDevice.status]);
         }

         this.mThisDevicePref.setPersistent(false);
         this.mThisDevicePref.setEnabled(true);
         this.mThisDevicePref.setSelectable(false);
      }

      this.onPeersAvailable(this.mPeers);
   }

   public void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.addPreferencesFromResource(R.xml.wifi_p2p_settings);
      this.mIntentFilter.addAction("android.net.wifi.p2p.STATE_CHANGED");
      this.mIntentFilter.addAction("android.net.wifi.p2p.PEERS_CHANGED");
      this.mIntentFilter.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
      this.mIntentFilter.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
      Activity var2 = this.getActivity();
      this.mWifiP2pManager = (WifiP2pManager)this.getSystemService("wifip2p");
      if(this.mWifiP2pManager != null) {
         this.mChannel = this.mWifiP2pManager.initialize(var2, this.getActivity().getMainLooper(), (ChannelListener)null);
         if(this.mChannel == null) {
            Log.e("WifiP2pSettings", "Failed to set up connection with wifi p2p service");
            this.mWifiP2pManager = null;
         }
      } else {
         Log.e("WifiP2pSettings", "mWifiP2pManager is null !");
      }

      this.mConnectListener = new OnClickListener() {
         public void onClick(DialogInterface var1, int var2) {
            if(var2 == -1) {
               WifiP2pConfig var3 = WifiP2pSettings.this.mConnectDialog.getConfig();
               if(WifiP2pSettings.this.mWifiP2pManager != null) {
                  WifiP2pSettings.this.mWifiP2pManager.connect(WifiP2pSettings.this.mChannel, var3, new ActionListener() {
                     public void onFailure(int var1) {
                        Log.d("WifiP2pSettings", " connect fail " + var1);
                     }
                     public void onSuccess() {
                        Log.d("WifiP2pSettings", " connect success");
                     }
                  });
               }
            }

         }
      };
      this.mDisconnectListener = new OnClickListener() {
         public void onClick(DialogInterface var1, int var2) {
            if(var2 == -1 && WifiP2pSettings.this.mWifiP2pManager != null) {
               WifiP2pSettings.this.mWifiP2pManager.removeGroup(WifiP2pSettings.this.mChannel, new ActionListener() {
                  public void onFailure(int var1) {
                     Log.d("WifiP2pSettings", " remove group fail " + var1);
                  }
                  public void onSuccess() {
                     Log.d("WifiP2pSettings", " remove group success");
                  }
               });
            }

         }
      };
      this.setHasOptionsMenu(true);
   }

   public Dialog onCreateDialog(int var1) {
      WifiP2pDialog var2;
      if(var1 == 1) {
         this.mConnectDialog = new WifiP2pDialog(this.getActivity(), this.mConnectListener, this.mSelectedWifiPeer.device);
         var2 = this.mConnectDialog;
      } else {
         var2 = null;
         if(var1 == 2) {
            return (new Builder(this.getActivity())).setTitle(this.getActivity().getResources().getString(R.string.wifi_direct_disconnect_title)).setMessage(this.getActivity().getResources().getString(R.string.wifi_direct_disconnect_message)).setPositiveButton(this.getActivity().getString(R.string.dlg_ok), this.mDisconnectListener).setNegativeButton(this.getActivity().getString(R.string.dlg_cancel), (OnClickListener)null).create();
         }
      }

      return var2;
   }

   public void onCreateOptionsMenu(Menu var1, MenuInflater var2) {
      var1.add(0, 1, 0, R.string.wifi_p2p_menu_search).setShowAsAction(1);
      var1.add(0, 2, 0, R.string.wifi_p2p_menu_create_group).setShowAsAction(1);
      var1.add(0, 3, 0, R.string.wifi_p2p_menu_remove_group).setShowAsAction(1);
      super.onCreateOptionsMenu(var1, var2);
   }

   public boolean onOptionsItemSelected(MenuItem var1) {
      boolean var2 = true;
      switch(var1.getItemId()) {
      case 1:
         if(this.mWifiP2pManager != null) {
            this.mWifiP2pManager.discoverPeers(this.mChannel, new ActionListener() {
               public void onFailure(int var1) {
                  Log.d("WifiP2pSettings", " discover fail " + var1);
               }
               public void onSuccess() {
                  Log.d("WifiP2pSettings", " discover success");
               }
            });
            return var2;
         }
         break;
      case 2:
         if(this.mWifiP2pManager != null) {
            this.mWifiP2pManager.createGroup(this.mChannel, new ActionListener() {
               public void onFailure(int var1) {
                  Log.d("WifiP2pSettings", " create group fail " + var1);
               }
               public void onSuccess() {
                  Log.d("WifiP2pSettings", " create group success");
               }
            });
            return var2;
         }
         break;
      case 3:
         if(this.mWifiP2pManager != null) {
            this.mWifiP2pManager.removeGroup(this.mChannel, new ActionListener() {
               public void onFailure(int var1) {
                  Log.d("WifiP2pSettings", " remove group fail " + var1);
               }
               public void onSuccess() {
                  Log.d("WifiP2pSettings", " remove group success");
               }
            });
            return var2;
         }
      case 4:
         break;
      default:
         var2 = super.onOptionsItemSelected(var1);
      }

      return var2;
   }

   public void onPause() {
      super.onPause();
      this.getActivity().unregisterReceiver(this.mReceiver);
   }

   public void onPeersAvailable(WifiP2pDeviceList var1) {
      PreferenceScreen var2 = this.getPreferenceScreen();
      var2.removeAll();
      var2.setOrderingAsAdded(true);
      if(this.mPeersGroup == null) {
         this.mPeersGroup = new PreferenceCategory(this.getActivity());
      } else {
         this.mPeersGroup.removeAll();
      }

      var2.addPreference(this.mThisDevicePref);
      this.mPeersGroup.setTitle(R.string.wifi_p2p_available_devices);
      this.mPeersGroup.setEnabled(true);
      var2.addPreference(this.mPeersGroup);
      this.mPeers = var1;
      Iterator var5 = var1.getDeviceList().iterator();

      while(var5.hasNext()) {
         WifiP2pDevice var6 = (WifiP2pDevice)var5.next();
         this.mPeersGroup.addPreference(new WifiP2pPeer(this.getActivity(), var6));
      }

   }

   public boolean onPreferenceTreeClick(PreferenceScreen var1, Preference var2) {
      if(var2 instanceof WifiP2pPeer) {
         this.mSelectedWifiPeer = (WifiP2pPeer)var2;
         if(this.mSelectedWifiPeer.device.status == 0) {
            this.showDialog(2);
         } else {
            this.showDialog(1);
         }
      }

      return super.onPreferenceTreeClick(var1, var2);
   }

   public void onResume() {
      super.onResume();
      this.getActivity().registerReceiver(this.mReceiver, this.mIntentFilter);
      if(this.mWifiP2pManager != null) {
         this.mWifiP2pManager.discoverPeers(this.mChannel, new ActionListener() {
            public void onFailure(int var1) {
               Log.d("WifiP2pSettings", " discover fail " + var1);
            }
            public void onSuccess() {
               Log.d("WifiP2pSettings", " discover success");
            }
         });
      }

   }
}
