import org.trifort.rootbeer.runtime.Kernel;
import org.trifort.rootbeer.runtime.Rootbeer;
import org.trifort.rootbeer.runtime.Context;
import org.trifort.rootbeer.runtime.StatsRow;

import java.util.List;
import java.util.ArrayList;

public class PageRankApp {


    public static void main(String[] args)
    {
        double totTime = System.currentTimeMillis();
        int dim = 1000;
        int[][] links = new int[dim][dim];
        for(int i = 0; i < dim; ++i)
            for (int j = 0; j < dim; ++j)
                links[i][j] = (int) (Math.random()*2);

        int iterations = 40;
        int[] linksPerRow = linksPerPage(links);
        double[] ranks = new double[links[0].length];
        for (int i = 0; i < ranks.length; ++i)
            ranks[i] = 0;

        List<Kernel> jobs = new ArrayList<Kernel>();
        int[][] transpose = transpose(links);

        for (int i = 0; i < links[0].length; ++i)
            jobs.add(new Rank(transpose[i], linksPerRow, ranks, i));

        Rootbeer rootbeer = new Rootbeer();
        double time = System.currentTimeMillis();

        for (int i = 0; i < iterations; ++i){
          rootbeer.run(jobs);
        }

        //((Rank)jobs.get(0)).displayRanks();

        System.out.println(System.currentTimeMillis() - time + "ms elapsed");
        System.out.println("Stats, aggregate across " + iterations + " (de)serialization cycles on a " + dim + " node system:");
    }

    public static int[][] transpose(int[][] links)
    {
        int[][] transLinks = new int[links.length][links.length];
        int temp = 0;
        for (int i = 0; i < links.length; ++i)
            for (int j = 0; j < links.length; ++j)
                transLinks[j][i] = links[i][j];
        return transLinks;
    }

    public static int[] linksPerPage(int[][] links)
    {
        int linksOnRow = 0;
        int pages = links[0].length;
        int[] linksPerRow = new int[pages];
        for (int i = 0; i < pages; ++i)
        {
            for(int j = 0; j < pages; ++j)
                linksOnRow += links[i][j];
            linksPerRow[i] = linksOnRow;
            linksOnRow = 0;
        }
        return linksPerRow;
    }
}
