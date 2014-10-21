package org.trifort.rootbeer.examples.matrixshared;

import org.trifort.rootbeer.runtime.util.Stopwatch;
import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.GpuDevice;
import org.trifort.rootbeer.runtime.CacheConfig;
import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.StatsRow;
import org.trifort.rootbeer.runtime.ThreadConfig;
import java.util.List;
import java.util.ArrayList;

public class MatrixApp {

  private float[] matrixA;
  private float[] matrixB;
  private float[] matrixCGPU;
  private float[] matrixCCPU;
  private int dimA;
  private int dimB;
  private Context context;

  private void setup(){
    dimA = 32;
    dimB = 32;

    matrixA = new float[dimA*dimB];
    matrixB = new float[dimA*dimB];
    matrixCGPU = new float[dimA*dimB];
    matrixCCPU = new float[dimA*dimB];

    Rootbeer rootbeer = new Rootbeer();
    List<GpuDevice> devices = rootbeer.getDevices();
    GpuDevice device0 = devices.get(0);
    context = device0.createContext();
    context.setCacheConfig(CacheConfig.PREFER_SHARED);
    context.setThreadConfig(dimA, dimA, dimB, dimB, dimA * dimB);
    context.setKernel(new MatrixKernel(matrixA, matrixB, matrixCGPU));
    context.buildState();

    for(int i = 0; i < dimA; ++i){
      for(int j = 0; j < dimB; ++j){
        matrixA[i * dimB + j] = i * dimB + j;
        matrixB[i * dimB + j] = i * dimB + j;
      }
    }
  }

  private void init(){
    for(int i = 0; i < dimA; ++i){
      for(int j = 0; j < dimB; ++j){
        matrixCGPU[i * dimB + j] = 0;
        matrixCCPU[i * dimB + j] = 0;
      }
    }
  }

  private void cpuRun(){
    long startTime = System.currentTimeMillis();
    for(int i = 0; i < dimA; ++i){
      for(int j = 0; j < dimB; ++j){
        float sum = 0;
        for(int k = 0; k < dimA; ++k){
          sum += matrixA[i * dimB + k] * matrixB[k * dimB + j];
        }
        matrixCGPU[i * dimB + j] = sum;
      }
    }
    long stopTime = System.currentTimeMillis();
    long cpuTime = stopTime - startTime;

    System.out.println("cpu time: "+cpuTime+" ms");
  }

  private void gpuRun(){
    long startTime = System.currentTimeMillis();
    context.run();
    long stopTime = System.currentTimeMillis();
    long gpuTime = stopTime - startTime;

    System.out.println("gpu time: "+gpuTime+" ms");
  }

  public void run(){
    setup();
    for(int i = 0; i < 50; ++i){
      init();
      cpuRun();
      gpuRun();
    }
  }

  public static void main(String[] args){
    MatrixApp app = new MatrixApp();
    app.run();
  }
}
