package org.trifort.pattern_sync;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class PatternSyncKernel implements Kernel {

  private double[][] delta;
  private int[] barrier;
  
  public PatternSyncKernel(double[][] delta, int[] barrier) {
    this.delta = delta;
    this.barrier = barrier;
  }

  @Override
  public void gpuMethod() {
    int length = delta.length;
    int innerLength = delta[0].length;
    int threadId = RootbeerGpu.getThreadId();
    for(int i = 1; i < length; ++i){
      double rowSum = 0;
      for(int j = 0; j < innerLength; ++j){
        rowSum += delta[i-1][j];
      }
      delta[i][threadId] = rowSum;
      RootbeerGpu.threadfenceSystem();
      //globalSync(i, length - 1);
    }
  }
  /*
  private void globalSync(int goal_value, int end){
    int[] local_barrier = barrier;
    int count = 0;
    int iter = 0;
    int thread_value = 0;
    
    ////////////////////////////////////////
    // 28 threads and 28 blocks
    ////////////////////////////////////////
    while(count < 28){
      if(RootbeerGpu.getThreadIdxx() == 0){
        local_barrier[RootbeerGpu.getBlockIdxx()] = goal_value;
        RootbeerGpu.threadfenceSystem();
      }
      thread_value = local_barrier[RootbeerGpu.getThreadIdxx()];
      int match;
      if(thread_value == goal_value){
        match = 1;
      } else {
        match = 0;
      }
      count = RootbeerGpu.syncthreadsCount(match);
    }
    
    if(RootbeerGpu.getBlockIdxx() == 0 && goal_value == end){
      for(int i = 0; i < 28; ++i){
        if(i == RootbeerGpu.getThreadIdxx()){
          System.out.println(RootbeerGpu.getThreadIdxx()+" -> "+thread_value+" -> "+count);
        }
        RootbeerGpu.syncthreads();
      }
    }
  }
  */
}
