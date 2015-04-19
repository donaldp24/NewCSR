package com.general.mediaplayer.csr.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.general.mediaplayer.csr.R;


class WifiDialog extends AlertDialog {

   static final int BUTTON_FORGET = -3;
   static final int BUTTON_SUBMIT = -1;
   private final WifiSpotItem mAccessPoint;
   //private WifiConfigController mController;
   private final boolean mEdit;
   private final OnClickListener mListener;
   private View mView;

   TextView mSsidView;
   Spinner mSecuritySpinner;
   Spinner mIpSettingsSpinner;
   Spinner mProxySettingsSpinner;
   Spinner mNetworkSetupSpinner;
   Spinner mEapMethodSpinner;
   TextView mPasswordView;
   Spinner mPhase2Spinner;
   Spinner mEapCaCertSpinner;
   Spinner mEapUserCertSpinner;
   TextView mEapIdentityView;
   TextView mEapAnonymousView;
   int mAccessPointSecurity = 0; // index in SecuritySpinner:R.array.wifi_security
   TextView mIpAddressView;
   TextView mGatewayView;
   TextView mNetworkPrefixLengthView;
   TextView mDns1View;
   TextView mDns2View;
   TextView mProxyHostView;
   TextView mProxyPortView;
   TextView mProxyExclusionListView;


   public WifiDialog(Context ctx, OnClickListener listener, WifiSpotItem spot, boolean edit) {
      super(ctx);
      this.mEdit = edit;
      this.mListener = listener;
      this.mAccessPoint = spot;

       if ( spot != null ) {
           int nSecLevel = spot.getSecurityLevel();

           switch ( nSecLevel ) {
               case WifiSpotItem.LEVEL_NONE:
                   this.mAccessPointSecurity = 0;
                   break;

               case WifiSpotItem.LEVEL_WEP:
                   this.mAccessPointSecurity = 1;
                   break;

               case WifiSpotItem.LEVEL_WPA:
               case WifiSpotItem.LEVEL_WPA2:
               case WifiSpotItem.LEVEL_WPA_WPA2:
                   this.mAccessPointSecurity = 2;
                   break;

               case WifiSpotItem.LEVEL_EAP:
                   this.mAccessPointSecurity = 3;
                   break;

               default:
                   this.mAccessPointSecurity = -1;
                   break;
           }
       } else {
           this.mAccessPointSecurity = -1;
       }
   }

   public Button getCancelButton() {
      return this.getButton(-2);
   }

   /*public WifiConfigController getController() {
      return this.mController;
   }*/

   public Button getForgetButton() {
      return this.getButton(-3);
   }

   public Button getSubmitButton() {
      return this.getButton(-1);
   }

   public boolean isEdit() {
      return this.mEdit;
   }

   protected void onCreate(Bundle var1) {
      this.mView = this.getLayoutInflater().inflate(R.layout.wifi_dialog, (ViewGroup)null);
      this.setView(this.mView);
      this.setInverseBackgroundForced(true);
      //this.mController = new WifiConfigController(this, this.mView, this.mAccessPoint, this.mEdit);
      initControls();

      super.onCreate(var1);

      if(this.mAccessPoint!=null && this.mAccessPoint.getNetworkId()==-1) {
         this.getSubmitButton().setEnabled(false);
      }

   }

   public void setCancelButton(CharSequence var1) {
      this.setButton(-2, var1, this.mListener);
   }

   public void setForgetButton(CharSequence var1) {
      this.setButton(-3, var1, this.mListener);
   }

   public void setSubmitButton(CharSequence var1) {
      this.setButton(-1, var1, this.mListener);
   }

