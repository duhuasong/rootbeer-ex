package org.trifort.matrix.rbbasic;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class MatrixKernelTranpose implements Kernel {

  private double[][] a;
  private double[][] b;
  private double[][] c;
  
  public MatrixKernelTranpose(double[][] a, double[][] b, double[][] c){
    this.a = a;
    this.b = b;
    this.c = c;
  }
  
  @Override
  public void gpuMethod() {
    double[][] registerA = a;
    double[][] registerB = b;
    double[][] registerC = c;
    int size = registerA.length;
    
    int threadId = RootbeerGpu.getThreadId();
    int i = threadId / size;
    int j = threadId % size;

    double sum = 0;
    for(int k = 0; k < size; ++k){
      sum += registerA[i][k] * registerB[j][k];
    }
    registerC[i][j] = sum;
  }
}
