package org.trifort.matrix.aparapi.shared;

import org.apache.commons.lang3.time.StopWatch;
import org.trifort.matrix.gold.MatrixGold;

import com.amd.aparapi.device.Device;
import com.amd.aparapi.exception.DeprecatedException;
import com.amd.aparapi.Kernel;
import com.amd.aparapi.Kernel.PrivateMemorySpace;
import com.amd.aparapi.Range;

public class MatrixAparapi {

  private void clearArray(float[][] array){
    for(int i = 0; i < array.length; ++i){
      for(int j = 0; j < array[0].length; ++j){
        array[i][j] = 0;
      }
    }
  }
  
  public void run(){
    final int size = MatrixGold.size();
    final float[] matrixA = MatrixGold.createABSingle();
    final float[] matrixB = MatrixGold.createABSingle();
    final float[] matrixC = new float[size*size];

    final int blockCountX = 128;
    final int blockCountY = 128;
    final int threadCountX = 16;
    final int threadCountY = 16;
    
    StopWatch watch = new StopWatch();
    watch.start();
    
    Device gpuDevice0 = Device.best();
    Range range = gpuDevice0.createRange2D(blockCountX*threadCountX, 
        blockCountY*threadCountY);
    Kernel kernel = new Kernel(){

      final int TILE_SIZE = 16;
      final int SHARED_SIZE = TILE_SIZE * TILE_SIZE;
      protected @PrivateMemorySpace(2*SHARED_SIZE) float[] shared = new float[2*SHARED_SIZE];
      
      @Override 
      public void run(){

        int threadIdx = getGlobalId(0);
        int threadIdy = getGlobalId(1);
        
        int blockIdxx = threadIdx / TILE_SIZE;
        int blockIdxy = threadIdy / TILE_SIZE;
        
        int threadIdxx = threadIdx % TILE_SIZE;
        int threadIdxy = threadIdy % TILE_SIZE;
        
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
          float valueA = matrixA[a + arrayIndex];
          float valueB = matrixB[b + arrayIndex];
          
          int indexA = ((threadIdxy * TILE_SIZE) + threadIdxx);
          int indexB = SHARED_SIZE + ((threadIdxy * TILE_SIZE) + threadIdxx);

          shared[indexA] = valueA;
          shared[indexB] = valueB;
          
          localBarrier();
          
          for(int k = 0; k < TILE_SIZE; ++k){
            //sum += registerA[i][k] * registerB[k][j];
            
            indexA = ((threadIdxy * TILE_SIZE) + k);
            indexB = SHARED_SIZE + ((k * TILE_SIZE) + threadIdxx);
            valueA = shared[indexA];
            valueB = shared[indexB];
            
            sum += valueA * valueB;
          }
          
          localBarrier();
          
          a += aStep;
          b += bStep;
        }
        
        int c = width * TILE_SIZE * blockIdxy + TILE_SIZE * blockIdxx;
        matrixC[c + arrayIndex] = sum;
      }
    };
    kernel.execute(range);
    watch.stop();

    boolean match = MatrixGold.checkCSingle(matrixC);
    if(match){
      System.out.println("TEST PASSES: "+watch.getTime()+"ms");
    } else {
      System.out.println("TEST FAILS: "+watch.getTime()+"ms");
    }
    System.out.println("running on the gpu: "+kernel.getExecutionMode());
  }

  public static void main(String[] args){
    MatrixAparapi main = new MatrixAparapi();
    main.run();
  }
}