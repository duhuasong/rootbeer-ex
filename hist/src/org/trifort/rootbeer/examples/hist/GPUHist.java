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
      ret[i] = (byte) (i & 0xf);
    }
    return ret;
  }

  public void run(){
    int size = GPUHistConstants.THREAD_N;
    byte[] input = newArray(GPUHistConstants.DATA_N);
    int[] result = new int[GPUHistConstants.HISTOGRAM_SIZE];

    Rootbeer rootbeer = new Rootbeer();
    List<GpuDevice> devices = rootbeer.getDevices();
    GpuDevice device0 = devices.get(0);
    Context context0 = device0.createContext();
    context0.setCacheConfig(CacheConfig.PREFER_SHARED);
    context0.setThreadConfig(size, 1, size);
    context0.setKernel(new GPUHistKernel(input, result));
    context0.buildState();

    while(true){
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
    }
  }

  public static void main(String[] args){
    GPUHist hist = new GPUHist();
    hist.run();
  }
}
