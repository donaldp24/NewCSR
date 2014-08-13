package com.general.mediaplayer.csr.app;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import com.general.mediaplayer.csr.R;
import com.general.mediaplayer.csr.app.AppInfo;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PakageInfoProvider {

   private static final String tag = "GetappinfoActivity";
   private AppInfo appInfo;
   private List appInfos;
   private Context context;


   public PakageInfoProvider(Context var1) {
      this.context = var1;
   }

   public boolean filterApp(ApplicationInfo var1) {
      return (128 & var1.flags) != 0 || (1 & var1.flags) == 0;
   }

   public boolean filterCilentApp(String var1) {
      int var2 = var1.lastIndexOf('.');
      if(var2 > 0) {
         String var3 = var1.substring(0, var2);
         String var4 = this.context.getResources().getString(R.string.mediaplayer_packagename);
         if(!this.context.getResources().getString(R.string.mediaplayer_set_packagename).equalsIgnoreCase(var1) && var4.equalsIgnoreCase(var3)) {
            return true;
         }
      }

      return false;
   }

   public List getAppInfo() {
      PackageManager var1 = this.context.getPackageManager();
      List var2 = var1.getInstalledPackages(8192);
      this.appInfos = new ArrayList();

      for(Iterator var3 = var2.iterator(); var3.hasNext(); this.appInfo = null) {
         PackageInfo var4 = (PackageInfo)var3.next();
         this.appInfo = new AppInfo();
         String var5 = var4.applicationInfo.loadLabel(var1).toString();
         this.appInfo.setAppName(var5);
         String var6 = var4.versionName;
         this.appInfo.setAppVersion(var6);
         Drawable var7 = var4.applicationInfo.loadIcon(var1);
         this.appInfo.setDrawable(var7);
         this.appInfo.setIsUserApp(Boolean.valueOf(this.filterApp(var4.applicationInfo)));
         this.appInfo.setPackageName(var4.packageName);
         this.appInfo.setIsCilentApp(Boolean.valueOf(this.filterCilentApp(var4.packageName)));
         this.appInfos.add(this.appInfo);
      }

      return this.appInfos;
   }
}
