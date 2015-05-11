package com.general.mediaplayer.csr;

import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.general.mediaplayer.csr.ApkFileAdapter;
import com.general.mediaplayer.csr.app.ApkFileInfo;
import java.io.File;

public class AppsInstallFragement extends Fragment {

   public static final String EXTERN_SD_FILES_PATH = "/mnt/external_sd/ApkFiles";
   public static final String FILES_PATH = "\\ApkFiles\\";
   public static final String USB_FILES_PATH = "/mnt/usbhost1/ApkFiles";
   private ApkFileAdapter mApkFileAdapter;
   private ApkFileInfo mApkFileInfo = new ApkFileInfo();
   private ListView mApkFileListView;
   private View mContentView;
   private Button mExitButton;
   private Button mExternSdButton;
   private String mFilesPath;
   private boolean mIsSwitchUsb = false;
   private TextView mTxtPath;
   private Button mUsbButton;


   private void initView() {
      this.mApkFileListView = (ListView)this.mContentView.findViewById(R.id.app_install_file_listview);
      this.mApkFileAdapter = new ApkFileAdapter(this.getActivity());
      if(this.mIsSwitchUsb) {
         this.mFilesPath = USB_FILES_PATH;
      } else {
         this.mFilesPath = EXTERN_SD_FILES_PATH;
      }

      this.mApkFileAdapter.scanApkFiles(this.mFilesPath);
      this.mApkFileListView.setAdapter(this.mApkFileAdapter);
      this.mApkFileListView.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView var1, View var2, int var3, long var4) {
            AppsInstallFragement.this.mApkFileInfo = (ApkFileInfo)AppsInstallFragement.this.mApkFileAdapter.list.get(var3);
            String var7 = AppsInstallFragement.this.mApkFileInfo.getPath();
            AppsInstallFragement.this.installApkFile(var7);
         }
      });
      this.mExternSdButton = (Button)this.mContentView.findViewById(R.id.app_install_extern_sd_btn);
      this.mExternSdButton.setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AppsInstallFragement.this.mFilesPath = EXTERN_SD_FILES_PATH;
            AppsInstallFragement.this.mApkFileAdapter.scanApkFiles(AppsInstallFragement.this.mFilesPath);
            AppsInstallFragement.this.mIsSwitchUsb = false;
            AppsInstallFragement.this.updateFilesPath();
         }
      });
      this.mUsbButton = (Button)this.mContentView.findViewById(R.id.app_install_usb_btn);
      this.mUsbButton.setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AppsInstallFragement.this.mFilesPath = USB_FILES_PATH;
            AppsInstallFragement.this.mApkFileAdapter.scanApkFiles(AppsInstallFragement.this.mFilesPath);
            AppsInstallFragement.this.mIsSwitchUsb = true;
            AppsInstallFragement.this.updateFilesPath();
         }
      });
      this.mExitButton = (Button)this.mContentView.findViewById(R.id.app_install_exit_btn);
      this.mExitButton.setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AppsInstallFragement.this.doBack();
         }
      });
      this.mTxtPath = (TextView)this.mContentView.findViewById(R.id.app_install_files_path);
      this.updateFilesPath();
   }

   private void installApkFile(String var1) {
      Intent var2 = new Intent("android.intent.action.VIEW");
      File var3 = new File(var1);
      if(var3.exists()) {
         var2.addFlags(0x10000000);
         var2.setAction("android.intent.action.VIEW");
         var2.setDataAndType(Uri.fromFile(var3), "application/vnd.android.package-archive");
         this.startActivity(var2);
      }
   }

   private void updateFilesPath() {
      if(this.mIsSwitchUsb) {
         this.mTxtPath.setText("USB\nSourcePath:\\ApkFiles\\");
      } else {
         this.mTxtPath.setText("SD Card\nSourcePath:\\ApkFiles\\");
      }
   }

   public void doBack() {
      (new Thread() {
         public void run() {
            try {
               (new Instrumentation()).sendKeyDownUpSync(4);
            } catch (Exception var2) {
               Log.e("Exception when doBack", var2.toString());
            }
         }
      }).start();
   }

   public void onActivityCreated(Bundle var1) {
      super.onActivityCreated(var1);
      this.initView();
   }

   public View onCreateView(LayoutInflater var1, ViewGroup var2, Bundle var3) {
      Log.v(this.getTag(), "===app install onCreateView===");
      this.mContentView = var1.inflate(R.layout.app_install, (ViewGroup)null);
      return this.mContentView;
   }
}
