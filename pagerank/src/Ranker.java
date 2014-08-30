public class Ranker
{
    //adjacency matrix representing link from node <row> to node <column>
    int[][] links;
    //final ranks of the pages
    double[] ranks;
    int pages;
    int[] linksPerRow;

    public Ranker(int[][] inLinks)
    {
        links = inLinks;
        pages = links[0].length;
        initialRank();
    }

    public void initialRank()
    {
        double baseRank = 1/(double)pages;
        ranks = new double[pages];
        for(int i = 0; i < pages; ++i)
            ranks[i] = baseRank;
        linksPerPage();
    }

    public void linksPerPage()
    {
        int linksOnRow = 0;
        linksPerRow = new int[pages];
        for (int i = 0; i < pages; ++i)
        {
            for(int j = 0; j < pages; ++j)
                linksOnRow += links[i][j];
            linksPerRow[i] = linksOnRow;
            linksOnRow = 0;
        }
    }

    public void rank(int iterations)
    {
        for(int i = 0; i < iterations; ++i)
        {
            for (int j = 0; j < pages; ++j)
            {
                double individualRank = 0;
                for(int k = 0; k < pages; ++k)
                {
                    if (links[k][j] == 1)
                        individualRank += ranks[k]/((double)linksPerRow[k]);
                }
                individualRank = .15 + .85 * individualRank;
                ranks[j] = individualRank;
            }
        }
    }

    public void displayRanks()
    {
        double total = 0;
        for(int i = 0; i < pages; ++i)
            total += ranks[i];
        System.out.println(total);
        for(int i = 0; i < pages; ++i)
            System.out.println(i + ": " + ranks[i]);
    }

    public static void main(String[] args)
    {
        double totTime = System.currentTimeMillis();
        int dim = 1000;
        int[][] links = new int[dim][dim];
        for(int i = 0; i < dim; ++i)
            for (int j = 0; j < dim; ++j)
                links[i][j] = (int) (Math.random()*2);

        Ranker r = new Ranker(links);
        double time = System.currentTimeMillis();
        r.rank(40);
        //r.displayRanks();
        System.out.println(System.currentTimeMillis() - time + "ms elapsed");
        System.out.println("Total program time: " + (System.currentTimeMillis() - totTime) + "ms");
    }

}
