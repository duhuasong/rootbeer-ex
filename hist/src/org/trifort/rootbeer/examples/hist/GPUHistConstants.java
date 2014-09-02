package org.trifort.rootbeer.examples.hist;

public class GPUHistConstants {

  public static final int BIN_COUNT = 64;
  public static final int BYTE_SIZE = 1;
  public static final int INT_SIZE = 4;
  public static final int HISTOGRAM_SIZE = BIN_COUNT * INT_SIZE;
  public static final int THREAD_N = 192;
  public static final int BLOCK_MEMORY = THREAD_N * BIN_COUNT;
  public static final int BLOCK_DATA = THREAD_N * 63;
  public static final int DATA_N = 96000000;
  public static final int DATA_SIZE = DATA_N * BYTE_SIZE;

}