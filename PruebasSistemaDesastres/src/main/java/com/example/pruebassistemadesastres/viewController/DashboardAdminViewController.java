package com.example.pruebassistemadesastres.viewController;

import com.sothawo.mapjfx.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

public class DashboardAdminViewController {

    @FXML private Pane paneMapa;

    private MapView mapView;

    // Mantén referencias FUERTES a los overlays
    private Marker mDesastre;
    private Marker mHospital;
    private CoordinateLine ruta;
    private MapCircle zonaCritica;
    private MapCircle zonaModerada;
    private MapCircle zonaEstable;
    private MapLabel lblCalarca;

    @FXML
    private void initialize() {
        mapView = new MapView();
        mapView.setMapType(MapType.OSM);

        paneMapa.getChildren().add(mapView);
        mapView.setPrefSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        mapView.prefWidthProperty().bind(paneMapa.widthProperty());
        mapView.prefHeightProperty().bind(paneMapa.heightProperty());

        // Inicializa el MapView
        mapView.initialize();

        // Cuando el mapa esté listo, configura y añade overlays
        mapView.initializedProperty().addListener((obs, was, ready) -> {
            if (ready) {
                configurarCentro();
                crearOverlays();

                // Añadirlos justo después del primer render
                Platform.runLater(this::agregarOverlaysAlMapa);
            }
        });
    }

    private void configurarCentro() {
        Coordinate calarca = new Coordinate(4.5333, -75.6500);
        mapView.setCenter(calarca);
        mapView.setZoom(14);
    }

    private void crearOverlays() {
        Coordinate calarca  = new Coordinate(4.5333, -75.6500);
        Coordinate desastre = new Coordinate(4.53242, -75.64957);
        Coordinate hospital = new Coordinate(4.533338, -75.640813);

        mDesastre = Marker.createProvided(Marker.Provided.BLUE)
                .setPosition(desastre)
                .setVisible(true);

        mHospital = Marker.createProvided(Marker.Provided.RED)
                .setPosition(hospital)
                .setVisible(true);

        ruta = new CoordinateLine(desastre, hospital)
                .setColor(Color.ORANGE)
                .setWidth(4)
                .setVisible(true);

        zonaCritica = new MapCircle(desastre, 250)
                .setFillColor(Color.color(1, 0, 0.0, 0.5))
                .setVisible(true);
        zonaModerada = new MapCircle(desastre, 500)
                .setFillColor(Color.color(1.0, 0.5, 0.0, 0.35))
                .setVisible(true);
        zonaEstable = new MapCircle(desastre, 1000)
                .setFillColor(Color.color(0.5, 1, 0.0, 0.15))
                .setVisible(true);

        lblCalarca = new MapLabel("DesastreNaturalRandom")
                .setPosition(desastre)
                .setVisible(true);
    }

    private void agregarOverlaysAlMapa() {
        // Re-agrega por si el WebView hizo un refresh de capas
        mapView.addMarker(mDesastre);
        mapView.addMarker(mHospital);
        mapView.addCoordinateLine(ruta);
        mapView.addMapCircle(zonaCritica);
        mapView.addMapCircle(zonaModerada);
        mapView.addMapCircle(zonaEstable);
        mapView.addLabel(lblCalarca);
    }
}