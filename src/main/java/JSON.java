
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import mpi.*;

public class JSON {

    public static ArrayList<Site> randomizeDataset(int times) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Read JSON file into a List of objects
            ArrayList<Site> objects = objectMapper.readValue(new File("germany.json"),
                    new TypeReference<ArrayList<Site>>() {
                    });

            Random rand = new Random();
            int randomIndex;
            // array to store info of which objects have been selected
            ArrayList<Site> selectedObjects = new ArrayList<Site>();

            // Access elements in the List
            for (int i = 0; i < times; i++) {
                randomIndex = rand.nextInt(objects.size());
                if (!selectedObjects.contains(objects.get(randomIndex))) {
                    selectedObjects.add(objects.get(randomIndex));
                }
            }
            return selectedObjects;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // generate random coordinates within europe that are in the geo json SHAPE
    // POLYGON ( only on landmass )
    // Assume methods for parsing GeoJSON and extracting country polygons are
    // present

    // Extract polygon coordinates from GeoJSON
    private static List<List<List<Double>>> extractPolygons(JsonNode root) {
        List<List<List<Double>>> polygons = new ArrayList<>();

        JsonNode features = root.path("features");
        for (JsonNode feature : features) {
            JsonNode geometry = feature.path("geometry");
            String type = geometry.path("type").asText();

            // Extract coordinates of polygons
            if ("Polygon".equals(type)) {
                polygons.add(extractPolygonCoordinates(geometry.path("coordinates")));
            } else if ("MultiPolygon".equals(type)) {
                JsonNode coordinates = geometry.path("coordinates");
                for (JsonNode polygonCoords : coordinates) {
                    polygons.add(extractPolygonCoordinates(polygonCoords));
                }
            }
        }
        return polygons;
    }

    // Extract coordinates of a single polygon
    private static List<List<Double>> extractPolygonCoordinates(JsonNode coordinates) {
        List<List<Double>> polygon = new ArrayList<>();
        for (JsonNode coord : coordinates) {
            for (JsonNode point : coord) {
                List<Double> innerPolygon = new ArrayList<>();
                innerPolygon.add(point.get(0).asDouble()); // longitude
                innerPolygon.add(point.get(1).asDouble()); // latitude
                polygon.add(innerPolygon);
            }
        }
        return polygon;
    }

    // Generate random coordinate within a polygon
    private static List<Double> generateRandomCoordinate(List<List<List<Double>>> polygons) {
        Random rand = new Random();
        List<List<Double>> polygon = polygons.get(rand.nextInt(polygons.size()));

        // Find bounding box of polygon
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE, maxY = Double.MIN_VALUE;

        for (List<Double> point : polygon) {
            double x = point.get(0);
            double y = point.get(1);

            if (x < minX)
                minX = x;
            if (x > maxX)
                maxX = x;
            if (y < minY)
                minY = y;
            if (y > maxY)
                maxY = y;
        }

        // Generate random longitude and latitude within bounding box
        double randomLon, randomLat;
        do {
            randomLon = minX + (maxX - minX) * rand.nextDouble();
            randomLat = minY + (maxY - minY) * rand.nextDouble();
        } while (!isPointInPolygon(randomLon, randomLat, polygon));

        List<Double> randomCoordinate = new ArrayList<>();
        randomCoordinate.add(randomLat);
        randomCoordinate.add(randomLon);

        return randomCoordinate;
    }

    // Check if a point is within a polygon using ray casting algorithm
    private static boolean isPointInPolygon(double lon, double lat, List<List<Double>> polygon) {
        int i, j;
        boolean c = false;
        int n = polygon.size();

        for (i = 0, j = n - 1; i < n; j = i++) {
            double xi = polygon.get(i).get(0);
            double yi = polygon.get(i).get(1);
            double xj = polygon.get(j).get(0);
            double yj = polygon.get(j).get(1);

            if (((yi > lat) != (yj > lat)) && (lon < (xj - xi) * (lat - yi) / (yj - yi) + xi)) {
                c = !c;
            }
        }
        return c;
    }

