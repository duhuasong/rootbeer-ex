package org.trifort.rootbeer.sort;

import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.GpuDevice;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.ThreadConfig;
import org.trifort.rootbeer.runtime.StatsRow;
import org.trifort.rootbeer.runtime.CacheConfig;
import java.util.List;
import java.util.Arrays;

public class GPUSort {

  private int[] newReversedArray(int size){
    int[] ret = new int[size];

    for(int i = 0; i < size; ++i){
      ret[size-1-i] = i;
    }
    return ret;
  }

  public void sort(){
    int size = 2048;
    int sizeBy2 = size / 2;
    int numMultiProcessors = 2;
    int blocksPerMultiProcessor = 1024;
    int outerCount = numMultiProcessors*blocksPerMultiProcessor;
    int[][] array = new int[outerCount][];
    for(int i = 0; i < outerCount; ++i){
      array[i] = newReversedArray(size);
    }

    GPUSortKernel kernel = new GPUSortKernel(array);
    Rootbeer rootbeer = new Rootbeer();
    List<GpuDevice> devices = rootbeer.getDevices();
    GpuDevice device0 = devices.get(0);
    Context context0 = device0.createContext();
    context0.setCacheConfig(CacheConfig.PREFER_SHARED);
    ThreadConfig config0 = new ThreadConfig(sizeBy2, outerCount, sizeBy2);
    long gpuStart = System.currentTimeMillis();
    rootbeer.run(kernel, config0, context0);
    long gpuStop = System.currentTimeMillis();
    long gpuTime = gpuStop - gpuStart;

    List<StatsRow> stats = context0.getStats();
    StatsRow row0 = stats.get(0);
    System.out.println("serialization_time: "+row0.getSerializationTime());
    System.out.println("execution_time: "+row0.getExecutionTime());
    System.out.println("deserialization_time: "+row0.getDeserializationTime());
    System.out.println("gpu_time: "+gpuTime);

    for(int i = 0; i < outerCount; ++i){
      array[i] = newReversedArray(size);
    }

    long cpuStart = System.currentTimeMillis();
    for(int i = 0; i < outerCount; ++i){
      Arrays.sort(array[i]);
    }
    long cpuStop = System.currentTimeMillis();
    long cpuTime = cpuStop - cpuStart;
    System.out.println("cpu_time: "+cpuTime);
    double ratio = (double) cpuTime / (double) gpuTime;
    System.out.println("ratio: "+ratio);
  }

  public static void main(String[] args){
    GPUSort sorter = new GPUSort();
    sorter.sort();
  }
}
