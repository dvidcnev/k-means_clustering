package src;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class JSON {

    // public static void main(String[] args) {
    // JSON json = new JSON();
    // // json.printObjects();
    // int numberOfObjects = json.getCapacity();
    // System.out.println("Number of objects in the JSON array: " +
    // numberOfObjects);
    // json.assignPoints(5);
    // }

    public ArrayList<Site> fetchObjects() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Read JSON file into a List of objects
            ArrayList<Site> objects = objectMapper.readValue(new File("germany.json"),
                    new TypeReference<ArrayList<Site>>() {
                    });

            return objects;
            // Access elements in the List
            // for (Site obj : objects) {
            // System.out.println("Name: " + obj.getName());
            // System.out.println("Capacity: " + obj.getCapacity());
            // System.out.println("Latitude: " + obj.getLa());
            // System.out.println("Longitude: " + obj.getLo());
            // System.out.println();
            // }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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

    public static double maxLatitude(ArrayList<Site> objects) {
        double max = 0;
        for (Site obj : objects) {
            if (Double.valueOf(obj.getLa()) > max) {
                max = Double.valueOf(obj.getLa());
            }
        }
        return max;
    }

    public static double minLatitude(ArrayList<Site> objects) {
        double min = 100;
        for (Site obj : objects) {
            if (Double.valueOf(obj.getLa()) < min) {
                min = Double.valueOf(obj.getLa());
            }
        }
        return min;
    }

    public static double maxLongitude(ArrayList<Site> objects) {
        double max = 0;
        for (Site obj : objects) {
            if (Double.valueOf(obj.getLo()) > max) {
                max = Double.valueOf(obj.getLo());
            }
        }
        return max;
    }

    public static double minLongitude(ArrayList<Site> objects) {
        double min = 100;
        for (Site obj : objects) {
            if (Double.valueOf(obj.getLo()) < min) {
                min = Double.valueOf(obj.getLo());
            }
        }
        return min;
    }

    // generate a random number between min and max
    public static double generateRandom(double min, double max) {
        Random rand = new Random();
        return min + rand.nextDouble() * (max - min);
    }

    // with the given clusters ( amount of clusters ), assign points to each with
    // random
    // coordinates
    public static ArrayList<Cluster> randomClusters(int numClusters, double minLatitude, double maxLatitude,
            double minLongitude, double maxLongitude) {
        ArrayList<Cluster> clusters = new ArrayList<Cluster>();
        for (int i = 0; i < numClusters; i++) {
            double randomLatitude = generateRandom(minLatitude, maxLatitude);
            double randomLongitude = generateRandom(minLongitude, maxLongitude);
            clusters.add(new Cluster(randomLatitude, randomLongitude));
        }
        return clusters;
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