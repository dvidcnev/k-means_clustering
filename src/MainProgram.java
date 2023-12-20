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

        // generate random rgb for each cluster and save it for the index of the cluster
        Dataset.setRGB(JSON.randomRGB(NumClusters));

        Dataset.setSites(JSON.randomPoints(NumSites));
        MapGUI mapGui = new MapGUI();

        // SEQUENTIAL
        if (Integer.valueOf(args[1]) == 0) {
            // the random clusters that get generated
            Dataset.setClusters(JSON.randomClusters(NumClusters, Dataset.getSites()));
        }

        if (Boolean.valueOf(args[0])) {
            mapGui.launchMap();
        }

    }

}
