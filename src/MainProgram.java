package src;

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

        // generate random rgb for each cluster (each cluster has an rgb value that is
        // empty initially and can be set )

        Dataset.setSites(JSON.randomPoints(NumSites));

        // SEQUENTIAL
        if (Integer.valueOf(args[1]) == 0) {
            // the random clusters that get generated
            Dataset.setClusters(JSON.randomClusters(NumClusters, Dataset.getSites()));
            Dataset.printFirstCluster();
            // assign each site to a cluster
            JSON.assignSitesToClusters(Dataset.getSites(), Dataset.getClusters());
            Dataset.printFirstCluster();
            ArrayList<Cluster> copiedClusters = JSON.deepCopyClusters(Dataset.getClusters());
            // calculate the new center of each cluster
            JSON.calculateNewCenters(Dataset.getClusters());
            Dataset.printFirstCluster();
            JSON.printFirstCluster(copiedClusters);
            // assign the cluster to the new centers
            JSON.assignSitesToNewClusters(Dataset.getSites(), Dataset.getClusters());
            while (!JSON.clustersAreTheSame(Dataset.getClusters(), copiedClusters)) {
                JSON.printFirstCluster(copiedClusters);
                Dataset.printFirstCluster();
                copiedClusters = JSON.deepCopyClusters(Dataset.getClusters());
                // calculate the new center of each cluster
                JSON.calculateNewCenters(Dataset.getClusters());
                // assign the cluster to the new centers
                JSON.assignSitesToNewClusters(Dataset.getSites(), Dataset.getClusters());
            }
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
