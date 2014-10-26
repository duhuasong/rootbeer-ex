package org.trifort.rootbeer.examples.hmm;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import be.ac.ulg.montefiore.run.jahmm.io.HmmBinaryWriter;
import be.ac.ulg.montefiore.run.jahmm.io.HmmBinaryReader;
import be.ac.ulg.montefiore.run.jahmm.Hmm;
import be.ac.ulg.montefiore.run.jahmm.ObservationInteger;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.File;
import org.apache.commons.io.IOUtils;
import java.util.List;
import java.util.ArrayList;

public class Main {

  private Hmm openHmm(String localFilename){
    try {
      FileInputStream stream = new FileInputStream(localFilename);
      Hmm hmm = HmmBinaryReader.read(stream);
      stream.close();
      return hmm;
    } catch(Exception ex){
      ex.printStackTrace();
      return null;
    }
  }

  private Hmm createHmm(){
    return null;
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

  private void downloadFile(String remoteFilename, String localFilename){
    System.out.println("downloading: "+remoteFilename);
    try {
      URL url = new URL(remoteFilename);
      OutputStream outputStream = new FileOutputStream(localFilename);
      InputStream inputStream = url.openStream();

      IOUtils.copy(inputStream, outputStream);
      inputStream.close();
      outputStream.close();
    } catch(Exception ex){
      ex.printStackTrace();
    }
  }

  private List<File> getMovieFiles(){
    File sourceFolder = new File("Mutant_Fbodies/");
    File[] children = sourceFolder.listFiles();
    List<File> ret = new ArrayList<File>();
    for(File child : children){
      if(child.isDirectory()){
        File[] children2 = child.listFiles();
        for(File child2 : children2){
          if(child2.isDirectory()){
            String name = child2.getName();
            if(name.startsWith(".")){
              continue;
            }

            ret.add(child2);
          }
        }
      }
    }
    return ret;
  }

  public void runTest(boolean test1){
    Hmm hmm = null;
    int numStates = 8160;
    String localFilename = "hmm_"+numStates+".hmm";
    File localFile = new File(localFilename);
    if(localFile.exists()){
      hmm = openHmm(localFilename);
    } else {
      String remoteFilename = "http://trifort.org/hmm-gpu/"+localFilename;
      if(remoteFileExists(remoteFilename)){
        downloadFile(remoteFilename, localFilename);
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

    List<File> movieFiles = getMovieFiles();

    if(test1){
      File file1 = movieFiles.get(11);
      OfCoarseMovie movie = new OfCoarseMovie(file1);
      List<ObservationInteger> signal1 = movie.getSignal();

      ForwardBackwardScaledGpuInt forwardBackward =
          new ForwardBackwardScaledGpuInt(convert(signal1), new HmmGpuInt(hmm));
      double prob = forwardBackward.probability();
    } else {

    }
  }

  private int[] convert(List<ObservationInteger> signal){
    int[] ret = new int[signal.size()];
    int index = 0;
    for(ObservationInteger item : signal){
      ret[index] = item.value;
      ++index;
    }
    return ret;
  }

  private void showUsage(){
    System.out.println("Usage: java -jar hmm.jar --test1");
    System.out.println("Usage: java -jar hmm.jar --testAll");
    System.exit(0);
  }

  private boolean parseArgs(String[] args){
    if(args.length != 1){
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


  public static void main(String[] args){
    Main main = new Main();
    try {
      boolean test1 = main.parseArgs(args);
      main.runTest(test1);
    } catch(Exception ex){
      ex.printStackTrace();
    }
  }
}
