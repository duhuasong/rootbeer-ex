package org.trifort.pattern_sync;

import java.util.List;

import org.trifort.rootbeer.runtime.CacheConfig;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.GpuDevice;
import org.trifort.rootbeer.runtime.Rootbeer;

public class Main {

  public void runTest(){

    int signalLength = 8000;
    int numStates = 196;
    int numThreads = 14;
    int numBlocks = 14;
    int memorySize1 = 157485664;
    double[][] delta = new double[signalLength][numStates];
    
    for (int i = 0; i < numStates; i++) {
      delta[0][i] = 1.0;
    }

    int[] barrier = new int[numBlocks];
    PatternSyncKernel kernel = new PatternSyncKernel(delta, barrier);
    Rootbeer rootbeer = new Rootbeer();
    List<GpuDevice> devices = rootbeer.getDevices();
    GpuDevice device0 = devices.get(0);
    Context context = device0.createContext(memorySize1);
    context.setCacheConfig(CacheConfig.PREFER_L1);
    context.setThreadConfig(numThreads, numBlocks, numStates);
    context.setKernel(kernel);
    context.buildState();
    context.run();
    
    System.out.println("last_sum: "+delta[signalLength-1][0]);
    context.close();
  }
  
  public void runTest1(){
    int numStates = 784;
    double[][] delta = new double[8000][numStates];
    
    for (int i = 0; i < numStates; i++) {
      delta[0][i] = 1;
    }
    
    for(int i = 1; i < delta.length; ++i){
      double sum = 0;
      for(int j = 1; j < numStates; ++j){
        sum += delta[i-1][j];
      }
      sum /= 784.0;
      for(int j = 0; j < numStates; ++j){
        delta[i][j] = sum;
      }
    }
    
    System.out.println("last_sum: "+delta[7999][0]);
  }
  
  public void run(){
    //for(int i = 0; i < 250; ++i){
      runTest();
    //}
  }
  
  public static void main(String[] args){
    Main main = new Main();
    main.run();
  }
}