    public static ArrayList<double[]> generateLatLon(int times) {
        File geoJSONFile = new File("europe.geo.json");

        try {
            // Parse GeoJSON using Jackson library
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(geoJSONFile);

            // Extract polygon coordinates
            List<List<List<Double>>> polygons = extractPolygons(root);

            ArrayList<double[]> latLon = new ArrayList<>();

            for (int i = 0; i < times; i++) {
                List<Double> randomCoordinate = generateRandomCoordinate(polygons);
                double[] latLonArray = new double[2];
                latLonArray[0] = randomCoordinate.get(0);
                latLonArray[1] = randomCoordinate.get(1);
                // check if coordinates exist, if not add to list
                if (!latLon.contains(latLonArray)) {
                    latLon.add(latLonArray);
                }

            }
            return latLon;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    // generate random sites around EUROPE with random latitudes and longtiudes around central europe and the max capacity for each site generated around europe is a random number between 0 and the max capacity of the dataset
    public static ArrayList<Site> randomizeEurope(int times) {
        Random rand = new Random();
        ArrayList<Site> sites = new ArrayList<Site>();

        // Assuming the maximum capacity is set as 100 for example purposes
        double maxCapacity = findMaxCapacity();

        // Generate random sites around Europe AFTER the whole dataset from europe has been generated
        ArrayList<double[]> randomCoordinates = generateLatLon(times);

        // Generate random sites around Europe
        for (int i = 0; i < randomCoordinates.size(); i++) {
            double randomLatitude = randomCoordinates.get(i)[0];
            double randomLongitude = randomCoordinates.get(i)[1];
            double randomCapacity = rand.nextDouble() * maxCapacity;
            Site site = new Site();
            site.setLa(randomLatitude);
            site.setLo(randomLongitude);
            site.setCapacity(randomCapacity);
            // check if the coordinate has been added already
            if (!sites.contains(site)) {
                sites.add(site);
            }
        }

        return sites;
    }

    // find out the max bound of the capacity in the JSON
    public static double findMaxCapacity() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Read JSON file into a List of objects
            ArrayList<Site> objects = objectMapper.readValue(new File("germany.json"),
                    new TypeReference<ArrayList<Site>>() {
                    });
            double maxCapacity = 0;
            for (Site site : objects) {
                if (site.getCapacity() > maxCapacity) {
                    maxCapacity = site.getCapacity();
                }
            }
            return maxCapacity;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // get the size of the json dataset ( how many elements in total)
    public static int getDatasetSize() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Read JSON file into a List of objects
            ArrayList<Site> objects = objectMapper.readValue(new File("germany.json"),
                    new TypeReference<ArrayList<Site>>() {
                    });
            return objects.size();
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // with the given clusters ( amount of clusters ), assign points to each with random coordinates
    public static ArrayList<Cluster> randomClusters(int numClusters, ArrayList<Site> sites) {
        if (sites.size() < numClusters) {
            throw new IllegalArgumentException("Number of clusters cannot exceed number of sites");
        }
    
        ArrayList<Cluster> clusters = new ArrayList<>();
        Random rand = new Random();
        Set<Integer> usedIndices = new HashSet<>(); 
    
        // Generate random clusters
        while (clusters.size() < numClusters) {
            int randomIndex;
            do {
                randomIndex = rand.nextInt(sites.size());
            } while (usedIndices.contains(randomIndex)); 
    
            usedIndices.add(randomIndex);
            Site site = sites.get(randomIndex);
            double randomLatitude = Double.valueOf(site.getLa());
            double randomLongitude = Double.valueOf(site.getLo());
    
            Cluster cluster = new Cluster(randomLatitude, randomLongitude);
            cluster.setId(clusters.size()); 
            clusters.add(cluster);
        }
    
        return clusters;
    }
    
    // make each cluster have an RGB value ( for the color ) and make sure that the rgb sticks for that index of the cluster
    public static ArrayList<RGB> randomRGB(int numClusters) {
        ArrayList<RGB> rgb = new ArrayList<RGB>();
        Random rand = new Random();
        for (int i = 0; i < numClusters; i++) {
            int r = rand.nextInt(255);
            int g = rand.nextInt(255);
            int b = rand.nextInt(255);
            RGB rgbObj = new RGB(r, g, b);
            if (!rgb.contains(rgbObj)) {
                rgb.add(rgbObj);
            }
        }
        return rgb;
    }

    // copy clusters into a new set of memory so they don't overwrite existing clusters in Dataset
    public static ArrayList<Cluster> deepCopyClusters(ArrayList<Cluster> clusters) {
        ArrayList<Cluster> copiedClusters = new ArrayList<>();
        for (Cluster cluster : clusters) {
            Cluster newCluster = new Cluster(cluster.getLa(), cluster.getLo());
            copiedClusters.add(newCluster);
        }
        return copiedClusters;
    }

    // assign the cluster to the new centers
    public static void assignSitesToNewClusters(ArrayList<Site> sites, ArrayList<Cluster> clusters) {
        for (Site site : sites) {
            double minDistance = Double.MAX_VALUE;
            Cluster closestCluster = null;
            for (Cluster cluster : clusters) {
                double distance = calculateDistance(site, cluster);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCluster = cluster;
                }
            }
            site.setCluster(closestCluster);
        }
        Dataset.setSites(sites);
    }

    // calculate the new center of each cluster ( make sure it's in the center of the sites that it contains)
    public static void calculateNewCenters(ArrayList<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            double sumLatitude = 0;
            double sumLongitude = 0;
            int numSites = 0;
            for (Site site : Dataset.getSites()) {
                if (site.getCluster().equals(cluster)) {

                    sumLatitude += Double.valueOf(site.getLa());
                    sumLongitude += Double.valueOf(site.getLo());
                    numSites++;
                }
            }
            if (numSites != 0) {
                cluster.setLa(sumLatitude / numSites);
                cluster.setLo(sumLongitude / numSites);
            }
        }
    }

