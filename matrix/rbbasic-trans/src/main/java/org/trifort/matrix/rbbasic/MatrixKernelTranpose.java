package org.trifort.matrix.rbbasic;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class MatrixKernelTranpose implements Kernel {

  private float[][] a;
  private float[][] b;
  private float[][] c;
  
  public MatrixKernelTranpose(float[][] a, float[][] b, float[][] c){
    this.a = a;
    this.b = b;
    this.c = c;
  }
  
  @Override
  public void gpuMethod() {
    float[][] registerA = a;
    float[][] registerB = b;
    float[][] registerC = c;
    int size = registerA.length;
    
    int threadId = RootbeerGpu.getThreadId();
    int i = threadId / size;
    int j = threadId % size;

    float sum = 0;
    for(int k = 0; k < size; ++k){
      sum += registerA[i][k] * registerB[j][k];
    }
    registerC[i][j] = sum;
  }
}
