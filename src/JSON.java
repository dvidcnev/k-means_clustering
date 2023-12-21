package src;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

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

    // generate random sites around EUROPE with random latitudes and longtiudes
    // around central europe and the max capacity for each site generated around
    // europe is a random number between 0 and the max capacity of the dataset
    public static ArrayList<Site> randomizeEurope(int times) {
        Random rand = new Random();
        ArrayList<Site> sites = randomizeDataset(getDatasetSize());

        // Assuming the maximum capacity is set as 100 for example purposes
        double maxCapacity = findMaxCapacity();

        // Generate random sites around Europe AFTER the whole dataset from europe has
        // been generated
        double minLatitude = 36.0;
        double maxLatitude = 72.0;
        double minLongitude = -25.0;
        double maxLongitude = 45.0;

        for (int i = 0; i < times - getDatasetSize(); i++) {
            double randomLatitude = minLatitude + (maxLatitude - minLatitude) * rand.nextDouble();
            double randomLongitude = minLongitude + (maxLongitude - minLongitude) * rand.nextDouble();
            double randomCapacity = 0 + (maxCapacity - 0) * rand.nextDouble();
            Site site = new Site();
            site.setLa(randomLatitude);
            site.setLo(randomLongitude);
            site.setCapacity(randomCapacity);
            sites.add(site);
        }
        return sites;
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
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        Random rand = new Random();
        // for each cluster, generate a random point that is from the sites, and don't
        // regenerate if it's already been generated
        for (int i = 0; i < numClusters; i++) {
            int randomIndex = rand.nextInt(sites.size());
            double randomLatitude = Double.valueOf(sites.get(randomIndex).getLa());
            double randomLongitude = Double.valueOf(sites.get(randomIndex).getLo());
            Cluster cluster = new Cluster(randomLatitude, randomLongitude);
            if (!clusters.contains(cluster)) {
                clusters.add(cluster);
            }
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

    // print first cluster in the list of clusters
    public static void printFirstCluster(ArrayList<Cluster> clusters) {
        System.out.println("JSON: First cluster: " + clusters.get(0).getLa() + " " + clusters.get(0).getLo());
    }

    // check if the clusters are the same for a given new site with new center and
    // another older site provided
    public static boolean clustersAreTheSame(ArrayList<Cluster> newClusters, ArrayList<Cluster> oldClusters) {
        double threshold = 0.001;
        for (int i = 0; i < newClusters.size(); i++) {
            double distance = calculateDistanceBetweenCentroids(newClusters.get(i), oldClusters.get(i));
            if (distance > threshold) { // clusters are the same
                return false;
            }
        }
        return true;
    }

    private static double calculateDistanceBetweenCentroids(Cluster newCluster, Cluster oldCluster) {
        // Calculate distance between centroids (newCluster and oldCluster)
        double distance = Math.sqrt(Math.pow((newCluster.getLa() - oldCluster.getLa()), 2)
                + Math.pow((newCluster.getLo() - oldCluster.getLo()), 2));
        return distance;
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
class Site {
    private String name;
    private double capacity;
    private String la;
    private String lo;
    private Cluster cluster;

    // Getters and setters
    public void setLa(double la) {
        this.la = String.valueOf(la);
    }

    public void setLo(double lo) {
        this.lo = String.valueOf(lo);
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public double getCapacity() {
        return capacity;
    }

    public String getLa() {
        return la;
    }

    public String getLo() {
        return lo;
    }

    public Cluster getCluster() {
        return cluster;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

}

class Cluster {
    private double Latitude;
    private double Longitude;
    private RGB rgb;

    // Getters and setters
    public Cluster(double Latitude, double Longitude) {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
    }

    public double getLa() {
        return Latitude;
    }

    public double getLo() {
        return Longitude;
    }

    public void setLa(double Latitude) {
        this.Latitude = Latitude;
    }

    public void setLo(double Longitude) {
        this.Longitude = Longitude;
    }

    public void setRGB(RGB rgb) {
        this.rgb = rgb;
    }

    public RGB getRGB() {
        return rgb;
    }

}

class RGB {
    private int r;
    private int g;
    private int b;

    public RGB(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    public int getR() {
        return r;
    }

    public int getG() {
        return g;
    }

    public int getB() {
        return b;

    }
}