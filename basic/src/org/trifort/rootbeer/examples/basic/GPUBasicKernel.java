package org.trifort.rootbeer.examples.basic;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class GPUBasicKernel implements Kernel {

  private float[] data;

  public GPUBasicKernel(float[] data){
    this.data = data;
  }

  @Override
  public void gpuMethod(){
    data[RootbeerGpu.getThreadIdxx()] *= 2;
  }
}
