package org.trifort.rootbeer.examples.scan;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class GPUScanKernel implements Kernel {

  private int[][] inputData;
  private int[][] outputData;

  public GPUScanKernel(int[][] inputData, int[][] outputData){
    this.inputData = inputData;
    this.outputData = outputData;
  }

  public void gpuMethod(){
    int block_idxx = RootbeerGpu.getBlockIdxx();
    int thread_idxx = RootbeerGpu.getThreadIdxx();

    int pout = 0;
    int pin = 1;
    int[] localInputData = inputData[block_idxx];
    int n = localInputData.length;

    int value;
    if(thread_idxx > 0){
      value = localInputData[thread_idxx - 1];
    } else {
      value = 0;
    }
    RootbeerGpu.setSharedInteger((pout * n + thread_idxx) * GPUScanConstants.INT_SIZE, value);

    RootbeerGpu.syncthreads();

    for(int offset = 1; offset < n; offset *= 2){
      pout = 1 - pout;
      pin = 1 - pout;
      if(thread_idxx >= offset){
        int index_out = (pout * n + thread_idxx) * GPUScanConstants.INT_SIZE;
        int temp = RootbeerGpu.getSharedInteger(index_out);
        temp += RootbeerGpu.getSharedInteger((pin * n + thread_idxx - offset) * GPUScanConstants.INT_SIZE);
        RootbeerGpu.setSharedInteger(index_out, temp);
      } else {
        int value2 = RootbeerGpu.getSharedInteger((pin * n + thread_idxx) * GPUScanConstants.INT_SIZE);
        RootbeerGpu.setSharedInteger((pin * n + thread_idxx) * GPUScanConstants.INT_SIZE, value2);
      }
      RootbeerGpu.syncthreads();
    }

    int outputValue = RootbeerGpu.getSharedInteger((pout * n + thread_idxx) * GPUScanConstants.INT_SIZE);
    outputData[block_idxx][thread_idxx] = outputValue;
  }
}
