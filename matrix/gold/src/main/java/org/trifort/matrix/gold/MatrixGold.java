package org.trifort.matrix.gold;

public class MatrixGold {

  private static final int SIZE = 2048;

  public static int size(){
    return SIZE;
  }
  
  public static double[][] createAB(){
    double[][] ret = new double[SIZE][SIZE];
    int index = 1;
    for(int i = 0; i < SIZE; ++i){
      for(int j = 0; j < SIZE; ++j){
        ret[i][j] = (double) index;
        ++index;
      }
    }
    return ret;
  }

  public static double[][] createTransposeB(){
    double[][] ret = new double[SIZE][SIZE];
    int index = 1;
    for(int i = 0; i < SIZE; ++i){
      for(int j = 0; j < SIZE; ++j){
        ret[j][i] = (double) index;
        ++index;
      }
    }
    return ret;
  }
  
  public static double[][] createC(){
    double[][] ret = new double[SIZE][SIZE];
    double[][] a = createAB();
    double[][] b = createAB();
    
    for(int i = 0; i < SIZE; ++i){
      for(int j = 0; j < SIZE; ++j){
        double sum = 0;
        for(int k = 0; k < SIZE; ++k){
          sum += a[i][k] * b[k][j];
        }
        ret[i][j] = sum;
      }
    }
    return ret;
  }
  
  public static boolean checkC(double[][] c){
    double[][] gold = createC();
    
    if(gold.length != c.length){
      System.out.println("mismatch in outer length");
      return false;
    }
    if(gold[0].length != c[0].length){
      System.out.println("mismatch in inner length");
      return false;
    }
    
    boolean ret = true;
    for(int i = 0; i < gold.length; ++i){
      for(int j = 0; j < gold[0].length; ++j){
        double lhs = gold[i][j];
        double rhs = c[i][j];
        
        if(Math.abs(lhs - rhs) > 0.0001){
          System.out.println("mismatch at ["+i+"]["+j+"]");
          System.out.println("  lhs: "+lhs);
          System.out.println("  rhs: "+rhs);
          ret = false;
        }
      }
    }
    return ret;
  }
}
