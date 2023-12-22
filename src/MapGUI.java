
package src;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.NonNull;

// import io.github.makbn.jlmap.JLMapCallbackHandler;
import io.github.makbn.jlmap.JLMapView;
import io.github.makbn.jlmap.JLProperties;
import io.github.makbn.jlmap.listener.OnJLMapViewListener;
import io.github.makbn.jlmap.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapGUI extends Application {
        static final Logger log = LogManager.getLogger(MapGUI.class);

        public JLMapView map;

        public void launchMap() {
                launch(); // Calls the start(Stage) method internally
        }

        @Override
        public void start(@NonNull Stage stage) {
                // building a new map view
                map = JLMapView
                                .builder()
                                .mapType(JLProperties.MapType.OSM_HOT)
                                .showZoomController(true)
                                .startCoordinate(JLLatLng.builder()
                                                .lat(44)
                                                .lng(2)
                                                .build())
                                .build();
                // creating a window
                AnchorPane root = new AnchorPane(map);
                root.setBackground(Background.EMPTY);
                Scene scene = new Scene(root);

                stage.setMinHeight(600);
                stage.setMinWidth(800);
                stage.setHeight(600);
                stage.setWidth(800);
                scene.setFill(Color.TRANSPARENT);
                stage.setTitle("K-Means Clustering");
                stage.setScene(scene);
                stage.show();

                // Setting where the window pops up
                Rectangle2D primScreenBounds = Screen.getPrimary().getVisualBounds();
                stage.setX((primScreenBounds.getWidth() - stage.getWidth()) / 2);
                stage.setY((primScreenBounds.getHeight() - stage.getHeight()) / 2);

                // set listener fo map events
                map.setMapListener(new OnJLMapViewListener() {
                        @Override
                        public void mapLoadedSuccessfully(@NonNull JLMapView mapView) {
                                log.info("map loaded!");

                                drawClusters(map);
                                drawSites(map);

                                log.info("map loaded with sites!");

                                JLBounds bounds = JLBounds.builder()
                                                .southWest(JLLatLng.builder()
                                                                .lat(Dataset.southwestBound[0])
                                                                .lng(Dataset.southwestBound[1])
                                                                .build())
                                                .northEast(JLLatLng.builder()
                                                                .lat(Dataset.northeastBound[0])
                                                                .lng(Dataset.northeastBound[1])
                                                                .build())
                                                .build();
                                // map zoom functionalities
                                map.getControlLayer().setMaxBounds(bounds);
                                map.getControlLayer().setZoom(Dataset.Zoom);
                                map.getControlLayer().zoomIn(2);
                                map.getControlLayer().zoomOut(1);
                                map.getControlLayer().setMinZoom(Dataset.minZoom);

                        }

                        @Override
                        public void mapFailed() {
                                log.error("map failed!");
                        }

                });
        }

        private void drawSites(JLMapView map) {
                System.out.println("Drawing sites...");
                for (Site obj : Dataset.getSites()) {
                        map.getVectorLayer()
                                        .addCircle(JLLatLng.builder()
                                                        .lat(Double.valueOf(obj.getLa()))
                                                        .lng(Double.valueOf(obj.getLo()))
                                                        .build(), 300,

                                                        JLOptions.builder()
                                                                        .color(
                                                                                        // get the rgb of the cluster
                                                                                        // that the site belongs to
                                                                                        Color.rgb(
                                                                                                        obj.getCluster().getRGB()
                                                                                                                        .getR(),
                                                                                                        obj.getCluster().getRGB()
                                                                                                                        .getG(),
                                                                                                        obj.getCluster().getRGB()
                                                                                                                        .getB()))
                                                                        .build());
                }
        }

        private void drawClusters(JLMapView map) {
                for (Cluster cluster : Dataset.getClusters()) {
                        map.getVectorLayer()
                                        .addCircle(JLLatLng.builder()
                                                        .lat(Double.valueOf(cluster.getLa()))
                                                        .lng(Double.valueOf(cluster.getLo()))
                                                        .build(), 5000,

                                                        JLOptions.builder()
                                                                        .color(
                                                                                        Color.rgb(
                                                                                                        cluster.getRGB().getR(),
                                                                                                        cluster.getRGB().getG(),
                                                                                                        cluster.getRGB().getB()))
                                                                        .build());
                }
        }

}