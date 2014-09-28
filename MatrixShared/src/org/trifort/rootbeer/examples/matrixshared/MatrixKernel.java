package org.trifort.rootbeer.examples.matrixshared;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

import java.util.List;
import java.util.ArrayList;

public class MatrixKernel implements Kernel {

  private float[][] matrixA;
  private float[][] matrixB;
  private float[][] matrixCGPU;
  private const int FLOAT_SIZE = 4;
  private const int TILE_WIDTH = 32;
  private const int SUBMATRIX_SIZE = TILE_WIDTH*TILE_WIDTH;
  private int numSubmatrices;

  public MatrixKernel(float[][] matrixA, float[][] matrixB, float[][] matrixCGPU){
    this.matrixA = matrixA;
    this.matrixB = matrixB;
    this.matrixCGPU = matrixCGPU;

    numSubmatrices = matrixA.length = SUBMATRIX_SIZE;
  }

  public void gpuMethod(){
    int thread_idxx = RootbeerGpu.getThreadIdxx();
    int block_idxx = RootbeerGpu.getBlockIdxx();
    int dim = RootbeerGpu.getBlockDimx();

    int localnumSubmatrices = numSubmatrices;
    for(int i = 0; i < localnumSubmatrices; ++i){
      int matrixARow = (i * TILE_WIDTH) + thread_idxx;
    }

    float sum = 0;
    for(int k = 0; k < dim; ++k){
      sum += matrixA[block_idxx][k] * matrixB[k][thread_idxx];
    }
    matrixCGPU[block_idxx][thread_idxx] = sum;
  }
}
