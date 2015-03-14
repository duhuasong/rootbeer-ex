package org.trifort.matrix.rbshared;

import org.trifort.matrix.gold.MatrixGold;
import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class MatrixKernel implements Kernel {

  private float[] a;
  private float[] b;
  private float[] c;
  
  private static final int SIZE_FLOAT = 4;
  private static final int TILE_SIZE = 16;
  private static final int SHARED_B_START = TILE_SIZE * TILE_SIZE;
  
  public MatrixKernel(float[] a, float[] b, float[] c){
    this.a = a;
    this.b = b;
    this.c = c;
  }
  
  @Override
  public void gpuMethod() {
    float[] registerA = a;
    float[] registerB = b;
    float[] registerC = c;
    
    int blockIdxx = RootbeerGpu.getBlockIdxx();
    int blockIdxy = RootbeerGpu.getBlockIdxy();
    
    int threadIdxx = RootbeerGpu.getThreadIdxx();
    int threadIdxy = RootbeerGpu.getThreadIdxy();
    
    int width = 2048;
    int aBegin = width * TILE_SIZE * blockIdxy;
    int aEnd = aBegin + width - 1;
    int aStep = TILE_SIZE;
    int bBegin = TILE_SIZE * blockIdxx;
    int bStep = TILE_SIZE * width;
    float sum = 0;
    int arrayIndex = width * threadIdxy + threadIdxx;
    
    int a = aBegin;
    int b = bBegin;
    
    while(a <= aEnd){
      float valueA = registerA[a + arrayIndex];
      float valueB = registerB[b + arrayIndex];
    
      int indexA = ((threadIdxy * TILE_SIZE) + threadIdxx);
      int indexB = SHARED_B_START + ((threadIdxy * TILE_SIZE) + threadIdxx);

      RootbeerGpu.setSharedFloat(indexA * SIZE_FLOAT, valueA);
      RootbeerGpu.setSharedFloat(indexB * SIZE_FLOAT, valueB);
      RootbeerGpu.syncthreads();
      
      for(int k = 0; k < TILE_SIZE; ++k){
        //sum += registerA[i][k] * registerB[k][j];
        
        indexA = ((threadIdxy * TILE_SIZE) + k);
        indexB = SHARED_B_START + ((k * TILE_SIZE) + threadIdxx);
        
        valueA = RootbeerGpu.getSharedFloat(indexA * SIZE_FLOAT);
        valueB = RootbeerGpu.getSharedFloat(indexB * SIZE_FLOAT);
        sum += valueA * valueB;
      }

      RootbeerGpu.syncthreads();

      a += aStep;
      b += bStep;
    }

    int c = width * TILE_SIZE * blockIdxy + TILE_SIZE * blockIdxx;
    registerC[c + arrayIndex] = sum;
  }
}
