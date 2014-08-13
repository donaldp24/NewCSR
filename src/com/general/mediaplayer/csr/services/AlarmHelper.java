package com.general.mediaplayer.csr.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import com.general.mediaplayer.csr.services.CallAlarm;

public class AlarmHelper {

   private AlarmManager mAlarmManager;
   private Context mContext;


   public AlarmHelper(Context context) {
      this.mContext = context;
      this.mAlarmManager = (AlarmManager)this.mContext.getSystemService("alarm");
   }

   public void closeAlarm(int requestCode) {
      Intent intent = new Intent();
      intent.setClass(this.mContext, CallAlarm.class);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, requestCode, intent, 0);
      this.mAlarmManager.cancel(pendingIntent);
   }

   public void openAlarm(int requestCode, long triggerAtTime, long interval) {
      Intent intent = new Intent();
      intent.setClass(this.mContext, CallAlarm.class);
      PendingIntent pendingIntent = PendingIntent.getBroadcast(this.mContext, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT);
      this.mAlarmManager.setRepeating(0, triggerAtTime, interval, pendingIntent);
   }
}