    // calculate the distance between the centroids of two clusters
    public static double calculateDistanceBetweenCentroids(Cluster cluster1, Cluster cluster2) {
        double latDiff = cluster1.getLa() - cluster2.getLa();
        double lonDiff = cluster1.getLo() - cluster2.getLo();
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }

    // Calculate the distance of a site between all the clusters
    public static double calculateDistance(Site site, Cluster cluster) {
        double siteLatitude = Double.valueOf(site.getLa());
        double siteLongitude = Double.valueOf(site.getLo());
        double clusterLatitude = cluster.getLa();
        double clusterLongitude = cluster.getLo();
        double distance = Math.sqrt(Math.pow((siteLatitude - clusterLatitude), 2)
                + Math.pow((siteLongitude - clusterLongitude), 2));
        return distance;
    }


    // check if the clusters are the same for a given new site with new center and another older site provided
    public static boolean clustersAreTheSame(ArrayList<Cluster> newClusters, ArrayList<Cluster> oldClusters) {
        for (int i = 0; i < newClusters.size(); i++) {
            double distance = calculateDistanceBetweenCentroids(newClusters.get(i), oldClusters.get(i));
            if (distance > 0) { // clusters are the same
                return false;
            }
        }
        return true;
    }
    
}

class Parallel {

    public static ArrayList<Cluster> deepCopyClusters(ArrayList<Cluster> clusters) {
        ArrayList<Cluster> copiedClusters = new ArrayList<>();
    
        clusters.parallelStream()
            // Create a copy of each cluster
            .map(cluster -> new Cluster(cluster.getLa(), cluster.getLo())) 
            .forEachOrdered(copiedClusters::add); 
    
        return copiedClusters;
    }
    
    public static void calculateNewCenters(ArrayList<Cluster> clusters) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        List<Future<?>> futures = new ArrayList<>();
    
        for (Cluster cluster : clusters) {
            // Submit a task to the executor service
            futures.add(executor.submit(() -> {
                double sumLatitude = 0;
                double sumLongitude = 0;
                int numSites = 0;
                for (Site site : Dataset.getSites()) {
                    if (site.getCluster().equals(cluster)) {
                        sumLatitude += Double.valueOf(site.getLa());
                        sumLongitude += Double.valueOf(site.getLo());
                        numSites++;
                    }
                }
                if (numSites != 0) {
                    cluster.setLa(sumLatitude / numSites);
                    cluster.setLo(sumLongitude / numSites);
                }
            }));
        }
    
        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get(); 
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown(); 
    }


    public static void assignSitesToNewClusters(ArrayList<Site> sites, ArrayList<Cluster> clusters) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); 
        List<Future<?>> futures = new ArrayList<>();
    
        for (Site site : sites) {
            futures.add(executor.submit(() -> {
                double minDistance = Double.MAX_VALUE;
                Cluster closestCluster = null;
                for (Cluster cluster : clusters) {
                    double distance = JSON.calculateDistance(site, cluster);
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestCluster = cluster;
                    }
                }
                site.setCluster(closestCluster);
            }));
        }
    
        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get(); 
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown(); 
    }



    public static boolean clustersAreTheSame(ArrayList<Cluster> newClusters, ArrayList<Cluster> oldClusters) {
        return newClusters.parallelStream()
                          .allMatch(newCluster -> {
                              int index = newClusters.indexOf(newCluster);
                              Cluster oldCluster = oldClusters.get(index);
                              double distance = JSON.calculateDistanceBetweenCentroids(newCluster, oldCluster);
                              return distance == 0; 
                          });
    }

}
class Distributive {

