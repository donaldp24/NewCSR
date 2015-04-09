package com.general.mediaplayer.csr.wifi;

import android.app.Activity;
import android.content.Intent;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.general.mediaplayer.csr.wifi.AccessPoint;
import com.general.mediaplayer.csr.wifi.Summary;
import com.general.mediaplayer.csr.wifi.WifiConfigUiForSetupWizardXL;
import com.general.mediaplayer.csr.wifi.WifiSettings;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Iterator;
import com.general.mediaplayer.csr.R;

public class WifiSettingsForSetupWizardXL extends Activity implements OnClickListener {

   private static final boolean DEBUG = true;
   private static final String EXTRA_PREFS_LANDSCAPE_LOCK = "extra_prefs_landscape_lock";
   private static final String EXTRA_PREFS_PORTRAIT_LOCK = "extra_prefs_portrait_lock";
   private static final int SCREEN_STATE_CONNECTED = 3;
   private static final int SCREEN_STATE_CONNECTING = 2;
   private static final int SCREEN_STATE_DISCONNECTED = 0;
   private static final int SCREEN_STATE_EDITING = 1;
   private static final String TAG = "SetupWizard";
   private static final EnumMap sNetworkStateMap = new EnumMap(DetailedState.class);
   private Button mAddNetworkButton;
   private Button mBackButton;
   private View mBottomPadding;
   private Button mConnectButton;
   private View mConnectingStatusLayout;
   private TextView mConnectingStatusView;
   private View mContentPadding;
   private CharSequence mEditingTitle;
   private InputMethodManager mInputMethodManager;
   private CharSequence mNetworkName = "";
   private DetailedState mPreviousNetworkState;
   private ProgressBar mProgressBar;
   private Button mRefreshButton;
   private int mScreenState = 0;
   private Button mSkipOrNextButton;
   private TextView mTitleView;
   private View mTopDividerNoProgress;
   private View mTopPadding;
   private WifiConfigUiForSetupWizardXL mWifiConfig;
   private WifiManager mWifiManager;
   private WifiSettings mWifiSettings;
   private View mWifiSettingsFragmentLayout;


   static {
      sNetworkStateMap.put(DetailedState.IDLE, DetailedState.DISCONNECTED);
      sNetworkStateMap.put(DetailedState.SCANNING, DetailedState.SCANNING);
      sNetworkStateMap.put(DetailedState.CONNECTING, DetailedState.CONNECTING);
      sNetworkStateMap.put(DetailedState.AUTHENTICATING, DetailedState.CONNECTING);
      sNetworkStateMap.put(DetailedState.OBTAINING_IPADDR, DetailedState.CONNECTING);
      sNetworkStateMap.put(DetailedState.CONNECTED, DetailedState.CONNECTED);
      sNetworkStateMap.put(DetailedState.SUSPENDED, DetailedState.SUSPENDED);
      sNetworkStateMap.put(DetailedState.DISCONNECTING, DetailedState.DISCONNECTED);
      sNetworkStateMap.put(DetailedState.DISCONNECTED, DetailedState.DISCONNECTED);
      sNetworkStateMap.put(DetailedState.FAILED, DetailedState.FAILED);
   }

   public WifiSettingsForSetupWizardXL() {
      this.mPreviousNetworkState = DetailedState.DISCONNECTED;
   }

   private void hideSoftwareKeyboard() {
      Log.i("SetupWizard", "Hiding software keyboard.");
      View var2 = this.getCurrentFocus();
      if(var2 != null) {
         this.mInputMethodManager.hideSoftInputFromWindow(var2.getWindowToken(), 0);
      }

   }

