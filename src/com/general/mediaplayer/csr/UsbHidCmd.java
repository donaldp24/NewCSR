package com.general.mediaplayer.csr;

public class UsbHidCmd
{
  public static final int[] mGetBlockDataBase;
  public static final int[] mGetMcuI2CDataBase;
  public static final int[] mGetUidBase;
  public static final int[] mSetBlockDataBase;
  public static final int[] mSetLedColorBase;
  public static final int[] mSetLedFadeBase;
  public static final int[] mSetLedFlashBase;
  public static final int[] mSetMcuI2CDataBase;
  public static final int[] mWakeCmd = { 255, 17, 128, 6, 40, 99, 41, 72, 117, 110, 103, 87, 97, 105, 32, 50, 48, 49, 51, 227 };

  static
  {
    mGetUidBase = new int[] { 255, 3, 180, 3, 0, 185 };
    mGetBlockDataBase = new int[] { 255, 5, 162, 0, 0, 0, 0, 166 };
    mSetBlockDataBase = new int[] { 255, 21, 163, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 183 };
    mSetLedColorBase = new int[] { 255, 6, 144, 7, 0, 0, 0, 0, 156 };
    mSetLedFadeBase = new int[] { 255, 8, 146, 10, 0, 0, 0, 0, 0, 0, 163 };
    mSetLedFlashBase = new int[] { 255, 9, 147, 11, 0, 0, 0, 0, 0, 0, 0, 166 };
    mGetMcuI2CDataBase = new int[] { 255, 5, 164, 0, 0, 0, 0, 168 };
    mSetMcuI2CDataBase = new int[] { 255, 21, 165, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 183 };
  }
}

/* Location:           D:\Elance\works\41_MatthewDemos\CSR\CSR_unzipped\classes_dex2jar.jar
 * Qualified Name:     com.general.mediaplayer.csr.UsbHidCmd
 * JD-Core Version:    0.6.0
 */