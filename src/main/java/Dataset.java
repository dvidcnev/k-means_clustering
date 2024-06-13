
import java.io.Serializable;
import java.util.ArrayList;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

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