   private void initViews() {
      Intent var1 = this.getIntent();
      if(var1.getBooleanExtra("firstRun", false)) {
         this.findViewById(R.id.layout_root).setSystemUiVisibility(4194304);
      }

      if(var1.getBooleanExtra("extra_prefs_landscape_lock", false)) {
         this.setRequestedOrientation(6);
      }

      if(var1.getBooleanExtra("extra_prefs_portrait_lock", false)) {
         this.setRequestedOrientation(7);
      }

      this.mTitleView = (TextView)this.findViewById(R.id.wifi_setup_title);
      this.mProgressBar = (ProgressBar)this.findViewById(R.id.scanning_progress_bar);
      this.mProgressBar.setMax(2);
      this.mTopDividerNoProgress = this.findViewById(R.id.top_divider_no_progress);
      this.mBottomPadding = this.findViewById(R.id.bottom_padding);
      this.mProgressBar.setVisibility(0);
      this.mProgressBar.setIndeterminate(true);
      this.mTopDividerNoProgress.setVisibility(8);
      this.mAddNetworkButton = (Button)this.findViewById(R.id.wifi_setup_add_network);
      this.mAddNetworkButton.setOnClickListener(this);
      this.mRefreshButton = (Button)this.findViewById(R.id.wifi_setup_refresh_list);
      this.mRefreshButton.setOnClickListener(this);
      this.mSkipOrNextButton = (Button)this.findViewById(R.id.wifi_setup_skip_or_next);
      this.mSkipOrNextButton.setOnClickListener(this);
      this.mConnectButton = (Button)this.findViewById(R.id.wifi_setup_connect);
      this.mConnectButton.setOnClickListener(this);
      this.mBackButton = (Button)this.findViewById(R.id.wifi_setup_cancel);
      this.mBackButton.setOnClickListener(this);
      this.mTopPadding = this.findViewById(R.id.top_padding);
      this.mContentPadding = this.findViewById(R.id.content_padding);
      this.mWifiSettingsFragmentLayout = this.findViewById(R.id.wifi_settings_fragment_layout);
      this.mConnectingStatusLayout = this.findViewById(R.id.connecting_status_layout);
      this.mConnectingStatusView = (TextView)this.findViewById(R.id.connecting_status);
   }

   private void onAddNetworkButtonPressed() {
      this.mWifiSettings.onAddNetworkPressed();
   }

   private void onAuthenticationFailure() {
      this.mScreenState = 1;
      this.mSkipOrNextButton.setVisibility(8);
      this.mConnectButton.setVisibility(0);
      this.mConnectButton.setEnabled(true);
      if(!TextUtils.isEmpty(this.mEditingTitle)) {
         this.mTitleView.setText(this.mEditingTitle);
      } else {
         Log.w("SetupWizard", "Title during editing/adding a network was empty.");
         this.showEditingTitle();
      }

      ((ViewGroup)this.findViewById(R.id.wifi_config_ui)).setVisibility(0);
      this.mConnectingStatusLayout.setVisibility(8);
      this.showDisconnectedProgressBar();
      this.setPaddingVisibility(8);
   }

   private void onBackButtonPressed() {
      if(this.mScreenState != 2 && this.mScreenState != 3) {
         this.mScreenState = 0;
         this.mWifiSettings.resumeWifiScan();
         this.restoreFirstVisibilityState();
         this.mAddNetworkButton.setEnabled(true);
         this.mRefreshButton.setEnabled(true);
         this.mSkipOrNextButton.setEnabled(true);
         this.showDisconnectedProgressBar();
         this.mWifiSettingsFragmentLayout.setVisibility(0);
         this.mBottomPadding.setVisibility(8);
      } else {
         Log.d("SetupWizard", "Back button pressed after connect action.");
         this.mScreenState = 0;
         this.restoreFirstVisibilityState();
         this.mSkipOrNextButton.setEnabled(true);
         this.changeNextButtonState(false);
         this.showScanningState();
         Iterator var2 = this.mWifiManager.getConfiguredNetworks().iterator();

         while(var2.hasNext()) {
            WifiConfiguration var4 = (WifiConfiguration)var2.next();
            Object[] var5 = new Object[]{var4.SSID, Integer.valueOf(var4.networkId)};
            Log.d("SetupWizard", String.format("forgeting Wi-Fi network \"%s\" (id: %d)", var5));
            this.mWifiManager.forgetNetwork(var4.networkId);
         }

         this.mWifiSettingsFragmentLayout.setVisibility(8);
         this.refreshAccessPoints(true);
      }

      this.setPaddingVisibility(0);
      this.mConnectingStatusLayout.setVisibility(8);
      ViewGroup var3 = (ViewGroup)this.findViewById(R.id.wifi_config_ui);
      var3.removeAllViews();
      var3.setVisibility(8);
      this.mWifiConfig = null;
   }

   private void onEapNetworkSelected() {
      this.mConnectButton.setVisibility(8);
      this.mBackButton.setText(R.string.wifi_setup_back);
   }

   private void refreshAccessPoints(boolean var1) {
      this.showScanningState();
      if(var1) {
         this.mWifiManager.disconnect();
      }

      this.mWifiSettings.refreshAccessPoints();
   }

   private void restoreFirstVisibilityState() {
      this.showDefaultTitle();
      this.mAddNetworkButton.setVisibility(0);
      this.mRefreshButton.setVisibility(0);
      this.mSkipOrNextButton.setVisibility(0);
      this.mConnectButton.setVisibility(8);
      this.mBackButton.setVisibility(8);
      this.setPaddingVisibility(0);
   }

