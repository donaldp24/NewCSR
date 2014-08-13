package com.general.mediaplayer.csr.app;

import android.graphics.drawable.Drawable;

public class AppInfo {

   private String appName;
   private String appVersion;
   private Drawable drawable;
   private Boolean isCilentApp;
   private Boolean isUserApp;
   private String packageName;


   public String getAppName() {
      return this.appName;
   }

   public String getAppVersion() {
      return this.appVersion;
   }

   public Drawable getDrawable() {
      return this.drawable;
   }

   public Boolean getIsCilentApp() {
      return this.isCilentApp;
   }

   public Boolean getIsUserApp() {
      return this.isUserApp;
   }

   public String getPackageName() {
      return this.packageName;
   }

   public void setAppName(String var1) {
      this.appName = var1;
   }

   public void setAppVersion(String var1) {
      this.appVersion = var1;
   }

   public void setDrawable(Drawable var1) {
      this.drawable = var1;
   }

   public void setIsCilentApp(Boolean var1) {
      this.isCilentApp = var1;
   }

   public void setIsUserApp(Boolean var1) {
      this.isUserApp = var1;
   }

   public void setPackageName(String var1) {
      this.packageName = var1;
   }
}
