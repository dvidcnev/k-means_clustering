
import java.io.Serializable;
import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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


}


@JsonIgnoreProperties(ignoreUnknown = true)
class Site implements Serializable {
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
    
    public static double[] serializeSite(Site site) {
        double[] data = new double[4];
        data[0] = Double.parseDouble(site.getLa());
        data[1] = Double.parseDouble(site.getLo());
        data[2] = site.getCapacity();
        data[3] = site.getCluster() != null ? site.getCluster().getId() : -1; // Use -1 if cluster is null
        return data;
    }
    
    public static Site deserializeSite(double[] data) {
        Site site = new Site();
        site.setLa(data[0]);
        site.setLo(data[1]);
        site.setCapacity(data[2]);
        // Cluster needs to be set separately after deserialization
        return site;
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

    public static double[] serializeCluster(Cluster cluster) {
        double[] data = new double[4];
        data[0] = cluster.getLa();
        data[1] = cluster.getLo();
        data[3] = cluster.getId();
        return data;
    }

    public static Cluster deserializeCluster(double[] data) {
        Cluster cluster = new Cluster(data[0], data[1]);
        cluster.setId((int) data[3]);
        return cluster;
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