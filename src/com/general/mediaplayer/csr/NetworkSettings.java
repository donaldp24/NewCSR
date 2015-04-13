package com.general.mediaplayer.csr;

import android.os.Bundle;
import com.general.mediaplayer.csr.SettingsPreferenceFragment;

public class NetworkSettings extends SettingsPreferenceFragment {

   public void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.addPreferencesFromResource(R.xml.network_settings);
   }
}
