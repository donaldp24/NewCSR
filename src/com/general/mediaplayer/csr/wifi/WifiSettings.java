package com.general.mediaplayer.csr.wifi;

import android.app.Activity;
import android.app.Dialog;
import android.app.ActionBar.LayoutParams;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsResult;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.security.Credentials;
import android.security.KeyStore;
import android.security.KeyStore.State;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;
import com.general.mediaplayer.csr.SettingsPreferenceFragment;
import com.general.mediaplayer.csr.wifi.AccessPoint;
import com.general.mediaplayer.csr.wifi.AdvancedWifiSettings;
import com.general.mediaplayer.csr.wifi.WifiConfigController;
import com.general.mediaplayer.csr.wifi.WifiDialog;
import com.general.mediaplayer.csr.wifi.WifiEnabler;
import com.general.mediaplayer.csr.wifi.WifiSettingsForSetupWizardXL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WifiSettings extends SettingsPreferenceFragment implements OnClickListener {

   private static final String EXTRA_ENABLE_NEXT_ON_CONNECT = "wifi_enable_next_on_connect";
   private static final int MENU_ID_ADD_NETWORK = 2;
   private static final int MENU_ID_ADVANCED = 3;
   private static final int MENU_ID_CONNECT = 4;
   private static final int MENU_ID_FORGET = 5;
   private static final int MENU_ID_MODIFY = 6;
   private static final int MENU_ID_SCAN = 1;
   private static final String SAVE_DIALOG_ACCESS_POINT_STATE = "wifi_ap_state";
   private static final String SAVE_DIALOG_EDIT_MODE = "edit_mode";
   private static final String TAG = "WifiSettings";
   private static final int WIFI_DIALOG_ID = 1;
   private static final int WIFI_RESCAN_INTERVAL_MS = 10000;
   private Bundle mAccessPointSavedState;
   private AtomicBoolean mConnected = new AtomicBoolean(false);
   private WifiDialog mDialog;
   private AccessPoint mDlgAccessPoint;
   private boolean mDlgEdit;
   private TextView mEmptyView;
   private boolean mEnableNextOnConnection;
   private final IntentFilter mFilter = new IntentFilter();
   private boolean mInXlSetupWizard;
   private int mKeyStoreNetworkId = -1;
   private android.net.wifi.WifiInfo mLastInfo;
   private DetailedState mLastState;
   private final BroadcastReceiver mReceiver;
   private final Scanner mScanner;
   private AccessPoint mSelectedAccessPoint;
   private WifiEnabler mWifiEnabler;
   private WifiManager mWifiManager;


   public WifiSettings() {
      this.mFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
      this.mFilter.addAction("android.net.wifi.SCAN_RESULTS");
      this.mFilter.addAction("android.net.wifi.NETWORK_IDS_CHANGED");
      this.mFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
      this.mFilter.addAction("android.net.wifi.CONFIGURED_NETWORKS_CHANGE");
      this.mFilter.addAction("android.net.wifi.LINK_CONFIGURATION_CHANGED");
      this.mFilter.addAction("android.net.wifi.STATE_CHANGE");
      this.mFilter.addAction("android.net.wifi.RSSI_CHANGED");
      this.mFilter.addAction("android.net.wifi.ERROR");
      this.mReceiver = new BroadcastReceiver() {
         public void onReceive(Context context, Intent intent) {
            WifiSettings.this.handleEvent(context, intent);
         }
      };
      this.mScanner = new Scanner(null);
   }

   private void addMessagePreference(int var1) {
      if(this.mEmptyView != null) {
         this.mEmptyView.setText(var1);
      }

      this.getPreferenceScreen().removeAll();
   }

   private void changeNextButtonState(boolean var1) {
      if(this.mInXlSetupWizard) {
         ((WifiSettingsForSetupWizardXL)this.getActivity()).changeNextButtonState(var1);
      } else if(this.mEnableNextOnConnection && this.hasNextButton()) {
         this.getNextButton().setEnabled(var1);
         return;
      }

   }

   private List constructAccessPoints() {
      ArrayList var1 = new ArrayList();
      Multimap var2 = new Multimap(null);
      List var3 = this.mWifiManager.getConfiguredNetworks();
      if(var3 != null) {
         Iterator var11 = var3.iterator();

         while(var11.hasNext()) {
            WifiConfiguration var12 = (WifiConfiguration)var11.next();
            AccessPoint var13 = new AccessPoint(this.getActivity(), var12);
            var13.update(this.mLastInfo, this.mLastState);
            var1.add(var13);
            var2.put(var13.ssid, var13);
         }
      }

      List var4 = this.mWifiManager.getScanResults();
      if(var4 != null) {
         Iterator var5 = var4.iterator();

         while(var5.hasNext()) {
            ScanResult var6 = (ScanResult)var5.next();
            if(var6.SSID != null && var6.SSID.length() != 0 && !var6.capabilities.contains("[IBSS]")) {
               boolean var7 = false;
               Iterator var8 = var2.getAll(var6.SSID).iterator();

               while(var8.hasNext()) {
                  if(((AccessPoint)var8.next()).update(var6)) {
                     var7 = true;
                  }
               }

               if(!var7) {
                  AccessPoint var9 = new AccessPoint(this.getActivity(), var6);
                  var1.add(var9);
                  var2.put(var9.ssid, var9);
               }
            }
         }
      }

      Collections.sort(var1);
      return var1;
   }

   private void handleEvent(Context var1, Intent var2) {
      String var3 = var2.getAction();
      if("android.net.wifi.WIFI_STATE_CHANGED".equals(var3)) {
         this.updateWifiState(var2.getIntExtra("wifi_state", 4));
      } else {
         if("android.net.wifi.SCAN_RESULTS".equals(var3) || "android.net.wifi.CONFIGURED_NETWORKS_CHANGE".equals(var3) || "android.net.wifi.LINK_CONFIGURATION_CHANGED".equals(var3)) {
            this.updateAccessPoints();
            return;
         }

         if("android.net.wifi.supplicant.STATE_CHANGE".equals(var3)) {
            if(!this.mConnected.get()) {
               this.updateConnectionState(android.net.wifi.WifiInfo.getDetailedStateOf((SupplicantState)var2.getParcelableExtra("newState")));
            }

            if(this.mInXlSetupWizard) {
               ((WifiSettingsForSetupWizardXL)this.getActivity()).onSupplicantStateChanged(var2);
               return;
            }
         } else {
            if("android.net.wifi.STATE_CHANGE".equals(var3)) {
               NetworkInfo var4 = (NetworkInfo)var2.getParcelableExtra("networkInfo");
               this.mConnected.set(var4.isConnected());
               this.changeNextButtonState(var4.isConnected());
               this.updateAccessPoints();
               this.updateConnectionState(var4.getDetailedState());
               return;
            }

            if("android.net.wifi.RSSI_CHANGED".equals(var3)) {
               this.updateConnectionState((DetailedState)null);
               return;
            }

            if("android.net.wifi.ERROR".equals(var3)) {
               switch(var2.getIntExtra("errorCode", 0)) {
               case 1:
                  Toast.makeText(var1, 2131427836, 0).show();
                  return;
               default:
                  return;
               }
            }
         }
      }

   }

   private boolean requireKeyStore(WifiConfiguration var1) {
      if(WifiConfigController.requireKeyStore(var1) && KeyStore.getInstance().state() != State.UNLOCKED) {
         this.mKeyStoreNetworkId = var1.networkId;
         Credentials.getInstance().unlock(this.getActivity());
         return true;
      } else {
         return false;
      }
   }

   private void saveNetwork(WifiConfiguration var1) {
      if(this.mInXlSetupWizard) {
         ((WifiSettingsForSetupWizardXL)this.getActivity()).onSaveNetwork(var1);
      } else {
         this.mWifiManager.saveNetwork(var1);
      }
   }

   private void showConfigUi(AccessPoint var1, boolean var2) {
      if(this.mInXlSetupWizard) {
         ((WifiSettingsForSetupWizardXL)this.getActivity()).showConfigUi(var1, var2);
      } else {
         this.showDialog(var1, var2);
      }
   }

   private void showDialog(AccessPoint var1, boolean var2) {
      if(this.mDialog != null) {
         this.removeDialog(1);
         this.mDialog = null;
      }

      this.mDlgAccessPoint = var1;
      this.mDlgEdit = var2;
      this.showDialog(1);
   }

   private void updateAccessPoints() {
      switch(this.mWifiManager.getWifiState()) {
      case 0:
         this.addMessagePreference(2131427765);
         return;
      case 1:
         this.addMessagePreference(2131427782);
         return;
      case 2:
         this.getPreferenceScreen().removeAll();
         return;
      case 3:
         List var1 = this.constructAccessPoints();
         this.getPreferenceScreen().removeAll();
         if(this.mInXlSetupWizard) {
            ((WifiSettingsForSetupWizardXL)this.getActivity()).onAccessPointsUpdated(this.getPreferenceScreen(), var1);
            return;
         } else {
            Iterator var2 = var1.iterator();

            while(var2.hasNext()) {
               AccessPoint var3 = (AccessPoint)var2.next();
               this.getPreferenceScreen().addPreference(var3);
            }
         }
      default:
      }
   }

   private void updateConnectionState(DetailedState var1) {
      if(!this.mWifiManager.isWifiEnabled()) {
         this.mScanner.pause();
      } else {
         if(var1 == DetailedState.OBTAINING_IPADDR) {
            this.mScanner.pause();
         } else {
            this.mScanner.resume();
         }

         this.mLastInfo = this.mWifiManager.getConnectionInfo();
         if(var1 != null) {
            this.mLastState = var1;
         }

         for(int var2 = -1 + this.getPreferenceScreen().getPreferenceCount(); var2 >= 0; --var2) {
            Preference var3 = this.getPreferenceScreen().getPreference(var2);
            if(var3 instanceof AccessPoint) {
               ((AccessPoint)var3).update(this.mLastInfo, this.mLastState);
            }
         }

         if(this.mInXlSetupWizard) {
            ((WifiSettingsForSetupWizardXL)this.getActivity()).updateConnectionState(this.mLastState);
            return;
         }
      }

   }

   private void updateWifiState(int var1) {
      this.getActivity().invalidateOptionsMenu();
      switch(var1) {
      case 1:
         this.addMessagePreference(2131427782);
         break;
      case 2:
         this.addMessagePreference(2131427764);
         break;
      case 3:
         this.mScanner.resume();
         return;
      }

      this.mLastInfo = null;
      this.mLastState = null;
      this.mScanner.pause();
   }

   void forget() {
      this.mWifiManager.forgetNetwork(this.mSelectedAccessPoint.networkId);
      if(this.mWifiManager.isWifiEnabled()) {
         this.mScanner.resume();
      }

      this.updateAccessPoints();
      this.changeNextButtonState(false);
   }

   int getAccessPointsCount() {
      return this.mWifiManager.isWifiEnabled()?this.getPreferenceScreen().getPreferenceCount():0;
   }

   public void onActivityCreated(Bundle var1) {
      this.mWifiManager = (WifiManager)this.getSystemService("wifi");
      this.mWifiManager.asyncConnect(this.getActivity(), new WifiServiceHandler(null));
      if(var1 != null && var1.containsKey("wifi_ap_state")) {
         this.mDlgEdit = var1.getBoolean("edit_mode");
         this.mAccessPointSavedState = var1.getBundle("wifi_ap_state");
      }

      Activity var2 = this.getActivity();
      this.mEnableNextOnConnection = var2.getIntent().getBooleanExtra("wifi_enable_next_on_connect", false);
      if(this.mEnableNextOnConnection && this.hasNextButton()) {
         ConnectivityManager var5 = (ConnectivityManager)this.getActivity().getSystemService("connectivity");
         if(var5 != null) {
            this.changeNextButtonState(var5.getNetworkInfo(1).isConnected());
         }
      }

      if(this.mInXlSetupWizard) {
         this.addPreferencesFromResource(2131034167);
      } else {
         this.addPreferencesFromResource(2131034170);
         Switch var3 = new Switch(var2);
         if(var2 instanceof PreferenceActivity) {
            PreferenceActivity var4 = (PreferenceActivity)var2;
            if(var4.onIsHidingHeaders() || !var4.onIsMultiPane()) {
               var3.setPadding(0, 0, var2.getResources().getDimensionPixelSize(2131492866), 0);
               var2.getActionBar().setDisplayOptions(16, 16);
               var2.getActionBar().setCustomView(var3, new LayoutParams(-2, -2, 21));
            }
         }

         this.mWifiEnabler = new WifiEnabler(var2, var3);
      }

      this.mEmptyView = (TextView)this.getView().findViewById(16908292);
      this.getListView().setEmptyView(this.mEmptyView);
      this.registerForContextMenu(this.getListView());
      this.setHasOptionsMenu(true);
      super.onActivityCreated(var1);
   }

   void onAddNetworkPressed() {
      this.mSelectedAccessPoint = null;
      this.showConfigUi((AccessPoint)null, true);
   }

   public void onAttach(Activity var1) {
      super.onAttach(var1);
      this.mInXlSetupWizard = var1 instanceof WifiSettingsForSetupWizardXL;
   }

   public void onClick(DialogInterface var1, int var2) {
      if(this.mInXlSetupWizard) {
         if(var2 == -3 && this.mSelectedAccessPoint != null) {
            this.forget();
         } else if(var2 == -1) {
            ((WifiSettingsForSetupWizardXL)this.getActivity()).onConnectButtonPressed();
            return;
         }
      } else {
         if(var2 == -3 && this.mSelectedAccessPoint != null) {
            this.forget();
            return;
         }

         if(var2 == -1) {
            this.submit(this.mDialog.getController());
            return;
         }
      }

   }

   public boolean onContextItemSelected(MenuItem var1) {
      boolean var2 = true;
      if(this.mSelectedAccessPoint == null) {
         var2 = super.onContextItemSelected(var1);
      } else {
         switch(var1.getItemId()) {
         case 4:
            if(this.mSelectedAccessPoint.networkId == -1) {
               if(this.mSelectedAccessPoint.security == 0) {
                  this.mSelectedAccessPoint.generateOpenNetworkConfig();
                  this.mWifiManager.connectNetwork(this.mSelectedAccessPoint.getConfig());
                  return var2;
               }

               this.showConfigUi(this.mSelectedAccessPoint, var2);
               return var2;
            }

            if(!this.requireKeyStore(this.mSelectedAccessPoint.getConfig())) {
               this.mWifiManager.connectNetwork(this.mSelectedAccessPoint.networkId);
               return var2;
            }
            break;
         case 5:
            this.mWifiManager.forgetNetwork(this.mSelectedAccessPoint.networkId);
            return var2;
         case 6:
            this.showConfigUi(this.mSelectedAccessPoint, var2);
            return var2;
         default:
            return super.onContextItemSelected(var1);
         }
      }

      return var2;
   }

   public void onCreateContextMenu(ContextMenu var1, View var2, ContextMenuInfo var3) {
      if(this.mInXlSetupWizard) {
         ((WifiSettingsForSetupWizardXL)this.getActivity()).onCreateContextMenu(var1, var2, var3);
      } else if(var3 instanceof AdapterContextMenuInfo) {
         Preference var4 = (Preference)this.getListView().getItemAtPosition(((AdapterContextMenuInfo)var3).position);
         if(var4 instanceof AccessPoint) {
            this.mSelectedAccessPoint = (AccessPoint)var4;
            var1.setHeaderTitle(this.mSelectedAccessPoint.ssid);
            if(this.mSelectedAccessPoint.getLevel() != -1 && this.mSelectedAccessPoint.getState() == null) {
               var1.add(0, 4, 0, 2131427779);
            }

            if(this.mSelectedAccessPoint.networkId != -1) {
               var1.add(0, 5, 0, 2131427780);
               var1.add(0, 6, 0, 2131427781);
               return;
            }
         }
      }

   }

   public Dialog onCreateDialog(int var1) {
      AccessPoint var2 = this.mDlgAccessPoint;
      if(var2 == null && this.mAccessPointSavedState != null) {
         var2 = new AccessPoint(this.getActivity(), this.mAccessPointSavedState);
         this.mDlgAccessPoint = var2;
      }

      this.mSelectedAccessPoint = var2;
      this.mDialog = new WifiDialog(this.getActivity(), this, var2, this.mDlgEdit);
      return this.mDialog;
   }

   public void onCreateOptionsMenu(Menu var1, MenuInflater var2) {
      if(!this.mInXlSetupWizard) {
         boolean var3 = this.mWifiManager.isWifiEnabled();
         var1.add(0, 1, 0, 2131427777).setEnabled(var3).setShowAsAction(1);
         var1.add(0, 2, 0, 2131427775).setEnabled(var3).setShowAsAction(1);
         var1.add(0, 3, 0, 2131427778).setShowAsAction(0);
      }

      super.onCreateOptionsMenu(var1, var2);
   }

   public boolean onOptionsItemSelected(MenuItem var1) {
      switch(var1.getItemId()) {
      case 1:
         if(this.mWifiManager.isWifiEnabled()) {
            this.mScanner.forceScan();
         }

         return true;
      case 2:
         if(this.mWifiManager.isWifiEnabled()) {
            this.onAddNetworkPressed();
         }

         return true;
      case 3:
         if(this.getActivity() instanceof PreferenceActivity) {
            ((PreferenceActivity)this.getActivity()).startPreferencePanel(AdvancedWifiSettings.class.getCanonicalName(), (Bundle)null, 2131427837, (CharSequence)null, this, 0);
         } else {
            this.startFragment(this, AdvancedWifiSettings.class.getCanonicalName(), -1, (Bundle)null);
         }

         return true;
      default:
         return super.onOptionsItemSelected(var1);
      }
   }

   public void onPause() {
      super.onPause();
      if(this.mWifiEnabler != null) {
         this.mWifiEnabler.pause();
      }

      this.getActivity().unregisterReceiver(this.mReceiver);
      this.mScanner.pause();
   }

   public boolean onPreferenceTreeClick(PreferenceScreen var1, Preference var2) {
      if(!(var2 instanceof AccessPoint)) {
         return super.onPreferenceTreeClick(var1, var2);
      } else {
         this.mSelectedAccessPoint = (AccessPoint)var2;
         if(this.mSelectedAccessPoint.security == 0 && this.mSelectedAccessPoint.networkId == -1) {
            this.mSelectedAccessPoint.generateOpenNetworkConfig();
            this.mWifiManager.connectNetwork(this.mSelectedAccessPoint.getConfig());
         } else {
            this.showConfigUi(this.mSelectedAccessPoint, false);
         }

         return true;
      }
   }

   public void onResume() {
      super.onResume();
      if(this.mWifiEnabler != null) {
         this.mWifiEnabler.resume();
      }

      this.getActivity().registerReceiver(this.mReceiver, this.mFilter);
      if(this.mKeyStoreNetworkId != -1 && KeyStore.getInstance().state() == State.UNLOCKED) {
         this.mWifiManager.connectNetwork(this.mKeyStoreNetworkId);
      }

      this.mKeyStoreNetworkId = -1;
      this.updateAccessPoints();
   }

   public void onSaveInstanceState(Bundle var1) {
      super.onSaveInstanceState(var1);
      if(this.mDialog != null && this.mDialog.isShowing()) {
         var1.putBoolean("edit_mode", this.mDlgEdit);
         if(this.mDlgAccessPoint != null) {
            this.mAccessPointSavedState = new Bundle();
            this.mDlgAccessPoint.saveWifiState(this.mAccessPointSavedState);
            var1.putBundle("wifi_ap_state", this.mAccessPointSavedState);
         }
      }

   }

   void pauseWifiScan() {
      if(this.mWifiManager.isWifiEnabled()) {
         this.mScanner.pause();
      }

   }

   void refreshAccessPoints() {
      if(this.mWifiManager.isWifiEnabled()) {
         this.mScanner.resume();
      }

      this.getPreferenceScreen().removeAll();
   }

   void resumeWifiScan() {
      if(this.mWifiManager.isWifiEnabled()) {
         this.mScanner.resume();
      }

   }

   void submit(WifiConfigController var1) {
      switch(var1.chosenNetworkSetupMethod()) {
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

      this.updateAccessPoints();
   }

   private class WifiServiceHandler extends Handler {

      private WifiServiceHandler() {}

      // $FF: synthetic method
      WifiServiceHandler(Object var2) {
         this();
      }

      public void handleMessage(Message var1) {
         switch(var1.what) {
         case 11:
            WpsResult var2 = (WpsResult)var1.obj;
            if(var2 != null) {
               Builder var3 = (new Builder(WifiSettings.this.getActivity())).setTitle(2131427786).setPositiveButton(17039370, (OnClickListener)null);
               switch(null.$SwitchMap$android$net$wifi$WpsResult$Status[var2.status.ordinal()]) {
               case 1:
                  var3.setMessage(2131427789);
                  var3.show();
                  return;
               case 2:
                  var3.setMessage(2131427788);
                  var3.show();
                  return;
               default:
                  if(var2.pin != null) {
                     Resources var8 = WifiSettings.this.getResources();
                     Object[] var9 = new Object[]{var2.pin};
                     var3.setMessage(var8.getString(2131427787, var9));
                     var3.show();
                     return;
                  }
               }
            }
            break;
         case 69632:
            if(var1.arg1 != 0) {
               Log.e("WifiSettings", "Failed to establish AsyncChannel connection");
               return;
            }
         }

      }
   }

   private class Multimap {

      private HashMap store;


      private Multimap() {
         this.store = new HashMap();
      }

      // $FF: synthetic method
      Multimap(Object var2) {
         this();
      }

      List getAll(Object var1) {
         List var2 = (List)this.store.get(var1);
         return var2 != null?var2:Collections.emptyList();
      }

      void put(Object var1, Object var2) {
         Object var3 = (List)this.store.get(var1);
         if(var3 == null) {
            var3 = new ArrayList(3);
            this.store.put(var1, var3);
         }

         ((List)var3).add(var2);
      }
   }

   private class Scanner extends Handler {

      private int mRetry;


      private Scanner() {
         this.mRetry = 0;
      }

      // $FF: synthetic method
      Scanner(Object var2) {
         this();
      }

      void forceScan() {
         this.removeMessages(0);
         this.sendEmptyMessage(0);
      }

      public void handleMessage(Message var1) {
         if(WifiSettings.this.mWifiManager.startScanActive()) {
            this.mRetry = 0;
         } else {
            int var2 = 1 + this.mRetry;
            this.mRetry = var2;
            if(var2 >= 3) {
               this.mRetry = 0;
               Toast.makeText(WifiSettings.this.getActivity(), 2131427768, 1).show();
               return;
            }
         }

         this.sendEmptyMessageDelayed(0, 10000L);
      }

      void pause() {
         this.mRetry = 0;
         this.removeMessages(0);
      }

      void resume() {
         if(!this.hasMessages(0)) {
            this.sendEmptyMessage(0);
         }

      }
   }
}
