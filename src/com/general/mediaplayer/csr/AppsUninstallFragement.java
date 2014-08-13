package com.general.mediaplayer.csr;

import android.app.Fragment;
import android.app.Instrumentation;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import com.general.mediaplayer.csr.app.AppInfo;
import com.general.mediaplayer.csr.app.PakageInfoProvider;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppsUninstallFragement extends Fragment {

   protected static final int LOAD_INSTALLED_APP_FINISH = 11;
   private Handler handler = new Handler() {
      public void handleMessage(Message msg) {
         switch(msg.what) {
         case LOAD_INSTALLED_APP_FINISH:
            AppsUninstallFragement.this.initClientAppInfo();
            AppsUninstallFragement.this.mInstalledAppListView.setAdapter(AppsUninstallFragement.this.mInstalledAppAdater);
            AppsUninstallFragement.this.mIsInstalledAppViewprefer = true;
            return;
         default:
         }
      }
   };
   private List mAllInstallAppInfos;
   private AppInfo mAppInfo;
   private List mClientAppInfos;
   private View mContentView;
   private Button mExitButton;
   private InstalledAppListViewAdapter mInstalledAppAdater;
   private ListView mInstalledAppListView;
   private LinearLayout mInstalledAppView;
   private boolean mIsInstalledAppViewprefer = false;
   private PakageInfoProvider mPakageInfoProvider;


   private void LoadInstalledAppInfo() {
      (new Thread() {
         public void run() {
            AppsUninstallFragement.this.mAllInstallAppInfos = AppsUninstallFragement.this.mPakageInfoProvider.getAppInfo();
            Message msg = Message.obtain();
            msg.what = LOAD_INSTALLED_APP_FINISH;
            AppsUninstallFragement.this.handler.sendMessage(msg);
         }
      }).start();
   }

   private void initClientAppInfo() {
      this.mClientAppInfos = new ArrayList();
      Iterator iterator = this.mAllInstallAppInfos.iterator();

      while(iterator.hasNext()) {
         AppInfo appInfo = (AppInfo)iterator.next();
         if(appInfo.getIsCilentApp().booleanValue()) {
            this.mClientAppInfos.add(appInfo);
         }
      }

   }

   private void initView() {
      this.mInstalledAppListView = (ListView)this.mContentView.findViewById(R.id.app_uninstall_file_listview);
      this.mInstalledAppAdater = new InstalledAppListViewAdapter();
      this.mPakageInfoProvider = new PakageInfoProvider(this.getActivity().getApplicationContext());
      this.LoadInstalledAppInfo();
      this.mInstalledAppListView.setOnItemClickListener(new OnItemClickListener() {
         public void onItemClick(AdapterView parent, View view, int position, long id) {
            if(position < AppsUninstallFragement.this.mClientAppInfos.size()) {
               String var6 = ((AppInfo)AppsUninstallFragement.this.mClientAppInfos.get(position)).getPackageName();
               AppsUninstallFragement.this.uninstallApp(var6);
            }

         }
      });
      this.mExitButton = (Button)this.mContentView.findViewById(R.id.app_uninstall_exit_btn);
      this.mExitButton.setOnClickListener(new OnClickListener() {
         public void onClick(View var1) {
            AppsUninstallFragement.this.doBack();
         }
      });
   }

   private void uninstallApp(String packageName) {
      Intent intent = new Intent();
      intent.setAction("android.intent.action.DELETE");
      intent.addCategory("android.intent.category.DEFAULT");
      intent.setData(Uri.parse("package:" + packageName));
      this.startActivityForResult(intent, 0);
   }

   public void doBack() {
      (new Thread() {
         public void run() {
            try {
               (new Instrumentation()).sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
            } catch (Exception e) {
               Log.e("Exception when sendKeyDownUpSync", e.toString());
            }
         }
      }).start();
   }

   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      this.initView();
   }

   public void onActivityResult(int responseCode, int resultCode, Intent data) {
      super.onActivityResult(responseCode, resultCode, data);
      this.LoadInstalledAppInfo();
   }

   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
      this.mContentView = (LinearLayout)inflater.inflate(R.layout.app_uninstall, (ViewGroup)null);
      return this.mContentView;
   }

   static class ViewHolder {
      Button bt;
      ImageView iv_icon;
      TextView tv_name;
      TextView tv_version;
   }

   private class InstalledAppListViewAdapter extends BaseAdapter {

      protected static final String TAG = "myListViewAdapter";


      private InstalledAppListViewAdapter() {
          //
      }

      public int getCount() {
         return AppsUninstallFragement.this.mClientAppInfos.size();
      }

      public Object getItem(int var1) {
         return null;
      }

      public long getItemId(int id) {
         return (long)id;
      }

      public View getView(int position, View convertView, ViewGroup parent) {
         View itemView;
         ViewHolder viewHolder;
         if(convertView != null && !(convertView instanceof TextView)) {
            itemView = convertView;
            viewHolder = (ViewHolder)convertView.getTag();
         } else {
            itemView = View.inflate(AppsUninstallFragement.this.getActivity().getApplicationContext(), R.layout.app_info_item, (ViewGroup)null);
            viewHolder = new ViewHolder();
            viewHolder.tv_name = (TextView)itemView.findViewById(R.id.tv_app_info_item_name);
            viewHolder.tv_version = (TextView)itemView.findViewById(R.id.tv_app_info_item_version);
            viewHolder.iv_icon = (ImageView)itemView.findViewById(R.id.iv_app_info_item);
            viewHolder.bt = (Button)itemView.findViewById(R.id.btn_app_info_item);
            itemView.setTag(viewHolder);
         }

         AppInfo var6 = (AppInfo)AppsUninstallFragement.this.mClientAppInfos.get(position);
         viewHolder.iv_icon.setImageDrawable(var6.getDrawable());
         viewHolder.tv_name.setText(var6.getAppName());
         viewHolder.tv_version.setText("Version:" + var6.getAppVersion());
         viewHolder.bt.setVisibility(View.GONE);
         viewHolder.bt.setTag(var6.getPackageName());
         return itemView;
      }
   }
}
