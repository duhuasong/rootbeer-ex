package org.trifort.rootbeer.examples.hmm;

import be.ac.ulg.montefiore.run.jahmm.OpdfInteger;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;

public class OpdfGpuInt {

  private float[] probabilities;

  public OpdfGpuInt(int nbEntries) {
    probabilities = new float[nbEntries];

    for (int i = 0; i < nbEntries; i++)
      probabilities[i] = 1.0f / ((float) nbEntries);
  }

  public float probability(int o) {
    return probabilities[o];
  }

  public OpdfGpuInt(OpdfInteger opdf){
    probabilities = new float[opdf.nbEntries()];
    for(int i = 0; i < opdf.nbEntries(); ++i){
      ObservationInteger temp = new ObservationInteger(i);
      probabilities[i] = (float) opdf.probability(temp);
    }
  }
}
