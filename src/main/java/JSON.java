
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;

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

        // Assuming GeoJSON structure has 'features' containing polygons
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

    // generate random sites around EUROPE with random latitudes and longtiudes
    // around central europe and the max capacity for each site generated around
    // europe is a random number between 0 and the max capacity of the dataset
    public static ArrayList<Site> randomizeEurope(int times) {
        Random rand = new Random();
        ArrayList<Site> sites = new ArrayList<Site>();

        // Assuming the maximum capacity is set as 100 for example purposes
        double maxCapacity = findMaxCapacity();

        // Generate random sites around Europe AFTER the whole dataset from europe has
        // been generated
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

    // with the given clusters ( amount of clusters ), assign points to each with
    // random
    // coordinates
    public static ArrayList<Cluster> randomClusters(int numClusters, ArrayList<Site> sites) {
        if (sites.size() < numClusters) {
            throw new IllegalArgumentException("Number of clusters cannot exceed number of sites");
        }
    
        ArrayList<Cluster> clusters = new ArrayList<>();
        Random rand = new Random();
        Set<Integer> usedIndices = new HashSet<>(); // To track already used site indices
    
        while (clusters.size() < numClusters) {
            int randomIndex;
            do {
                randomIndex = rand.nextInt(sites.size());
            } while (usedIndices.contains(randomIndex)); // Ensure unique site selection
    
            usedIndices.add(randomIndex);
            Site site = sites.get(randomIndex);
            double randomLatitude = Double.valueOf(site.getLa());
            double randomLongitude = Double.valueOf(site.getLo());
    
            Cluster cluster = new Cluster(randomLatitude, randomLongitude);
            cluster.setId(clusters.size()); // Assign unique ID based on order of creation
            clusters.add(cluster);
        }
    
        return clusters;
    }
    

    // make each cluster have an RGB value ( for the color ) and make sure that the
    // rgb sticks for that index of the cluster
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

    public static ArrayList<Cluster> deepCopyClusters(ArrayList<Cluster> clusters) {
        ArrayList<Cluster> copiedClusters = new ArrayList<>();
        for (Cluster cluster : clusters) {
            // Create a new Cluster instance and copy values
            Cluster newCluster = new Cluster(cluster.getLa(), cluster.getLo());
            // If there are other properties to copy, do it here
            copiedClusters.add(newCluster);
        }
        return copiedClusters;
    }

    // if a site is closer to a cluster than the other clusters, then assign it to
    // that cluster
    public static void assignSitesToClusters(ArrayList<Site> sites, ArrayList<Cluster> clusters) {
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



    // calculate the new center of each cluster ( make sure it's in the center of
    // the sites that it contains)
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
            if (numSites != 0) { // Avoid division by zero
                // Update the coordinates in the actual clusters list
                cluster.setLa(sumLatitude / numSites);
                cluster.setLo(sumLongitude / numSites);
            }
        }
    }

    

    public static double calculateDistanceBetweenCentroids(Cluster cluster1, Cluster cluster2) {
        double latDiff = cluster1.getLa() - cluster2.getLa();
        double lonDiff = cluster1.getLo() - cluster2.getLo();
        return Math.sqrt(latDiff * latDiff + lonDiff * lonDiff);
    }


    // check if the clusters are the same for a given new site with new center and
    // another older site provided
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
        // Initialize the list that will store the copies
        ArrayList<Cluster> copiedClusters = new ArrayList<>();
    
        // Use parallelStream for concurrent processing
        clusters.parallelStream()
                .map(cluster -> new Cluster(cluster.getLa(), cluster.getLo())) // Create a copy of each cluster
                .forEachOrdered(copiedClusters::add); // Manually add each copy to the list
    
        return copiedClusters;
    }
    


    public static void calculateNewCenters(ArrayList<Cluster> clusters) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // Use available cores
        List<Future<?>> futures = new ArrayList<>();
    
        for (Cluster cluster : clusters) {
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
                if (numSites != 0) { // Avoid division by zero
                    cluster.setLa(sumLatitude / numSites);
                    cluster.setLo(sumLongitude / numSites);
                }
            }));
        }
    
        // Wait for all tasks to complete
        for (Future<?> future : futures) {
            try {
                future.get(); // This will block until the task completes
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown(); // Shut down the executor service
    }


    public static void assignSitesToNewClusters(ArrayList<Site> sites, ArrayList<Cluster> clusters) {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); // Use available cores
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
                future.get(); // This will block until the task completes
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        executor.shutdown(); // Shut down the executor service
    }



    public static boolean clustersAreTheSame(ArrayList<Cluster> newClusters, ArrayList<Cluster> oldClusters) {
        // Use parallelStream() for concurrent processing
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

    public static ArrayList<Cluster> deepCopyClusters(ArrayList<Cluster> originalClusters) {
        ArrayList<Cluster> copiedClusters = new ArrayList<>();
        for (Cluster cluster : originalClusters) {
            Cluster copiedCluster = new Cluster(cluster.getLa(), cluster.getLo());
            copiedCluster.setId(cluster.getId());
            // copiedCluster.setRGB(cluster.getRGB()); // Assuming RGB is immutable or handled elsewhere
            copiedClusters.add(copiedCluster);
        }
        return copiedClusters;
    }

    public static void calculateNewCenters(ArrayList<Cluster> clusters) {
        for (Cluster cluster : clusters) {
            double sumLat = 0.0;
            double sumLon = 0.0;
            int count = 0;
            
            for (Site site : Dataset.getSites()) {
                if (site.getCluster().getId() == cluster.getId()) {
                    sumLat += Double.parseDouble(site.getLa());
                    sumLon += Double.parseDouble(site.getLo());
                    count++;
                }
            }

            if (count > 0) {
                cluster.setLa(sumLat / count);
                cluster.setLo(sumLon / count);
            }
        }
    }
    public static void assignSitesToNewClusters(ArrayList<Site> sites, ArrayList<Cluster> clusters) {
        for (Site site : sites) {
            double minDistance = Double.MAX_VALUE;
            Cluster nearestCluster = null;

            for (Cluster cluster : clusters) {
                double distance = calculateDistance(site.getLa(), site.getLo(), cluster.getLa(), cluster.getLo());
                if (distance < minDistance) {
                    minDistance = distance;
                    nearestCluster = cluster;
                }
            }

            if (nearestCluster != null) {
                site.setCluster(nearestCluster);
            }
        }
    }

    private static double calculateDistance(String la1, String lo1, double la2, double lo2) {
        double latitude1 = Double.parseDouble(la1);
        double longitude1 = Double.parseDouble(lo1);
        double latitude2 = la2;
        double longitude2 = lo2;

        double earthRadius = 6371; // km
        double dLat = Math.toRadians(latitude2 - latitude1);
        double dLng = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + 
                    Math.cos(Math.toRadians(latitude1)) * Math.cos(Math.toRadians(latitude2)) * 
                    Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = earthRadius * c;

        return distance;
    }


    public static boolean clustersAreTheSame(ArrayList<Cluster> clusters1, ArrayList<Cluster> clusters2) {
        if (clusters1.size() != clusters2.size()) {
            return false;
        }

        for (int i = 0; i < clusters1.size(); i++) {
            Cluster cluster1 = clusters1.get(i);
            Cluster cluster2 = clusters2.get(i);
            if (cluster1.getId() != cluster2.getId() ||
                cluster1.getLa() != cluster2.getLa() ||
                cluster1.getLo() != cluster2.getLo()) {
                return false;
            }
        }

        return true;
    }
}