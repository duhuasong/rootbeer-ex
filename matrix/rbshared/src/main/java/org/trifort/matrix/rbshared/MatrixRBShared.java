package org.trifort.matrix.rbshared;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.trifort.matrix.gold.MatrixGold;
import org.trifort.rootbeer.runtime.CacheConfig;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.GpuDevice;
import org.trifort.rootbeer.runtime.Rootbeer;

public class MatrixRBShared {

  private void clearArray(float[][] array){
    for(int i = 0; i < array.length; ++i){
      for(int j = 0; j < array[0].length; ++j){
        array[i][j] = 0;
      }
    }
  }
  
  public void run(){
    int size = MatrixGold.size();
    float[][] a = MatrixGold.createAB();
    float[][] b = MatrixGold.createAB();
    float[][] c = new float[size][size];
    
    int blockCountX = 64;
    int blockCountY = 64;
    int threadCountX = 32;
    int threadCountY = 32;
    int numThreads = blockCountX * blockCountY * threadCountX * threadCountY;
    
    Rootbeer rootbeer = new Rootbeer();
    List<GpuDevice> devices = rootbeer.getDevices();
    GpuDevice device0 = devices.get(0);
    Context context0 = device0.createContext();
    context0.setCacheConfig(CacheConfig.PREFER_SHARED);
    context0.setThreadConfig(threadCountX, threadCountY, blockCountX, blockCountY, numThreads);
    context0.setKernel(new MatrixKernel(a, b, c));
    context0.buildState();
 
    for(int i = 0; i < 8; ++i){
      clearArray(c);
      StopWatch watch = new StopWatch();
      watch.start();
      context0.run();
      watch.stop();

      boolean match = MatrixGold.checkC(c);
      if(match){
        System.out.println("TEST PASSES: "+watch.getTime()+"ms");
      }
    }
  }

  public static void main(String[] args){
    MatrixRBShared engine = new MatrixRBShared();
    try {
      engine.run();
    } catch(Exception ex){
      ex.printStackTrace();
    }
  }
}
