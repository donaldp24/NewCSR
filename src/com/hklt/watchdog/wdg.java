package com.hklt.watchdog;

import android.util.Log;

public class wdg
{
  public static final int WDG_FAIL = -1;
  public static final int WDG__SUCCESS = 0;
  private static boolean mLibLoadFlag = true;
  private static wdg mwdg = null;

  static
  {
    try
    {
      System.loadLibrary("wdg_ctrl");
    }
    catch (UnsatisfiedLinkError localUnsatisfiedLinkError)
    {
      Log.d("wdg_ctrl", "wdg_ctrl jni library not found!");
      mLibLoadFlag = false;
    }
  }

  public wdg()
    throws Exception
  {
    if ((mLibLoadFlag) && (openwdog() == WDG_FAIL))
      throw new Exception("It's not found watchdog device!");
  }

  public static wdg newInstance()
    throws Exception
  {
    if (mLibLoadFlag)
    {
      if (mwdg == null)
        mwdg = new wdg();
      return mwdg;
    }
    return null;
  }

  public native int openwdog();

  public native int usbpwr_reset(int paramInt);

  public native int wdg_sw(int paramInt);
}

/* Location:           D:\Elance\works\41_MatthewDemos\CSR\CSR_unzipped\classes_dex2jar.jar
 * Qualified Name:     com.hklt.watchdog.wdg
 * JD-Core Version:    0.6.0
 */