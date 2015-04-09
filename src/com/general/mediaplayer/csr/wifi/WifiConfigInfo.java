package com.general.mediaplayer.csr.wifi;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.TextView;
import java.util.List;

import com.general.mediaplayer.csr.R;

public class WifiConfigInfo extends Activity {

   private static final String TAG = "WifiConfigInfo";
   private TextView mConfigList;
   private WifiManager mWifiManager;


   protected void onCreate(Bundle var1) {
      super.onCreate(var1);
      this.mWifiManager = (WifiManager)this.getSystemService("wifi");
      this.setContentView(R.layout.wifi_config_info);
      this.mConfigList = (TextView)this.findViewById(R.id.config_list);
   }

   protected void onResume() {
      super.onResume();
      List var1 = this.mWifiManager.getConfiguredNetworks();
      StringBuffer var2 = new StringBuffer();

      for(int var3 = -1 + var1.size(); var3 >= 0; --var3) {
         var2.append(var1.get(var3));
      }

      this.mConfigList.setText(var2);
   }
}
