
import soot.options.Options;
import java.util.List;
import java.util.ArrayList;
import soot.rtaclassload.RTAClassLoader;
import soot.rtaclassload.MainEntryMethodTester;
import soot.SootMethod;
import soot.SootClass;
import soot.Scene;
import soot.util.Chain;

public class Main {

  public void run(String jarPath){
    long startTime = System.currentTimeMillis();
    Options.v().set_rtaclassload_verbose(true);
    Options.v().set_rtaclassload_context_sensitive_new_invokes(false);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_prepend_classpath(true);
    List<String> procesDirectory = new ArrayList<String>();
    procesDirectory.add(jarPath);
    Options.v().set_process_dir(procesDirectory);
    RTAClassLoader.v().addApplicationJar(jarPath);

    RTAClassLoader.v().addEntryMethodTester(new MainEntryMethodTester());
    RTAClassLoader.v().loadNecessaryClasses();
    long stopTime = System.currentTimeMillis();
    long totalTime = stopTime - startTime;
    System.out.println("totalTime: "+totalTime);
    System.gc();
    System.out.println("totalMemory: "+Runtime.getRuntime().totalMemory());
    System.out.println("freeMemory: "+Runtime.getRuntime().freeMemory());
    long usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    System.out.println("usedMemory: "+usedMemory);
  }

  public static void main(String[] args){
    Main main = new Main();
    main.run(args[0]);
  }
}