        public static void assignSitesToNewClusters(ArrayList<Site> sites, ArrayList<Cluster> clusters) {
            try {
                int id = MPI.COMM_WORLD.Rank();
                int size = MPI.COMM_WORLD.Size();
        
                int numSites = sites.size();
        
                // Calculate send counts and displacements for sites
                int[] siteSendCounts = new int[size];
                int[] siteDisplacements = new int[size];
                for (int i = 0; i < size; i++) {
                    siteSendCounts[i] = numSites / size;
                    siteDisplacements[i] = i * siteSendCounts[i];
                }
                if (numSites % size != 0) {
                    siteSendCounts[size - 1] += numSites % size;
                }
        
                // Distribute sites among processors
                Site[] siteArray = new Site[numSites];
                if (id == 0) {
                    siteArray = sites.toArray(new Site[0]);
                }
        
                Site[] localSites = new Site[siteSendCounts[id]];
                MPI.COMM_WORLD.Scatterv(siteArray, 0, siteSendCounts, siteDisplacements, MPI.OBJECT, localSites, 0, siteSendCounts[id], MPI.OBJECT, 0);
        
                // Perform local assignment of sites to clusters
                for (Site site : localSites) {
                    double minDistance = Double.MAX_VALUE;
                    Cluster closestCluster = null;
                    for (Cluster cluster : clusters) {
                        double distance = JSON.calculateDistance(site, cluster);
                        if (distance < minDistance) {
                            minDistance = distance;
                            closestCluster = cluster;
                        }
                    }
                    site.setCluster(closestCluster);
                }
        
                // Gather the updated sites from all processors
                Site[] updatedSites = new Site[numSites];
                MPI.COMM_WORLD.Gatherv(localSites, 0, localSites.length, MPI.OBJECT, updatedSites, 0, siteSendCounts, siteDisplacements, MPI.OBJECT, 0);
        
                // Broadcast the updated sites to all processors
                MPI.COMM_WORLD.Bcast(updatedSites, 0, updatedSites.length, MPI.OBJECT, 0);
        
                // Update the local copy of sites with the new assignments
                Dataset.setSites(new ArrayList<>(Arrays.asList(updatedSites)));
        
            } catch (MPIException e) {
                System.err.println("MPIException occurred: " + e.getMessage());
                e.printStackTrace();
            }
        }
      
    /*     
    public static void initializeClustersForProcessors() {
        try {
            int id = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();
    
            ArrayList<Cluster> clusters = null;
            ArrayList<Site> sites = null;
    
            if (id == 0) {
                clusters = Dataset.getClusters();
                sites = Dataset.getSites();
            }
    
            // Broadcast sizes
            int[] sizes = new int[2];
            if (id == 0) {
                sizes[0] = clusters.size();
                sizes[1] = sites.size();
            }
            MPI.COMM_WORLD.Bcast(sizes, 0, 2, MPI.INT, 0);
            int numClusters = sizes[0];
            int numSites = sizes[1];
    
            // Broadcast sites to all processors
            Site[] siteArray = new Site[numSites];
            if (id == 0) {
                siteArray = sites.toArray(new Site[0]);
            }
            MPI.COMM_WORLD.Bcast(siteArray, 0, numSites, MPI.OBJECT, 0);
            Dataset.setSites(new ArrayList<>(Arrays.asList(siteArray)));
    
            // Distribute clusters among processors
            int[] clusterSendCounts = new int[size];
            int[] clusterDisplacements = new int[size];
            for (int i = 0; i < size; i++) {
                clusterSendCounts[i] = numClusters / size;
                clusterDisplacements[i] = i * clusterSendCounts[i];
            }
            if (numClusters % size != 0) {
                clusterSendCounts[size - 1] += numClusters % size;
            }
    
            Cluster[] clusterArray = new Cluster[numClusters];
            if (id == 0) {
                clusterArray = clusters.toArray(new Cluster[0]);
            }
    
            Cluster[] localClusters = new Cluster[clusterSendCounts[id]];
            MPI.COMM_WORLD.Scatterv(clusterArray, 0, clusterSendCounts, clusterDisplacements, MPI.OBJECT, localClusters, 0, clusterSendCounts[id], MPI.OBJECT, 0);
            Dataset.setClusters(new ArrayList<>(Arrays.asList(localClusters)));
    
        } catch (MPIException e) {
            System.err.println("MPIException occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */

