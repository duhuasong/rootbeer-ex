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
import java.util.Collections;

import org.trifort.coarsening.storage.Droplet;
import org.trifort.coarsening.storage.OfCoarseMovie;
import org.trifort.coarsening.storage.OfCoarseMovieFrame;

import be.ac.ulg.montefiore.run.jahmm.OpdfIntegerFactory;
import be.ac.ulg.montefiore.run.jahmm.learn.KMeansLearner;

public class Main {

  private int numStates;
  private int maxValue;

  public Main(){
    numStates = 8160;
  }

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

  private Hmm createHmm(File singleMovieSource){
    OpdfIntegerFactory opdfFactory = new OpdfIntegerFactory(maxValue+1);

    List<List<ObservationInteger>> fullSignal = new ArrayList<List<ObservationInteger>>();
    OfCoarseMovie movie0 = new OfCoarseMovie();
    movie0.open(singleMovieSource);
    List<ObservationInteger> fullSignal0 = createSignal(movie0);
    fullSignal.add(fullSignal0);

    long startTime = System.currentTimeMillis();
    KMeansLearner<ObservationInteger> learner =
      new KMeansLearner<ObservationInteger>(numStates, opdfFactory, fullSignal);
    Hmm hmm = learner.learn();
    long stopTime = System.currentTimeMillis();

    long time = stopTime - startTime;
    System.out.println("  learn time: "+time);

    return hmm;
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

  private void findMaxValue(List<File> movieFiles){
    int ret = -1;
    for(File file : movieFiles){
      OfCoarseMovie movie = new OfCoarseMovie();
      movie.open(file);
      List<ObservationInteger> signal1 = createSignal(movie);
      for(ObservationInteger observation : signal1){
        int value = observation.value;
        if(value > ret){
          ret = value;
        }
      }
    }
    maxValue = ret;
  }

  public void runTest(boolean test1){
    List<File> movieFiles = getMovieFiles();
    findMaxValue(movieFiles);
    File singleMovieSource = movieFiles.get(11);

    Hmm hmm = null;
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
        try {
          hmm = createHmm(singleMovieSource);
        } catch(OutOfMemoryError ex){
          ex.printStackTrace();
        }
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
      File file1 = singleMovieSource;
      OfCoarseMovie movie = new OfCoarseMovie();
      movie.open(file1);
      List<ObservationInteger> signal1 = createSignal(movie);

      long startTime = System.currentTimeMillis();
      ForwardBackwardScaledGpuInt forwardBackward =
          new ForwardBackwardScaledGpuInt(convert(signal1), new HmmGpuInt(hmm), true);
      double prob = forwardBackward.probability();
      long stopTime = System.currentTimeMillis();
      long time = stopTime - startTime;

      System.out.println("GPU time: "+time);
      System.out.println("GPU probability: "+prob);

      startTime = System.currentTimeMillis();
      forwardBackward =
        new ForwardBackwardScaledGpuInt(convert(signal1), new HmmGpuInt(hmm), false);
      prob = forwardBackward.probability();
      stopTime = System.currentTimeMillis();
      time = stopTime - startTime;

      System.out.println("CPU time: "+time);
      System.out.println("CPU probability: "+prob);
    } else {

    }
  }

  private List<ObservationInteger> createSignal(OfCoarseMovie movie){
    List<ObservationInteger> ret = new ArrayList<ObservationInteger>();
    for(int i = 0; i < movie.size(); ++i){
      OfCoarseMovieFrame frame = movie.getFrame(i);
      List<Droplet> frameDrops = frame.getDroplets();
      ret.addAll(createSignalFrame(frameDrops));
    }
    return ret;
  }

  private List<ObservationInteger> createSignalFrame(List<Droplet> frameDroplets){
    List<Integer> radii = new ArrayList<Integer>();
    for(Droplet droplet : frameDroplets){
      if(droplet.getVolume() < 1){
        continue;
      }
      radii.add((int) droplet.getRadius());
    }

    Collections.sort(radii);
    List<ObservationInteger> ret = new ArrayList<ObservationInteger>();
    for(int radius : radii){
      ret.add(new ObservationInteger(radius));
    }
    return ret;
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
