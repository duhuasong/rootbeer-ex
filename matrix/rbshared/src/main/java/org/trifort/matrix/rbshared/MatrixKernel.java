package org.trifort.matrix.rbshared;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class MatrixKernel implements Kernel {

  private float[][] a;
  private float[][] b;
  private float[][] c;
  
  private static final int SIZE_FLOAT = 4;
  private static final int TILE_SIZE = 32;
  private static final int SHARED_B_START = TILE_SIZE * TILE_SIZE;
  
  public MatrixKernel(float[][] a, float[][] b, float[][] c){
    this.a = a;
    this.b = b;
    this.c = c;
  }
  
  @Override
  public void gpuMethod() {
    float[][] registerA = a;
    float[][] registerB = b;
    float[][] registerC = c;
    
    int blockIdxx = RootbeerGpu.getBlockIdxx();
    int blockIdxy = RootbeerGpu.getBlockIdxy();
    
    int threadIdxx = RootbeerGpu.getThreadIdxx();
    int threadIdxy = RootbeerGpu.getThreadIdxy();
    
    int i = (blockIdxy * TILE_SIZE) + threadIdxy;
    int j = (blockIdxx * TILE_SIZE) + threadIdxx;

    float valueA = registerA[i][j];
    float valueB = registerB[i][j];
  
    int indexA = ((threadIdxy * TILE_SIZE) + threadIdxx);
    int indexB = SHARED_B_START + ((threadIdxy * TILE_SIZE) + threadIdxx);
  
    RootbeerGpu.setSharedFloat(indexA, valueA);
    RootbeerGpu.setSharedFloat(indexB, valueB);
    RootbeerGpu.syncthreads();
    
    float sum = 0;
    for(int k = 0; k < TILE_SIZE; ++k){
      //sum += registerA[i][k] * registerB[j][k];
      
      indexA = ((threadIdxy * TILE_SIZE) + k);
      indexB = SHARED_B_START + ((threadIdxx * TILE_SIZE) + k);
      
      valueA = RootbeerGpu.getSharedFloat(indexA);
      valueB = RootbeerGpu.getSharedFloat(indexB);
      sum += valueA * valueB;
    }
    
    RootbeerGpu.atomicAddGlobal(registerC[i], j, sum);
  }
}
