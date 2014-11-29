package org.trifort.rootbeer.examples.hmm;

public class OpdfGpuIntFactory {

  private int nbEntries;

  public OpdfGpuIntFactory(int maxValue) {
    nbEntries = maxValue;
  }

  public OpdfGpuInt factor() {
    return new OpdfGpuInt(nbEntries);
  }

}
