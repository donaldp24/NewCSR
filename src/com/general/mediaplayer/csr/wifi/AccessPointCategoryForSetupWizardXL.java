package com.general.mediaplayer.csr.wifi;

import android.content.Context;
import android.util.AttributeSet;
import com.general.mediaplayer.csr.ProgressCategoryBase;
import com.general.mediaplayer.csr.R;


public class AccessPointCategoryForSetupWizardXL extends ProgressCategoryBase {

   public AccessPointCategoryForSetupWizardXL(Context var1, AttributeSet var2) {
      super(var1, var2);
      this.setLayoutResource(R.layout.access_point_category_for_setup_wizard_xl);
   }

   public void setProgress(boolean var1) {
      this.notifyChanged();
   }
}
