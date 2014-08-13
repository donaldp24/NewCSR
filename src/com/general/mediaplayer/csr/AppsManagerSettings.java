package com.general.mediaplayer.csr;

import android.os.Bundle;
import com.general.mediaplayer.csr.SettingsPreferenceFragment;

public class AppsManagerSettings extends SettingsPreferenceFragment {

   public void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.addPreferencesFromResource(R.xml.apps_manager_settings);
   }
}
