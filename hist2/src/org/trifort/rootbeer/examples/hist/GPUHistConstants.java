package org.trifort.rootbeer.examples.hist;

public class GPUHistConstants {

  public static final int MAX_PARTIAL_HISTOGRAM64_COUNT = 32768;
  public static final int HISTOGRAM64_BIN_COUNT = 64;
  public static final int SHARED_MEMORY_BANKS = 16;
  public static final int HISTOGRAM64_THREADBLOCK_SIZE = (4 * GPUHistConstants.SHARED_MEMORY_BANKS);
  public static final int INT_SIZE = 4;

}
