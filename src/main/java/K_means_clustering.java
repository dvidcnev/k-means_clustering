
import java.io.IOException;
import java.util.ArrayList;
import mpi.*;
import java.util.List;


public class K_means_clustering {

    public static void main(String[] args) {
        
        // Parameters passed to the program from the run.sh script
        int cycles;
        boolean DrawMap = false;
        int mode = 0;
        int NumClusters = 1;
        int NumSites = 1;

        // Lists to hold custom and MPI arguments separately
        List<String> customArgs = new ArrayList<>();
        List<String> mpiArgs = new ArrayList<>();

        // A switch case where we go through the arguments and assign them to the correct variables
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
        int rank = -1;
        // Convert mpiArgs to an array
        String[] mpiArgsArray = mpiArgs.toArray(new String[0]);
        if ( mode == 2 ) {
            MPI.Init(mpiArgsArray);
            rank = MPI.COMM_WORLD.Rank();

        }


        if( rank == 0 || mode != 2 ) 
        {
            // if numsites is less or equal to the provided dataset, generate with dataset size
            if (NumSites <= JSON.getDatasetSize()) {
                // generate random sites
                Dataset.setSites(JSON.randomizeDataset(NumSites));
            } else {
                // generate random sites around europe, and set the min and max zoom levels, also set the bounds for geographical map
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
            JSON.assignSitesToNewClusters(Dataset.getSites(), Dataset.getClusters());

        }
            
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
                cycles = 0;
                boolean converged = false;

                if (rank == 0) {
                    System.out.println("Site size: " + Dataset.getSites().size() + " even though numsites is " + NumSites);
                }

                Dataset.initializeClustersForProcessors();
                ArrayList<Cluster> copiedClusters = JSON.deepCopyClusters(Dataset.getClusters());

                long startTime = System.currentTimeMillis();
                long measurementS = 0;
                long measurementE = 0;

                // System.out.println("Processor " + rank + " has " + Dataset.getClusters().size() + " clusters and " + Dataset.getSites().size() + " sites");
                // for ( int i=0; i<copiedClusters.size(); i++ ) {
                //     System.out.println("Cluster["+i+"] " + copiedClusters.get(i).getId() + " with latitude " + copiedClusters.get(i).getLa() + " and longitude " + copiedClusters.get(i).getLo() + " for processor " + rank);
                // }
                // for ( int i=0; i<Dataset.getSites().size(); i++ ) {
                //     System.out.println("Site["+i+"] with latitude " + Dataset.getSites().get(i).getLa() + " and longitude " + Dataset.getSites().get(i).getLo() + " for processor " + rank);
                // }
                
                while (!converged) {
                    cycles++;

                    // if ( rank == 0 ) {
                    //     measurementS = System.currentTimeMillis();
                    // }
                    Distributive.calculateNewCenters(Dataset.getClusters());
                    
                    // if (rank == 0) {
                    //     measurementE = System.currentTimeMillis();
                    //     System.out.println("Time to calculate new centers: " + (measurementE - measurementS) / 1000.0 + "s");
                    // }

                    measurementS = System.currentTimeMillis();
                    Distributive.assignSitesToNewClusters(Dataset.getSites(), Dataset.getClusters());
                    measurementE = System.currentTimeMillis();
                    // if (rank == 0) {
                    //     System.out.println("Time to assign sites to new clusters: " + (measurementE - measurementS) / 1000.0 + "s");
                    // }

                    measurementS = System.currentTimeMillis();
                    converged = Distributive.clustersAreTheSame(Dataset.getClusters(), copiedClusters);
                    measurementE = System.currentTimeMillis();
                    // if (rank == 0) {
                    //     System.out.println("Time to check if clusters are the same: " + (measurementE - measurementS) / 1000.0 + "s");
                    // }

                    if (rank == 0) {
                        System.out.println("Cycle: " + cycles);
                    }

                    copiedClusters = JSON.deepCopyClusters(Dataset.getClusters());
                    System.out.println("Processor " + rank + " has " + Dataset.getClusters().size() + " clusters and " + Dataset.getSites().size() + " sites");
                    for ( int i=0; i<copiedClusters.size(); i++ ) {
                        System.out.println("Cluster["+i+"] " + copiedClusters.get(i).getId() + " with latitude " + copiedClusters.get(i).getLa() + " and longitude " + copiedClusters.get(i).getLo() + " for processor " + rank);
                    }
                }
                
                if (rank == 0) {
                    long endTime = System.currentTimeMillis();
                    System.out.println("[DISTRIBUTED] Total time: " + (endTime - startTime) / 1000.0 + "s");
                    System.out.println("[DISTRIBUTED] Total cycles: " + cycles);
                }
                MPI.Finalize();
        }
        
                



        // IF TRUE, DRAW GUI MAP
        if (DrawMap) {
            if ( mode == 2 ) {
                MPI.Init(mpiArgsArray);
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
