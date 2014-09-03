package org.trifort.rootbeer.examples.hist;

import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.GpuDevice;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.ThreadConfig;
import org.trifort.rootbeer.runtime.StatsRow;
import org.trifort.rootbeer.runtime.CacheConfig;
import org.trifort.rootbeer.runtime.Kernel;

import java.util.List;

public class GPUHist {

  public GPUHist(){

  }

  private byte[] newArray(int size){
    byte[] ret = new byte[size];
    for(int i = 0; i < size; ++i){
      ret[i] = (byte) (i & 0x1f);
    }
    return ret;
  }

  private void histCPU(byte[] data, int[] result){
    for(int i = 0; i < GPUHistConstants.DATA_N; ++i){
      byte item = data[i];
      result[(item >> 2) & 0x3F]++;
    }
  }

  private void verify(int[][] resultCPU, int[][] resultGPU){
    for(int i = 0; i < resultCPU.length; ++i){
      int[] subCPU = resultCPU[i];
      int[] subGPU = resultGPU[i];
      for(int j = 0; j < subCPU.length; ++j){
        int cpu_value = subCPU[j];
        int gpu_value = subGPU[j];

        if(cpu_value != gpu_value){
          System.out.println("VERIFY FAILED");
          System.out.println("cpu_value: "+cpu_value);
          System.out.println("gpu_value: "+gpu_value);
          System.out.println("i: "+i+" j: "+j);
          return;
        }
      }
    }
    System.out.println("VERIFY PASSED");
  }

  public void run(){
    int size = GPUHistConstants.THREAD_N;
    int numSMs = 2;
    int blocksPerSM = 2;
    int blockSize = numSMs * blocksPerSM;
    byte[][] input = new byte[blockSize][];
    for(int i = 0; i < blockSize; ++i){
      input[i] = newArray(GPUHistConstants.DATA_N);
    }
    int[][] resultGPU = new int[blockSize][GPUHistConstants.BIN_COUNT];
    int[][] resultCPU = new int[blockSize][GPUHistConstants.BIN_COUNT];

    Rootbeer rootbeer = new Rootbeer();
    List<GpuDevice> devices = rootbeer.getDevices();
    GpuDevice device0 = devices.get(0);
    Context context0 = device0.createContext();
    context0.setCacheConfig(CacheConfig.PREFER_SHARED);
    context0.setThreadConfig(size, blockSize, blockSize * size);
    context0.setKernel(new GPUHistKernel(input, resultGPU));
    context0.buildState();

    histCPU(input[0], resultCPU[0]);
    System.out.println("resultCPU[0][0]: "+resultCPU[0][0]);

    while(true){
      for(int i = 0; i < blockSize; ++i){
        for(int j = 0; j < GPUHistConstants.BIN_COUNT; ++j){
          resultGPU[i][j] = 0;
          resultCPU[i][j] = 0;
        }
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
      for(int i = 0; i < blockSize; ++i){
        histCPU(input[i], resultCPU[i]);
      }
      long cpuStop = System.currentTimeMillis();
      long cpuTime = cpuStop - cpuStart;
      System.out.println("cpu_time: "+cpuTime);
      double ratio = (double) cpuTime / (double) gpuTime;
      System.out.println("ratio: "+ratio);

      verify(resultCPU, resultGPU);
    }
  }

  public static void main(String[] args){
    GPUHist hist = new GPUHist();
    hist.run();
  }
}
