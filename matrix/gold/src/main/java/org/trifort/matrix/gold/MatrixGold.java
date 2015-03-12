package org.trifort.matrix.gold;

public class MatrixGold {

  private static final int SIZE = 2048;

  public static int size(){
    return SIZE;
  }
  
  public static float[][] createAB(){
    float[][] ret = new float[SIZE][SIZE];
    int index = 1;
    for(int i = 0; i < SIZE; ++i){
      for(int j = 0; j < SIZE; ++j){
        ret[i][j] = (float) index;
        ++index;
      }
    }
    return ret;
  }

  public static float[][] createTransposeB(){
    float[][] ret = new float[SIZE][SIZE];
    int index = 1;
    for(int i = 0; i < SIZE; ++i){
      for(int j = 0; j < SIZE; ++j){
        ret[j][i] = (float) index;
        ++index;
      }
    }
    return ret;
  }
  
  public static float[][] createC(){
    float[][] ret = new float[SIZE][SIZE];
    float[][] a = createAB();
    float[][] b = createAB();
    
    for(int i = 0; i < SIZE; ++i){
      for(int j = 0; j < SIZE; ++j){
        float sum = 0;
        for(int k = 0; k < SIZE; ++k){
          sum += a[i][k] * b[k][j];
        }
        ret[i][j] = sum;
      }
    }
    return ret;
  }
  
  public static boolean checkC(float[][] c){
    float[][] gold = createC();
    
    if(gold.length != c.length){
      System.out.println("mismatch in outer length");
      return false;
    }
    if(gold[0].length != c[0].length){
      System.out.println("mismatch in inner length");
      return false;
    }
    
    boolean ret = true;
    int errorCount = 0;
    for(int i = 0; i < gold.length; ++i){
      for(int j = 0; j < gold[0].length; ++j){
        float lhs = gold[i][j];
        float rhs = c[i][j];
        
        if(Math.abs(lhs - rhs) > 0.0001){
          if(ret == true){
            System.out.println("mismatch at ["+i+"]["+j+"]");
            System.out.println("  lhs: "+lhs);
            System.out.println("  rhs: "+rhs);
            ret = false;
          } 
          ++errorCount;
        }
      }
    }
    if(errorCount > 1){
      System.out.println(errorCount+" other mismatches.");
    }
    return ret;
  }
}
