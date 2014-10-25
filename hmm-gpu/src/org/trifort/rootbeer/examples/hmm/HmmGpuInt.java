package org.trifort.rootbeer.examples.hmm;

import java.util.ArrayList;
import java.util.List;

import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;

public class HmmGpuInt {

  private float pi[];
  private float a[][];
  private OpdfGpuInt[] opdfs;
  private int nbStates;

  public HmmGpuInt(int nbStates, int maxValue) {
    this.nbStates = nbStates;
    pi = new float[nbStates];
    a = new float[nbStates][nbStates];
    opdfs = new OpdfGpuInt[nbStates];

    OpdfGpuIntFactory opdfFactory = new OpdfGpuIntFactory(maxValue+1);

    for (int i = 0; i < nbStates; i++) {
      pi[i] = 1.0f / ((float) nbStates);
      opdfs[i] = opdfFactory.factor();

      for (int j = 0; j < nbStates; j++)
        a[i][j] = 1.0f / ((float) nbStates);
    }
  }

  public HmmGpuInt(Hmm<ObservationInteger> hmm){
    this.nbStates = hmm.nbStates();
    pi = new float[nbStates];
    a = new float[nbStates][nbStates];
    opdfs = new OpdfGpuInt[nbStates];

    for(int i = 0; i < nbStates; ++i){
      pi[i] = (float) hmm.getPi(i);
      opdfs[i] = new OpdfGpuInt((OpdfInteger) hmm.getOpdf(i));
    }
    for(int i = 0; i < nbStates; ++i){
      for(int j = 0; j < nbStates; ++j){
        a[i][j] = (float) hmm.getAij(i, j);
      }
    }
  }

  public int nbStates() {
    return nbStates;
  }

  public float getPi(int i) {
    return pi[i];
  }

  public OpdfGpuInt getOpdf(int i) {
    return opdfs[i];
  }

  public float getAij(int i, int j) {
    return a[i][j];
  }

  public float[][] getA(){
    return a;
  }
}
