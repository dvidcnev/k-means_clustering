
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
import io.github.makbn.jlmap.geojson.JLGeoJsonObject;
import io.github.makbn.jlmap.listener.OnJLMapViewListener;
import io.github.makbn.jlmap.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MapGUI extends Application {
    static final Logger log = LogManager.getLogger(MapGUI.class);

    public void launchMap() {
        launch(); // Calls the start(Stage) method internally
    }

    @Override
    public void start(@NonNull Stage stage) {
        // building a new map view
        final JLMapView map = JLMapView
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

                // map.setView(JLLatLng.builder()
                // .lng(50)
                // .lat(13)
                // .build());

                JLBounds bounds = JLBounds.builder()
                        .southWest(JLLatLng.builder()
                                .lat(47)
                                .lng(3)
                                .build())
                        .northEast(JLLatLng.builder()
                                .lat(55.5)
                                .lng(18)
                                .build())
                        .build();
                // map zoom functionalities
                map.getControlLayer().setMaxBounds(bounds);
                map.getControlLayer().setZoom(6);
                map.getControlLayer().zoomIn(2);
                map.getControlLayer().zoomOut(1);
                map.getControlLayer().setMinZoom(6);

            }

            @Override
            public void mapFailed() {
                log.error("map failed!");
            }

        });
    }

}