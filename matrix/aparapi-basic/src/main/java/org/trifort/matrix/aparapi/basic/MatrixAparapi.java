package org.trifort.matrix.aparapi.basic;

import org.apache.commons.lang3.time.StopWatch;
import org.trifort.matrix.gold.MatrixGold;

import com.amd.aparapi.device.Device;
import com.amd.aparapi.Kernel;
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
    final float[][] a = MatrixGold.createAB();
    final float[][] b = MatrixGold.createAB();
    final float[][] c = new float[size][size];
    
    final int blockCount = 16384;
    final int threadCount = 256;
    
    StopWatch watch = new StopWatch();
    watch.start();

    Device gpuDevice0 = Device.best();
    Range range = gpuDevice0.createRange(blockCount*threadCount);
    Kernel kernel = new Kernel(){
      @Override public void run(){
        int threadId = getGlobalId();
        int i = threadId / size;
        int j = threadId % size;

        float sum = 0;
        for(int k = 0; k < size; ++k){
          sum += a[i][k] * b[k][j];
        }
        c[i][j] = sum;
      }
    };
    kernel.execute(range);
    watch.stop();

    boolean match = MatrixGold.checkC(c);
    if(match){
      System.out.println("TEST PASSES: "+watch.getTime()+"ms");
    } else {
      System.out.println("TEST FAILS: "+watch.getTime()+"ms");
    }
  }

  public static void main(String[] args){
    MatrixAparapi main = new MatrixAparapi();
    main.run();
  }
}