   private void showAddNetworkTitle() {
      this.mNetworkName = "";
      this.mTitleView.setText(R.string.wifi_setup_title_add_network);
   }

   private void showConnectedProgressBar() {
      this.showTopDividerWithProgressBar();
      this.mProgressBar.setIndeterminate(false);
      this.mProgressBar.setProgress(2);
   }

   private void showConnectedState() {
      this.mScreenState = 3;
      this.hideSoftwareKeyboard();
      this.setPaddingVisibility(0);
      this.showConnectedTitle();
      this.showConnectedProgressBar();
      this.mWifiSettingsFragmentLayout.setVisibility(8);
      this.mConnectingStatusLayout.setVisibility(0);
      this.mConnectingStatusView.setText(R.string.wifi_setup_description_connected);
      this.mConnectButton.setVisibility(8);
      this.mAddNetworkButton.setVisibility(8);
      this.mRefreshButton.setVisibility(8);
      this.mBackButton.setVisibility(0);
      this.mBackButton.setText(R.string.wifi_setup_back);
      this.mSkipOrNextButton.setVisibility(0);
      this.mSkipOrNextButton.setEnabled(true);
   }

   private void showConnectedTitle() {
      if(TextUtils.isEmpty(this.mNetworkName) && this.mWifiConfig != null) {
         if(this.mWifiConfig.getController() != null && this.mWifiConfig.getController().getConfig() != null) {
            this.mNetworkName = this.mWifiConfig.getController().getConfig().SSID;
         } else {
            Log.w("SetupWizard", "Unexpected null found (WifiController or WifiConfig is null). Ignore them.");
         }
      }

      TextView var1 = this.mTitleView;
      Object[] var2 = new Object[]{this.mNetworkName};
      var1.setText(this.getString(R.string.wifi_setup_title_connected_network, var2));
   }

   private void showConnectingProgressBar() {
      this.showTopDividerWithProgressBar();
      this.mProgressBar.setIndeterminate(false);
      this.mProgressBar.setProgress(1);
   }

   private void showConnectingState() {
      this.mScreenState = 2;
      this.mBackButton.setVisibility(0);
      this.mEditingTitle = this.mTitleView.getText();
      this.showConnectingTitle();
      this.showConnectingProgressBar();
      this.setPaddingVisibility(0);
   }

   private void showConnectingTitle() {
      if(TextUtils.isEmpty(this.mNetworkName) && this.mWifiConfig != null) {
         if(this.mWifiConfig.getController() != null && this.mWifiConfig.getController().getConfig() != null) {
            this.mNetworkName = this.mWifiConfig.getController().getConfig().SSID;
         } else {
            Log.w("SetupWizard", "Unexpected null found (WifiController or WifiConfig is null). Ignore them.");
         }
      }

      TextView var1 = this.mTitleView;
      Object[] var2 = new Object[]{this.mNetworkName};
      var1.setText(this.getString(R.string.wifi_setup_title_connecting_network, var2));
   }

   private void showDefaultTitle() {
      this.mTitleView.setText(this.getString(R.string.wifi_setup_title));
   }

   private void showDisconnectedProgressBar() {
      if(this.mScreenState == 0) {
         this.mProgressBar.setVisibility(8);
         this.mProgressBar.setIndeterminate(false);
         this.mTopDividerNoProgress.setVisibility(0);
      } else {
         this.mProgressBar.setVisibility(0);
         this.mProgressBar.setIndeterminate(false);
         this.mProgressBar.setProgress(0);
         this.mTopDividerNoProgress.setVisibility(8);
      }
   }

   private void showDisconnectedState(String var1) {
      this.showDisconnectedProgressBar();
      if(this.mScreenState == 0 && this.mWifiSettings.getAccessPointsCount() > 0) {
         this.mWifiSettingsFragmentLayout.setVisibility(0);
         this.mBottomPadding.setVisibility(8);
      }

      this.mAddNetworkButton.setEnabled(true);
      this.mRefreshButton.setEnabled(true);
   }

   private void showEditingButtonState() {
      this.mSkipOrNextButton.setVisibility(8);
      this.mAddNetworkButton.setVisibility(8);
      this.mRefreshButton.setVisibility(8);
      this.mBackButton.setVisibility(0);
   }

   private void showEditingTitle() {
      if(TextUtils.isEmpty(this.mNetworkName) && this.mWifiConfig != null) {
         if(this.mWifiConfig.getController() != null && this.mWifiConfig.getController().getConfig() != null) {
            this.mNetworkName = this.mWifiConfig.getController().getConfig().SSID;
         } else {
            Log.w("SetupWizard", "Unexpected null found (WifiController or WifiConfig is null). Ignore them.");
         }
      }

      TextView var1 = this.mTitleView;
      Object[] var2 = new Object[]{this.mNetworkName};
      var1.setText(this.getString(R.string.wifi_setup_title_editing_network, var2));
   }

