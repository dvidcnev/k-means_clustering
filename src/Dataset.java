package src;

import java.util.ArrayList;

public class Dataset {
    private static ArrayList<Site> sites; // Static variable to hold sites
    private static ArrayList<Cluster> clusters;
    // each cluster has an rgb
    private static ArrayList<RGB> rgb;

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

    public static void setRGB(ArrayList<RGB> rgb) {
        Dataset.rgb = rgb;
    }

    public static ArrayList<RGB> getRGB() {
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
