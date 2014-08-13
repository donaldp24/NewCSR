package com.general.mediaplayer.csr;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.general.mediaplayer.csr.app.ApkFileInfo;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public class ApkFileAdapter extends BaseAdapter {

   public String currPath;
   public List list = new LinkedList();
   private Context mContext;


   public ApkFileAdapter(Context var1) {
      this.mContext = var1;
   }

   public static ApkFileInfo getApkFileInfo(Context paramContext, String paramString) {
       System.out.println(paramString);
       File localFile1 = new File(paramString);
       ApkFileInfo localApkFileInfo;
       if ((!localFile1.exists()) || (!paramString.toLowerCase().endsWith(".apk")))
       {
           System.out.println("file path is not correct");
           localApkFileInfo = null;
           return localApkFileInfo;
       }
       while (true)
       {
           try
           {
               Class localClass1 = Class.forName("android.content.pm.PackageParser");
               Object localObject1 = localClass1.getConstructor(new Class[] { String.class }).newInstance(new Object[] { paramString });
               DisplayMetrics localDisplayMetrics = new DisplayMetrics();
               localDisplayMetrics.setToDefaults();
               Class[] arrayOfClass1 = new Class[4];
               arrayOfClass1[0] = File.class;
               arrayOfClass1[1] = String.class;
               arrayOfClass1[2] = DisplayMetrics.class;
               arrayOfClass1[3] = Integer.TYPE;
               Method localMethod = localClass1.getDeclaredMethod("parsePackage", arrayOfClass1);
               Object[] arrayOfObject1 = new Object[4];
               File localFile2 = new File(paramString);
               arrayOfObject1[0] = localFile2;
               arrayOfObject1[1] = paramString;
               arrayOfObject1[2] = localDisplayMetrics;
               arrayOfObject1[3] = Integer.valueOf(0);
               Object localObject2 = localMethod.invoke(localObject1, arrayOfObject1);
               if (localObject2 == null)
                   return null;
               Field localField = localObject2.getClass().getDeclaredField("applicationInfo");
               if (localField.get(localObject2) == null)
                   return null;
               ApplicationInfo localApplicationInfo = (ApplicationInfo)localField.get(localObject2);
               Class localClass2 = Class.forName("android.content.res.AssetManager");
               Object localObject3 = localClass2.newInstance();
               localClass2.getDeclaredMethod("addAssetPath", new Class[] { String.class }).invoke(localObject3, new Object[] { paramString });
               Resources localResources1 = paramContext.getResources();
               Class[] arrayOfClass2 = new Class[3];
               arrayOfClass2[0] = localObject3.getClass();
               arrayOfClass2[1] = localResources1.getDisplayMetrics().getClass();
               arrayOfClass2[2] = localResources1.getConfiguration().getClass();
               Constructor localConstructor = Resources.class.getConstructor(arrayOfClass2);
               Object[] arrayOfObject2 = new Object[3];
               arrayOfObject2[0] = localObject3;
               arrayOfObject2[1] = localResources1.getDisplayMetrics();
               arrayOfObject2[2] = localResources1.getConfiguration();
               Resources localResources2 = (Resources)localConstructor.newInstance(arrayOfObject2);
               localApkFileInfo = new ApkFileInfo();
               if (localApplicationInfo == null)
                   return null; // break label xx
               localApkFileInfo.setPath(paramString);
               if (localApplicationInfo.icon == 0)
                   continue;
               localApkFileInfo.setDrawable(localResources2.getDrawable(localApplicationInfo.icon));
               if (localApplicationInfo.labelRes != 0)
               {
                   localApkFileInfo.setapkFileName((String)localResources2.getText(localApplicationInfo.labelRes));
                   localApkFileInfo.setPackageName(localApplicationInfo.packageName);
                   PackageInfo localPackageInfo = paramContext.getPackageManager().getPackageArchiveInfo(paramString, 1);
                   if (localPackageInfo == null)
                       break;
                   localApkFileInfo.setAppVersion(localPackageInfo.versionName);
                   return localApkFileInfo;
               }
           }
           catch (Exception localException)
           {
               localException.printStackTrace();
               return null;
           }
           String str = localFile1.getName();
           localApkFileInfo.setapkFileName(str.substring(0, str.lastIndexOf(".")));
       }
       /*label xx:*/ return null;
   }

   public int getCount() {
      return this.list.size();
   }

   public Object getItem(int var1) {
      return null;
   }

   public long getItemId(int var1) {
      return (long)var1;
   }

   public View getView(int var1, View var2, ViewGroup var3) {
      ViewHolder var4;
      if(var2 == null) {
         var4 = new ViewHolder();
         var2 = View.inflate(this.mContext, R.layout.app_info_item, (ViewGroup)null);
         var4.tv_name = (TextView)var2.findViewById(R.id.tv_app_info_item_name);
         var4.tv_version = (TextView)var2.findViewById(R.id.tv_app_info_item_version);
         var4.iv_icon = (ImageView)var2.findViewById(R.id.iv_app_info_item);
         var4.bt = (Button)var2.findViewById(R.id.btn_app_info_item);
         var2.setTag(var4);
      } else {
         var4 = (ViewHolder)var2.getTag();
      }

      ApkFileInfo var5 = (ApkFileInfo)this.list.get(var1);
      var4.iv_icon.setImageDrawable(var5.getDrawable());
      var4.tv_name.setText(var5.getapkFileName());
      var4.tv_version.setText("Version:" + var5.getAppVersion());
      var4.bt.setVisibility(8);
      var4.bt.setText("Install");
      return var2;
   }

   public void scanApkFiles(String var1) {
      if(var1 != null) {
         this.list.clear();
         File var2 = new File(var1);
         if(var2.exists() && var2.isDirectory()) {
            File[] var4 = var2.listFiles();
            Log.v("scanApkFiles", "LoadApkFileInfo==file.size=" + var4.length);
            int var6 = var4.length;

            for(int var7 = 0; var7 < var6; ++var7) {
               File var8 = var4[var7];
               if(var8.isFile() && var8.getName().toString().endsWith(".apk")) {
                  String var9 = var8.getAbsolutePath();
                  Log.v("scanApkFiles", "strApkPath= " + var9);
                  ApkFileInfo var11 = getApkFileInfo(this.mContext, var9);
                  if(var11 != null) {
                     String var12 = var11.getPackageName();
                     String var13 = var12.substring(0, var12.lastIndexOf('.'));
                     if(this.mContext.getResources().getString(R.string.mediaplayer_packagename).equalsIgnoreCase(var13)) {
                        this.list.add(var11);
                     }
                  }
               }
            }
         } else {
            Log.v("scanApkFiles", "LoadApkFileInfo==null=");
         }

         this.notifyDataSetChanged();
         this.currPath = var1;
      }
   }

   static class ViewHolder {

      Button bt;
      ImageView iv_icon;
      TextView tv_name;
      TextView tv_version;


   }
}
