package org.trifort.rootbeer.examples.scan;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class GPUScanKernel implements Kernel {

  private float[][] inputData;
  private float[][] outputData;

  public GPUScanKernel(float[][] inputData, float[][] outputData){
    this.inputData = inputData;
    this.outputData = outputData;
  }

  public void gpuMethod(){
    int block_idxx = RootbeerGpu.getBlockIdxx();
    int thid = RootbeerGpu.getThreadIdxx();
    int offset = 1;

    float[] localInputData = inputData[block_idxx];
    int n = localInputData.length;
    RootbeerGpu.setSharedFloat((2*thid)*GPUScanConstants.INT_SIZE, localInputData[2*thid]);
    RootbeerGpu.setSharedFloat((2*thid+1)*GPUScanConstants.INT_SIZE, localInputData[2*thid+1]);

    for(int d = n >> 1; d > 0; d >>= 1){
      RootbeerGpu.syncthreads();
      if(thid < d){
        int ai = offset*(2*thid+1)-1;
        int bi = offset*(2*thid+2)-1;
        float temp_ai = RootbeerGpu.getSharedFloat(ai*GPUScanConstants.INT_SIZE);
        float temp_bi = RootbeerGpu.getSharedFloat(bi*GPUScanConstants.INT_SIZE);
        RootbeerGpu.setSharedFloat(bi*GPUScanConstants.INT_SIZE, temp_ai + temp_bi);
      }
      offset *= 2;
    }

    if(thid == 0){
      RootbeerGpu.setSharedFloat((n-1)*GPUScanConstants.INT_SIZE, 0);
    }

    for(int d = 1; d < n; d *= 2){
      offset >>= 1;
      RootbeerGpu.syncthreads();
      if(thid < d){
        int ai = offset*(2*thid+1)-1;
        int bi = offset*(2*thid+2)-1;

        float tA = RootbeerGpu.getSharedFloat(ai*GPUScanConstants.INT_SIZE);
        float tB = RootbeerGpu.getSharedFloat(bi*GPUScanConstants.INT_SIZE);

        RootbeerGpu.setSharedFloat(ai*GPUScanConstants.INT_SIZE, tB);
        RootbeerGpu.setSharedFloat(bi*GPUScanConstants.INT_SIZE, tA + tB);
      }
    }

    RootbeerGpu.syncthreads();
    outputData[block_idxx][2*thid] = RootbeerGpu.getSharedFloat((2*thid)*GPUScanConstants.INT_SIZE);
    outputData[block_idxx][2*thid+1] = RootbeerGpu.getSharedFloat((2*thid+1)*GPUScanConstants.INT_SIZE);
  }
}
