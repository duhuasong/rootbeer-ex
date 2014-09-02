package org.trifort.rootbeer.examples.hist;

import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.GpuDevice;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.ThreadConfig;
import org.trifort.rootbeer.runtime.StatsRow;
import org.trifort.rootbeer.runtime.CacheConfig;
import org.trifort.rootbeer.runtime.Kernel;

import java.util.List;
import java.util.Random;

public class GPUHist {

  public GPUHist(){

  }

  private void histCPU(byte[] data, int[] result){
    for(int i = 0; i < data.length; ++i){
      result[(data[i] >> 2) & 0x3F]++;
    }
  }

  private void verify(int[] resultCPU, int[] resultGPU){
    for(int i = 0; i < resultCPU.length; ++i){
      int cpu_value = resultCPU[i];
      int gpu_value = resultGPU[i];

      if(cpu_value != gpu_value){
        System.out.println("VERIFY FAILED");
        System.out.println("cpu_value: "+cpu_value);
        System.out.println("gpu_value: "+gpu_value);
        System.out.println("i: "+i);
        throw new RuntimeException();
      }
    }
    System.out.println("VERIFY PASSED");
  }

  private int iDivUp(int a, int b){
    return (a % b != 0) ? (a / b + 1) : (a / b);
  }

  private int iSnapDown(int a, int b){
    return a - a % b;
  }

  public void run(){
    int byteCount = 64 * 1048576;
    int dataCount = 1048576;
    int histogramCount = iDivUp(byteCount,
      GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE *
      iSnapDown(255, 4 * GPUHistConstants.INT_SIZE));

    byte[] inputData0 = new byte[dataCount];
    byte[] inputData1 = new byte[dataCount];
    byte[] inputData2 = new byte[dataCount];
    byte[] inputData3 = new byte[dataCount];

    Random random = new Random(2009);
    for(int i = 0; i < dataCount; ++i){
      inputData0[i] = (byte) (random.nextInt() % 256);
      inputData1[i] = (byte) (random.nextInt() % 256);
      inputData2[i] = (byte) (random.nextInt() % 256);
      inputData3[i] = (byte) (random.nextInt() % 256);
    }

    int[] partialHistograms = new int[GPUHistConstants.MAX_PARTIAL_HISTOGRAM64_COUNT
      * GPUHistConstants.HISTOGRAM64_BIN_COUNT];

    int[] resultGPU = new int[GPUHistConstants.HISTOGRAM64_BIN_COUNT];
    int[] resultCPU = new int[GPUHistConstants.HISTOGRAM64_BIN_COUNT];

    Rootbeer rootbeer = new Rootbeer();
    List<GpuDevice> devices = rootbeer.getDevices();
    GpuDevice device0 = devices.get(0);
    Context context0 = device0.createContext();
    context0.setCacheConfig(CacheConfig.PREFER_SHARED);
    context0.setThreadConfig(GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE, histogramCount,
      histogramCount * GPUHistConstants.HISTOGRAM64_THREADBLOCK_SIZE);
    context0.setKernel(new GPUHistogram64Kernel(partialHistograms, inputData0,
      inputData1, inputData2, inputData3, dataCount));
    context0.buildState();

    while(true){
      for(int i = 0; i < GPUHistConstants.HISTOGRAM64_BIN_COUNT; ++i){
        resultGPU[i] = 0;
        resultCPU[i] = 0;
      }

      long gpuStart = System.currentTimeMillis();
      context0.run();
      long gpuStop = System.currentTimeMillis();
      long gpuTime = gpuStop - gpuStart;

      StatsRow row0 = context0.getStats();
      System.out.println("serialization_time: "+row0.getSerializationTime());
      System.out.println("execution_time: "+row0.getExecutionTime());
      System.out.println("deserialization_time: "+row0.getDeserializationTime());
      System.out.println("gpu_required_memory: "+context0.getRequiredMemory());
      System.out.println("gpu_time: "+gpuTime);

      long cpuStart = System.currentTimeMillis();
      histCPU(inputData0, resultCPU);
      histCPU(inputData1, resultCPU);
      histCPU(inputData2, resultCPU);
      histCPU(inputData3, resultCPU);
      long cpuStop = System.currentTimeMillis();
      long cpuTime = cpuStop - cpuStart;
      System.out.println("cpu_time: "+cpuTime);
      double ratio = (double) cpuTime / (double) gpuTime;
      System.out.println("ratio: "+ratio);

      //verify(resultCPU, resultGPU);
    }
  }

  public static void main(String[] args){
    GPUHist hist = new GPUHist();
    hist.run();
  }
}
