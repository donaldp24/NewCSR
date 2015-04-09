package com.general.mediaplayer.csr.wifi.p2p;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import com.general.mediaplayer.csr.R;

public class WifiP2pPeer extends Preference {

   private static final int SIGNAL_LEVELS = 4;
   private static final int[] STATE_SECURED = new int[]{R.attr.state_encrypted};
   public WifiP2pDevice device;
   private int mRssi;
   private ImageView mSignal;


   public WifiP2pPeer(Context var1, WifiP2pDevice var2) {
      super(var1);
      this.device = var2;
      this.setWidgetLayoutResource(R.layout.preference_widget_wifi_signal);
      this.mRssi = 60;
   }

   private void refresh() {
      if(this.mSignal != null) {
         Context context = this.getContext();
         this.mSignal.setImageLevel(this.getLevel());
         this.setSummary(context.getResources().getStringArray(R.array.wifi_p2p_status)[this.device.status]);
      }
   }

   public int compareTo(Preference var1) {
      if(var1 instanceof WifiP2pPeer) {
         WifiP2pPeer var2 = (WifiP2pPeer)var1;
         if(this.device.status == var2.device.status) {
            if(this.device.deviceName != null) {
               return this.device.deviceName.compareToIgnoreCase(var2.device.deviceName);
            }

            return this.device.deviceAddress.compareToIgnoreCase(var2.device.deviceAddress);
         }

         if(this.device.status < var2.device.status) {
            return -1;
         }
      }

      return 1;
   }

   int getLevel() {
      return this.mRssi == Integer.MAX_VALUE?-1:WifiManager.calculateSignalLevel(this.mRssi, 4);
   }

   protected void onBindView(View var1) {
      if(TextUtils.isEmpty(this.device.deviceName)) {
         this.setTitle(this.device.deviceAddress);
      } else {
         this.setTitle(this.device.deviceName);
      }

      this.mSignal = (ImageView)var1.findViewById(R.id.signal);
      if(this.mRssi == Integer.MAX_VALUE) {
         this.mSignal.setImageDrawable((Drawable)null);
      } else {
         this.mSignal.setImageResource(R.drawable.wifi_signal);
         this.mSignal.setImageState(STATE_SECURED, true);
      }

      this.refresh();
      super.onBindView(var1);
   }
}
