package com.general.mediaplayer.csr.wifi;

import android.app.Activity;
import android.app.Fragment;
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
import org.w3c.dom.Text;

import java.util.List;

public class WifiSettingsEmptyFragment extends Fragment {

    TextView m_lblWifiStatus;

    private BroadcastReceiver m_networkChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action)) {
                handleWifiStateChanged(intent.getIntExtra(
                        WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN));
            }
        }
    };

    private void registerNetworkStateReceiver () {
        IntentFilter filter = new IntentFilter();

        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        getActivity().registerReceiver(m_networkChangedReceiver, filter);
    }

    private void unregisterNetworkStateReceiver () {
        getActivity().unregisterReceiver(m_networkChangedReceiver);
    }


   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      View v = inflater.inflate(R.layout.wifi_settings_empty, null);

       m_lblWifiStatus = (TextView) v.findViewById(R.id.lblWifiStatus);

       registerNetworkStateReceiver();

      return v;
   }

   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
   }

   @Override
   public void onDestroyView() {

       unregisterNetworkStateReceiver();

      super.onDestroyView();
   }

    private void handleWifiStateChanged(int state) {
        switch (state) {
            case WifiManager.WIFI_STATE_ENABLING:
                m_lblWifiStatus.setText(R.string.wifi_starting);
                break;
            case WifiManager.WIFI_STATE_ENABLED:
                WifiSettingsFragment frag = new WifiSettingsFragment();
                PreferenceActivity parentActivity = (PreferenceActivity) getActivity();

                parentActivity.startPreferenceFragment(frag, false);
                break;
            case WifiManager.WIFI_STATE_DISABLING:
                m_lblWifiStatus.setText(R.string.wifi_stopping);
                break;
            case WifiManager.WIFI_STATE_DISABLED:
                m_lblWifiStatus.setText(R.string.wifi_empty_list_wifi_off);
                break;
            default:
                m_lblWifiStatus.setText("");
                break;
        }
    }
}
