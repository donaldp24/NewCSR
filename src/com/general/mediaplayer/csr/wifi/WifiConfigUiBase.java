package com.general.mediaplayer.csr.wifi;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.Button;
import com.general.mediaplayer.csr.wifi.WifiConfigController;

public interface WifiConfigUiBase {

   Button getCancelButton();

   Context getContext();

   WifiConfigController getController();

   Button getForgetButton();

   LayoutInflater getLayoutInflater();

   Button getSubmitButton();

   boolean isEdit();

   void setCancelButton(CharSequence var1);

   void setForgetButton(CharSequence var1);

   void setSubmitButton(CharSequence var1);

   void setTitle(int var1);

   void setTitle(CharSequence var1);
}
