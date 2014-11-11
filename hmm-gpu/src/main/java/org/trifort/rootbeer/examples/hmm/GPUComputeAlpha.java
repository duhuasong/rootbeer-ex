package org.trifort.rootbeer.examples.hmm;

import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.RootbeerGpu;

public class GPUComputeAlpha implements Kernel {

  private HmmGpuInt hmm;
  private int[] oseq;
  private static final int INT_SIZE = 4;
  private float[] alpha0;
  private float[] alphaN;

  public GPUComputeAlpha(HmmGpuInt hmm, float[] alpha0, float[] alphaN,
      int[] oseq) {

    this.alpha0 = alpha0;
    this.alphaN = alphaN;
    this.hmm = hmm;
    this.oseq = oseq;
  }

  @Override
  public void gpuMethod() {
    int[] sequence = oseq;
    int seqLength = sequence.length;
    int thread_idxx = RootbeerGpu.getThreadIdxx();
    int thread_id = RootbeerGpu.getThreadId();
    int block_idxx = RootbeerGpu.getBlockIdxx();
    int block_dimx = RootbeerGpu.getBlockDimx();
    float[][] a = hmm.getA();

    float alphaPrev[] = alpha0;
    float alphaNext[] = alphaN;

    int numStates = alphaPrev.length;

    for(int t = 1; t < seqLength; ++t){
      int j = thread_id;
      float sum = 0;
      for(int i = 0; i < numStates; ++i){
        double alpha_value = alphaPrev[i];
        double aij_value = a[i][j];
        double mult = alpha_value * aij_value;
        sum += mult;
      }

      int o = sequence[t];
      float prob = hmm.getOpdf(j).probability(o);
      alphaNext[j] = sum * prob;

      float[] temp = alphaPrev;
      alphaPrev = alphaNext;
      alphaNext = temp;

      RootbeerGpu.threadfence();
      RootbeerGpu.syncthreads();
    }
  }
}