   private void showScanningProgressBar() {
      this.showTopDividerWithProgressBar();
      this.mProgressBar.setIndeterminate(true);
   }

   private void showScanningState() {
      this.setPaddingVisibility(0);
      this.mWifiSettingsFragmentLayout.setVisibility(8);
      this.showScanningProgressBar();
   }

   private void showTopDividerWithProgressBar() {
      this.mProgressBar.setVisibility(0);
      this.mTopDividerNoProgress.setVisibility(8);
      this.mBottomPadding.setVisibility(8);
   }

   void changeNextButtonState(boolean var1) {
      if(var1) {
         this.mSkipOrNextButton.setText(R.string.wifi_setup_next);
      } else {
         this.mSkipOrNextButton.setText(R.string.wifi_setup_skip);
      }
   }

   boolean initSecurityFields(View var1, int var2) {
      var1.findViewById(R.id.eap_not_supported).setVisibility(8);
      var1.findViewById(R.id.eap_not_supported_for_add_network).setVisibility(8);
      var1.findViewById(R.id.ssid_text).setVisibility(0);
      var1.findViewById(R.id.ssid_layout).setVisibility(0);
      if(var2 == 3) {
         this.setPaddingVisibility(0);
         this.hideSoftwareKeyboard();
         if(var1.findViewById(R.id.type_ssid).getVisibility() == 0) {
            var1.findViewById(R.id.eap_not_supported_for_add_network).setVisibility(0);
         } else {
            var1.findViewById(R.id.eap_not_supported).setVisibility(0);
         }

         var1.findViewById(R.id.security_fields).setVisibility(8);
         var1.findViewById(R.id.ssid_text).setVisibility(8);
         var1.findViewById(R.id.ssid_layout).setVisibility(8);
         this.onEapNetworkSelected();
         return false;
      } else {
         this.mConnectButton.setVisibility(0);
         this.setPaddingVisibility(8);
         if(this.mWifiConfig != null) {
            if(var2 != 2 && var2 != 1) {
               this.mWifiConfig.requestFocusAndShowKeyboard(R.id.ssid);
            } else {
               this.mWifiConfig.requestFocusAndShowKeyboard(R.id.password);
            }
         }

         return true;
      }
   }

   void onAccessPointsUpdated(PreferenceScreen var1, Collection var2) {
      if(this.mProgressBar.isIndeterminate() && var2.size() > 0) {
         this.showDisconnectedProgressBar();
         if(this.mScreenState == 0) {
            this.mWifiSettingsFragmentLayout.setVisibility(0);
            this.mBottomPadding.setVisibility(8);
         }

         this.mAddNetworkButton.setEnabled(true);
         this.mRefreshButton.setEnabled(true);
      }

      Iterator var3 = var2.iterator();

      while(var3.hasNext()) {
         AccessPoint var4 = (AccessPoint)var3.next();
         var4.setLayoutResource(R.layout.custom_preference);
         var1.addPreference(var4);
      }

   }

   public void onClick(View var1) {
      this.hideSoftwareKeyboard();
      if(var1 == this.mAddNetworkButton) {
         Log.d("SetupWizard", "AddNetwork button pressed");
         this.onAddNetworkButtonPressed();
      } else {
         if(var1 == this.mRefreshButton) {
            Log.d("SetupWizard", "Refresh button pressed");
            this.refreshAccessPoints(true);
            return;
         }

         if(var1 == this.mSkipOrNextButton) {
            Log.d("SetupWizard", "Skip/Next button pressed");
            if(TextUtils.equals(this.getString(R.string.wifi_setup_skip), ((Button)var1).getText())) {
               this.mWifiManager.setWifiEnabled(false);
               this.setResult(1);
            } else {
               this.setResult(-1);
            }

            this.finish();
            return;
         }

         if(var1 == this.mConnectButton) {
            Log.d("SetupWizard", "Connect button pressed");
            this.onConnectButtonPressed();
            return;
         }

         if(var1 == this.mBackButton) {
            Log.d("SetupWizard", "Back button pressed");
            this.onBackButtonPressed();
            return;
         }
      }

   }

