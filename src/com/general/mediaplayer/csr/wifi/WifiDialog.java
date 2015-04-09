package com.general.mediaplayer.csr.wifi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.general.mediaplayer.csr.wifi.AccessPoint;
import com.general.mediaplayer.csr.wifi.WifiConfigController;
import com.general.mediaplayer.csr.wifi.WifiConfigUiBase;

import com.general.mediaplayer.csr.R;

class WifiDialog extends AlertDialog implements WifiConfigUiBase {

   static final int BUTTON_FORGET = -3;
   static final int BUTTON_SUBMIT = -1;
   private final AccessPoint mAccessPoint;
   private WifiConfigController mController;
   private final boolean mEdit;
   private final OnClickListener mListener;
   private View mView;


   public WifiDialog(Context var1, OnClickListener var2, AccessPoint var3, boolean var4) {
      super(var1, R.style.Theme_WifiDialog);
      this.mEdit = var4;
      this.mListener = var2;
      this.mAccessPoint = var3;
   }

   public Button getCancelButton() {
      return this.getButton(-2);
   }

   public WifiConfigController getController() {
      return this.mController;
   }

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
      this.mController = new WifiConfigController(this, this.mView, this.mAccessPoint, this.mEdit);
      super.onCreate(var1);
      if(this.mAccessPoint != null && this.mAccessPoint.networkId == -1) {
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
}
