package org.trifort.matrix.java4;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.time.StopWatch;
import org.trifort.matrix.gold.MatrixGold;

public class MatrixJava4 {

  public void run(){
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
      ThreadProc proc = new ThreadProc(a, b, c, i, workItems);
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
      System.out.println("TEST PASSES: "+watch.getTime()+"ms");
    }
  }

  public static void main(String[] args){
    MatrixJava4 engine = new MatrixJava4();
    try {
      engine.run();
    } catch(Exception ex){
      ex.printStackTrace();
    }
  }
}