
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootResolver;
import soot.Transform;
import soot.options.Options;

public class Main {

  public void run(String jarName){
    long startTime = System.currentTimeMillis();
    PackManager.v().getPack("wjpp").add(new Transform("wjpp.stats", new StatsTransformer("wjpp")));
    PackManager.v().getPack("wjop").add(new Transform("wjop.stats", new StatsTransformer("wjop")));

    Options.v().set_allow_phantom_refs(true);

    String[] args = {
      "-pp",
      "-process-dir", jarName,
      "-w",
      "-p", "cg", "enabled:true",
      "-p", "cg.spark", "enabled:true",
      "-f", "n"
    };
    Scene.v().addBasicClass("com.installshield.dim.IDimCondition",SootClass.HIERARCHY);
    soot.Main.main(args);

    int loadedClasses = 0 ;
    for (SootClass sc : Scene.v().getClasses()){
      ++loadedClasses;
    }

    long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    System.out.println("loaded classes: "+loadedClasses);
    System.out.println("memory used: "+memoryUsed);

    long stopTime = System.currentTimeMillis();
    long totalTime = stopTime - startTime;
    System.out.println("totalTimeAll: "+totalTime);
  }

  public static void main(String[] args){
    Main main = new Main();
    main.run(args[0]);
  }
}
