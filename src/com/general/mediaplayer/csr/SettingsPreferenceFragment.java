package com.general.mediaplayer.csr;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Button;
import com.general.mediaplayer.csr.ButtonBarHandler;
import com.general.mediaplayer.csr.DialogCreatable;

public class SettingsPreferenceFragment extends PreferenceFragment implements DialogCreatable {

   private static final String TAG = "SettingsPreferenceFragment";
   private SettingsDialogFragment mDialogFragment;


   public void finish() {
      this.getActivity().onBackPressed();
   }

   public final void finishFragment() {
      this.getActivity().onBackPressed();
   }

   protected ContentResolver getContentResolver() {
      return this.getActivity().getContentResolver();
   }

   protected Button getNextButton() {
      return ((ButtonBarHandler)this.getActivity()).getNextButton();
   }

   protected PackageManager getPackageManager() {
      return this.getActivity().getPackageManager();
   }

   protected Object getSystemService(String var1) {
      return this.getActivity().getSystemService(var1);
   }

   protected boolean hasNextButton() {
      return ((ButtonBarHandler)this.getActivity()).hasNextButton();
   }

   public void onActivityCreated(Bundle var1) {
      super.onActivityCreated(var1);
   }

   public Dialog onCreateDialog(int var1) {
      return null;
   }

   public void onDetach() {
      if(this.isRemoving() && this.mDialogFragment != null) {
         this.mDialogFragment.dismiss();
         this.mDialogFragment = null;
      }

      super.onDetach();
   }

   protected void removeDialog(int var1) {
      if(this.mDialogFragment != null && this.mDialogFragment.getDialogId() == var1) {
         this.mDialogFragment.dismiss();
      }

      this.mDialogFragment = null;
   }

   protected void setOnCancelListener(OnCancelListener var1) {
      if(this.mDialogFragment != null) {
         this.mDialogFragment.mOnCancelListener = var1;
      }

   }

   protected void setOnDismissListener(OnDismissListener var1) {
      if(this.mDialogFragment != null) {
         this.mDialogFragment.mOnDismissListener = var1;
      }

   }

   protected void showDialog(int var1) {
      if(this.mDialogFragment != null) {
         Log.e("SettingsPreferenceFragment", "Old dialog fragment not null!");
      }

      this.mDialogFragment = new SettingsDialogFragment(this, var1);
      this.mDialogFragment.show(this.getActivity().getFragmentManager(), Integer.toString(var1));
   }

   public boolean startFragment(Fragment var1, String var2, int var3, Bundle var4) {
      if(this.getActivity() instanceof PreferenceActivity) {
         ((PreferenceActivity)this.getActivity()).startPreferencePanel(var2, var4, R.string.lock_settings_picker_title, (CharSequence)null, var1, var3);
         return true;
      } else {
         Log.w("SettingsPreferenceFragment", "Parent isn\'t PreferenceActivity, thus there\'s no way to launch the given Fragment (name: " + var2 + ", requestCode: " + var3 + ")");
         return false;
      }
   }

   public static class SettingsDialogFragment extends DialogFragment {

      private static final String KEY_DIALOG_ID = "key_dialog_id";
      private static final String KEY_PARENT_FRAGMENT_ID = "key_parent_fragment_id";
      private int mDialogId;
      private OnCancelListener mOnCancelListener;
      private OnDismissListener mOnDismissListener;
      private Fragment mParentFragment;


      public SettingsDialogFragment() {}

      public SettingsDialogFragment(DialogCreatable var1, int var2) {
         this.mDialogId = var2;
         if(!(var1 instanceof Fragment)) {
            throw new IllegalArgumentException("fragment argument must be an instance of " + Fragment.class.getName());
         } else {
            this.mParentFragment = (Fragment)var1;
         }
      }

      public int getDialogId() {
         return this.mDialogId;
      }

      public void onCancel(DialogInterface var1) {
         super.onCancel(var1);
         if(this.mOnCancelListener != null) {
            this.mOnCancelListener.onCancel(var1);
         }

      }

      public Dialog onCreateDialog(Bundle var1) {
         if(var1 != null) {
            this.mDialogId = var1.getInt("key_dialog_id", 0);
            int var2 = var1.getInt("key_parent_fragment_id", -1);
            if(var2 > -1) {
               this.mParentFragment = this.getFragmentManager().findFragmentById(var2);
               if(!(this.mParentFragment instanceof DialogCreatable)) {
                  throw new IllegalArgumentException("key_parent_fragment_id must implement " + DialogCreatable.class.getName());
               }
            }

            if(this.mParentFragment instanceof SettingsPreferenceFragment) {
               ((SettingsPreferenceFragment)this.mParentFragment).mDialogFragment = this;
            }
         }

         return ((DialogCreatable)this.mParentFragment).onCreateDialog(this.mDialogId);
      }

      public void onDetach() {
         super.onDetach();
         if(this.mParentFragment instanceof SettingsPreferenceFragment && ((SettingsPreferenceFragment)this.mParentFragment).mDialogFragment == this) {
            ((SettingsPreferenceFragment)this.mParentFragment).mDialogFragment = null;
         }

      }

      public void onDismiss(DialogInterface var1) {
         super.onDismiss(var1);
         if(this.mOnDismissListener != null) {
            this.mOnDismissListener.onDismiss(var1);
         }

      }

      public void onSaveInstanceState(Bundle var1) {
         super.onSaveInstanceState(var1);
         if(this.mParentFragment != null) {
            var1.putInt("key_dialog_id", this.mDialogId);
            var1.putInt("key_parent_fragment_id", this.mParentFragment.getId());
         }

      }
   }
}
