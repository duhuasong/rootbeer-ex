package org.trifort.rootbeer.sort;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;


public class GPUSortKernel implements Kernel {

  private int[] array;

  public GPUSortKernel(int[] array){
    this.array = array;
  }

  @Override
  public void gpuMethod(){
    int index1a = RootbeerGpu.getThreadIdxx() * 2;
    int index1b = (RootbeerGpu.getThreadIdxx() * 2) + 1;
    int index2a = index1a - 1;
    int index2b = index1b - 1;

    RootbeerGpu.setSharedInteger(4*index1a,array[index1a]);
    RootbeerGpu.setSharedInteger(4*index1b,array[index1b]);
    //outer pass
    for(int i = 0; i < array.length; ++i){
      int value1 = RootbeerGpu.getSharedInteger(4*index1a);
      int value2 = RootbeerGpu.getSharedInteger(4*index1b);
      if(value2 < value1){
        RootbeerGpu.setSharedInteger(4*index1a, value2);
        RootbeerGpu.setSharedInteger(4*index1b, value1);
      }
      RootbeerGpu.syncthreads();
      if(index2a >= 0){
        value1 = RootbeerGpu.getSharedInteger(4*index2a);
        value2 = RootbeerGpu.getSharedInteger(4*index2b);
        if(value2 < value1){
          RootbeerGpu.setSharedInteger(4*index2a, value2);
          RootbeerGpu.setSharedInteger(4*index2b, value1);
        }
      }
      RootbeerGpu.syncthreads();
    }
    array[index1a] = RootbeerGpu.getSharedInteger(4*index1a);
    array[index1b] = RootbeerGpu.getSharedInteger(4*index1b);
  }
}
