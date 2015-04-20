package com.general.mediaplayer.csr.wifi;

import android.app.Activity;
import android.app.ListFragment;
import android.content.*;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.*;
import android.widget.*;
import com.general.mediaplayer.csr.R;
import com.general.mediaplayer.csr.Settings;

import java.util.List;

public class WifiSettingsFragment extends ListFragment {

    private BroadcastReceiver m_networkChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if ( state != WifiManager.WIFI_STATE_ENABLED ) {
                    switchToEmptyFragment();

                    return;
                }
            }

            getActivity().runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    m_wifiAdmin.StartScan();
                    refreshWifiList();
                }
            });
        }
    };

    private void registerNetworkStateReceiver () {
        IntentFilter filter = new IntentFilter();

        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        getActivity().registerReceiver(m_networkChangedReceiver, filter);
    }

    private void unregisterNetworkStateReceiver () {
        getActivity().unregisterReceiver(m_networkChangedReceiver);
    }

    private BroadcastReceiver m_scanResultReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getActivity().runOnUiThread( new Runnable() {
                @Override
                public void run() {
                    refreshWifiList();
                }
            });
        }
    };

    private void registerScanResultReceiver () {
        IntentFilter filter = new IntentFilter();

        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        getActivity().registerReceiver(m_scanResultReceiver, filter);
    }

    private void unregisterScanResultReceiver () {
        getActivity().unregisterReceiver(m_scanResultReceiver);
    }


   private WifiAdmin m_wifiAdmin;
   private WifiSpotItemAdapter m_wifiSpotItemAdapter = null;

   public static final int[] STATE_SECURED = {R.attr.state_encrypted};
   public static final int[] STATE_NONE = {};

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View v = inflater.inflate(R.layout.wifi_settings, null);

      m_instance = this;

      setHasOptionsMenu(true);

       registerNetworkStateReceiver();
       registerScanResultReceiver();

      return v;
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      m_wifiAdmin = new WifiAdmin(getActivity());

      m_wifiSpotItemAdapter = new WifiSpotItemAdapter(getActivity());
      setListAdapter(m_wifiSpotItemAdapter);

       refreshWifiList();
       m_wifiAdmin.StartScan();

      /*Thread.State state = m_threadWifiScanner.getState();
      if ( state == Thread.State.NEW ) {
         m_threadWifiScanner.start();
      }*/
   }

   @Override
   public void onDestroyView() {
      m_instance = null;

      setHasOptionsMenu(false);

       unregisterScanResultReceiver();
       unregisterNetworkStateReceiver();

      super.onDestroyView();
   }

   @Override
   public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
      inflater.inflate(R.menu.wifi_menu, menu);
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch ( item.getItemId() ) {
         case R.id.action_wifi_scan:
             m_wifiAdmin.StartScan();
            break;

         case R.id.action_wifi_add_network: {
             DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                 @Override
                 public void onClick(DialogInterface dialogInterface, int i) {
                     switch (i) {
                         case DialogInterface.BUTTON_POSITIVE: {
                             WifiDialog wifiDialog = (WifiDialog) dialogInterface;
                             WifiConfiguration wcf = wifiDialog.getWifiConfiguration();

                             int netId = m_wifiAdmin.mWifiManager.addNetwork(wcf);
                             if (netId != -1) {
                                 m_wifiAdmin.mWifiManager.saveConfiguration();
                             }

                             m_wifiAdmin.StartScan();

                             break;
                         }

                         case DialogInterface.BUTTON_NEGATIVE:
                         default:
                             break;
                     }
                 }
             };

             WifiDialog dialog = new WifiDialog(getActivity(), onClickListener, null, false);
             dialog.show();

             break;
         }

         case R.id.action_wifi_advanced: {
             WifiSettingsAdvancedFragment fragAdv = new WifiSettingsAdvancedFragment();
             PreferenceActivity parentActivity = (PreferenceActivity) getActivity();

             parentActivity.startPreferenceFragment(fragAdv, true);
             break;
         }
      }

      return true;
   }

   @Override
   public void onListItemClick(ListView l, View v, int position, long id) {
      super.onListItemClick(l, v, position, id);

      WifiSpotItem wifiSpotItem = m_wifiSpotItemAdapter.getItem(position);
      DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialogInterface, int i) {
            switch (i) {
               case DialogInterface.BUTTON_POSITIVE: {
                   WifiDialog wifiDialog = (WifiDialog) dialogInterface;
                   WifiConfiguration wcf = wifiDialog.getWifiConfiguration();

                   // stackoverflow:8818290:how-to-connect-to-a-specific-wifi-network-in-android-programmatically
                   int netId = m_wifiAdmin.mWifiManager.addNetwork(wcf);
                   if (netId != -1) {
                       m_wifiAdmin.mWifiManager.enableNetwork(netId, true);
                       m_wifiAdmin.mWifiManager.saveConfiguration();
                       m_wifiAdmin.mWifiManager.setWifiEnabled(true);

                       //m_wifiAdmin.mWifiManager.disconnect();
                       //m_wifiAdmin.mWifiManager.enableNetwork(netId, true);
                       //m_wifiAdmin.mWifiManager.reconnect();
                   }

                  /*WifiDialog wifiDialog = (WifiDialog)dialogInterface;
                  switch(wifiDialog.chosenNetworkSetupMethod()) {
                     case 0:
                        WifiConfiguration var2 = var1.getConfig();
                        if(var2 == null) {
                           if(this.mSelectedAccessPoint != null && !this.requireKeyStore(this.mSelectedAccessPoint.getConfig()) && this.mSelectedAccessPoint.networkId != -1) {
                              this.mWifiManager.connectNetwork(this.mSelectedAccessPoint.networkId);
                           }
                        } else if(var2.networkId != -1) {
                           if(this.mSelectedAccessPoint != null) {
                              this.saveNetwork(var2);
                           }
                        } else if(!var1.isEdit() && !this.requireKeyStore(var2)) {
                           this.mWifiManager.connectNetwork(var2);
                        } else {
                           this.saveNetwork(var2);
                        }
                        break;
                     case 1:
                     case 2:
                     case 3:
                        this.mWifiManager.startWps(var1.getWpsConfig());
                  }

                  if(this.mWifiManager.isWifiEnabled()) {
                     this.mScanner.resume();
                  }

                  this.updateAccessPoints();*/
                   break;
               }

               case DialogInterface.BUTTON_NEUTRAL: { // Forget
                   WifiDialog wifiDialog = (WifiDialog)dialogInterface;
                   WifiConfiguration wcf = wifiDialog.getWifiConfiguration();

                   m_wifiAdmin.mWifiManager.removeNetwork(wcf.networkId);

                   m_wifiAdmin.StartScan();
                   break;
               }

               case DialogInterface.BUTTON_NEGATIVE:
               default:
                  break;
            }
         }
      };
      WifiDialog dialog = new WifiDialog(getActivity(), onClickListener, wifiSpotItem, false);
      dialog.show();
   }

   public class WifiSpotItemAdapter extends ArrayAdapter<WifiSpotItem> {
      public WifiSpotItemAdapter(Context context) {
         super(context, 0);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup parent) {
         if ( convertView == null ) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.wifi_settings_row, null);
         }

         WifiSpotItem itemData = getItem(position);

         TextView lblSSID = (TextView) convertView.findViewById(R.id.lblSSID);
         lblSSID.setText(itemData.szSSID);

         TextView lblCap = (TextView) convertView.findViewById(R.id.lblCap);

          lblCap.setText(itemData.getSummary(getActivity()));
         //lblCap.setText(String.format("Secured with %s", szSecurity));

         ImageView imgStatus = (ImageView) convertView.findViewById(R.id.imgStatus);
          if ( itemData.nSignalLevel == -1 ) {
              imgStatus.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
          } else {
              imgStatus.setImageResource(R.drawable.wifi_signal);
              imgStatus.setImageLevel(itemData.nSignalLevel);

              int szSecurity = itemData.getSecurityLevel();
              if (szSecurity == WifiSpotItem.LEVEL_NONE) {
                  imgStatus.setImageState(STATE_SECURED, false);
              } else {
                  imgStatus.setImageState(STATE_SECURED, true);
              }
          }

         return convertView;
      }
   }

   private static WifiSettingsFragment m_instance = null;
   /*private static Thread m_threadWifiScanner = new Thread(new Runnable()
   {
      public void run()
      {

         while(true)
         {
            if ( m_instance != null ) {
               m_instance.getActivity().runOnUiThread(new Runnable() {
                  public void run() {
                     m_instance.m_wifiAdmin.StartScan();
                  }
               });
            }
            try{
               Thread.sleep(30000); // 30 seconds
            }catch(InterruptedException e){
               e.printStackTrace();
            }
         }
      }
   });*/

   private void refreshWifiList () {
      List<WifiSpotItem> wifiSpotItems = m_wifiAdmin.GetWifiSpotList();

      if ( wifiSpotItems != null ) {
         m_wifiSpotItemAdapter.clear();
         for (int i = 0; i < wifiSpotItems.size(); i++) {
            m_wifiSpotItemAdapter.add(wifiSpotItems.get(i));
         }

         m_wifiSpotItemAdapter.notifyDataSetChanged();
      }
   }

    private void switchToEmptyFragment () {
        WifiSettingsEmptyFragment fragEmpty = new WifiSettingsEmptyFragment();
        PreferenceActivity parentActivity = (PreferenceActivity) getActivity();

        parentActivity.startPreferenceFragment(fragEmpty, false);
    }
}
