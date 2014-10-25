package org.trifort.rootbeer.examples.hmm;

public class Main {

  private Hmm openHmm(String localFilename){
    try {
      FileInputStream stream = new FileInputStream(saved_hmm);
      hmm = HmmBinaryReader.read(stream);
      stream.close();
      return hmm;
    } catch(Exception ex){
      ex.printStackTrace();
      return null;
    }
  }

  private boolean remoteFileExists(String remoteFile){
    try {
      HttpURLConnection.setFollowRedirects(false);
      HttpURLConnection con =
         (HttpURLConnection) new URL(remoteFile).openConnection();
      con.setRequestMethod("HEAD");
      return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
    } catch (Exception ex){
      ex.printStackTrace();
      return false;
    }
  }

  public void runTest(boolean test1){
    Hmm hmm = null;
    int numStates = 8160;
    String localFilename = "hmm_"+numStates+".hmm";
    File localFile = new File(localFilename);
    if(localFile.exists()){
      hmm = openHmm(localFilename);
    } else {
      String remoteFile = "http://trifort.org/hmm/"+localFilename;
      if(remoteFileExists(remoteFile)){
        downloadFile(remoteFile, localFile);
        hmm = openHmm(localFilename);
      } else {
        hmm = createHmm();
        try {
          FileOutputStream stream = new FileOutputStream(localFilename);
          HmmBinaryWriter.write(stream, hmm);
          stream.close();
        } catch(Exception ex){
          ex.printStackTrace();
        }
      }
    }

    if(test1){
      ForwardBackwardScaledGpuInt forwardBackward =
          new ForwardBackwardScaledGpuInt(convert(full_signal.get(0)), new HmmGpuInt(hmm));
      double prob = forwardBackward.probability();
    } else {
      
    }
  }

  private void showUsage(){
    System.out.println("Usage: java -jar hmm.jar --test1");
    System.out.println("Usage: java -jar hmm.jar --testAll");
    System.exit(0);
  }

  private boolean parseArgs(String[] args){
    if(args.length != 0){
      showUsage();
    } else {
      String arg0 = args[0];
      if(arg0.equals("--test1")){
        return true;
      } else if(arg0.equals("--testAll")){
        return false;
      } else {
        showUsage();
      }
    }
    return true;
  }


  public static main(String[] args){
    Main main = new Main();
    try {
      boolean test1 = main.parseArgs(args);
      main.runTest(test1);
    } catch(Exception ex){
      ex.printStackTrace();
    }
  }
}
