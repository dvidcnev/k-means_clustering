package src;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.geojson.*;

public class GeoJSONHelper {

    public static ArrayList<double[]> generateRandomCoordinatesWithinEurope(int times) {
        ArrayList<double[]> coordinates = new ArrayList<>();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            FeatureCollection featureCollection = objectMapper.readValue(new File("europe.geo.json"),
                    FeatureCollection.class);

            Set<String> coordinateSet = new HashSet<>();
            Random rand = new Random();

            int generated = 0;
            while (generated < times) {
                for (Feature feature : featureCollection.getFeatures()) {
                    Geometry geometry = (Geometry) feature.getGeometry();
                    if ((geometry instanceof Polygon) || (geometry instanceof MultiPolygon)) {
                        List<LngLatAlt> allPoints = getAllPoints(geometry);

                        int randomIndex = rand.nextInt(allPoints.size());
                        LngLatAlt randomCoordinate = allPoints.get(randomIndex);
                        String coordString = randomCoordinate.getLatitude() + "," + randomCoordinate.getLongitude();

                        if (!coordinateSet.contains(coordString)) {
                            double[] coord = { randomCoordinate.getLatitude(), randomCoordinate.getLongitude() };
                            coordinates.add(coord);
                            coordinateSet.add(coordString);
                            generated++;
                            if (generated == times) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return coordinates;
    }

    private static List<LngLatAlt> getAllPoints(Geometry geometry) {
        List<LngLatAlt> allPoints = new ArrayList<>();
        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            allPoints.addAll(polygon.getExteriorRing());
            allPoints.addAll((List<? extends LngLatAlt>) polygon.getInteriorRings());
        } else if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            for (List<List<LngLatAlt>> polygons : multiPolygon.getCoordinates()) {
                for (List<LngLatAlt> ring : polygons) {
                    allPoints.addAll(ring);
                }
            }
        }
        return allPoints;
    }

    public static void main(String[] args) {
        ArrayList<double[]> randomCoordinates = generateRandomCoordinatesWithinEurope(1);
        for (double[] coordinate : randomCoordinates) {
            System.out.println("Latitude: " + coordinate[0] + ", Longitude: " + coordinate[1]);
        }
    }
}
