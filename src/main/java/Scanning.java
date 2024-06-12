
import java.util.ArrayList;




import mpi.*;
import java.util.List;


public class Scanning {

    public static void main(String[] args) {
        
        int cycles = 0;
        boolean DrawMap = false;
        int mode = 2;
        int NumClusters = 10;
        int NumSites = 1000;

        // Lists to hold custom and MPI arguments separately
        List<String> customArgs = new ArrayList<>();
        List<String> mpiArgs = new ArrayList<>();

        // Separate arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--DrawMap":
                    DrawMap = Boolean.parseBoolean(args[++i]);
                    customArgs.add(args[i-1]);
                    customArgs.add(args[i]);
                    break;
                case "--mode":
                    mode = Integer.parseInt(args[++i]);
                    customArgs.add(args[i-1]);
                    customArgs.add(args[i]);
                    break;
                case "--NumClusters":
                    NumClusters = Integer.parseInt(args[++i]);
                    customArgs.add(args[i-1]);
                    customArgs.add(args[i]);
                    break;
                case "--NumSites":
                    NumSites = Integer.parseInt(args[++i]);
                    customArgs.add(args[i-1]);
                    customArgs.add(args[i]);
                    break;
                default:
                    mpiArgs.add(args[i]);
                    break;
            }
        }

        // Convert mpiArgs to an array
        String[] mpiArgsArray = mpiArgs.toArray(new String[0]);


        // if numsites is less or equal to the provided dataset, generate with dataset size
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

        // SEQUENTIAL
        if (mode == 0) {
            cycles = 0;
            // Measure the time
            long startTime = System.currentTimeMillis();

            while (true) {
                cycles++;
                ArrayList<Cluster> copiedClusters = JSON.deepCopyClusters(Dataset.getClusters());
                JSON.calculateNewCenters(Dataset.getClusters());
                JSON.assignSitesToNewClusters(Dataset.getSites(), Dataset.getClusters());
                if (JSON.clustersAreTheSame(Dataset.getClusters(), copiedClusters)) {
                    break;
                }
                System.out.println("Cycle: " + cycles);
            }
            // Measure the time in seconds
            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;
            // System.out.println("[SEQUENTIAL] Total time: " + totalTime + "ms");
            System.out.println("[SEQUENTIAL] Total time: " + totalTime / 1000.0 + "s");
            System.out.println("[SEQUENTIAL] Total cycles: " + cycles);
        }
        // PARALLEL
        if (mode == 1) {
            cycles = 0;
            // MEASURE THE TIME
            long startTime = System.currentTimeMillis();

            while (true) {
                cycles++;
                ArrayList<Cluster> copiedClusters = Parallel.deepCopyClusters(Dataset.getClusters());
                Parallel.calculateNewCenters(Dataset.getClusters());
                Parallel.assignSitesToNewClusters(Dataset.getSites(), Dataset.getClusters()); 
                if (Parallel.clustersAreTheSame(Dataset.getClusters(), copiedClusters)) {
                    break;
                }
                System.out.println("Cycle: " + cycles);
            }

            long endTime = System.currentTimeMillis();
            long totalTime = endTime - startTime;

            System.out.println("[PARALLEL] Total time: " + totalTime / 1000.0 + "s");
            System.out.println("[PARALLEL] Total cycles: " + cycles);
        }
        // DISTRIBUTED
        if (mode == 2) {
            MPI.Init(mpiArgsArray);
            try {
                cycles = 0;
                boolean converged = false;
                int rank = MPI.COMM_WORLD.Rank(); // Get the rank of the current processor
        
                ArrayList<Cluster> copiedClusters = Distributive.deepCopyClusters(Dataset.getClusters());

                long startTime = System.currentTimeMillis();
        
                while (!converged) {
                    cycles++;
        
                    // System.out.println("Calculating new centers");
                    Distributive.calculateNewCenters(Dataset.getClusters());
                    // System.out.println("Assigning sites to new clusters");
                    Distributive.assignSitesToNewClusters(Dataset.getSites(), Dataset.getClusters());
                    // System.out.println("Checking if clusters are the same");
                    converged = Distributive.clustersAreTheSame(Dataset.getClusters(), copiedClusters);
        
                    if (rank == 0) {
                        System.out.println("Cycle: " + cycles);
                    }
        
                    // Update copiedClusters with the new clusters
                    copiedClusters = Distributive.deepCopyClusters(Dataset.getClusters());
                }
                long endTime = System.currentTimeMillis();
                if ( rank == 0 ) {
                    System.out.println("[DISTRIBUTED] Total time: " + (endTime - startTime) / 1000.0 + "s");
                    System.out.println("[DISTRIBUTED] Total cycles: " + cycles);
                }
            } finally {
                MPI.Finalize();
            }
        }
        
                



        // IF TRUE, DRAW GUI MAP
        if (DrawMap) {
            if ( mode == 2 ) {
                MPI.Init(mpiArgsArray);
                int rank = MPI.COMM_WORLD.Rank();
                if (rank == 0) {
                    drawMap();
                }
            }
            else {
                drawMap();
            }
        }

    }
    
    public static void drawMap () {
        for (Cluster clr : Dataset.getClusters()) {
            clr.setRGB(
                    new RGB((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255)));
            // Cluster id printed
            // System.out.println("Cluster ID: " + clr.getId() + " RGB: " + clr.getRGB().getR() + " "
            //         + clr.getRGB().getG() + " " + clr.getRGB().getB());
        }
        for (Site site : Dataset.getSites()) {
            // Find the cluster with the same ID as the site's current cluster and update the site's cluster reference
            Cluster updatedCluster = Dataset.getClusters().stream()
                                            .filter(clr -> clr.getId() == site.getCluster().getId())
                                            .findFirst()
                                            .orElse(null);
            if (updatedCluster != null) {
                site.setCluster(updatedCluster);
            }
        }
        MapGUI mapGui = new MapGUI();
        mapGui.launchMap();
    }
}
