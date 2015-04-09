package com.general.mediaplayer.csr.wifi;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.general.mediaplayer.csr.wifi.AccessPoint;
import com.general.mediaplayer.csr.wifi.WifiConfigController;
import com.general.mediaplayer.csr.wifi.WifiConfigUiBase;
import com.general.mediaplayer.csr.wifi.WifiSettingsForSetupWizardXL;

import com.general.mediaplayer.csr.R;

public class WifiConfigUiForSetupWizardXL implements WifiConfigUiBase, OnFocusChangeListener {

   private static final String TAG = "SetupWizard";
   private AccessPoint mAccessPoint;
   private final WifiSettingsForSetupWizardXL mActivity;
   private Button mCancelButton;
   private Button mConnectButton;
   private WifiConfigController mController;
   private boolean mEdit;
   private Handler mHandler = new Handler();
   private LayoutInflater mInflater;
   private final InputMethodManager mInputMethodManager;
   private View mView;


   public WifiConfigUiForSetupWizardXL(WifiSettingsForSetupWizardXL var1, ViewGroup var2, AccessPoint var3, boolean var4) {
      this.mActivity = var1;
      this.mConnectButton = (Button)var1.findViewById(R.id.wifi_setup_connect);
      this.mCancelButton = (Button)var1.findViewById(R.id.wifi_setup_cancel);
      this.mAccessPoint = var3;
      this.mEdit = var4;
      this.mInflater = (LayoutInflater)var1.getSystemService("layout_inflater");
      this.mView = this.mInflater.inflate(R.layout.wifi_config_ui_for_setup_wizard, var2, true);
      this.mController = new WifiConfigController(this, this.mView, this.mAccessPoint, var4);
      this.mInputMethodManager = (InputMethodManager)var1.getSystemService("input_method");
      if(this.mView.findViewById(R.id.security_fields).getVisibility() == 0) {
         this.requestFocusAndShowKeyboard(R.id.password);
      } else if(this.mView.findViewById(R.id.type_ssid).getVisibility() == 0) {
         this.requestFocusAndShowKeyboard(R.id.ssid);
         return;
      }

   }

   public AccessPoint getAccessPoint() {
      return this.mAccessPoint;
   }

   public Button getCancelButton() {
      return this.mCancelButton;
   }

   public Context getContext() {
      return this.mActivity;
   }

   public WifiConfigController getController() {
      return this.mController;
   }

   public Button getForgetButton() {
      return null;
   }

   public LayoutInflater getLayoutInflater() {
      return this.mInflater;
   }

   public Button getSubmitButton() {
      return this.mConnectButton;
   }

   public View getView() {
      return this.mView;
   }

   public boolean isEdit() {
      return this.mEdit;
   }

   public void onFocusChange(View var1, boolean var2) {
      var1.setOnFocusChangeListener((OnFocusChangeListener)null);
      if(var2) {
         this.mHandler.post(new FocusRunnable(var1));
      }

   }

   public void requestFocusAndShowKeyboard(int var1) {
      View var2 = this.mView.findViewById(var1);
      if(var2 == null) {
         Log.w("SetupWizard", "password field to be focused not found.");
      } else {
         if(!(var2 instanceof EditText)) {
            Log.w("SetupWizard", "password field is not EditText");
            return;
         }

         if(var2.isFocused()) {
            Log.i("SetupWizard", "Already focused");
            if(!this.mInputMethodManager.showSoftInput(var2, 0)) {
               Log.w("SetupWizard", "Failed to show SoftInput");
               return;
            }
         } else {
            var2.setOnFocusChangeListener(this);
            boolean var3 = var2.requestFocus();
            Object[] var4 = new Object[1];
            String var5;
            if(var3) {
               var5 = "successful";
            } else {
               var5 = "failed";
            }

            var4[0] = var5;
            Log.i("SetupWizard", String.format("Focus request: %s", var4));
            if(!var3) {
               var2.setOnFocusChangeListener((OnFocusChangeListener)null);
               return;
            }
         }
      }

   }

   public void setCancelButton(CharSequence var1) {
      this.mCancelButton.setVisibility(0);
   }

   public void setForgetButton(CharSequence var1) {}

   public void setSubmitButton(CharSequence var1) {
      this.mConnectButton.setVisibility(0);
      this.mConnectButton.setText(var1);
   }

   public void setTitle(int var1) {
      Log.d("SetupWizard", "Ignoring setTitle");
   }

   public void setTitle(CharSequence var1) {
      Log.d("SetupWizard", "Ignoring setTitle");
   }

   private class FocusRunnable implements Runnable {

      final View mViewToBeFocused;


      public FocusRunnable(View var2) {
         this.mViewToBeFocused = var2;
      }

      public void run() {
         if(WifiConfigUiForSetupWizardXL.this.mInputMethodManager.showSoftInput(this.mViewToBeFocused, 0)) {
            WifiConfigUiForSetupWizardXL.this.mActivity.setPaddingVisibility(8);
         } else {
            Log.w("SetupWizard", "Failed to show software keyboard ");
         }
      }
   }
}
