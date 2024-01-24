
import java.util.ArrayList;

public class MainProgram {

    public static void main(String[] args) {

        // If args length is not 1, then exit
        if (args.length < 4) {
            System.out.println("ERROR! Not enough arguments provided!");
            System.exit(1);
        }

        final int NumClusters = Integer.valueOf(args[2]);
        final int NumSites = Integer.valueOf(args[3]);
        int cycles = 0;

        // SEQUENTIAL
        if (Integer.valueOf(args[1]) == 0) {
            // Measure the time
            long startTime = System.currentTimeMillis();

            // if numsites is less or equal to the provided dataset, generate with dataset
            // size
            if (NumSites <= JSON.getDatasetSize()) {
                // generate random sites
                Dataset.setSites(JSON.randomizeDataset(NumSites));
            } else {
                // generate random sites around EUROPE
                // change the zoom value and the south east and north west bounds in Dataset
                // class
                Dataset.minZoom = 4;
                Dataset.Zoom = 4;
                Dataset.southwestBound[0] = 30.86166;
                Dataset.southwestBound[1] = -17.005859;
                Dataset.northeastBound[0] = 62.239811;
                Dataset.northeastBound[1] = 34.317188;

                Dataset.setSites(JSON.randomizeEurope(NumSites));
            }

            // the random clusters that get generated
            Dataset.setClusters(JSON.randomClusters(NumClusters, Dataset.getSites()));
            // assign each site to a cluster
            JSON.assignSitesToClusters(Dataset.getSites(), Dataset.getClusters());

            System.out.println("Cycle: " + cycles);
            ArrayList<Cluster> copiedClusters = JSON.deepCopyClusters(Dataset.getClusters());
            // calculate the new center of each cluster
            JSON.calculateNewCenters(Dataset.getClusters());
            // assign the cluster to the new centers
            JSON.assignSitesToNewClusters(Dataset.getSites(), Dataset.getClusters());
            while (!JSON.clustersAreTheSame(Dataset.getClusters(), copiedClusters)) {
                cycles++;
                System.out.println("Cycle: " + cycles);

                copiedClusters = JSON.deepCopyClusters(Dataset.getClusters());
                // calculate the new center of each cluster
                JSON.calculateNewCenters(Dataset.getClusters());
                // assign the cluster to the new centers
                JSON.assignSitesToNewClusters(Dataset.getSites(), Dataset.getClusters());
            }
            // Measure the time in seconds
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            // System.out.println("[SEQUENTIAL] Total time: " + totalTime + "ms");
            System.out.println("[SEQUENTIAL] Total time: " + totalTime / 1000.0 + "s");
            System.out.println("[SEQUENTIAL] Total cycles: " + cycles);
        }

        if (Boolean.valueOf(args[0])) {
            for (Cluster clr : Dataset.getClusters()) {
                clr.setRGB(
                        new RGB((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
            }
            MapGUI mapGui = new MapGUI();
            mapGui.launchMap();
        }

    }

}
