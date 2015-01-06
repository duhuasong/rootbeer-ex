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
  private static final int TILES_PER_BLOCK = 4;
  private static final int MATRIX_SIZE = 2048;
  private static final int TILES_PER_MATRIX = MATRIX_SIZE / TILE_SIZE;
  
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
    int blockIdxx = RootbeerGpu.getBlockIdxx();

    int tileRowA = i / TILES_PER_MATRIX;
    int tileColB = j / TILES_PER_MATRIX;
    
    int tileRowAStart = tileRowA * TILE_SIZE;
    int tileColBStart = tileColB * TILE_SIZE; 
    
    double sum = 0;
    
    for(int tileA = 0; tileA < TILES_PER_BLOCK; ++tileA){
      for(int tileB = 0; tileB < TILES_PER_BLOCK; ++tileB){
        for(int tileLoadRow = 0; tileLoadRow < TILE_SIZE; tileLoadRow += 2){
          int tileLoadRowStart = threadIdxx / 2;
          int tileLoadColStart = threadIdxx / 4;
          int indexRow = tileRowAStart + tileLoadRowStart;
          int indexCol = tileColBStart + tileLoadColStart;
          double valueA = registerA[indexRow][indexCol];  
          
          long valueALong = Double.doubleToLongBits(valueA);
          //bitmask to find int0 and int1
          
          
        }
      }
    }
    
    //for(int k = 0; k < size; ++k){
    //  sum += registerA[i][k] * registerB[j][k];
    //}
    
    RootbeerGpu.atomicAddGlobal(registerC[i], j, sum);
  }
}
