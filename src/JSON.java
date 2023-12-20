package src;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class JSON {

    public static ArrayList<Site> randomPoints(int times) {
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

}

class Site {
    private String name;
    private double capacity;
    private String la;
    private String lo;

    // Getters and setters
    public String getName() {
        return name;
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

}

class Cluster {
    private double Latitude;
    private double Longitude;

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

}