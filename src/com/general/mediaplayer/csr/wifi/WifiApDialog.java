package com.general.mediaplayer.csr.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import com.general.mediaplayer.csr.R;

public class WifiApDialog extends AlertDialog implements OnClickListener, TextWatcher, OnItemSelectedListener {

   static final int BUTTON_SUBMIT = -1;
   public static final int OPEN_INDEX = 0;
   public static final int WPA2_INDEX = 2;
   public static final int WPA_INDEX = 1;
   private final android.content.DialogInterface.OnClickListener mListener;
   private EditText mPassword;
   private int mSecurityTypeIndex = 0;
   private TextView mSsid;
   private View mView;
   WifiConfiguration mWifiConfig;


   public WifiApDialog(Context var1, android.content.DialogInterface.OnClickListener var2, WifiConfiguration var3) {
      super(var1);
      this.mListener = var2;
      this.mWifiConfig = var3;
      if(var3 != null) {
         this.mSecurityTypeIndex = getSecurityTypeIndex(var3);
      }

   }

   public static int getSecurityTypeIndex(WifiConfiguration var0) {
      return var0.allowedKeyManagement.get(1)?1:(var0.allowedKeyManagement.get(4)?2:0);
   }

   private void showSecurityFields() {
      if(this.mSecurityTypeIndex == 0) {
         this.mView.findViewById(R.id.fields).setVisibility(View.GONE);
      } else {
         this.mView.findViewById(R.id.fields).setVisibility(View.VISIBLE);
      }
   }

   private void validate() {
      if((this.mSsid == null || this.mSsid.length() != 0) && (this.mSecurityTypeIndex != 1 && this.mSecurityTypeIndex != 2 || this.mPassword.length() >= 8)) {
         this.getButton(-1).setEnabled(true);
      } else {
         this.getButton(-1).setEnabled(false);
      }
   }

   public void afterTextChanged(Editable var1) {
      this.validate();
   }

   public void beforeTextChanged(CharSequence var1, int var2, int var3, int var4) {}

   public WifiConfiguration getConfig() {
      WifiConfiguration var1 = new WifiConfiguration();
      var1.SSID = this.mSsid.getText().toString();
      switch(this.mSecurityTypeIndex) {
      case 0:
         var1.allowedKeyManagement.set(0);
         return var1;
      case 1:
         var1.allowedKeyManagement.set(1);
         var1.allowedAuthAlgorithms.set(0);
         if(this.mPassword.length() != 0) {
            var1.preSharedKey = this.mPassword.getText().toString();
            return var1;
         }
         break;
      case 2:
         var1.allowedKeyManagement.set(4);
         var1.allowedAuthAlgorithms.set(0);
         if(this.mPassword.length() != 0) {
            var1.preSharedKey = this.mPassword.getText().toString();
            return var1;
         }
         break;
      default:
         var1 = null;
      }

      return var1;
   }

   public void onClick(View var1) {
      EditText var2 = this.mPassword;
      short var3;
      if(((CheckBox)var1).isChecked()) {
         var3 = 144;
      } else {
         var3 = 128;
      }

      var2.setInputType(var3 | 1);
   }

   protected void onCreate(Bundle var1) {
      this.mView = this.getLayoutInflater().inflate(R.layout.wifi_ap_dialog, (ViewGroup)null);
      Spinner var2 = (Spinner)this.mView.findViewById(R.id.security);
      this.setView(this.mView);
      this.setInverseBackgroundForced(true);
      Context var3 = this.getContext();
      this.setTitle(R.string.wifi_tether_configure_ap_text);
      this.mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
      this.mSsid = (TextView)this.mView.findViewById(R.id.ssid);
      this.mPassword = (EditText)this.mView.findViewById(R.id.password);
      this.setButton(-1, var3.getString(R.string.wifi_save), this.mListener);
      this.setButton(-2, var3.getString(R.string.wifi_cancel), this.mListener);
      if(this.mWifiConfig != null) {
         this.mSsid.setText(this.mWifiConfig.SSID);
         var2.setSelection(this.mSecurityTypeIndex);
         if(this.mSecurityTypeIndex == 1 || this.mSecurityTypeIndex == 2) {
            this.mPassword.setText(this.mWifiConfig.preSharedKey);
         }
      }

      this.mSsid.addTextChangedListener(this);
      this.mPassword.addTextChangedListener(this);
      ((CheckBox)this.mView.findViewById(R.id.show_password)).setOnClickListener(this);
      var2.setOnItemSelectedListener(this);
      super.onCreate(var1);
      this.showSecurityFields();
      this.validate();
   }

   public void onItemSelected(AdapterView var1, View var2, int var3, long var4) {
      this.mSecurityTypeIndex = var3;
      this.showSecurityFields();
      this.validate();
   }

   public void onNothingSelected(AdapterView var1) {}

   public void onTextChanged(CharSequence var1, int var2, int var3, int var4) {}
}
