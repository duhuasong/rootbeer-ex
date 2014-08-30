public class Rank implements Kernel
{
    int[] linksColumn;
    double[] ranks;
    int[] linksPerRow;
    int pages;
    int column;

    public Rank( int[] inLinksColumn, int[] inLinksPerRow, double[] inRanks, int inColumn )
    {
        linksColumn = inLinksColumn;
        pages = linksColumn.length;
        ranks = inRanks;
        linksPerRow = inLinksPerRow;
        column = inColumn;
    }

    public void displayRanks()
    {
        for(int i = 0; i < pages; ++i)
            System.out.println(i + ": " + ranks[i]);
    }

    @Override
    public void gpuMethod()
    {
        double individualRank = 0;
        for(int k = 0; k < pages; ++k)
        {
            if (linksColumn[k] == 1)
                individualRank += ranks[k]/((double)linksPerRow[k]);
        }
        individualRank = .15 + .85 * individualRank;
        ranks[column] = individualRank;
    }
}
