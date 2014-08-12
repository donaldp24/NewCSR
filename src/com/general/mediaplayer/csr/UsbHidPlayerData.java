package com.general.mediaplayer.csr;

public class UsbHidPlayerData
{
  public static final int A = 10001;
  public static final int ANTENNALOG_ERROR_MAX_CNT = 10;
  public static final int B = 10002;
  public static final int C = 10003;
  public static final int CURRENTPLAYERLEASTTIME = 500;
  public static final int IDSURETIME = 200;
  public static final int ID_SURE_CNT = 3;
  public static final boolean IS_LED_FALSE = true;
  public static final int MAX_POWERDIS = 4999999;
  public static final int MAX_SET_LED_ERROR_CNT = 5;
  public static final int MIN_POWERDIS = 3000000;
  public static final int USB_ERROR_MAX_TIME_2S = 10;
  public static final int USB_RESET_ERROR_CNT = 5;
  public static final int USB_RESET_TIME_MS = 5000;
  public static final int VIDEO_LOOP_POSITION = 100;
  public static final int VIDEO_TM_POSITION = 255;
  public static final boolean mPlayer_DK_001 = true;
  public int[][] mAntennaId = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
  public boolean[][] mAntennaIdSure = { { false, false, false }, { false, false, false }, { false, false, false } };
  public int[][] mAntennaLog = { { 0, 0, 0 }, { 0, 0, 0 }, { 0, 0, 0 } };
  public int[] mAntennaLogErrorCnt = { 0, 0, 0 };
  public int[] mAntennaNum = { 0, 0, 0 };
  public int[] mCombinIdNum = { 0, 0 };
  public boolean mCurrentLedIsIdleMode = false;
  public int[] mCurrentPlayIdNum = { 0, 0 };
  public boolean mCurrentPlayIdRemove = false;
  public int mCurrentPlayIdTime = 0;
  public int[] mIdName = { 0, 0, 0, 0 };
  public boolean mIdScanInitReadyed = false;
  public int[] mIdTime = { 0, 0, 0 };
  public String[] mIdVideoPath = { null, null, null, null };
  public boolean mIsCurrentPlayPowerdis = false;
  public boolean[] mIsIdPlayed = { false, false, false, false };
  public boolean[] mIsIdPlayedLed = { false, false, false };
  public boolean[] mIsIdSure = { false, false, false };
  public boolean[] mIsIdSureLed = { false, false, false };
  public boolean[] mIsPowerDisId = { false, false, false };
}

/* Location:           D:\Elance\works\41_MatthewDemos\CSR\CSR_unzipped\classes_dex2jar.jar
 * Qualified Name:     com.general.mediaplayer.csr.UsbHidPlayerData
 * JD-Core Version:    0.6.0
 */