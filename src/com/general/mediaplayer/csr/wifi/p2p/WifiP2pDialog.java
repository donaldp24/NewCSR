package com.general.mediaplayer.csr.wifi.p2p;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import com.general.mediaplayer.csr.R;

public class WifiP2pDialog extends AlertDialog implements OnItemSelectedListener {

   static final int BUTTON_SUBMIT = -1;
   private static final int WPS_DISPLAY = 2;
   private static final int WPS_KEYPAD = 1;
   private static final int WPS_PBC = 0;
   WifiP2pDevice mDevice;
   private TextView mDeviceAddress;
   private TextView mDeviceName;
   private final OnClickListener mListener;
   private View mView;
   private int mWpsSetupIndex = 0;


   public WifiP2pDialog(Context var1, OnClickListener var2, WifiP2pDevice var3) {
      super(var1);
      this.mListener = var2;
      this.mDevice = var3;
   }

   public WifiP2pConfig getConfig() {
      WifiP2pConfig var1 = new WifiP2pConfig();
      var1.deviceAddress = this.mDeviceAddress.getText().toString();
      var1.wps = new WpsInfo();
      switch(this.mWpsSetupIndex) {
      case 0:
         var1.wps.setup = 0;
         return var1;
      case 1:
         var1.wps.setup = 2;
         var1.wps.pin = ((TextView)this.mView.findViewById(R.id.wps_pin)).getText().toString();
         return var1;
      case 2:
         var1.wps.setup = 1;
         return var1;
      default:
         var1.wps.setup = 0;
         return var1;
      }
   }

   protected void onCreate(Bundle var1) {
      this.mView = this.getLayoutInflater().inflate(R.layout.wifi_p2p_dialog, (ViewGroup)null);
      Spinner var2 = (Spinner)this.mView.findViewById(R.id.wps_setup);
      this.setView(this.mView);
      this.setInverseBackgroundForced(true);
      Context var3 = this.getContext();
      this.setTitle(R.string.wifi_p2p_settings_title);
      this.mDeviceName = (TextView)this.mView.findViewById(R.id.device_name);
      this.mDeviceAddress = (TextView)this.mView.findViewById(R.id.device_address);
      this.setButton(-1, var3.getString(R.string.wifi_connect), this.mListener);
      this.setButton(-2, var3.getString(R.string.wifi_cancel), this.mListener);
      if(this.mDevice != null) {
         this.mDeviceName.setText(this.mDevice.deviceName);
         this.mDeviceAddress.setText(this.mDevice.deviceAddress);
         var2.setSelection(this.mWpsSetupIndex);
      }

      var2.setOnItemSelectedListener(this);
      super.onCreate(var1);
   }

   public void onItemSelected(AdapterView var1, View var2, int var3, long var4) {
      this.mWpsSetupIndex = var3;
      if(this.mWpsSetupIndex == 1) {
         this.mView.findViewById(R.id.wps_pin_entry).setVisibility(0);
      } else {
         this.mView.findViewById(R.id.wps_pin_entry).setVisibility(8);
      }
   }

   public void onNothingSelected(AdapterView var1) {}
}
