package org.trifort.rootbeer.examples.matrixshared;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

import java.util.List;
import java.util.ArrayList;

public class MatrixKernel implements Kernel {

  private float[] matrixA;
  private float[] matrixB;
  private float[] matrixCGPU;
  private final static int FLOAT_SIZE = 4;
  private final static int BLOCK_SIZE = 32;
  private final static int wA = 32;
  private final static int wB = 32;

  public MatrixKernel(float[] matrixA, float[] matrixB, float[] matrixCGPU){
    this.matrixA = matrixA;
    this.matrixB = matrixB;
    this.matrixCGPU = matrixCGPU;
  }

  public void gpuMethod(){
    int bx = RootbeerGpu.getBlockIdxx();
    int by = RootbeerGpu.getBlockIdxy();
    int tx = RootbeerGpu.getThreadIdxx();
    int ty = RootbeerGpu.getThreadIdxy();

    int aBegin = wA * BLOCK_SIZE * by;
    int aEnd = aBegin + wA - 1;
    int aStep = BLOCK_SIZE;
    int bBegin = BLOCK_SIZE * bx;
    int bStep  = BLOCK_SIZE * wB;
    float Csub = 0;

    for (int a = aBegin, b = bBegin;
         a <= aEnd;
         a += aStep, b += bStep)
    {

      float aValue = matrixA[a + wA * ty + tx];
      float bValue = matrixB[b + wB * ty + tx];

      int sharedArrayIndex = ty * BLOCK_SIZE + tx;
      int aStart = 0;
      int bStart = BLOCK_SIZE * BLOCK_SIZE;
      int aIndex = aStart + sharedArrayIndex;
      int bIndex = bStart + sharedArrayIndex;

      RootbeerGpu.setSharedFloat(aIndex * FLOAT_SIZE, aValue);
      RootbeerGpu.setSharedFloat(bIndex * FLOAT_SIZE, bValue);

      RootbeerGpu.syncthreads();

      for (int k = 0; k < BLOCK_SIZE; ++k)
      {
        aValue = RootbeerGpu.getSharedFloat((aStart + (ty * BLOCK_SIZE + k)) * FLOAT_SIZE);
        bValue = RootbeerGpu.getSharedFloat((bStart + (k * BLOCK_SIZE + tx)) * FLOAT_SIZE);
        Csub += aValue * bValue;
      }

      RootbeerGpu.syncthreads();
    }

    int c = wB * BLOCK_SIZE * by + BLOCK_SIZE * bx;
    matrixCGPU[c + wB * ty + tx] = Csub;
  }
}
