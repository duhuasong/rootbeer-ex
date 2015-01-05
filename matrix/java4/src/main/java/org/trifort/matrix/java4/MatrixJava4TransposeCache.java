package org.trifort.matrix.java4;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.trifort.matrix.gold.MatrixGold;

public class MatrixJava4TransposeCache {

  public void run(int blockSize){
    int size = MatrixGold.size();
    double[][] a = MatrixGold.createAB();
    double[][] b = MatrixGold.createTransposeB();
    double[][] c = new double[size][size];
    
    int threadCount = 4;
    int workItems = size / threadCount;
    
    List<Thread> threads = new ArrayList<Thread>();
    StopWatch watch = new StopWatch();
    watch.start();
    for(int i = 0; i < threadCount; ++i){
      ThreadProcJava4TranposeCache proc = new ThreadProcJava4TranposeCache(a, b, 
          c, i, workItems, blockSize);
      Thread thread = new Thread(proc);
      thread.start();
      threads.add(thread);
    }
    
    for(int i = 0; i < threadCount; ++i){
      Thread thread = threads.get(i);
      try {
        thread.join();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
    watch.stop();
    
    boolean match = MatrixGold.checkC(c);
    if(match){
      System.out.println("BLOCKSIZE: "+blockSize+" TEST PASSES: "+watch.getTime()+"ms");
    }
  }

  public static void main(String[] args){
    MatrixJava4TransposeCache engine = new MatrixJava4TransposeCache();
    
    //CACHE_LINE SIZE = 64 bytes
    //CACHE_ALIGNMENT = 64 bytes
    //L1_CACHE SIZE = 32K bytes
    //512 CACHE LINES
    List<Integer> blockSizes = new ArrayList<Integer>();
    blockSizes.add(2);
    blockSizes.add(4);
    blockSizes.add(8);
    blockSizes.add(16);
    blockSizes.add(32);
    blockSizes.add(64);
    blockSizes.add(128);
    blockSizes.add(256);
    blockSizes.add(512);
    for(int blockSize : blockSizes){
      try {
        //run 8 times to allow jvm code optimizations to run
        for(int i = 0; i < 8; ++i){
          engine.run(blockSize);
        }
      } catch(Exception ex){
        ex.printStackTrace();
      }
    }
  }
}