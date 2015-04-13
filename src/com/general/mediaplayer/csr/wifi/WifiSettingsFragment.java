package com.general.mediaplayer.csr.wifi;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.*;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.general.mediaplayer.csr.R;

import java.util.List;

public class WifiSettingsFragment extends ListFragment {

   private WifiAdmin m_wifiAdmin;
   private WifiSpotItemAdapter m_wifiSpotItemAdapter = null;

   public static final int[] STATE_SECURED = {R.attr.state_encrypted};
   public static final int[] STATE_NONE = {};

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View v = inflater.inflate(R.layout.wifi_settings, null);

      m_instance = this;

      setHasOptionsMenu(true);

      return v;
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);

      m_wifiAdmin = new WifiAdmin(getActivity());

      m_wifiSpotItemAdapter = new WifiSpotItemAdapter(getActivity());
      setListAdapter(m_wifiSpotItemAdapter);

      refreshWifiList();

      /*Thread.State state = m_threadWifiScanner.getState();
      if ( state == Thread.State.NEW ) {
         m_threadWifiScanner.start();
      }*/
   }

   @Override
   public void onDestroyView() {
      m_instance = null;

      setHasOptionsMenu(false);

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
            refreshWifiList();
            break;

         case R.id.action_wifi_add_network:
            DialogInterface.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
               @Override
               public void onClick(DialogInterface dialogInterface, int i) {
                  switch (i) {
                     case DialogInterface.BUTTON_POSITIVE:
                        i = 1;
                        break;
                     case DialogInterface.BUTTON_NEGATIVE:
                        i = 2;
                        break;
                     default:
                        break;
                  }
               }
            };
            WifiApDialog dialog = new WifiApDialog(getActivity(), onClickListener, null);
            dialog.show();
            break;

         case R.id.action_wifi_advanced:
            break;
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
               case DialogInterface.BUTTON_POSITIVE:
                  i = 1;
                  break;
               case DialogInterface.BUTTON_NEGATIVE:
                  i = 2;
                  break;
               default:
                  break;
            }
         }
      };
      WifiDialog dialog = new WifiDialog(getActivity(), onClickListener,wifiSpotItem, false);
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
         String szSecurity = WifiAdmin.getSecurityFromCap(itemData.szCaps);
         lblCap.setText(String.format("Secured with %s", szSecurity));

         ImageView imgStatus = (ImageView) convertView.findViewById(R.id.imgStatus);
         imgStatus.setImageLevel(itemData.nSignalLevel);

         if ( szSecurity.equals(WifiAdmin.NONE) ) {
            imgStatus.setImageState(STATE_SECURED, false);
         } else {
            imgStatus.setImageState(STATE_SECURED, true);
         }

         return convertView;
      }
   }

   private static WifiSettingsFragment m_instance = null;
   private static Thread m_threadWifiScanner = new Thread(new Runnable()
   {
      public void run()
      {

         while(true)
         {
            if ( m_instance != null ) {
               m_instance.getActivity().runOnUiThread(new Runnable() {
                  public void run() {
                     m_instance.refreshWifiList();
                  }
               });
            }
            try{
               Thread.sleep(2000);
            }catch(InterruptedException e){
               e.printStackTrace();
            }
         }
      }
   });;

   private void refreshWifiList () {
      m_wifiAdmin.StartScan();

      List<WifiSpotItem> wifiSpotItems = m_wifiAdmin.GetWifiSpotList();

      if ( wifiSpotItems != null ) {
         m_wifiSpotItemAdapter.clear();
         for (int i = 0; i < wifiSpotItems.size(); i++) {
            m_wifiSpotItemAdapter.add(wifiSpotItems.get(i));
         }

         m_wifiSpotItemAdapter.notifyDataSetChanged();
      }
   }
}

   /*private WiFiListAdapter mNearbyWiFiListAdapter = null;
   private List<WifiClusterItem> mWifiClusterItems = null;

   public void onListItemClick(ListView paramListView, View paramView, int paramInt, long paramLong)
   {
      super.onListItemClick(paramListView, paramView, paramInt, paramLong);
      WifiModel localWifiModel = (WifiModel)paramView.getTag();
      Intent localIntent = new Intent(getActivity(), WifiDetailActivity.class);
      localIntent.putExtra("mac_address", localWifiModel.getMacAddress());
      startActivity(localIntent);
   }

   public void onViewCreated(View paramView, Bundle paramBundle)
   {
      super.onViewCreated(paramView, paramBundle);
      getListView().setBackgroundColor(Color.DKGRAY); //getResources().getColor(2131230729)
      this.mNearbyWiFiListAdapter = new WiFiListAdapter(getActivity(), this.mWifiClusterItems);
      setListAdapter(this.mNearbyWiFiListAdapter);
   }

   public void setData(List<WifiClusterItem> paramList) {
      this.mWifiClusterItems = paramList;
   }*/

