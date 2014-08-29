package org.trifort.rootbeer.sort;

import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.GpuDevice;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.ThreadConfig;
import java.util.List;

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
    Context context0 = device0.createContext(1024);
    ThreadConfig config0 = new ThreadConfig(sizeBy2, 1, sizeBy2);
    rootbeer.run(kernel, config0, context0);

    for(int value : array){
      System.out.println("value: "+value);
    }
  }

  public static void main(String[] args){
    GPUSort sorter = new GPUSort();
    sorter.sort();
  }
}
