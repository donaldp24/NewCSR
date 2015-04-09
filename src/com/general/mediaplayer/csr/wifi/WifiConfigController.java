package com.general.mediaplayer.csr.wifi;

import android.content.Context;
import android.content.res.Resources;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.NetworkUtils;
import android.net.ProxyProperties;
import android.net.RouteInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WpsInfo;
import android.net.wifi.WifiConfiguration.EnterpriseField;
import android.net.wifi.WifiConfiguration.IpAssignment;
import android.net.wifi.WifiConfiguration.ProxySettings;
import android.security.KeyStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import com.general.mediaplayer.csr.ProxySelector;
import com.general.mediaplayer.csr.wifi.AccessPoint;
import com.general.mediaplayer.csr.wifi.Summary;
import com.general.mediaplayer.csr.wifi.WifiConfigUiBase;
import com.general.mediaplayer.csr.wifi.WifiConfigUiForSetupWizardXL;
import com.general.mediaplayer.csr.wifi.WifiSettingsForSetupWizardXL;
import java.net.InetAddress;
import java.util.Iterator;

public class WifiConfigController implements TextWatcher, OnClickListener, OnItemSelectedListener {

   private static final int DHCP = 0;
   private static final String KEYSTORE_SPACE = "keystore://";
   public static final int MANUAL = 0;
   public static final int PROXY_NONE = 0;
   public static final int PROXY_STATIC = 1;
   private static final int STATIC_IP = 1;
   private static final String TAG = "WifiConfigController";
   public static final int WPS_DISPLAY = 3;
   public static final int WPS_KEYPAD = 2;
   public static final int WPS_PBC = 1;
   private final AccessPoint mAccessPoint;
   private int mAccessPointSecurity;
   private final WifiConfigUiBase mConfigUi;
   private TextView mDns1View;
   private TextView mDns2View;
   private TextView mEapAnonymousView;
   private Spinner mEapCaCertSpinner;
   private TextView mEapIdentityView;
   private Spinner mEapMethodSpinner;
   private Spinner mEapUserCertSpinner;
   private boolean mEdit;
   private TextView mGatewayView;
   private final boolean mInXlSetupWizard;
   private TextView mIpAddressView;
   private IpAssignment mIpAssignment;
   private Spinner mIpSettingsSpinner;
   private LinkProperties mLinkProperties;
   private TextView mNetworkPrefixLengthView;
   private Spinner mNetworkSetupSpinner;
   private TextView mPasswordView;
   private Spinner mPhase2Spinner;
   private TextView mProxyExclusionListView;
   private TextView mProxyHostView;
   private TextView mProxyPortView;
   private ProxySettings mProxySettings;
   private Spinner mProxySettingsSpinner;
   private Spinner mSecuritySpinner;
   private TextView mSsidView;
   private final View mView;


