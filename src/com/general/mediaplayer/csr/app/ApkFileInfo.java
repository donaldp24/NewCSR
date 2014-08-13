package com.general.mediaplayer.csr.app;

import android.graphics.drawable.Drawable;

public class ApkFileInfo {

   private String apkFileName;
   private String apkPath;
   private String appVersion;
   private Drawable drawable;
   private Boolean isCilentApk;
   private String packageName;


   public String getAppVersion() {
      return this.appVersion;
   }

   public Drawable getDrawable() {
      return this.drawable;
   }

   public Boolean getIsCilentApp() {
      return this.isCilentApk;
   }

   public String getPackageName() {
      return this.packageName;
   }

   public String getPath() {
      return this.apkPath;
   }

   public String getapkFileName() {
      return this.apkFileName;
   }

   public void setAppVersion(String var1) {
      this.appVersion = var1;
   }

   public void setDrawable(Drawable var1) {
      this.drawable = var1;
   }

   public void setIsCilentApp(Boolean var1) {
      this.isCilentApk = var1;
   }

   public void setPackageName(String var1) {
      this.packageName = var1;
   }

   public void setPath(String var1) {
      this.apkPath = var1;
   }

   public void setapkFileName(String var1) {
      this.apkFileName = var1;
   }
}
