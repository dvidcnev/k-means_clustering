package src;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
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

    public void printObjects() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Read JSON file into a List of objects
            List<MyObject> objects = objectMapper.readValue(new File("germany.json"),
                    new TypeReference<List<MyObject>>() {
                    });

            // Access elements in the List
            for (MyObject obj : objects) {
                System.out.println("Name: " + obj.getName());
                System.out.println("Capacity: " + obj.getCapacity());
                System.out.println("Latitude: " + obj.getLa());
                System.out.println("Longitude: " + obj.getLo());
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<MyObject> getPoints(int times) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // Read JSON file into a List of objects
            ArrayList<MyObject> objects = objectMapper.readValue(new File("germany.json"),
                    new TypeReference<ArrayList<MyObject>>() {
                    });

            Random rand = new Random();
            int randomIndex;
            // array to store info of which objects have been selected
            ArrayList<MyObject> selectedObjects = new ArrayList<MyObject>();

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

    public int getCapacity() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            // Read JSON file into a List of objects
            List<MyObject> objects = objectMapper.readValue(new File("germany.json"),
                    new TypeReference<List<MyObject>>() {
                    });

            // Get the number of objects in the array
            int numberOfObjects = objects.size();
            return numberOfObjects;
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }
    }
}

class MyObject {
    private String name;
    private double capacity;
    private String la;
    private String lo;

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public String getLa() {
        return la;
    }

    public void setLa(String la) {
        this.la = la;
    }

    public String getLo() {
        return lo;
    }

    public void setLo(String lo) {
        this.lo = lo;
    }
}