   private void initControls() {
      if(this.mAccessPoint == null) {
         this.setTitle(R.string.wifi_add_network);
         this.mSsidView = (TextView)this.mView.findViewById(R.id.ssid);
         this.mSsidView.addTextChangedListener(m_editboxChangedListener);
         this.mSecuritySpinner = (Spinner)this.mView.findViewById(R.id.security);
         this.mSecuritySpinner.setOnItemSelectedListener(m_spinnerSelectedListener);

         // TODO
         /*if(this.mInXlSetupWizard) {
            this.mView.findViewById(R.id.type_ssid).setVisibility(View.VISIBLE);
            this.mView.findViewById(R.id.type_security).setVisibility(View.VISIBLE);
            ArrayAdapter var16 = new ArrayAdapter(var6, R.layout.wifi_setup_custom_list_item_1, 16908308, var6.getResources().getStringArray(R.array.wifi_security_no_eap));
            this.mSecuritySpinner.setAdapter(var16);
         } else {*/
            this.mView.findViewById(R.id.type).setVisibility(View.VISIBLE);
         //}

         this.setSubmitButton(getContext().getString(R.string.wifi_save));
      } else {
         this.setTitle(this.mAccessPoint.szSSID);
         this.mIpSettingsSpinner = (Spinner)this.mView.findViewById(R.id.ip_settings);
         this.mIpSettingsSpinner.setOnItemSelectedListener(m_spinnerSelectedListener);
         this.mProxySettingsSpinner = (Spinner)this.mView.findViewById(R.id.proxy_settings);
         this.mProxySettingsSpinner.setOnItemSelectedListener(m_spinnerSelectedListener);
         ViewGroup var8 = (ViewGroup)this.mView.findViewById(R.id.info);
         NetworkInfo.DetailedState var9 = this.mAccessPoint.getState();
         if(var9 != null) {
            this.addRow(var8, R.string.wifi_status, Summary.get(this.getContext(), var9));
         }

         int var10 = this.mAccessPoint.getSignalLevel();
         if(var10 != -1) {
            this.addRow(var8, R.string.wifi_signal, getContext().getResources().getStringArray(R.array.wifi_signal)[var10]);
         }

         android.net.wifi.WifiInfo var11 = this.mAccessPoint.getInfo();
         if(var11 != null && var11.getLinkSpeed() != -1) {
            this.addRow(var8, R.string.wifi_speed, var11.getLinkSpeed() + "Mbps");
         }

         this.addRow(var8, R.string.wifi_security, this.mAccessPoint.getSecurityString(getContext(), false));
         int var12 = this.mAccessPoint.getNetworkId();
         boolean var13 = false;
         if(var12 != -1) {
            WifiConfiguration var14 = this.mAccessPoint.wifiConfig;
            // TODO
            /*if(var14.ipAssignment == IpAssignment.STATIC) {
               this.mIpSettingsSpinner.setSelection(1);
               var13 = true;
            } else {*/
               this.mIpSettingsSpinner.setSelection(0);
               var13 = false;
            //}

            // TODO
            /*Iterator var15 = var14.linkProperties.getAddresses().iterator();

            while(var15.hasNext()) {
               this.addRow(var8, R.string.wifi_ip_address, ((InetAddress)var15.next()).getHostAddress());
            }*/

            // TODO
            /*if(var14.proxySettings == ProxySettings.STATIC) {
               this.mProxySettingsSpinner.setSelection(1);
               var13 = true;
            } else {*/
               this.mProxySettingsSpinner.setSelection(0);
            /*}

            if(var14.status == 1 && var14.disableReason == 1) {
               this.addRow(var8, R.string.wifi_disabled_heading, var6.getString(R.string.wifi_disabled_help));
            }*/
         }

         if(this.mAccessPoint.getNetworkId() == -1 && this.mAccessPoint.getWpsAvailable()) {
            this.showNetworkSetupFields();
         }

         if(this.mAccessPoint.getNetworkId() == -1 || this.mEdit) {
            this.showSecurityFields();
            this.showIpConfigFields();
            this.showProxyFields();
            this.mView.findViewById(R.id.wifi_advanced_toggle).setVisibility(View.VISIBLE);
            this.mView.findViewById(R.id.wifi_advanced_togglebox).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                  CheckBox chkAdvanced = (CheckBox)view;
                  boolean bChecked = chkAdvanced.isChecked();

                  if ( bChecked ) {
                     mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
                  } else {
                     mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.GONE);
                  }
               }
            });
            if(var13) {
               ((CheckBox)this.mView.findViewById(R.id.wifi_advanced_togglebox)).setChecked(true);
               this.mView.findViewById(R.id.wifi_advanced_fields).setVisibility(View.VISIBLE);
            }
         }

         if(this.mEdit) {
            this.setSubmitButton(getContext().getString(R.string.wifi_save));
         } else {
            if(var9 == null && var10 != -1) {
               this.setSubmitButton(getContext().getString(R.string.wifi_connect));
            } else {
               this.mView.findViewById(R.id.ip_fields).setVisibility(View.GONE);
            }

            if(this.mAccessPoint.getNetworkId() != -1) {
               this.setForgetButton(getContext().getString(R.string.wifi_forget));
            }
         }
      }

      this.setCancelButton(getContext().getString(R.string.wifi_cancel));
      if(this.getSubmitButton() != null) {
         this.enableSubmitIfAppropriate();
      }
   }

   private void addRow(ViewGroup var1, int var2, String var3) {
      View var4 = this.getLayoutInflater().inflate(R.layout.wifi_dialog_row, var1, false);
      ((TextView)var4.findViewById(R.id.name)).setText(var2);
      ((TextView)var4.findViewById(R.id.value)).setText(var3);
      var1.addView(var4);
   }

   private void showNetworkSetupFields() {
      this.mView.findViewById(R.id.setup_fields).setVisibility(View.VISIBLE);
      if(this.mNetworkSetupSpinner == null) {
         this.mNetworkSetupSpinner = (Spinner)this.mView.findViewById(R.id.network_setup);
         this.mNetworkSetupSpinner.setOnItemSelectedListener(m_spinnerSelectedListener);
      }

      int var1 = this.mNetworkSetupSpinner.getSelectedItemPosition();
      if(var1 == 2) {
         this.mView.findViewById(R.id.wps_fields).setVisibility(View.VISIBLE);
      } else {
         this.mView.findViewById(R.id.wps_fields).setVisibility(View.GONE);
      }

      if(var1 != 3 && var1 != 2 && var1 != 1) {
         this.mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);
      } else {
         this.mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
      }
   }

   public int chosenNetworkSetupMethod() {
      return this.mNetworkSetupSpinner != null?this.mNetworkSetupSpinner.getSelectedItemPosition():0;
   }

   private boolean ipAndProxyFieldsAreValid() {
      // TODO
      return true;
      /*this.mLinkProperties.clear();
      IpAssignment var1;
      if(this.mIpSettingsSpinner != null && this.mIpSettingsSpinner.getSelectedItemPosition() == 1) {
         var1 = IpAssignment.STATIC;
      } else {
         var1 = IpAssignment.DHCP;
      }

      this.mIpAssignment = var1;
      if(this.mIpAssignment == IpAssignment.STATIC && this.validateIpConfigFields(this.mLinkProperties) != 0) {
         return false;
      } else {
         ProxySettings var2;
         if(this.mProxySettingsSpinner != null && this.mProxySettingsSpinner.getSelectedItemPosition() == 1) {
            var2 = ProxySettings.STATIC;
         } else {
            var2 = ProxySettings.NONE;
         }

         this.mProxySettings = var2;
         if(this.mProxySettings == ProxySettings.STATIC) {
            String var3 = this.mProxyHostView.getText().toString();
            String var4 = this.mProxyPortView.getText().toString();
            String var5 = this.mProxyExclusionListView.getText().toString();
            int var6 = 0;

            int var8;
            label32: {
               int var10;
               try {
                  var6 = Integer.parseInt(var4);
                  var10 = ProxySelector.validate(var3, var4, var5);
               } catch (NumberFormatException var11) {
                  var8 = 2131427498;
                  break label32;
               }

               var8 = var10;
            }

            if(var8 != 0) {
               return false;
            }

            ProxyProperties var9 = new ProxyProperties(var3, var6, var5);
            this.mLinkProperties.setHttpProxy(var9);
         }

         return true;
      }*/
   }

   private void enableSubmitIfAppropriate() {
      Button var1 = this.getSubmitButton();
      if(var1 != null) {
         int var2 = this.chosenNetworkSetupMethod();
         boolean var3 = false;
         if(var2 == 0) {
            label56: {
               if(this.mAccessPointSecurity != 1 || this.mPasswordView.length() != 0) {
                  int var5 = this.mAccessPointSecurity;
                  var3 = false;
                  if(var5 != 2) {
                     break label56;
                  }

                  int var6 = this.mPasswordView.length();
                  var3 = false;
                  if(var6 >= 8) {
                     break label56;
                  }
               }

               var3 = true;
            }
         }

         boolean var4;
         if((this.mSsidView == null || this.mSsidView.length() != 0) && (this.mAccessPoint != null && this.mAccessPoint.getNetworkId() != -1 || !var3)) {
            if(this.ipAndProxyFieldsAreValid()) {
               var4 = true;
            } else {
               var4 = false;
            }
         } else {
            var4 = false;
         }

         var1.setEnabled(var4);
      }
   }

   boolean initSecurityFields(View var1, int var2) {
      // TODO
      return true;
      /*var1.findViewById(2131231182).setVisibility(8);
      var1.findViewById(2131231183).setVisibility(8);
      var1.findViewById(2131231185).setVisibility(0);
      var1.findViewById(2131231186).setVisibility(0);
      if(var2 == 3) {
         this.setPaddingVisibility(0);
         this.hideSoftwareKeyboard();
         if(var1.findViewById(2131231184).getVisibility() == 0) {
            var1.findViewById(2131231183).setVisibility(0);
         } else {
            var1.findViewById(2131231182).setVisibility(0);
         }

         var1.findViewById(2131231159).setVisibility(8);
         var1.findViewById(2131231185).setVisibility(8);
         var1.findViewById(2131231186).setVisibility(8);
         this.onEapNetworkSelected();
         return false;
      } else {
         this.mConnectButton.setVisibility(0);
         this.setPaddingVisibility(8);
         if(this.mWifiConfig != null) {
            if(var2 != 2 && var2 != 1) {
               this.mWifiConfig.requestFocusAndShowKeyboard(2131231150);
            } else {
               this.mWifiConfig.requestFocusAndShowKeyboard(2131231131);
            }
         }

         return true;
      }*/
   }

   private void showSecurityFields() {
      if( initSecurityFields(this.mView, this.mAccessPointSecurity)) { // FIXME - this line
         if(this.mAccessPointSecurity == 0) {
            this.mView.findViewById(R.id.security_fields).setVisibility(View.GONE);
            return;
         }

         this.mView.findViewById(R.id.security_fields).setVisibility(View.VISIBLE);
         if(this.mPasswordView == null) {
            this.mPasswordView = (TextView)this.mView.findViewById(R.id.password);
            this.mPasswordView.addTextChangedListener(m_editboxChangedListener);
            ((CheckBox)this.mView.findViewById(R.id.show_password)).setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                  CheckBox chkShowPassword = (CheckBox)view;
                  boolean bChecked = chkShowPassword.isChecked();

                  if ( bChecked ) {
                     mPasswordView.setInputType(144 | 1);
                  } else {
                     mPasswordView.setInputType(128 | 1);
                  }
               }
            });
            if(this.mAccessPoint != null && this.mAccessPoint.getNetworkId() != -1) {
               this.mPasswordView.setHint(R.string.wifi_unchanged);
            }
         }

         if(this.mAccessPointSecurity != 3) {
            this.mView.findViewById(R.id.eap).setVisibility(View.GONE);
            return;
         }

         this.mView.findViewById(R.id.eap).setVisibility(View.VISIBLE);
         if(this.mEapMethodSpinner == null) {
            this.mEapMethodSpinner = (Spinner)this.mView.findViewById(R.id.method);
            this.mPhase2Spinner = (Spinner)this.mView.findViewById(R.id.phase2);
            this.mEapCaCertSpinner = (Spinner)this.mView.findViewById(R.id.ca_cert);
            this.mEapUserCertSpinner = (Spinner)this.mView.findViewById(R.id.user_cert);
            this.mEapIdentityView = (TextView)this.mView.findViewById(R.id.identity);
            this.mEapAnonymousView = (TextView)this.mView.findViewById(R.id.anonymous);
            this.loadCertificates(this.mEapCaCertSpinner, "CACERT_");
            this.loadCertificates(this.mEapUserCertSpinner, "USRPKEY_");
            if(this.mAccessPoint != null && this.mAccessPoint.getNetworkId() != -1) {
               // TODO
               /*WifiConfiguration var1 = this.mAccessPoint.getConfig();
               this.setSelection(this.mEapMethodSpinner, var1.eap.value());
               this.setSelection(this.mPhase2Spinner, var1.phase2.value());
               this.setCertificate(this.mEapCaCertSpinner, "CACERT_", var1.ca_cert.value());
               this.setCertificate(this.mEapUserCertSpinner, "USRPKEY_", var1.private_key.value());
               this.mEapIdentityView.setText(var1.identity.value());
               this.mEapAnonymousView.setText(var1.anonymous_identity.value());*/
               return;
            }
         }
      }

   }

   private void setSelection(Spinner var1, String var2) {
      if(var2 != null) {
         ArrayAdapter var3 = (ArrayAdapter)var1.getAdapter();

         for(int var4 = -1 + var3.getCount(); var4 >= 0; --var4) {
            if(var2.equals(var3.getItem(var4))) {
               var1.setSelection(var4);
               break;
            }
         }
      }

   }

   private void setCertificate(Spinner var1, String var2, String var3) {
      String var4 = "keystore://" + var2;
      if(var3 != null && var3.startsWith(var4)) {
         this.setSelection(var1, var3.substring(var4.length()));
      }

   }

   private void loadCertificates(Spinner var1, String var2) {
      Context var3 = this.getContext();
      String var4 = var3.getString(R.string.wifi_unspecified);
      String[] var5;
      // TODO
      /*
      try {
         var5 = KeyStore.getInstance("").saw(var2);
      } catch ( KeyStoreException e ) {
         e.printStackTrace();
         return;
      }
      String[] var6;
      if(var5 != null && var5.length != 0) {
         String[] var8 = new String[1 + var5.length];
         var8[0] = var4;
         System.arraycopy(var5, 0, var8, 1, var5.length);
         var6 = var8;
      } else {
         var6 = new String[]{var4};
      }

      ArrayAdapter var7 = new ArrayAdapter(var3, 17367048, var6);
      var7.setDropDownViewResource(17367049);
      var1.setAdapter(var7);*/
   }

   private void showIpConfigFields() {
      this.mView.findViewById(R.id.ip_fields).setVisibility(View.VISIBLE);
      WifiSpotItem var1 = this.mAccessPoint;
      WifiConfiguration var2 = null;
      if(var1 != null) {
         int var9 = this.mAccessPoint.getNetworkId();
         var2 = null;
         if(var9 != -1) {
            var2 = this.mAccessPoint.wifiConfig;
         }
      }

      if(this.mIpSettingsSpinner.getSelectedItemPosition() == 1) {
         this.mView.findViewById(R.id.staticip).setVisibility(View.VISIBLE);
         if(this.mIpAddressView == null) {
            this.mIpAddressView = (TextView)this.mView.findViewById(R.id.ipaddress);
            this.mIpAddressView.addTextChangedListener(m_editboxChangedListener);
            this.mGatewayView = (TextView)this.mView.findViewById(R.id.gateway);
            this.mGatewayView.addTextChangedListener(m_editboxChangedListener);
            this.mNetworkPrefixLengthView = (TextView)this.mView.findViewById(R.id.network_prefix_length);
            this.mNetworkPrefixLengthView.addTextChangedListener(m_editboxChangedListener);
            this.mDns1View = (TextView)this.mView.findViewById(R.id.dns1);
            this.mDns1View.addTextChangedListener(m_editboxChangedListener);
            this.mDns2View = (TextView)this.mView.findViewById(R.id.dns2);
            this.mDns2View.addTextChangedListener(m_editboxChangedListener);
         }

         // TODO
         /*if(var2 != null) {
            LinkProperties var3 = var2.linkProperties;
            Iterator var4 = var3.getLinkAddresses().iterator();
            if(var4.hasNext()) {
               LinkAddress var8 = (LinkAddress)var4.next();
               this.mIpAddressView.setText(var8.getAddress().getHostAddress());
               this.mNetworkPrefixLengthView.setText(Integer.toString(var8.getNetworkPrefixLength()));
            }

            Iterator var5 = var3.getRoutes().iterator();

            while(var5.hasNext()) {
               MediaRouter.RouteInfo var7 = (MediaRouter.RouteInfo)var5.next();
               if(var7.isDefaultRoute()) {
                  this.mGatewayView.setText(var7.getGateway().getHostAddress());
                  break;
               }
            }

            Iterator var6 = var3.getDnses().iterator();
            if(var6.hasNext()) {
               this.mDns1View.setText(((InetAddress)var6.next()).getHostAddress());
            }

            if(var6.hasNext()) {
               this.mDns2View.setText(((InetAddress)var6.next()).getHostAddress());
            }
         }*/

      } else {
         this.mView.findViewById(R.id.staticip).setVisibility(View.GONE);
      }
   }

   private void showProxyFields() {
      this.mView.findViewById(R.id.proxy_settings_fields).setVisibility(View.VISIBLE);
      WifiSpotItem var1 = this.mAccessPoint;
      WifiConfiguration var2 = null;
      if(var1 != null) {
         int var4 = this.mAccessPoint.getNetworkId();
         var2 = null;
         if(var4 != -1) {
            var2 = this.mAccessPoint.wifiConfig;
         }
      }

      if(this.mProxySettingsSpinner.getSelectedItemPosition() == 1) {
         this.mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.VISIBLE);
         this.mView.findViewById(R.id.proxy_fields).setVisibility(View.VISIBLE);
         if(this.mProxyHostView == null) {
            this.mProxyHostView = (TextView)this.mView.findViewById(R.id.proxy_hostname);
            this.mProxyHostView.addTextChangedListener(m_editboxChangedListener);
            this.mProxyPortView = (TextView)this.mView.findViewById(R.id.proxy_port);
            this.mProxyPortView.addTextChangedListener(m_editboxChangedListener);
            this.mProxyExclusionListView = (TextView)this.mView.findViewById(R.id.proxy_exclusionlist);
            this.mProxyExclusionListView.addTextChangedListener(m_editboxChangedListener);
         }

         // TODO
         /*if(var2 != null) {
            ProxyProperties var3 = var2.linkProperties.getHttpProxy();
            if(var3 != null) {
               this.mProxyHostView.setText(var3.getHost());
               this.mProxyPortView.setText(Integer.toString(var3.getPort()));
               this.mProxyExclusionListView.setText(var3.getExclusionList());
            }
         }*/

      } else {
         this.mView.findViewById(R.id.proxy_warning_limited_support).setVisibility(View.GONE);
         this.mView.findViewById(R.id.proxy_fields).setVisibility(View.GONE);
      }
   }

   TextWatcher m_editboxChangedListener = new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

      @Override
      public void afterTextChanged(Editable editable) {
         enableSubmitIfAppropriate();
      }
   };

   AdapterView.OnItemSelectedListener m_spinnerSelectedListener = new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
         if(adapterView == mSecuritySpinner) {
            mAccessPointSecurity = i;
            showSecurityFields();
         } else if(adapterView == mNetworkSetupSpinner) {
            showNetworkSetupFields();
         } else if(adapterView == mProxySettingsSpinner) {
            showProxyFields();
         } else {
            showIpConfigFields();
         }

         enableSubmitIfAppropriate();
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {}
   };

   public WifiConfiguration getWifiConfiguration() {
      if ( mAccessPoint==null || mAccessPoint.wifiConfig == null ) {
          WifiConfiguration wifiConfig = new WifiConfiguration();

          // create a new WifiConfiguration
          // stackoverflow:8818290:how-to-connect-to-a-specific-wifi-network-in-android-programmatically
          if ( mSsidView != null ) {
              wifiConfig.SSID = "\"" + mSsidView.getText() + "\"";
          } else {
              if ( mAccessPoint != null) {
                  wifiConfig.SSID = "\"" + mAccessPoint.szSSID + "\"";
              } else {
                  // This case will not appear, but for exception.
                  return null;
              }
          }
          wifiConfig.status = WifiConfiguration.Status.DISABLED;
          wifiConfig.priority = 40;

          switch ( mAccessPointSecurity ) {
              case 1: { // WEP
                  wifiConfig.wepKeys[0] = "\"" + mPasswordView.getText() + "\"";
                  wifiConfig.wepTxKeyIndex = 0;
                  wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                  wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                  wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                  wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                  wifiConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                  wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                  wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                  break;
              }

              case 2: { // PSK Generic
                  wifiConfig.preSharedKey = "\"" + mPasswordView.getText() + "\"";
                  wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                  wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                  wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                  wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                  wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                  //wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                  //wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);

                  wifiConfig.status = WifiConfiguration.Status.ENABLED;
                  wifiConfig.hiddenSSID = true;
                  break;
              }

              case 0: { // None
                  wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                  wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                  wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                  wifiConfig.allowedAuthAlgorithms.clear();
                  wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                  wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                  break;
              }

              case 3: { // EAP
                  // FIXME - I am not sure this is correct
                  // same as WPA, except for key management
                  wifiConfig.preSharedKey = "\"" + mPasswordView.getText() + "\"";
                  wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                  wifiConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                  wifiConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_EAP);
                  wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                  wifiConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                  wifiConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                  break;
              }

              default:
                  break;
          }

          return wifiConfig;
      }

      return mAccessPoint.wifiConfig;
   }
}
