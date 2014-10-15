
import java.util.Map;
import soot.G;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;

public class StatsTransformer extends SceneTransformer {
  private final String phase;
  private long startTime;

  public StatsTransformer(final String phase){
    this.phase = phase;
    startTime = System.currentTimeMillis();
  }

  @Override
  protected void internalTransform(String phaseName, Map options) {
    long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

    int loadedClasses = 0 ;
    for (SootClass sc : Scene.v().getClasses()){
      ++loadedClasses;
    }

    System.out.println("loaded classes: "+loadedClasses);
    System.out.println("memory used: "+memoryUsed);

    long stopTime = System.currentTimeMillis();
    long totalTime = stopTime - startTime;
    System.out.println("totalTime: "+totalTime);
  }
}
