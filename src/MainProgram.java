package src;

public class MainProgram {

    public static void main(String[] args) {

        // If args length is not 1, then exit
        if (args.length < 4) {
            System.out.println("ERROR! Not enough arguments provided!");
            System.exit(1);
        }

        final int NumClusters = Integer.valueOf(args[2]);
        final int NumSites = Integer.valueOf(args[3]);

        // generate random rgb for each cluster (each cluster has an rgb value that is
        // empty initially and can be set )

        Dataset.setSites(JSON.randomPoints(NumSites));

        MapGUI mapGui = new MapGUI();

        // SEQUENTIAL
        if (Integer.valueOf(args[1]) == 0) {
            // the random clusters that get generated
            Dataset.setClusters(JSON.randomClusters(NumClusters, Dataset.getSites()));
            // assign each site to a cluster
            JSON.assignSitesToClusters(Dataset.getSites(), Dataset.getClusters());
            // calculate the new center of each cluster
            JSON.calculateNewCenters(Dataset.getClusters());
            for (Cluster clr : Dataset.getClusters()) {
                clr.setRGB(
                        new RGB((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
            }
        }

        if (Boolean.valueOf(args[0])) {
            mapGui.launchMap();
        }

    }

}
