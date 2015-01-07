package org.trifort.matrix.rbshared;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class MatrixKernel implements Kernel {

  private double[][] a;
  private double[][] b;
  private double[][] c;
  
  private static final int SIZE_DOUBLE = 8;
  private static final int TILE_SIZE = 32;
  private static final int SHARED_B_START = TILE_SIZE * TILE_SIZE * SIZE_DOUBLE;
  
  public MatrixKernel(double[][] a, double[][] b, double[][] c){
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
    
    int threadIdxx = RootbeerGpu.getThreadIdxx();
    int threadIdxy = RootbeerGpu.getThreadIdxy();
    
    double valueA = registerA[i][j];
    double valueB = registerB[i][j];
    
    int indexA = ((threadIdxy * TILE_SIZE) + threadIdxx) * SIZE_DOUBLE;
    int indexB = (SHARED_B_START + ((threadIdxy * TILE_SIZE) + threadIdxx)) * SIZE_DOUBLE;
    
    if(threadIdxx == 2 && threadIdxy == 8){
      int blockIdxx = RootbeerGpu.getBlockIdxx();
      int blockIdxy = RootbeerGpu.getBlockIdxy();
      
      if(blockIdxx == 6 && blockIdxy == 0){
        System.out.println("indexA: "+indexA);
        System.out.println("indexB: "+indexB);
      }
    }
    
    RootbeerGpu.setSharedDouble(indexA, valueA);
    RootbeerGpu.setSharedDouble(indexB, valueB);
    RootbeerGpu.syncthreads();
    
    double sum = 0;
    for(int k = 0; k < size; ++k){
      //sum += registerA[i][k] * registerB[j][k];
      
      indexA = ((threadIdxy * TILE_SIZE) + k) * SIZE_DOUBLE;
      indexB = ((threadIdxy * TILE_SIZE) + k) * SIZE_DOUBLE;
      
      valueA = RootbeerGpu.getSharedDouble(indexA);
      valueB = RootbeerGpu.getSharedDouble(indexB);
      sum += valueA * valueB;
    }
    
    //RootbeerGpu.atomicAddGlobal(registerC[i], j, sum);
  }
}