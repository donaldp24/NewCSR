package com.general.mediaplayer.csr;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;

public abstract class ProgressCategoryBase extends PreferenceCategory {

   public ProgressCategoryBase(Context var1, AttributeSet var2) {
      super(var1, var2);
   }

   public abstract void setProgress(boolean var1);
}
