package org.trifort.rootbeer.examples.hmm;

import java.util.Arrays;
import java.util.Iterator;

import org.trifort.rootbeer.runtime.CacheConfig;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.StatsRow;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.Observation;

public class ForwardBackwardScaledGpuInt {

  private float[] ctFactors;
  private float[][] alpha;
  private float[] alpha0;
  private float[] alphaN;
  private float prob;
  private boolean usingScaling;

  public ForwardBackwardScaledGpuInt(int[] oseq, HmmGpuInt hmm, boolean useGpu){
    usingScaling = true;
    if(useGpu){
      computeGPU(hmm, oseq);
    } else {
      computeAlpha(hmm, oseq);
      computeProbability(oseq, hmm);
    }
  }

  private void computeGPU(HmmGpuInt hmm, int[] oseq) {
    System.out.println("computeAlphaGPU");

    int numThreads = 96;
    int numBlocks = 50;

    alpha0 = new float[numBlocks*numThreads];
    alphaN = new float[numBlocks*numThreads];

    int o = oseq[0];
    for(int i = 0; i < numBlocks; ++i){
      for(int j = 0; j < numThreads; ++j){
        int totalIndex = (i * numThreads) + j;
        float pi = hmm.getPi(totalIndex);
        float prob = hmm.getOpdf(totalIndex).probability(o);
        alpha0[i*numThreads+j] = pi * prob;
      }
    }

    Rootbeer rootbeer = new Rootbeer();
    Context context = rootbeer.createDefaultContext();
    context.setCacheConfig(CacheConfig.PREFER_L1);
    context.setKernel(new GPUComputeAlpha(hmm, alpha0, alphaN, oseq));
    context.setThreadConfig(numThreads, numBlocks, numThreads * numBlocks);
    context.buildState();
    context.run();

    StatsRow row = context.getStats();
    System.out.println("serial: "+row.getSerializationTime());
    System.out.println("memcpy: "+row.getDriverMemcopyToDeviceTime());
    System.out.println("exec: "+row.getDriverExecTime());
    System.out.println("memcpy: "+row.getDriverMemcopyFromDeviceTime());
    System.out.println("deserial: "+row.getDeserializationTime());

    float[] alphaLast;
    if(oseq.length % 2 == 0){
      alphaLast = alpha0;
    } else {
      alphaLast = alphaN;
    }

    prob = 0;
    for(int i = 0; i < numBlocks; ++i){
      for(int j = 0; j < numThreads; ++j){
        prob += alphaLast[i*numThreads+j];
      }
    }
  }

  private void computeAlpha(HmmGpuInt hmm, int[] oseq) {
    System.out.println("computeAlpha");
    alpha = new float[oseq.length][hmm.nbStates()];
    ctFactors = new float[oseq.length];
    Arrays.fill(ctFactors, 0.0f);

    for (int i = 0; i < hmm.nbStates(); i++)
      computeAlphaInit(hmm, oseq[0], i);
    scale(ctFactors, alpha, 0);

    for (int t = 1; t < oseq.length; t++) {

      for (int j = 0; j < hmm.nbStates(); j++){
        computeAlphaStep(hmm, oseq[t], t, j);
      }
      scale(ctFactors, alpha, t);
    }
  }

  private void computeAlphaStep(HmmGpuInt hmm, int o, int t, int j) {
    float sum = 0.0f;

    for (int i = 0; i < hmm.nbStates(); i++){
      double alpha_value = alpha[t-1][i];
      double aij_value = hmm.getAij(i, j);
      double mult = alpha_value * aij_value;
      sum += mult;
    }

    float prob = hmm.getOpdf(j).probability(o);
    alpha[t][j] = sum * prob;
  }

  private void scale(float[] ctFactors, float[][] array, int t) {
    if(usingScaling){
      float[] table = array[t];
      float sum = 0.0f;

      for (int i = 0; i < table.length; i++){
        sum += table[i];
      }

      ctFactors[t] = sum;
      for (int i = 0; i < table.length; i++){
        if(sum != 0){
          table[i] /= sum;
        }
      }
    }
  }

  private void computeAlphaInit(HmmGpuInt hmm, int o, int i) {
    float pi = hmm.getPi(i);
    float prob = hmm.getOpdf(i).probability(o);
    alpha[0][i] = pi * prob;
  }

  private void computeProbability(int[] oseq, HmmGpuInt hmm) {
    if(usingScaling){
      float lnProbability = 0.0f;
      for (int t = 0; t < oseq.length; t++){
        System.out.println("ctFactors["+t+"]: "+ctFactors[t]);
        lnProbability += Math.log(ctFactors[t]);
      }
      prob = (float) Math.exp(lnProbability);
    } else {
      for (int i = 0; i < hmm.nbStates(); i++) {
				prob += alpha[oseq.length-1][i];
      }
    }
  }

  public float probability(){
    return prob;
  }
}
