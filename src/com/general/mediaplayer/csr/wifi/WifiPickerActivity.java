package com.general.mediaplayer.csr.wifi;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.widget.Button;
import com.general.mediaplayer.csr.ButtonBarHandler;
import com.general.mediaplayer.csr.wifi.WifiSettings;

public class WifiPickerActivity extends PreferenceActivity implements ButtonBarHandler {

   private static final String EXTRA_PREFS_SET_BACK_TEXT = "extra_prefs_set_back_text";
   private static final String EXTRA_PREFS_SET_NEXT_TEXT = "extra_prefs_set_next_text";
   private static final String EXTRA_PREFS_SHOW_BUTTON_BAR = "extra_prefs_show_button_bar";


   public Intent getIntent() {
      Intent var1 = new Intent(super.getIntent());
      if(!var1.hasExtra(":android:show_fragment")) {
         var1.putExtra(":android:show_fragment", WifiSettings.class.getName());
      }

      var1.putExtra(":android:no_headers", true);
      return var1;
   }

    @Override
   public Button getNextButton() {
      //return super.getNextButton();
        return null;
   }

    @Override
   public boolean hasNextButton() {
      //return super.hasNextButton();
        return false;
   }

   public void startWithFragment(String var1, Bundle var2, Fragment var3, int var4) {
      Intent var5 = new Intent("android.intent.action.MAIN");
      var5.setClass(this, this.getClass());
      var5.putExtra(":android:show_fragment", var1);
      var5.putExtra(":android:show_fragment_args", var2);
      var5.putExtra(":android:no_headers", true);
      Intent var10 = this.getIntent();
      if(var10.hasExtra("extra_prefs_show_button_bar")) {
         var5.putExtra("extra_prefs_show_button_bar", var10.getBooleanExtra("extra_prefs_show_button_bar", false));
      }

      if(var10.hasExtra("extra_prefs_set_next_text")) {
         var5.putExtra("extra_prefs_set_next_text", var10.getStringExtra("extra_prefs_set_next_text"));
      }

      if(var10.hasExtra("extra_prefs_set_back_text")) {
         var5.putExtra("extra_prefs_set_back_text", var10.getStringExtra("extra_prefs_set_back_text"));
      }

      if(var3 == null) {
         this.startActivity(var5);
      } else {
         var3.startActivityForResult(var5, var4);
      }
   }
}
