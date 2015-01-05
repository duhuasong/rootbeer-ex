package org.trifort.matrix.java4;

public class ThreadProcJava4 implements Runnable {

  private double[][] a;
  private double[][] b;
  private double[][] c;
  private int index;
  private int workItems;
  
  public ThreadProcJava4(double[][] a, double[][] b, double[][] c, 
      int index, int workItems){
    
    this.a = a;
    this.b = b;
    this.c = c;
    this.index = index;
    this.workItems = workItems;
  }
  
	public void run(){
		
	  int startIndex = index * workItems;
	  int endIndex = (index + 1) * workItems;
	  int size = a.length;
	  
	  for(int i = startIndex; i < endIndex; ++i){
	    for(int j = 0; j < size; ++j){
	      double sum = 0;
	      for(int k = 0; k < size; ++k){
	        sum += a[i][k] * b[k][j];
	      }
	      c[i][j] = sum;
	    }
	  }
	  
	}
}
