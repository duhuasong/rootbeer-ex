package org.trifort.rootbeer.examples.hist;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class GPUHistKernel implements Kernel {

  private int[][] inputData;
  private int[][] outputData;

  public GPUHistKernel(int[][] inputData, int[][] outputData){
    this.inputData = inputData;
    this.outputData = outputData;
  }

  private int min(int left, int right){
    if(left < right){
      return left;
    } else {
      return right;
    }
  }

  private void addData64(int threadPos, int data){
    int index = threadPos + (data * GPUHistConstants.THREAD_N);
    byte value = RootbeerGpu.getSharedByte(index);
    ++value;
    RootbeerGpu.setSharedByte(index, value);
  }

  public void gpuMethod(){
    int block_dimx = RootbeerGpu.getBlockDimx();
    int block_idxx = RootbeerGpu.getBlockIdxx();
    int thread_idxx = RootbeerGpu.getThreadIdxx();

    //Global base index in input data for current block
    int baseIndex = GPUHistConstants.BLOCK_DATA * block_idxx;

    //Current block size, clamp by array border
    int dataSize = min(GPUHistConstants.DATA_N - baseIndex, GPUHistConstants.BLOCK_DATA);

    if(thread_idxx == 0){
      System.out.println("baseIndex: "+baseIndex);
      System.out.println("dataSize: "+dataSize);
    }

    //Encode thread index in order to avoid bank conflicts in s_Hist[] access:
    //each half-warp accesses consecutive shared memory banks
    //and the same bytes within the banks
    int threadPos =
      //[31 : 6] <== [31 : 6]
      ((thread_idxx & (~63)) >> 0) |
      //[5  : 2] <== [3  : 0]
      ((thread_idxx &    15) << 2) |
      //[1  : 0] <== [5  : 4]
      ((thread_idxx &    48) >> 4);

    int end_position = GPUHistConstants.BLOCK_MEMORY;
    for(int pos = thread_idxx; pos < end_position;  pos += block_dimx){
      RootbeerGpu.setSharedInteger(pos * GPUHistConstants.INT_SIZE, 0);
    }

    RootbeerGpu.syncthreads();

    ////////////////////////////////////////////////////////////////////////////
    // Cycle through current block, update per-thread histograms
    // Since only 64-bit histogram of 8-bit input data array is calculated,
    // only highest 6 bits of each 8-bit data element are extracted,
    // leaving out 2 lower bits.
    ////////////////////////////////////////////////////////////////////////////

    //read the handle from the field into a register because it is used
    //repeatedly in a loop
    int[] localInputData = inputData[block_idxx];
    int[] localOutputData = outputData[block_idxx];

    for(int pos = thread_idxx; pos < dataSize; pos += block_dimx){
      int item = localInputData[pos];
      addData64(threadPos, (item >>  2) & 0x3F);
      addData64(threadPos, (item >> 10) & 0x3F);
      addData64(threadPos, (item >> 18) & 0x3F);
      addData64(threadPos, (item >> 26) & 0x3F);
    }

    RootbeerGpu.syncthreads();

    ////////////////////////////////////////////////////////////////////////////
    // Merge per-thread histograms into per-block and write to global memory.
    // Start accumulation positions for half-warp each thread are shifted
    // in order to avoid bank conflicts.
    // See supplied whitepaper for detailed explanations.
    ////////////////////////////////////////////////////////////////////////////
    if(thread_idxx < GPUHistConstants.BIN_COUNT){
      //64 threads here
      int sum = 0;
      int value = thread_idxx;

      int valueBase = value * GPUHistConstants.THREAD_N;
      int startPos = (thread_idxx & 15) * 4;
      //int startPos = (thread_idxx & 15);

      //Threads with non-zero start positions wrap around the THREAD_N border
      int sharedIndex = 0;
      for(int i = 0, accumPos = startPos; i < GPUHistConstants.THREAD_N; i++){
        byte sharedValue = RootbeerGpu.getSharedByte(valueBase + accumPos);
        sum += sharedValue;
        if(thread_idxx == 0){
          System.out.println("index: "+sharedIndex+" value: "+sharedValue);
          ++sharedIndex;
        }
        if(++accumPos == GPUHistConstants.THREAD_N) {
          accumPos = 0;
        }
      }

      //value is index
      RootbeerGpu.atomicAddGlobal(localOutputData, value, sum);
    }
  }
}