    public static void calculateNewCenters(ArrayList<Cluster> clusters) {
        try {

            double[] localSums = new double[clusters.size() * 3]; // latitudeSum, longitudeSum, count
            Arrays.fill(localSums, 0.0);
    
            for (int i = 0; i < clusters.size(); i++) {
                Cluster cluster = clusters.get(i);
                double sumLatitude = 0;
                double sumLongitude = 0;
                int numSites = 0;
    
                for (Site site : Dataset.getSites()) {
                    if (site.getCluster().equals(cluster)) {
                        sumLatitude += Double.valueOf(site.getLa());
                        sumLongitude += Double.valueOf(site.getLo());
                        numSites++;
                    }
                }
    
                localSums[i * 3] = sumLatitude;
                localSums[i * 3 + 1] = sumLongitude;
                localSums[i * 3 + 2] = numSites;
            }
    
            double[] globalSums = new double[clusters.size() * 3];
            MPI.COMM_WORLD.Allreduce(localSums, 0, globalSums, 0, clusters.size() * 3, MPI.DOUBLE, MPI.SUM);
    
            for (int i = 0; i < clusters.size(); i++) {
                double sumLatitude = globalSums[i * 3];
                double sumLongitude = globalSums[i * 3 + 1];
                int numSites = (int) globalSums[i * 3 + 2];
    
                if (numSites != 0) { // Avoid division by zero
                    clusters.get(i).setLa(sumLatitude / numSites);
                    clusters.get(i).setLo(sumLongitude / numSites);
                }
            }
        } catch (MPIException e) {
            System.err.println("MPIException occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean clustersAreTheSame(ArrayList<Cluster> newClusters, ArrayList<Cluster> oldClusters) {
        try {
            int id = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();
    
            int numClusters = newClusters.size();
    
            // Broadcast the number of clusters to all processes
            int[] clusterSize = new int[1];
            if (id == 0) {
                clusterSize[0] = numClusters;
            }
            MPI.COMM_WORLD.Bcast(clusterSize, 0, 1, MPI.INT, 0);
            numClusters = clusterSize[0];
    
            // Distribute clusters among processors
            int[] sendCounts = new int[size];
            int[] displacements = new int[size];
            for (int i = 0; i < size; i++) {
                sendCounts[i] = numClusters / size;
                displacements[i] = i * sendCounts[i];
            }
            if (numClusters % size != 0) {
                sendCounts[size - 1] += numClusters % size;
            }
    
            Cluster[] newClusterArray = new Cluster[numClusters];
            Cluster[] oldClusterArray = new Cluster[numClusters];
            if (id == 0) {
                newClusterArray = newClusters.toArray(new Cluster[0]);
                oldClusterArray = oldClusters.toArray(new Cluster[0]);
            }
    
            Cluster[] localNewClusters = new Cluster[sendCounts[id]];
            Cluster[] localOldClusters = new Cluster[sendCounts[id]];
            MPI.COMM_WORLD.Scatterv(newClusterArray, 0, sendCounts, displacements, MPI.OBJECT, localNewClusters, 0, sendCounts[id], MPI.OBJECT, 0);
            MPI.COMM_WORLD.Scatterv(oldClusterArray, 0, sendCounts, displacements, MPI.OBJECT, localOldClusters, 0, sendCounts[id], MPI.OBJECT, 0);
    
            boolean localSame = true;
            for (int i = 0; i < localNewClusters.length; i++) {
                double distance = JSON.calculateDistanceBetweenCentroids(localNewClusters[i], localOldClusters[i]);
                if (distance > 0) {
                    localSame = false;
                    break;
                }
            }
    
            boolean[] allSame = new boolean[1];
            MPI.COMM_WORLD.Allreduce(new boolean[]{localSame}, 0, allSame, 0, 1, MPI.BOOLEAN, MPI.LAND);
    
            return allSame[0];
        } catch (MPIException e) {
            System.err.println("MPIException occurred: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
