package org.trifort.matrix.rbshared;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.trifort.matrix.gold.MatrixGold;
import org.trifort.rootbeer.runtime.CacheConfig;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.GpuDevice;
import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.StatsRow;

public class MatrixRBShared {

  private void clearArray(float[] array){
    for(int i = 0; i < array.length; ++i){
      array[i] = 0;
    }
  }
  
  public void run(){
    int size = MatrixGold.size();
    float[] a = MatrixGold.createABSingle();
    float[] b = MatrixGold.createABSingle();
    float[] c = new float[size*size];
    
    int blockCountX = 128;
    int blockCountY = 128;
    int threadCountX = 16;
    int threadCountY = 16;
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

      boolean match = MatrixGold.checkCSingle(c);
      if(match){
        System.out.println("TEST PASSES: "+watch.getTime()+"ms");
      } else {
        System.out.println("TEST FAILS: "+watch.getTime()+"ms");
      }
      
      StatsRow row = context0.getStats();
      System.out.println("Java Serialization Time: "+row.getSerializationTime()+"ms");
      System.out.println("JNI Driver Memcpy to Device: "+row.getDriverMemcopyToDeviceTime()+"ms");
      System.out.println("GPU Execution Time: "+row.getDriverExecTime()+"ms");
      System.out.println("JNI Driver Memcpy from Device: "+row.getDriverMemcopyFromDeviceTime()+"ms");
      System.out.println("Java Deserialization Time: "+row.getDeserializationTime()+"ms");
      
      
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
