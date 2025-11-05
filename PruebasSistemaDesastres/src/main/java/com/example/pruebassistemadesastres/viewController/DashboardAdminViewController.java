package com.example.pruebassistemadesastres.viewController;

import com.example.pruebassistemadesastres.model.Municipio;
import com.example.pruebassistemadesastres.model.ServicioRutas;
import com.example.pruebassistemadesastres.model.SistemaGestionDesastres;
import com.example.pruebassistemadesastres.model.Zona;
import com.sothawo.mapjfx.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.List;

public class DashboardAdminViewController {
    private SistemaGestionDesastres sistemaGestionDesastres;
    @FXML private Pane paneMapa;

    private MapView mapView;

    @FXML
    private Button ActualizarRecursosDistribuidos;

    @FXML
    private AnchorPane PaneAdmin;

    @FXML
    private AnchorPane PaneEstads;

    @FXML
    private AnchorPane PaneInicio;

    @FXML
    private AnchorPane PaneRutas;

    @FXML
    private Button actualizarAvanceEvacuaciones;

    @FXML
    private Button actualizarInventarioaAdmin;

    @FXML
    private Button actualizarRutas;

    @FXML
    private Button agregarRuta;

    @FXML
    private Button asginarEquiposAdmin;

    @FXML
    private Button btnAdmin;

    @FXML
    private Button btnEstadisticas;

    @FXML
    private Button btnInicio;

    @FXML
    private Button btnRutas;

    @FXML
    private Button cerrarSesionAdmin;

    @FXML
    private Button equiposAdmin;

    @FXML
    private PieChart graficoAvanceRecursos;

    @FXML
    private PieChart graficoRecursosDistribuidos;

    @FXML
    private Button minimizarAdmin;

    @FXML
    private Button recursosInicio;

    @FXML
    private Button salirAdmin;

    @FXML
    private Button simulacro;

    @FXML
    private TableView<?> tablaGestionInventario;

    @FXML
    private ComboBox<Municipio> comboMunicipio;
    @FXML
    private ComboBox<Zona> comboZonaInicio;
    @FXML
    private ComboBox<Zona> comboZonaFinal;
    @FXML
    private Button btnMostrarRuta;

    @FXML
    private void initialize() {
        mapView = new MapView();
        mapView.setMapType(MapType.OSM);
        mapView.setMaxSize(1920, 1080);     // o 2560x1440 si necesitas más
        paneMapa.setMaxSize(1920, 1080);
        mapView.setMinSize(2, 2);
        paneMapa.setMinSize(2, 2);

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
                // >>> Pinta la ruta real respetando carreteras <<<
                Coordinate desastre = new Coordinate(4.53242, -75.64957);
                Coordinate hospital = new Coordinate(4.533338, -75.640813);
                com.example.pruebassistemadesastres.model.ServicioRutas.dibujarRutaCarretera(
                        mapView,
                        java.util.List.of(hospital, desastre),
                        true // limpiarPrevias
                );
            }
        });
    }

    @FXML
    void btnActualizarAvanceEvacuaciones(ActionEvent event) {

    }

    @FXML
    void btnActualizarInventarioAdmin(ActionEvent event) {

    }

    @FXML
    void btnActualizarRecursosDistribuidos(ActionEvent event) {

    }

    @FXML
    void btnActualizarRutas(ActionEvent event) {

    }

    @FXML
    void btnAgregarRuta(ActionEvent event) {

    }

    @FXML
    void btnAsignarEquiposAdmin(ActionEvent event) {

    }

    @FXML
    void btnCerrarSesionAdmin(ActionEvent event) {

    }

    @FXML
    void btnEquiposInicio(ActionEvent event) {

    }

    @FXML
    void btnMinimizarAdmin(ActionEvent event) {

    }

    @FXML
    void btnRecursosInicio(ActionEvent event) {

    }

    @FXML
    void btnSalirAdmin(ActionEvent event) {

    }

    @FXML
    void btnSimulacro(ActionEvent event) {

    }

    @FXML
    void clickAdmin(ActionEvent event) {
        paneMapa.toFront();
        PaneInicio.setVisible(false);
        PaneRutas.setVisible(false);
        PaneEstads.setVisible(false);
        PaneAdmin.setVisible(true);
        paneMapa.setPrefWidth(750);
        paneMapa.setPrefHeight(536);
    }

    @FXML
    void clickEstadisticas(ActionEvent event) {
        PaneInicio.setVisible(false);
        PaneRutas.setVisible(false);
        PaneEstads.setVisible(true);
        PaneAdmin.setVisible(false);
        paneMapa.toBack();
    }

    @FXML
    void clickInicio(ActionEvent event) {
        paneMapa.toFront();
        PaneInicio.setVisible(true);
        PaneRutas.setVisible(false);
        PaneEstads.setVisible(false);
        PaneAdmin.setVisible(false);
        paneMapa.setPrefWidth(1257);
        paneMapa.setPrefHeight(536);
    }

    @FXML
    void clickRutas(ActionEvent event) {
        paneMapa.toFront();
        PaneInicio.setVisible(false);
        PaneRutas.setVisible(true);
        PaneEstads.setVisible(false);
        PaneAdmin.setVisible(false);
        paneMapa.setPrefWidth(750);
        paneMapa.setPrefHeight(536);
    }

    // Mantén referencias FUERTES a los overlays
    private Marker mDesastre;
    private Marker mHospital;
    private CoordinateLine ruta;
    private MapCircle zonaCritica;
    private MapCircle zonaModerada;
    private MapCircle zonaEstable;
    private MapLabel lblCalarca;



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
        mapView.addMapCircle(zonaCritica);
        mapView.addMapCircle(zonaModerada);
        mapView.addMapCircle(zonaEstable);
        mapView.addLabel(lblCalarca);
        ServicioRutas.dibujarRutaCarretera(
                mapView,
                List.of(mDesastre.getPosition(), mHospital.getPosition()),
                /*limpiarPrevias*/ true
        );
    }

    public SistemaGestionDesastres getSistemaGestionDesastres() {
        return sistemaGestionDesastres;
    }

    public void setSistemaGestionDesastres(SistemaGestionDesastres sistemaGestionDesastres) {
        this.sistemaGestionDesastres = sistemaGestionDesastres;
    }
}