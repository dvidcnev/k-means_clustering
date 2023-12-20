package src;

import java.util.ArrayList;

public class MainProgram {

    private static ArrayList<Site> sites; // Static variable to hold sites

    public static void main(String[] args) {

        // If args length is not 1, then exit
        if (args.length < 4) {
            System.out.println("ERROR! Not enough arguments provided!");
            System.exit(1);
        }

        final int NumClusters = Integer.valueOf(args[2]);
        final int NumSites = Integer.valueOf(args[3]);

        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        sites = JSON.randomPoints(NumSites);
        MapGUI mapGui = new MapGUI();

        // SEQUENTIAL
        if (Integer.valueOf(args[1]) == 0) {
            // the random clusters that get generated
            double minLatitude = JSON.minLatitude(sites);
            double maxLatitude = JSON.maxLatitude(sites);
            double minLongitude = JSON.minLongitude(sites);
            double maxLongitude = JSON.maxLongitude(sites);
            clusters = JSON.randomClusters(NumClusters, minLatitude, maxLatitude, minLongitude,
                    maxLongitude);
        }

        if (Boolean.valueOf(args[0])) {
            mapGui.launchMap();
        }

    }

    public static ArrayList<Site> getSites() {
        return sites; // Getter method to access sites from other classes
    }
}
