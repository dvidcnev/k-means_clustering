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

        MapGUI mapGui = new MapGUI();
        // GUI interface
        if (Boolean.valueOf(args[0])) {
            mapGui.launchMap();
        }
        // SEQUENTIAL
        if (Integer.valueOf(args[1]) == 0) {
            // the random sites that get generated
            ArrayList<Site> sites = JSON.randomPoints(NumSites);
            // the random clusters that get generated
            double minLatitude = JSON.minLatitude(sites);
            double maxLatitude = JSON.maxLatitude(sites);
            double minLongitude = JSON.minLongitude(sites);
            double maxLongitude = JSON.maxLongitude(sites);

        }

    }
}