   public WifiConfigController(WifiConfigUiBase var1, View var2, AccessPoint var3, boolean var4) {
      this.mIpAssignment = IpAssignment.UNASSIGNED;
      this.mProxySettings = ProxySettings.UNASSIGNED;
      this.mLinkProperties = new LinkProperties();
      this.mConfigUi = var1;
      this.mInXlSetupWizard = var1 instanceof WifiConfigUiForSetupWizardXL;
      this.mView = var2;
      this.mAccessPoint = var3;
      int var5;
      if(var3 == null) {
         var5 = 0;
      } else {
         var5 = var3.security;
      }

      this.mAccessPointSecurity = var5;
      this.mEdit = var4;
      Context var6 = this.mConfigUi.getContext();
      Resources var7 = var6.getResources();
      if(this.mAccessPoint == null) {
         this.mConfigUi.setTitle(2131427775);
         this.mSsidView = (TextView)this.mView.findViewById(2131231150);
         this.mSsidView.addTextChangedListener(this);
         this.mSecuritySpinner = (Spinner)this.mView.findViewById(2131231151);
         this.mSecuritySpinner.setOnItemSelectedListener(this);
         if(this.mInXlSetupWizard) {
            this.mView.findViewById(2131231184).setVisibility(0);
            this.mView.findViewById(2131231189).setVisibility(0);
            ArrayAdapter var16 = new ArrayAdapter(var6, 2130968721, 16908308, var6.getResources().getStringArray(2131165203));
            this.mSecuritySpinner.setAdapter(var16);
         } else {
            this.mView.findViewById(2131231111).setVisibility(0);
         }

         this.mConfigUi.setSubmitButton(var6.getString(2131427834));
      } else {
         this.mConfigUi.setTitle(this.mAccessPoint.ssid);
         this.mIpSettingsSpinner = (Spinner)this.mView.findViewById(2131231175);
         this.mIpSettingsSpinner.setOnItemSelectedListener(this);
         this.mProxySettingsSpinner = (Spinner)this.mView.findViewById(2131231168);
         this.mProxySettingsSpinner.setOnItemSelectedListener(this);
         ViewGroup var8 = (ViewGroup)this.mView.findViewById(2131231149);
         DetailedState var9 = this.mAccessPoint.getState();
         if(var9 != null) {
            this.addRow(var8, 2131427793, Summary.get(this.mConfigUi.getContext(), var9));
         }

         int var10 = this.mAccessPoint.getLevel();
         if(var10 != -1) {
            this.addRow(var8, 2131427792, var7.getStringArray(2131165209)[var10]);
         }

         android.net.wifi.WifiInfo var11 = this.mAccessPoint.getInfo();
         if(var11 != null && var11.getLinkSpeed() != -1) {
            this.addRow(var8, 2131427794, var11.getLinkSpeed() + "Mbps");
         }

         this.addRow(var8, 2131427791, this.mAccessPoint.getSecurityString(false));
         int var12 = this.mAccessPoint.networkId;
         boolean var13 = false;
         if(var12 != -1) {
            WifiConfiguration var14 = this.mAccessPoint.getConfig();
            if(var14.ipAssignment == IpAssignment.STATIC) {
               this.mIpSettingsSpinner.setSelection(1);
               var13 = true;
            } else {
               this.mIpSettingsSpinner.setSelection(0);
               var13 = false;
            }

            Iterator var15 = var14.linkProperties.getAddresses().iterator();

            while(var15.hasNext()) {
               this.addRow(var8, 2131427795, ((InetAddress)var15.next()).getHostAddress());
            }

            if(var14.proxySettings == ProxySettings.STATIC) {
               this.mProxySettingsSpinner.setSelection(1);
               var13 = true;
            } else {
               this.mProxySettingsSpinner.setSelection(0);
            }

            if(var14.status == 1 && var14.disableReason == 1) {
               this.addRow(var8, 2131427811, var6.getString(2131427812));
            }
         }

         if(this.mAccessPoint.networkId == -1 && this.mAccessPoint.wpsAvailable) {
            this.showNetworkSetupFields();
         }

         if(this.mAccessPoint.networkId == -1 || this.mEdit) {
            this.showSecurityFields();
            this.showIpConfigFields();
            this.showProxyFields();
            this.mView.findViewById(2131231192).setVisibility(0);
            this.mView.findViewById(2131231193).setOnClickListener(this);
            if(var13) {
               ((CheckBox)this.mView.findViewById(2131231193)).setChecked(true);
               this.mView.findViewById(2131231194).setVisibility(0);
            }
         }

         if(this.mEdit) {
            this.mConfigUi.setSubmitButton(var6.getString(2131427834));
         } else {
            if(var9 == null && var10 != -1) {
               this.mConfigUi.setSubmitButton(var6.getString(2131427832));
            } else {
               this.mView.findViewById(2131231174).setVisibility(8);
            }

            if(this.mAccessPoint.networkId != -1) {
               this.mConfigUi.setForgetButton(var6.getString(2131427833));
            }
         }
      }

      this.mConfigUi.setCancelButton(var6.getString(2131427835));
      if(this.mConfigUi.getSubmitButton() != null) {
         this.enableSubmitIfAppropriate();
      }

   }

   private void addRow(ViewGroup var1, int var2, String var3) {
      View var4 = this.mConfigUi.getLayoutInflater().inflate(2130968718, var1, false);
      ((TextView)var4.findViewById(2131230847)).setText(var2);
      ((TextView)var4.findViewById(2131230927)).setText(var3);
      var1.addView(var4);
   }

