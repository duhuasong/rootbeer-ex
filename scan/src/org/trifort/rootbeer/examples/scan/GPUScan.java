package org.trifort.rootbeer.examples.scan;

import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.GpuDevice;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.ThreadConfig;
import org.trifort.rootbeer.runtime.StatsRow;
import org.trifort.rootbeer.runtime.CacheConfig;
import org.trifort.rootbeer.runtime.Kernel;

import java.util.List;

public class GPUScan {

  public GPUScan(){

  }

  private int[] newArray(int size){
    int[] ret = new int[size];
    for(int i = 0; i < size; ++i){
      ret[i] = i;
    }
    return ret;
  }

  private void scanCPU(int[] inputData, int[] outputData){
    outputData[0] = 0;
    for(int k = 1; k < inputData.length; ++k){
      outputData[k] = inputData[k-1] + outputData[k-1];
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
        return;
      }
    }
    System.out.println("VERIFY PASSED");
  }

  public void run(){
    int threadSize = GPUScanConstants.THREAD_SIZE;
    int blockSize = GPUScanConstants.BLOCK_SIZE;

    System.out.println("threadSize: "+threadSize);
    System.out.println("blockSize: "+blockSize);

    int[][] input = new int[blockSize][];
    int[][] resultGPU = new int[blockSize][];
    int[][] resultCPU = new int[blockSize][];

    for(int i = 0; i < blockSize; ++i){
      input[i] = newArray(GPUScanConstants.DATA_N);
      resultGPU[i] = new int[GPUScanConstants.DATA_N];
      resultCPU[i] = new int[GPUScanConstants.DATA_N];
    }

    Rootbeer rootbeer = new Rootbeer();
    List<GpuDevice> devices = rootbeer.getDevices();
    GpuDevice device0 = devices.get(0);
    Context context0 = device0.createContext(67698864);
    context0.setCacheConfig(CacheConfig.PREFER_SHARED);
    context0.setThreadConfig(threadSize, blockSize, threadSize * blockSize);
    context0.setKernel(new GPUScanKernel(input, resultGPU));
    context0.buildState();

    while(true){
      for(int k = 0; k < blockSize; ++k){
        for(int i = 0; i < GPUScanConstants.DATA_N; ++i){
          resultGPU[k][i] = 0;
          resultCPU[k][i] = 0;
        }
      }

      long gpuStart = System.currentTimeMillis();
      context0.run();
      long gpuStop = System.currentTimeMillis();
      long gpuTime = gpuStop - gpuStart;

      StatsRow row0 = context0.getStats();
      System.out.println("serialization_time: "+row0.getSerializationTime());
      System.out.println("driver_memcopy_to_device_time: "+row0.getDriverMemcopyToDeviceTime());
      System.out.println("driver_execution_time: "+row0.getDriverExecTime());
      System.out.println("driver_memcopy_from_device_time: "+row0.getDriverMemcopyFromDeviceTime());
      System.out.println("total_driver_execution_time: "+row0.getTotalDriverExecutionTime());
      System.out.println("deserialization_time: "+row0.getDeserializationTime());
      System.out.println("gpu_required_memory: "+context0.getRequiredMemory());
      System.out.println("gpu_time: "+gpuTime);

      long cpuStart = System.currentTimeMillis();
      for(int i = 0; i < blockSize; ++i){
        scanCPU(input[i], resultCPU[i]);
      }
      long cpuStop = System.currentTimeMillis();
      long cpuTime = cpuStop - cpuStart;
      System.out.println("cpu_time: "+cpuTime);
      double ratio = (double) cpuTime / (double) gpuTime;
      System.out.println("ratio: "+ratio);

      verify(resultCPU[0], resultGPU[0]);
      //printArray("cpu", resultCPU);
      //printArray("gpu", resultGPU);
    }
  }

  private void printArray(String header, int[] array){
    System.out.println("printArray: "+header);
    for(int value : array){
      System.out.print(value+" ");
    }
  }

  public static void main(String[] args){
    GPUScan scan = new GPUScan();
    scan.run();
  }
}
