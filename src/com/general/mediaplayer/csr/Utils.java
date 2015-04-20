package com.general.mediaplayer.csr;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.ConnectivityManager;
//import android.net.LinkProperties;
import android.net.wifi.WifiManager;
import android.preference.Preference;
//import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceGroup;
import android.preference.PreferenceActivity.Header;
//import android.preference.PreferenceFrameLayout.LayoutParams;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Utils {

   private static final String META_DATA_PREFERENCE_ICON = "com.general.mediaplayer.csr.icon";
   private static final String META_DATA_PREFERENCE_SUMMARY = "com.general.mediaplayer.csr.summary";
   private static final String META_DATA_PREFERENCE_TITLE = "com.general.mediaplayer.csr.title";
   public static final int UPDATE_PREFERENCE_FLAG_SET_TITLE_TO_MATCHING_ACTIVITY = 1;


   public static Locale createLocaleFromString(String var0) {
      if(var0 == null) {
         return Locale.getDefault();
      } else {
         String[] var1 = var0.split("_", 3);
         return 1 == var1.length?new Locale(var1[0]):(2 == var1.length?new Locale(var1[0], var1[1]):new Locale(var1[0], var1[1], var1[2]));
      }
   }

   /*private static String formatIpAddresses(LinkProperties var0) {
      String var1 = null;
      if(var0 != null) {
         Iterator var2 = var0.getAddresses().iterator();
         boolean var3 = var2.hasNext();
         var1 = null;
         if(var3) {
            var1 = "";

            while(var2.hasNext()) {
               var1 = var1 + ((InetAddress)var2.next()).getHostAddress();
               if(var2.hasNext()) {
                  var1 = var1 + ", ";
               }
            }
         }
      }

      return var1;
   }*/

   public static String getBatteryPercentage(Intent var0) {
      int var1 = var0.getIntExtra("level", 0);
      int var2 = var0.getIntExtra("scale", 100);
      return var1 * 100 / var2 + "%";
   }

   public static String getBatteryStatus(Resources var0, Intent var1) {
      int var2 = var1.getIntExtra("plugged", 0);
      int var3 = var1.getIntExtra("status", 1);
      if(var3 == 2) {
         String var4 = var0.getString(R.string.battery_info_status_charging);
         if(var2 > 0) {
            StringBuilder var5 = (new StringBuilder()).append(var4).append(" ");
            int var6;
            if(var2 == 1) {
               var6 = R.string.battery_info_status_charging_ac;
            } else {
               var6 = R.string.battery_info_status_charging_usb;
            }

            var4 = var5.append(var0.getString(var6)).toString();
         }

         return var4;
      } else {
         return var3 == 3?var0.getString(R.string.battery_info_status_discharging):(var3 == 4?var0.getString(R.string.battery_info_status_not_charging):(var3 == 5?var0.getString(R.string.battery_info_status_full):var0.getString(R.string.battery_info_status_unknown)));
      }
   }

   /*21 public static String getDefaultIpAddresses(Context var0) {
      return formatIpAddresses(((ConnectivityManager)var0.getSystemService("connectivity")).getActiveLinkProperties());
   }

   public static int getTetheringLabel(ConnectivityManager var0) {
      String[] var1 = var0.getTetherableUsbRegexs();
      String[] var2 = var0.getTetherableWifiRegexs();
      String[] var3 = var0.getTetherableBluetoothRegexs();
      boolean var4;
      if(var1.length != 0) {
         var4 = true;
      } else {
         var4 = false;
      }

      boolean var5;
      if(var2.length != 0) {
         var5 = true;
      } else {
         var5 = false;
      }

      boolean var6;
      if(var3.length != 0) {
         var6 = true;
      } else {
         var6 = false;
      }

      return var5 && var4 && var6?R.string.tether_settings_title_all:(var5 && var4?R.string.tether_settings_title_all:(var5 && var6?R.string.tether_settings_title_all:(var5?R.string.tether_settings_title_wifi:(var4 && var6?R.string.tether_settings_title_usb_bluetooth:(var4?R.string.tether_settings_title_usb:R.string.tether_settings_title_bluetooth)))));
   }

   public static String getWifiIpAddresses(Context var0) {
       // API level 21
      return formatIpAddresses(((ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE)).getLinkProperties(1));
   }*/

   public static boolean isMonkeyRunning() {
      return ActivityManager.isUserAMonkey();
   }

   /*21 public static boolean isVoiceCapable(Context var0) {
      TelephonyManager var1 = (TelephonyManager)var0.getSystemService("phone");
      return var1 != null && var1.isVoiceCapable();
   }*/

   public static boolean isWifiOnly(Context var0) {
      boolean var1 = ((ConnectivityManager)var0.getSystemService(Context.CONNECTIVITY_SERVICE)).isActiveNetworkMetered(); //FIXME.isNetworkSupported(0);
      boolean var2 = false;
      if(!var1) {
         var2 = true;
      }

      return var2;
   }

   /*public static void prepareCustomPreferencesList(ViewGroup var0, View var1, ListView var2, boolean var3) {
      boolean var4;
      if(var2.getScrollBarStyle() == 33554432) {
         var4 = true;
      } else {
         var4 = false;
      }

      if(var4 && var0 instanceof PreferenceFrameLayout) {
         ((LayoutParams)var1.getLayoutParams()).removeBorders = true;
         Resources var5 = var2.getResources();
         var5.getDimensionPixelSize(17104933);
         int var7 = var5.getDimensionPixelSize(17104932);
         int var8;
         if(var3) {
            var8 = 0;
         } else {
            var8 = var7;
         }

         var2.setPadding(var8, 0, var8, var7);
      }

   }

   public static boolean updateHeaderToSpecificActivityFromMetaDataOrRemove(Context param0, List param1, Header param2) {
      // $FF: Couldn't be decompiled
   }

   public static boolean updatePreferenceToSpecificActivityFromMetaDataOrRemove(Context param0, PreferenceGroup param1, String param2) {
      // $FF: Couldn't be decompiled
   }*/

   public static boolean updatePreferenceToSpecificActivityOrRemove(Context var0, PreferenceGroup var1, String var2, int var3) {
      Preference var4 = var1.findPreference(var2);
      if(var4 == null) {
         return false;
      } else {
         Intent var5 = var4.getIntent();
         if(var5 != null) {
            PackageManager var7 = var0.getPackageManager();
            List var8 = var7.queryIntentActivities(var5, 0);
            int var9 = var8.size();

            for(int var10 = 0; var10 < var9; ++var10) {
               ResolveInfo var11 = (ResolveInfo)var8.get(var10);
               if((1 & var11.activityInfo.applicationInfo.flags) != 0) {
                  var4.setIntent((new Intent()).setClassName(var11.activityInfo.packageName, var11.activityInfo.name));
                  if((var3 & 1) != 0) {
                     var4.setTitle(var11.loadLabel(var7));
                  }

                  return true;
               }
            }
         }

         var1.removePreference(var4);
         return true;
      }
   }
}
