package com.general.mediaplayer.csr.wifi;

import android.content.Context;
import android.content.res.Resources;
import android.net.NetworkInfo.DetailedState;
import com.general.mediaplayer.csr.R;

class Summary {

   static String get(Context var0, DetailedState var1) {
      return get(var0, (String)null, var1);
   }

   static String get(Context var0, String var1, DetailedState var2) {
      Resources var3 = var0.getResources();
      int var4;
      if(var1 == null) {
         var4 = R.array.wifi_status;
      } else {
         var4 = R.array.wifi_status_with_ssid;
      }

      String[] var5 = var3.getStringArray(var4);
      int var6 = var2.ordinal();
      return var6 < var5.length && var5[var6].length() != 0?String.format(var5[var6], new Object[]{var1}):null;
   }
}
