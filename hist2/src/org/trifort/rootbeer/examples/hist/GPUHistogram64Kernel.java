package org.trifort.rootbeer.examples.hist;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class GPUHistogram64Kernel implements Kernel {

  private int[] partialHistograms;
  private byte[] inputData0;
  private byte[] inputData1;
  private byte[] inputData2;
  private byte[] inputData3;
  private int dataCount;

  public GPUHistogram64Kernel(int[] partialHistograms, byte[] inputData0,
    byte[] inputData1, byte[] inputData2, byte[] inputData3, int dataCount){

    this.inputData0 = inputData0;
    this.inputData1 = inputData1;
    this.inputData2 = inputData2;
    this.inputData3 = inputData3;
    this.partialHistograms = partialHistograms;
    this.dataCount = dataCount;
  }

  private int umad(int a, int b, int c){
    return (a * b) + c;
  }

  private int umul(int a, int b){
    return a * b;
  }

  private void addByte(int threadBase, byte data){
    int index = (data >>  2) & 0x3F;
    int umulIndex = umul(index, GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE);
    byte histValue = RootbeerGpu.getSharedByte(umulIndex);
    ++histValue;
    RootbeerGpu.setSharedByte(umulIndex, histValue);
  }

  public void gpuMethod(){
    int threadIdxx = RootbeerGpu.getThreadIdxx();
    int blockIdxx = RootbeerGpu.getBlockIdxx();
    int blockDimx = RootbeerGpu.getBlockDimx();
    int gridDimx = (int) RootbeerGpu.getGridDimx();

    int threadPos =
        ((threadIdxx & ~(GPUHistConstants.SHARED_MEMORY_BANKS * 4 - 1)) << 0) |
        ((threadIdxx & (GPUHistConstants.SHARED_MEMORY_BANKS     - 1)) << 2) |
        ((threadIdxx & (GPUHistConstants.SHARED_MEMORY_BANKS * 3)) >> 4);

    int loopSize = GPUHistConstants.HISTOGRAM64_BIN_COUNT / 4;
    for(int i = 0; i < loopSize; ++i){
      RootbeerGpu.setSharedByte(threadIdxx + i * GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE, (byte) 0);
      RootbeerGpu.setSharedByte(threadIdxx + i * GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE + 1, (byte) 0);
      RootbeerGpu.setSharedByte(threadIdxx + i * GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE + 2, (byte) 0);
      RootbeerGpu.setSharedByte(threadIdxx + i * GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE + 3, (byte) 0);
    }

    RootbeerGpu.syncthreads();

    int startPos = umad(blockIdxx, blockDimx, threadIdxx);
    int incrementer = umul(blockDimx, gridDimx);

    byte[] localInputData0 = inputData0;
    byte[] localInputData1 = inputData1;
    byte[] localInputData2 = inputData2;
    byte[] localInputData3 = inputData3;
    int[] localOutputData = partialHistograms;

    for(int pos = startPos; pos < dataCount; pos += incrementer){
      addByte(threadPos, localInputData0[pos]);
      addByte(threadPos, localInputData1[pos]);
      addByte(threadPos, localInputData2[pos]);
      addByte(threadPos, localInputData3[pos]);
    }

    RootbeerGpu.syncthreads();

    if(threadIdxx < GPUHistConstants.HISTOGRAM64_BIN_COUNT){
      int sharedBase = umul(threadIdxx, GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE);

      int sum = 0;
      int pos = 4 * (threadIdxx & (GPUHistConstants.SHARED_MEMORY_BANKS - 1));
      for (int i = 0; i < (GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE / 4); i++){

        sum += RootbeerGpu.getSharedByte(sharedBase) +
               RootbeerGpu.getSharedByte(sharedBase + 1) +
               RootbeerGpu.getSharedByte(sharedBase + 2) +
               RootbeerGpu.getSharedByte(sharedBase + 3);

        pos = (pos + 4) & (GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE - 1);
      }

      localOutputData[blockIdxx* GPUHistConstants.HISTOGRAM64_BIN_COUNT + threadIdxx] = sum;
    }
  }
}
