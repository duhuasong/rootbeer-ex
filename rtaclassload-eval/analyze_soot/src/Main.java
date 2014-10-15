
import soot.PackManager;
import soot.Scene;
import soot.SootClass;
import soot.SootResolver;
import soot.Transform;
import soot.options.Options;

public class Main {

  public void run(String jarName){
    PackManager.v().getPack("wjpp").add(new Transform("wjpp.stats", new StatsTransformer("wjpp")));

    Options.v().set_allow_phantom_refs(true);

    String[] args = {
      "-pp",
      "-process-dir", jarName,
      "-w",
    };
    soot.Main.main(args);
  }

  public static void main(String[] args){
    Main main = new Main();
    main.run(args[0]);
  }
}
