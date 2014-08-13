package com.general.mediaplayer.csr;

import android.app.LauncherActivity;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.net.Uri;
import android.view.View;
import android.widget.ListView;

public class CreateShortcut extends LauncherActivity {

   protected Intent getTargetIntent() {
      Intent var1 = new Intent("android.intent.action.MAIN", (Uri)null);
      var1.addCategory("com.general.mediaplayer.csr.SHORTCUT");
      var1.addFlags(268435456);
      return var1;
   }

   protected boolean onEvaluateShowIcons() {
      return false;
   }

   protected void onListItemClick(ListView var1, View var2, int var3, long var4) {
      Intent var6 = this.intentForPosition(var3);
      var6.setFlags(2097152);
      Intent var8 = new Intent();
      var8.putExtra("android.intent.extra.shortcut.ICON_RESOURCE", ShortcutIconResource.fromContext(this, R.mipmap.ic_launcher_settings));
      var8.putExtra("android.intent.extra.shortcut.INTENT", var6);
      var8.putExtra("android.intent.extra.shortcut.NAME", this.itemForPosition(var3).label);
      this.setResult(-1, var8);
      this.finish();
   }
}
