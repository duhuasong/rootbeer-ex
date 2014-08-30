package org.trifort.rootbeer.sort;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;


public class GPUSortKernel implements Kernel {

  private int[][] arrays;

  public GPUSortKernel(int[][] arrays){
    this.arrays = arrays;
  }

  @Override
  public void gpuMethod(){
    int[] array = arrays[RootbeerGpu.getBlockIdxx()];
    int index1a = RootbeerGpu.getThreadIdxx() * 2;
    int index1b = index1a + 1;
    int index2a = index1a - 1;
    int index2b = index1a;

    RootbeerGpu.setSharedInteger(index1a,array[index1a]);
    RootbeerGpu.setSharedInteger(index1b,array[index1b]);
    //outer pass
    int arrayLength = array.length;
    for(int i = 0; i < arrayLength; ++i){
      int value1 = RootbeerGpu.getSharedInteger(index1a);
      int value2 = RootbeerGpu.getSharedInteger(index1b);
      if(value2 < value1){
        RootbeerGpu.setSharedInteger(index1a, value2);
        RootbeerGpu.setSharedInteger(index1b, value1);
      }
      RootbeerGpu.syncthreads();
      if(index2a >= 0){
        value1 = RootbeerGpu.getSharedInteger(index2a);
        value2 = RootbeerGpu.getSharedInteger(index2b);
        if(value2 < value1){
          RootbeerGpu.setSharedInteger(index2a, value2);
          RootbeerGpu.setSharedInteger(index2b, value1);
        }
      }
      RootbeerGpu.syncthreads();
    }
    array[index1a] = RootbeerGpu.getSharedInteger(index1a);
    array[index1b] = RootbeerGpu.getSharedInteger(index1b);
  }
}
