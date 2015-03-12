package org.trifort.matrix.java4;

public class ThreadProcJava4Transpose implements Runnable {

  private float[][] a;
  private float[][] b;
  private float[][] c;
  private int index;
  private int workItems;
  
  public ThreadProcJava4Transpose(float[][] a, float[][] b, float[][] c, 
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
	      float sum = 0;
	      for(int k = 0; k < size; ++k){
	        sum += a[i][k] * b[j][k];
	      }
	      c[i][j] = sum;
	    }
	  }
	  
	}
}
