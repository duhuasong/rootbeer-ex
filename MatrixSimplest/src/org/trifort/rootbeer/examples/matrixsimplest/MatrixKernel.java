package org.trifort.rootbeer.examples.matrixsimplest;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

import java.util.List;
import java.util.ArrayList;

public class MatrixKernel implements Kernel {

  private float[][] matrixA;
  private float[][] matrixB;
  private float[][] matrixCGPU;

  public MatrixKernel(float[][] matrixA, float[][] matrixB, float[][] matrixCGPU){

    this.matrixA = matrixA;
    this.matrixB = matrixB;
    this.matrixCGPU = matrixCGPU;
  }

  public void gpuMethod(){
    int thread_idxx = RootbeerGpu.getThreadIdxx();
    int block_idxx = RootbeerGpu.getBlockIdxx();
    int dim = RootbeerGpu.getBlockDimx();

    float sum = 0;
    for(int k = 0; k < dim; ++k){
      sum += matrixA[block_idxx][k] * matrixB[k][thread_idxx];
    }
    matrixCGPU[block_idxx][thread_idxx] = sum;
  }
}
