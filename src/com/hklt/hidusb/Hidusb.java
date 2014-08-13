package com.hklt.hidusb;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Hidusb
{
  public static final int HIDUSB_FAIL = -1;
  public static final int HIDUSB__SUCCESS = 0;
  private static int[] mData;
  private static Hidusb mHidusb;
  private static boolean mLibLoadFlag = true;
  private OnDataChangeListener mDataChangeListener;
  private boolean mFlag;
  private Handler mHandler = new Handler()
  {
    public void handleMessage(Message paramMessage)
    {
    }
  };

  static
  {
    try
    {
        // have to uncomment
      //System.loadLibrary("hid_raws");
      mData = new int[0];
    }
    catch (UnsatisfiedLinkError localUnsatisfiedLinkError)
    {
      while (true)
      {
        Log.d("hid_raws", "hid_raws jni library not found!");
        mLibLoadFlag = false;
      }
    }
  }

  public Hidusb()
    throws Exception
  {
      /* have to uncomment
    if ((mLibLoadFlag) && (openhidraws() == -1))
      throw new Exception("It's not found device!");
      */
  }

  public static Hidusb newInstance()
    throws Exception
  {

    if (mHidusb == null)
      mHidusb = new Hidusb();
    return mHidusb;
  }

  public native int[] Trans_UID(int[] paramArrayOfInt1, int[] paramArrayOfInt2);

  public void close()
  {
    this.mFlag = false;
  }

  public int[] getData()
  {
    if (mLibLoadFlag)
      return get_data();
    return null;
  }

  public int[] getUidId(int[] paramArrayOfInt1, int[] paramArrayOfInt2)
  {
    if (mLibLoadFlag)
      return Trans_UID(paramArrayOfInt1, paramArrayOfInt2);
    return null;
  }

  public native int[] get_data();

  public native byte[] get_tag();

  public native int openhidraws();

  public int[] sendData(int[] paramArrayOfInt)
  {

      if (mLibLoadFlag)
      {
        int[] arrayOfInt2 = send_data(paramArrayOfInt, paramArrayOfInt.length);
        return arrayOfInt2;

      }
      return null;
  }

  public native int[] send_data(int[] paramArrayOfInt, int paramInt);

  public void setOnDataChangeListener(OnDataChangeListener paramOnDataChangeListener)
  {
    this.mDataChangeListener = paramOnDataChangeListener;
    this.mFlag = true;
  }

  public native int set_led(char paramChar, int paramInt);

  public native void usbpwr_reset(int paramInt);

  public native void wdg_sw(int paramInt);

  public static abstract interface OnDataChangeListener
  {
    public abstract void onDataChange(int[] paramArrayOfInt);
  }
}

/* Location:           D:\Elance\works\41_MatthewDemos\CSR\CSR_unzipped\classes_dex2jar.jar
 * Qualified Name:     com.hklt.hidusb.Hidusb
 * JD-Core Version:    0.6.0
 */