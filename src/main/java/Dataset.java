
import java.io.Serializable;
import java.util.ArrayList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import mpi.*;
import java.util.Arrays;


public class Dataset {
    private static ArrayList<Site> sites; // Static variable to hold sites
    private static ArrayList<Cluster> clusters;
    public static int minZoom = 6;
    public static int Zoom = 6;
    public static double[] southwestBound = { 47, 3 };
    public static double[] northeastBound = { 55.5, 18 };

    
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

    /* 
    public static void initializeClustersForProcessors() {
        try {
            int id = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();
    
            int numClusters = 0;
            int numSites = 0;
    
            if (id == 0) {
                numClusters = Dataset.getClusters().size();
                numSites = Dataset.getSites().size();
            }
    
            // Broadcast the number of clusters and sites to all processes
            int[] sizes = new int[2];
            if (id == 0) {
                sizes[0] = numClusters;
                sizes[1] = numSites;
            }
            MPI.COMM_WORLD.Bcast(sizes, 0, 2, MPI.INT, 0);
            numClusters = sizes[0];
            numSites = sizes[1];
    
            // Calculate send counts and displacements for clusters
            int[] clusterSendCounts = new int[size];
            int[] clusterDisplacements = new int[size];
            for (int i = 0; i < size; i++) {
                clusterSendCounts[i] = numClusters / size;
                clusterDisplacements[i] = i * clusterSendCounts[i];
            }
            if (numClusters % size != 0) {
                clusterSendCounts[size - 1] += numClusters % size;
            }
    
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
    
            if (id == 0) {
                ArrayList<Cluster> clusters = Dataset.getClusters();
                ArrayList<Site> sites = Dataset.getSites();
    
                for (int dest = 1; dest < size; dest++) {
                    Cluster[] clusterSubset = Arrays.copyOfRange(clusters.toArray(new Cluster[0]), clusterDisplacements[dest], clusterDisplacements[dest] + clusterSendCounts[dest]);
                    Site[] siteSubset = Arrays.copyOfRange(sites.toArray(new Site[0]), siteDisplacements[dest], siteDisplacements[dest] + siteSendCounts[dest]);
    
                    MPI.COMM_WORLD.Send(clusterSubset, 0, clusterSubset.length, MPI.OBJECT, dest, 0);
                    MPI.COMM_WORLD.Send(siteSubset, 0, siteSubset.length, MPI.OBJECT, dest, 1);
                }
    
                // Keep the root's own portion
                ArrayList<Cluster> localClusters = new ArrayList<>(clusters.subList(0, clusterSendCounts[0]));
                ArrayList<Site> localSites = new ArrayList<>(sites.subList(0, siteSendCounts[0]));
                Dataset.setClusters(localClusters);
                Dataset.setSites(localSites);
            } else {
                Cluster[] receivedClusters = new Cluster[clusterSendCounts[id]];
                Site[] receivedSites = new Site[siteSendCounts[id]];
    
                MPI.COMM_WORLD.Recv(receivedClusters, 0, clusterSendCounts[id], MPI.OBJECT, 0, 0);
                MPI.COMM_WORLD.Recv(receivedSites, 0, siteSendCounts[id], MPI.OBJECT, 0, 1);
    
                Dataset.setClusters(new ArrayList<>(Arrays.asList(receivedClusters)));
                Dataset.setSites(new ArrayList<>(Arrays.asList(receivedSites)));
            }
        } catch (MPIException e) {
            System.err.println("MPIException occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    */
    
    public static void initializeClustersForProcessors() {
        try {
            int id = MPI.COMM_WORLD.Rank();
    
            ArrayList<Cluster> clusters = null;
            ArrayList<Site> sites = null;
    
            if (id == 0) {
                clusters = Dataset.getClusters();
                sites = Dataset.getSites();
            }
    
            // Determine the number of clusters and sites on the root process
            int[] sizes = new int[2];
            if (id == 0) {
                sizes[0] = clusters.size();
                sizes[1] = sites.size();
            }
    
            // Broadcast the sizes of clusters and sites to all processes
            MPI.COMM_WORLD.Bcast(sizes, 0, 2, MPI.INT, 0);
            int numClusters = sizes[0];
            int numSites = sizes[1];
    
            // Initialize arrays for broadcasting clusters and sites
            Cluster[] clusterArray = new Cluster[numClusters];
            Site[] siteArray = new Site[numSites];
    
            if (id == 0) {
                clusterArray = clusters.toArray(new Cluster[0]);
                siteArray = sites.toArray(new Site[0]);
            }
    
            // Broadcast clusters and sites to all processes
            MPI.COMM_WORLD.Bcast(clusterArray, 0, numClusters, MPI.OBJECT, 0);
            MPI.COMM_WORLD.Bcast(siteArray, 0, numSites, MPI.OBJECT, 0);
    
            // Set the received clusters and sites in the Dataset
            Dataset.setClusters(new ArrayList<>(Arrays.asList(clusterArray)));
            Dataset.setSites(new ArrayList<>(Arrays.asList(siteArray)));
        } catch (MPIException e) {
            System.err.println("MPIException occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

}


@JsonIgnoreProperties(ignoreUnknown = true)
class Site implements Serializable {
    private double capacity;
    private String la;
    private String lo;
    private Cluster cluster;

    // Getters and setters
    public void setLa(double randomLatitude) {
        this.la = String.valueOf(randomLatitude);
    }

    public void setLo(double randomLongitude) {
        this.lo = String.valueOf(randomLongitude);
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

class Cluster implements Serializable {
    private double Latitude;
    private double Longitude;
    private RGB rgb;
    private int id;

    // Constructor
    public Cluster(double Latitude, double Longitude) {
        this.Latitude = Latitude;
        this.Longitude = Longitude;
    }

    // Getters and setters
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public double[] getCenter() {
        return new double[] {this.Latitude, this.Longitude};
    }
    
    public void setCenter(double[] center) {
        this.Latitude = center[0];
        this.Longitude = center[1];
    }


}



class RGB implements Serializable {

    private int r;
    private int g;
    private int b;

    // Constructor
    public RGB(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
    }

    // Getters
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