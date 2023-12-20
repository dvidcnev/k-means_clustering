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

        Dataset.setSites(JSON.randomPoints(NumSites));
        MapGUI mapGui = new MapGUI();

        // SEQUENTIAL
        if (Integer.valueOf(args[1]) == 0) {
            // the random clusters that get generated
            double minLatitude = JSON.minLatitude(Dataset.getSites());
            System.out.println("minLatitude: " + minLatitude);
            double maxLatitude = JSON.maxLatitude(Dataset.getSites());
            System.out.println("maxLatitude: " + maxLatitude);
            double minLongitude = JSON.minLongitude(Dataset.getSites());
            System.out.println("minLongitude: " + minLongitude);
            double maxLongitude = JSON.maxLongitude(Dataset.getSites());
            System.out.println("maxLongitude: " + maxLongitude);
            Dataset.setClusters(JSON.randomClusters(NumClusters, minLatitude, maxLatitude, minLongitude,
                    maxLongitude));
        }

        if (Boolean.valueOf(args[0])) {
            mapGui.launchMap();
        }

    }

}
