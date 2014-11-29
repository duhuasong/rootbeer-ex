package org.trifort.rootbeer.examples.basic;

import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.Kernel;
import java.util.List;
import java.util.ArrayList;

public class GPUBasic {

  private float[] mkData(){
    int size = 8;
    float[] ret = new float[size];
    for(int i = 1; i <= size; ++i){
      ret[i-1] = i * 2;
    }
    return ret;
  }

  private void printData(float[] data){
    System.out.println("data: ");
    for(int i = 0; i < data.length; ++i){
      System.out.println("  data["+i+"]: "+data[i]);
    }
  }

  public void run(){
    float[] data = mkData();

    List<Kernel> kernels = new ArrayList<Kernel>();
    for(int i = 0; i < data.length; ++i){
      GPUBasicKernel kernel = new GPUBasicKernel(data);
      kernels.add(kernel);
    }
    Rootbeer rootbeer = new Rootbeer();
    rootbeer.run(kernels);

    printData(data);
  }

  public static void main(String[] args){
    GPUBasic basic = new GPUBasic();
    basic.run();
  }
}