   void onConnectButtonPressed() {
      this.mScreenState = 2;
      this.mWifiSettings.submit(this.mWifiConfig.getController());
      this.showConnectingState();
      this.mBackButton.setVisibility(0);
      this.mBackButton.setText(R.string.wifi_setup_back);
      ((ViewGroup)this.findViewById(R.id.wifi_config_ui)).setVisibility(8);
      this.mConnectingStatusLayout.setVisibility(0);
      this.mConnectingStatusView.setText(R.string.wifi_setup_description_connecting);
      this.mSkipOrNextButton.setVisibility(0);
      this.mSkipOrNextButton.setEnabled(false);
      this.mConnectButton.setVisibility(8);
      this.mAddNetworkButton.setVisibility(8);
      this.mRefreshButton.setVisibility(8);
   }

   public void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.requestWindowFeature(1);
      this.setContentView(R.layout.wifi_settings_for_setup_wizard_xl);
      this.mWifiManager = (WifiManager)this.getSystemService("wifi");
      this.mWifiManager.setWifiEnabled(true);
      this.mWifiManager.asyncConnect(this, new WifiServiceHandler(null));
      this.mWifiSettings = (WifiSettings)this.getFragmentManager().findFragmentById(R.id.wifi_setup_fragment);
      this.mInputMethodManager = (InputMethodManager)this.getSystemService("input_method");
      this.initViews();
      this.showScanningState();
   }

   void onSaveNetwork(WifiConfiguration var1) {
      this.mWifiManager.connectNetwork(var1);
   }

   void onSupplicantStateChanged(Intent var1) {
      if(var1.getIntExtra("supplicantError", -1) == 1) {
         Log.i("SetupWizard", "Received authentication error event.");
         this.onAuthenticationFailure();
      }

   }

   void setPaddingVisibility(int var1) {
      this.mTopPadding.setVisibility(var1);
      this.mContentPadding.setVisibility(var1);
   }

   void showConfigUi(AccessPoint var1, boolean var2) {
      this.mScreenState = 1;
      if(var1 != null && (var1.security == 1 || var1.security == 2)) {
         var2 = true;
      }

      this.mWifiSettings.pauseWifiScan();
      this.mWifiSettingsFragmentLayout.setVisibility(8);
      this.mConnectingStatusLayout.setVisibility(8);
      ViewGroup var3 = (ViewGroup)this.findViewById(R.id.wifi_config_ui);
      var3.setVisibility(0);
      var3.removeAllViews();
      this.mWifiConfig = new WifiConfigUiForSetupWizardXL(this, var3, var1, var2);
      if(var1 == null) {
         this.showAddNetworkTitle();
         this.mConnectButton.setVisibility(0);
         this.showDisconnectedProgressBar();
         this.showEditingButtonState();
      } else if(var1.security == 0) {
         this.mNetworkName = var1.getTitle().toString();
         this.mConnectButton.performClick();
      } else {
         this.mNetworkName = var1.getTitle().toString();
         this.showEditingTitle();
         this.showDisconnectedProgressBar();
         this.showEditingButtonState();
         if(var1.security == 3) {
            this.onEapNetworkSelected();
         } else {
            this.mConnectButton.setVisibility(0);
            this.mConnectButton.setText(R.string.wifi_connect);
            this.mBackButton.setText(R.string.wifi_setup_cancel);
         }
      }
   }

   void updateConnectionState(DetailedState var1) {
      DetailedState var2 = (DetailedState)sNetworkStateMap.get(var1);
      if(var1 == DetailedState.FAILED) {
         this.refreshAccessPoints(true);
      }

      switch(null.$SwitchMap$android$net$NetworkInfo$DetailedState[var2.ordinal()]) {
      case 1:
         if(this.mScreenState == 0) {
            if(this.mWifiSettings.getAccessPointsCount() == 0) {
               this.showScanningState();
            } else {
               this.showDisconnectedProgressBar();
               this.mWifiSettingsFragmentLayout.setVisibility(0);
               this.mBottomPadding.setVisibility(8);
            }
         } else {
            this.showDisconnectedProgressBar();
         }
         break;
      case 2:
         if(this.mScreenState == 2) {
            this.showConnectingState();
         }
         break;
      case 3:
         this.showConnectedState();
         break;
      default:
         if(this.mScreenState != 3 && this.mWifiSettings.getAccessPointsCount() > 0) {
            this.showDisconnectedState(Summary.get(this, var2));
         }
      }

      this.mPreviousNetworkState = var2;
   }

   private class WifiServiceHandler extends Handler {

      private WifiServiceHandler() {}

      // $FF: synthetic method
      WifiServiceHandler(Object var2) {
         this();
      }

      public void handleMessage(Message var1) {
         switch(var1.what) {
         case 69632:
            if(var1.arg1 != 0) {
               Log.e("SetupWizard", "Failed to establish AsyncChannel connection");
               return;
            }
         default:
         }
      }
   }
}