   private void enableSubmitIfAppropriate() {
      Button var1 = this.mConfigUi.getSubmitButton();
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
         if((this.mSsidView == null || this.mSsidView.length() != 0) && (this.mAccessPoint != null && this.mAccessPoint.networkId != -1 || !var3)) {
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

   private boolean ipAndProxyFieldsAreValid() {
      this.mLinkProperties.clear();
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
      }
   }

   private void loadCertificates(Spinner var1, String var2) {
      Context var3 = this.mConfigUi.getContext();
      String var4 = var3.getString(2131427807);
      String[] var5 = KeyStore.getInstance().saw(var2);
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
      var1.setAdapter(var7);
   }

   static boolean requireKeyStore(WifiConfiguration var0) {
      if(var0 != null) {
         String[] var1 = new String[]{var0.ca_cert.value(), var0.client_cert.value(), var0.private_key.value()};
         int var2 = var1.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            String var4 = var1[var3];
            if(var4 != null && var4.startsWith("keystore://")) {
               return true;
            }
         }
      }

      return false;
   }

   private void setCertificate(Spinner var1, String var2, String var3) {
      String var4 = "keystore://" + var2;
      if(var3 != null && var3.startsWith(var4)) {
         this.setSelection(var1, var3.substring(var4.length()));
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

   private void showIpConfigFields() {
      this.mView.findViewById(2131231174).setVisibility(0);
      AccessPoint var1 = this.mAccessPoint;
      WifiConfiguration var2 = null;
      if(var1 != null) {
         int var9 = this.mAccessPoint.networkId;
         var2 = null;
         if(var9 != -1) {
            var2 = this.mAccessPoint.getConfig();
         }
      }

      if(this.mIpSettingsSpinner.getSelectedItemPosition() == 1) {
         this.mView.findViewById(2131231176).setVisibility(0);
         if(this.mIpAddressView == null) {
            this.mIpAddressView = (TextView)this.mView.findViewById(2131231177);
            this.mIpAddressView.addTextChangedListener(this);
            this.mGatewayView = (TextView)this.mView.findViewById(2131231178);
            this.mGatewayView.addTextChangedListener(this);
            this.mNetworkPrefixLengthView = (TextView)this.mView.findViewById(2131231179);
            this.mNetworkPrefixLengthView.addTextChangedListener(this);
            this.mDns1View = (TextView)this.mView.findViewById(2131231180);
            this.mDns1View.addTextChangedListener(this);
            this.mDns2View = (TextView)this.mView.findViewById(2131231181);
            this.mDns2View.addTextChangedListener(this);
         }

         if(var2 != null) {
            LinkProperties var3 = var2.linkProperties;
            Iterator var4 = var3.getLinkAddresses().iterator();
            if(var4.hasNext()) {
               LinkAddress var8 = (LinkAddress)var4.next();
               this.mIpAddressView.setText(var8.getAddress().getHostAddress());
               this.mNetworkPrefixLengthView.setText(Integer.toString(var8.getNetworkPrefixLength()));
            }

            Iterator var5 = var3.getRoutes().iterator();

            while(var5.hasNext()) {
               RouteInfo var7 = (RouteInfo)var5.next();
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
         }

      } else {
         this.mView.findViewById(2131231176).setVisibility(8);
      }
   }

   private void showNetworkSetupFields() {
      this.mView.findViewById(2131231155).setVisibility(0);
      if(this.mNetworkSetupSpinner == null) {
         this.mNetworkSetupSpinner = (Spinner)this.mView.findViewById(2131231156);
         this.mNetworkSetupSpinner.setOnItemSelectedListener(this);
      }

      int var1 = this.mNetworkSetupSpinner.getSelectedItemPosition();
      if(var1 == 2) {
         this.mView.findViewById(2131231157).setVisibility(0);
      } else {
         this.mView.findViewById(2131231157).setVisibility(8);
      }

      if(var1 != 3 && var1 != 2 && var1 != 1) {
         this.mView.findViewById(2131231159).setVisibility(0);
      } else {
         this.mView.findViewById(2131231159).setVisibility(8);
      }
   }

   private void showProxyFields() {
      this.mView.findViewById(2131231167).setVisibility(0);
      AccessPoint var1 = this.mAccessPoint;
      WifiConfiguration var2 = null;
      if(var1 != null) {
         int var4 = this.mAccessPoint.networkId;
         var2 = null;
         if(var4 != -1) {
            var2 = this.mAccessPoint.getConfig();
         }
      }

      if(this.mProxySettingsSpinner.getSelectedItemPosition() == 1) {
         this.mView.findViewById(2131231169).setVisibility(0);
         this.mView.findViewById(2131231170).setVisibility(0);
         if(this.mProxyHostView == null) {
            this.mProxyHostView = (TextView)this.mView.findViewById(2131231171);
            this.mProxyHostView.addTextChangedListener(this);
            this.mProxyPortView = (TextView)this.mView.findViewById(2131231172);
            this.mProxyPortView.addTextChangedListener(this);
            this.mProxyExclusionListView = (TextView)this.mView.findViewById(2131231173);
            this.mProxyExclusionListView.addTextChangedListener(this);
         }

         if(var2 != null) {
            ProxyProperties var3 = var2.linkProperties.getHttpProxy();
            if(var3 != null) {
               this.mProxyHostView.setText(var3.getHost());
               this.mProxyPortView.setText(Integer.toString(var3.getPort()));
               this.mProxyExclusionListView.setText(var3.getExclusionList());
            }
         }

      } else {
         this.mView.findViewById(2131231169).setVisibility(8);
         this.mView.findViewById(2131231170).setVisibility(8);
      }
   }

   private void showSecurityFields() {
      if(!this.mInXlSetupWizard || ((WifiSettingsForSetupWizardXL)this.mConfigUi.getContext()).initSecurityFields(this.mView, this.mAccessPointSecurity)) {
         if(this.mAccessPointSecurity == 0) {
            this.mView.findViewById(2131231159).setVisibility(8);
            return;
         }

         this.mView.findViewById(2131231159).setVisibility(0);
         if(this.mPasswordView == null) {
            this.mPasswordView = (TextView)this.mView.findViewById(2131231131);
            this.mPasswordView.addTextChangedListener(this);
            ((CheckBox)this.mView.findViewById(2131231153)).setOnClickListener(this);
            if(this.mAccessPoint != null && this.mAccessPoint.networkId != -1) {
               this.mPasswordView.setHint(2131427806);
            }
         }

         if(this.mAccessPointSecurity != 3) {
            this.mView.findViewById(2131231160).setVisibility(8);
            return;
         }

         this.mView.findViewById(2131231160).setVisibility(0);
         if(this.mEapMethodSpinner == null) {
            this.mEapMethodSpinner = (Spinner)this.mView.findViewById(2131231161);
            this.mPhase2Spinner = (Spinner)this.mView.findViewById(2131231162);
            this.mEapCaCertSpinner = (Spinner)this.mView.findViewById(2131231163);
            this.mEapUserCertSpinner = (Spinner)this.mView.findViewById(2131231164);
            this.mEapIdentityView = (TextView)this.mView.findViewById(2131231165);
            this.mEapAnonymousView = (TextView)this.mView.findViewById(2131231166);
            this.loadCertificates(this.mEapCaCertSpinner, "CACERT_");
            this.loadCertificates(this.mEapUserCertSpinner, "USRPKEY_");
            if(this.mAccessPoint != null && this.mAccessPoint.networkId != -1) {
               WifiConfiguration var1 = this.mAccessPoint.getConfig();
               this.setSelection(this.mEapMethodSpinner, var1.eap.value());
               this.setSelection(this.mPhase2Spinner, var1.phase2.value());
               this.setCertificate(this.mEapCaCertSpinner, "CACERT_", var1.ca_cert.value());
               this.setCertificate(this.mEapUserCertSpinner, "USRPKEY_", var1.private_key.value());
               this.mEapIdentityView.setText(var1.identity.value());
               this.mEapAnonymousView.setText(var1.anonymous_identity.value());
               return;
            }
         }
      }

   }

   private int validateIpConfigFields(LinkProperties var1) {
      String var2 = this.mIpAddressView.getText().toString();

      InetAddress var4;
      try {
         var4 = NetworkUtils.numericToInetAddress(var2);
      } catch (IllegalArgumentException var20) {
         return 2131427846;
      }

      int var5 = -1;

      label51: {
         int var16;
         try {
            var16 = Integer.parseInt(this.mNetworkPrefixLengthView.getText().toString());
         } catch (NumberFormatException var21) {
            break label51;
         }

         var5 = var16;
      }

      if(var5 >= 0 && var5 <= 32) {
         var1.addLinkAddress(new LinkAddress(var4, var5));
         String var7 = this.mGatewayView.getText().toString();

         InetAddress var9;
         try {
            var9 = NetworkUtils.numericToInetAddress(var7);
         } catch (IllegalArgumentException var19) {
            return 2131427847;
         }

         var1.addRoute(new RouteInfo(var9));
         String var10 = this.mDns1View.getText().toString();

         InetAddress var12;
         try {
            var12 = NetworkUtils.numericToInetAddress(var10);
         } catch (IllegalArgumentException var18) {
            return 2131427848;
         }

         var1.addDns(var12);
         if(this.mDns2View.length() > 0) {
            String var13 = this.mDns2View.getText().toString();

            InetAddress var15;
            try {
               var15 = NetworkUtils.numericToInetAddress(var13);
            } catch (IllegalArgumentException var17) {
               return 2131427848;
            }

            var1.addDns(var15);
         }

         return 0;
      } else {
         return 2131427849;
      }
   }

   public void afterTextChanged(Editable var1) {
      this.enableSubmitIfAppropriate();
   }

   public void beforeTextChanged(CharSequence var1, int var2, int var3, int var4) {}

   int chosenNetworkSetupMethod() {
      return this.mNetworkSetupSpinner != null?this.mNetworkSetupSpinner.getSelectedItemPosition():0;
   }

   WifiConfiguration getConfig() {
      if(this.mAccessPoint != null && this.mAccessPoint.networkId != -1 && !this.mEdit) {
         return null;
      } else {
         WifiConfiguration var1 = new WifiConfiguration();
         if(this.mAccessPoint == null) {
            var1.SSID = AccessPoint.convertToQuotedString(this.mSsidView.getText().toString());
            var1.hiddenSSID = true;
         } else if(this.mAccessPoint.networkId == -1) {
            var1.SSID = AccessPoint.convertToQuotedString(this.mAccessPoint.ssid);
         } else {
            var1.networkId = this.mAccessPoint.networkId;
         }

         switch(this.mAccessPointSecurity) {
         case 0:
            var1.allowedKeyManagement.set(0);
            break;
         case 1:
            var1.allowedKeyManagement.set(0);
            var1.allowedAuthAlgorithms.set(0);
            var1.allowedAuthAlgorithms.set(1);
            if(this.mPasswordView.length() != 0) {
               int var15 = this.mPasswordView.length();
               String var16 = this.mPasswordView.getText().toString();
               if((var15 == 10 || var15 == 26 || var15 == 58) && var16.matches("[0-9A-Fa-f]*")) {
                  var1.wepKeys[0] = var16;
               } else {
                  var1.wepKeys[0] = '\"' + var16 + '\"';
               }
            }
            break;
         case 2:
            var1.allowedKeyManagement.set(1);
            if(this.mPasswordView.length() != 0) {
               String var14 = this.mPasswordView.getText().toString();
               if(var14.matches("[0-9A-Fa-f]{64}")) {
                  var1.preSharedKey = var14;
               } else {
                  var1.preSharedKey = '\"' + var14 + '\"';
               }
            }
            break;
         case 3:
            var1.allowedKeyManagement.set(2);
            var1.allowedKeyManagement.set(3);
            var1.eap.setValue((String)this.mEapMethodSpinner.getSelectedItem());
            EnterpriseField var2 = var1.phase2;
            String var3;
            if(this.mPhase2Spinner.getSelectedItemPosition() == 0) {
               var3 = "";
            } else {
               var3 = "auth=" + this.mPhase2Spinner.getSelectedItem();
            }

            var2.setValue(var3);
            EnterpriseField var4 = var1.ca_cert;
            String var5;
            if(this.mEapCaCertSpinner.getSelectedItemPosition() == 0) {
               var5 = "";
            } else {
               var5 = "keystore://CACERT_" + (String)this.mEapCaCertSpinner.getSelectedItem();
            }

            var4.setValue(var5);
            EnterpriseField var6 = var1.client_cert;
            String var7;
            if(this.mEapUserCertSpinner.getSelectedItemPosition() == 0) {
               var7 = "";
            } else {
               var7 = "keystore://USRCERT_" + (String)this.mEapUserCertSpinner.getSelectedItem();
            }

            var6.setValue(var7);
            EnterpriseField var8 = var1.private_key;
            String var9;
            if(this.mEapUserCertSpinner.getSelectedItemPosition() == 0) {
               var9 = "";
            } else {
               var9 = "keystore://USRPKEY_" + (String)this.mEapUserCertSpinner.getSelectedItem();
            }

            var8.setValue(var9);
            EnterpriseField var10 = var1.identity;
            String var11;
            if(this.mEapIdentityView.length() == 0) {
               var11 = "";
            } else {
               var11 = this.mEapIdentityView.getText().toString();
            }

            var10.setValue(var11);
            EnterpriseField var12 = var1.anonymous_identity;
            String var13;
            if(this.mEapAnonymousView.length() == 0) {
               var13 = "";
            } else {
               var13 = this.mEapAnonymousView.getText().toString();
            }

            var12.setValue(var13);
            if(this.mPasswordView.length() != 0) {
               var1.password.setValue(this.mPasswordView.getText().toString());
            }
            break;
         default:
            return null;
         }

         var1.proxySettings = this.mProxySettings;
         var1.ipAssignment = this.mIpAssignment;
         var1.linkProperties = new LinkProperties(this.mLinkProperties);
         return var1;
      }
   }

   WpsInfo getWpsConfig() {
      WpsInfo var1 = new WpsInfo();
      switch(this.mNetworkSetupSpinner.getSelectedItemPosition()) {
      case 1:
         var1.setup = 0;
         break;
      case 2:
         var1.setup = 2;
         break;
      case 3:
         var1.setup = 1;
         break;
      default:
         var1.setup = 4;
         Log.e("WifiConfigController", "WPS not selected type");
         return var1;
      }

      var1.pin = ((TextView)this.mView.findViewById(2131231158)).getText().toString();
      String var2;
      if(this.mAccessPoint != null) {
         var2 = this.mAccessPoint.bssid;
      } else {
         var2 = null;
      }

      var1.BSSID = var2;
      var1.proxySettings = this.mProxySettings;
      var1.ipAssignment = this.mIpAssignment;
      var1.linkProperties = new LinkProperties(this.mLinkProperties);
      return var1;
   }

   public boolean isEdit() {
      return this.mEdit;
   }

   public void onClick(View var1) {
      if(var1.getId() == 2131231153) {
         TextView var2 = this.mPasswordView;
         short var3;
         if(((CheckBox)var1).isChecked()) {
            var3 = 144;
         } else {
            var3 = 128;
         }

         var2.setInputType(var3 | 1);
      } else if(var1.getId() == 2131231193) {
         if(((CheckBox)var1).isChecked()) {
            this.mView.findViewById(2131231194).setVisibility(0);
            return;
         }

         this.mView.findViewById(2131231194).setVisibility(8);
         return;
      }

   }

   public void onItemSelected(AdapterView var1, View var2, int var3, long var4) {
      if(var1 == this.mSecuritySpinner) {
         this.mAccessPointSecurity = var3;
         this.showSecurityFields();
      } else if(var1 == this.mNetworkSetupSpinner) {
         this.showNetworkSetupFields();
      } else if(var1 == this.mProxySettingsSpinner) {
         this.showProxyFields();
      } else {
         this.showIpConfigFields();
      }

      this.enableSubmitIfAppropriate();
   }

   public void onNothingSelected(AdapterView var1) {}

   public void onTextChanged(CharSequence var1, int var2, int var3, int var4) {}
}
