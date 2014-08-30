package org.trifort.rootbeer.sort;

import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.GpuDevice;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.ThreadConfig;
import org.trifort.rootbeer.runtime.StatsRow;
import java.util.List;
import java.util.Arrays;

public class GPUSort {

  public void sort(){
    int size = 128;
    int sizeBy2 = size / 2;
    int[] array = new int[size];
    for(int i = 0; i < size; ++i){
      array[size-1-i] = i;
    }

    GPUSortKernel kernel = new GPUSortKernel(array);
    Rootbeer rootbeer = new Rootbeer();
    List<GpuDevice> devices = rootbeer.getDevices();
    GpuDevice device0 = devices.get(0);
    Context context0 = device0.createContext(32+32+(sizeBy2*4));
    ThreadConfig config0 = new ThreadConfig(sizeBy2, 1, sizeBy2);
    rootbeer.run(kernel, config0, context0);

    List<StatsRow> stats = context0.getStats();
    StatsRow row0 = stats.get(0);
    System.out.println("serialization_time: "+row0.getSerializationTime());
    System.out.println("execution_time: "+row0.getExecutionTime());
    System.out.println("deserialization_time: "+row0.getDeserializationTime());

    for(int i = 0; i < size; ++i){
      array[size-1-i] = i;
    }
    long cpuStart = System.currentTimeMillis();
    Arrays.sort(array);
    long cpuStop = System.currentTimeMillis();
    long cpuTime = cpuStop - cpuStart;
    System.out.println("cpu_time: "+cpuTime);
  }

  public static void main(String[] args){
    GPUSort sorter = new GPUSort();
    sorter.sort();
  }
}
