package src;

import java.util.ArrayList;

public class Dataset {
    private static ArrayList<Site> sites; // Static variable to hold sites
    private static ArrayList<Cluster> clusters;

    public static void setSites(ArrayList<Site> sites) {
        Dataset.sites = sites;
    }

    public static ArrayList<Site> getSites() {
        return sites;
    }

    public static void setClusters(ArrayList<Cluster> clusters) {
        Dataset.clusters = clusters;
    }

    public static ArrayList<Cluster> getClusters() {
        return clusters;
    }

    // print instance of first cluster just for debugging purpose ( print the lat
    // and long of the first cluster)
    public static void printFirstCluster() {
        System.out.println("DATASET: First cluster: " + clusters.get(0).getLa() + " " + clusters.get(0).getLo());
    }

}
