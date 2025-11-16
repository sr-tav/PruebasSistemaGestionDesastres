package com.example.pruebassistemadesastres.viewController;

import com.example.pruebassistemadesastres.model.*;
import com.sothawo.mapjfx.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.StringConverter;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DashboardAdminViewController {
    /**
     * ---------------------------------------ESPACIO DE INICIALIZACION--------------------------------------
     */
    private SistemaGestionDesastres sistemaGestionDesastres;
    private MapView mapView;
    @FXML private AnchorPane PaneAdmin;
    @FXML private AnchorPane PaneEstads;
    @FXML private AnchorPane PaneInicio;
    @FXML private AnchorPane PaneRutas;
    @FXML private Pane paneMapa;
    private Marker mDesastre;
    private Marker mHospital;
    private CoordinateLine ruta;
    private MapCircle zonaCritica;
    private MapCircle zonaModerada;
    private MapCircle zonaEstable;
    private MapLabel lblCalarca;

    /**
     *
     */
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
                inicializarCombos();
                columnaRecurso.setCellValueFactory(new PropertyValueFactory<>("recurso"));
                columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
                columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
            }
        });
    }
    /**
     *
     */
    private void inicializarCombos() {
        if (sistemaGestionDesastres == null) return;

        List<Municipio> municipios = sistemaGestionDesastres.getZonas().stream()
                .map(Zona::getMunicipio)
                .distinct()
                .toList();

        comboMunicipio.setItems(FXCollections.observableArrayList(municipios));
        comboMunicipio.setConverter(new StringConverter<>() {
            @Override public String toString(Municipio m) { return m == null ? "" : m.getNombre(); }
            @Override public Municipio fromString(String s) { return null; }
        });

        StringConverter<Zona> zonaConverter = new StringConverter<>() {
            @Override public String toString(Zona z) { return z == null ? "" : z.getNombre(); }
            @Override public Zona fromString(String s) { return null; }
        };
        comboZonaInicio.setConverter(zonaConverter);
        comboZonaFinal.setConverter(zonaConverter);

        comboMunicipio.setOnAction(evt -> {
            Municipio seleccionado = comboMunicipio.getSelectionModel().getSelectedItem();
            if (seleccionado == null) return;

            mostrarMarcadoresMunicipio(seleccionado);

            List<Zona> zonasMunicipio = sistemaGestionDesastres.getZonas().stream()
                    .filter(z -> z.getMunicipio().equals(seleccionado))
                    .toList();
            ObservableList<Zona> obsZonas = FXCollections.observableArrayList(zonasMunicipio);
            comboZonaInicio.setItems(obsZonas);
            comboZonaFinal.setItems(obsZonas);
        });
    }
    /**
     * ---------------------------------------VENTANA DE INICIO--------------------------------------
     */
    @FXML private Button btnInicio;
    @FXML private Button btnDer;
    @FXML private Button btnIzq;
    private Municipio municipioActivo;
    private final List<String> ordenMunicipios = List.of(
            "Calarca","Armenia","Quimbaya","Montenegro","Tebaida",
            "Circasia","Salento","Filandia","Cordoba","Buenavista","Pijao","Genova"
    );
    private int idxMunicipio = 0;
    private Map<String, Municipio> cacheMunicipiosPorNombre = new LinkedHashMap<>();
    private List<String> ordenMunicipiosCambia= ordenMunicipios;

    /**
     *
     * @param event
     */
    @FXML
    void clickInicio(ActionEvent event) {
        paneMapa.toFront();
        btnDer.toFront();
        btnIzq.toFront();
        PaneInicio.setVisible(true);
        PaneRutas.setVisible(false);
        PaneEstads.setVisible(false);
        PaneAdmin.setVisible(false);
        paneMapa.setPrefWidth(1257);
        paneMapa.setPrefHeight(536);
        mostrarInicioDeMunicipios();
        prepararCacheMunicipios();
    }

    /**
     *
     */
    private void prepararCacheMunicipios() {
        if (sistemaGestionDesastres == null || sistemaGestionDesastres.getMunicipios() == null) {
            cacheMunicipiosPorNombre = new LinkedHashMap<>();
            return;
        }

        cacheMunicipiosPorNombre = sistemaGestionDesastres.getMunicipios()
                .stream()
                .collect(Collectors.toMap(
                        m -> normalizarNombre(m.getNombre()),
                        Function.identity(),
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

    /**
     *
     * @param s
     * @return
     */
    private String normalizarNombre(String s) {
        if (s == null) return "";
        String t = Normalizer.normalize(s.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");         // quita tildes
        return t.toLowerCase(Locale.ROOT);
    }
    /**
     *
     */
    public void mostrarInicioDeMunicipios() {
        if (ordenMunicipios.isEmpty()) return;

        String nombre = ordenMunicipios.get(idxMunicipio);
        municipioActivo = obtenerMunicipioPorNombre(nombre);

        if (municipioActivo != null) {
            mostrarMarcadoresMunicipio(municipioActivo);

            Coordinate centro = centroDeMunicipio(municipioActivo);
            if (centro != null) {
                centrarEn(centro, 14); 
            } else {
                System.err.println("No hay centro para el municipio: " + nombre);
            }
        } else {
            System.err.println("No encontré el Municipio en el modelo: " + nombre);
        }
    }

    /**
     *
     * @param nombreBuscado
     * @return
     */
    private Municipio obtenerMunicipioPorNombre(String nombreBuscado) {
        String clave = normalizarNombre(nombreBuscado);

        if (cacheMunicipiosPorNombre.containsKey(clave)) {
            return cacheMunicipiosPorNombre.get(clave);
        }

        for (Map.Entry<String, Municipio> e : cacheMunicipiosPorNombre.entrySet()) {
            String k = e.getKey();
            if (k.contains(clave) || clave.contains(k)) {
                return e.getValue();
            }
        }
        return null;
    }
    /**
     *
     * @param event
     */
    @FXML
    void clickDer(ActionEvent event) {
        System.out.println("accion dere");
        if (ordenMunicipios.isEmpty()) return;
        idxMunicipio = (idxMunicipio + 1) % ordenMunicipios.size();
        mostrarInicioDeMunicipios();
    }

    /**
     *
     * @param event
     */
    @FXML
    void clickIzq(ActionEvent event) {
        System.out.println("accion izq");
        if (ordenMunicipios.isEmpty()) return;
        idxMunicipio = (idxMunicipio - 1 + ordenMunicipios.size()) % ordenMunicipios.size();
        mostrarInicioDeMunicipios();
    }
    /**
     * ---------------------------------------VENTANA DE ADMIN--------------------------------------
     */
    @FXML private Button btnAdmin;
    /**
     *
     * @param event
     */
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
    /**
     * ---------------------------------------VENTANA DE RUTAS--------------------------------------
     */
    @FXML private Button btnRutas;
    @FXML private ComboBox<Municipio> comboMunicipio;
    @FXML private ComboBox<Zona> comboZonaInicio;
    @FXML private ComboBox<Zona> comboZonaFinal;
    @FXML private Button btnMostrarRuta;
    /**
     *
     * @param event
     */
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
    /**
     *
     * @param event
     */
    @FXML
    private void btnMostrarRutaAction(ActionEvent event) {
        Zona inicio = comboZonaInicio.getSelectionModel().getSelectedItem();
        Zona destino = comboZonaFinal.getSelectionModel().getSelectedItem();

        if (inicio == null || destino == null) {
            System.out.println("Debes seleccionar una zona de inicio y una de destino.");
            return;
        }

        Ruta ruta = sistemaGestionDesastres.getGrafo().calcularRutaMasCorta(inicio, destino);
        if (ruta == null) {
            System.out.println("No hay ruta disponible entre estas zonas.");
            return;
        }

        List<Coordinate> coordsRuta = ruta.getParadas().stream()
                .map(z -> new Coordinate(z.getLatitud(), z.getAltitud()))
                .toList();

        ServicioRutas.dibujarRutaCarretera(mapView, coordsRuta, true);

        double latCentro = coordsRuta.stream().mapToDouble(Coordinate::getLatitude).average().orElse(0);
        double lonCentro = coordsRuta.stream().mapToDouble(Coordinate::getLongitude).average().orElse(0);
        mapView.setCenter(new Coordinate(latCentro, lonCentro));
        mapView.setZoom(14);
    }
    /**
     * ---------------------------------------VENTANA DE ESTADISTICAS--------------------------------------
     */
    @FXML private Button btnEstadisticas;
    /**
     *
     * @param event
     */
    @FXML
    void clickEstadisticas(ActionEvent event) {
        PaneInicio.setVisible(false);
        PaneRutas.setVisible(false);
        PaneEstads.setVisible(true);
        PaneAdmin.setVisible(false);
        paneMapa.toBack();
    }
    /**
     * --------------------------------------- GENERALES --------------------------------------
     */
    @FXML private Button ActualizarRecursosDistribuidos;
    @FXML private Button actualizarAvanceEvacuaciones;
    @FXML private Button actualizarInventarioaAdmin;
    @FXML private Button actualizarRutas;
    @FXML private Button agregarRuta;
    @FXML private Button asginarEquiposAdmin;
    @FXML private Button cerrarSesionAdmin;
    @FXML private Button equiposAdmin;
    @FXML private PieChart graficoAvanceRecursos;
    @FXML private PieChart graficoRecursosDistribuidos;
    @FXML private Button minimizarAdmin;
    @FXML private Button recursosInicio;
    @FXML private Button salirAdmin;
    @FXML private Button simulacro;
    @FXML private TableView<RecursoInventarioView> tablaGestionInventario;
    @FXML private TableColumn<RecursoInventarioView, String> columnaRecurso;
    @FXML private TableColumn<RecursoInventarioView, Integer> columnaCantidad;
    @FXML private TableColumn<RecursoInventarioView, String> columnaEstado;

    private final List<Marker> marcadoresMunicipio = new ArrayList<>();

    /**
     *
     */
    private void configurarCentro() {
        Coordinate calarca = new Coordinate(4.5333, -75.6500);
        mapView.setCenter(calarca);
        mapView.setZoom(14);
    }
    /**
     *
     */
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

    /**
     *
     */
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

    /**
     *
     * @param municipio
     */
    private void mostrarMarcadoresMunicipio(Municipio municipio) {
        // Limpiar marcadores previos
        for (Marker m : marcadoresMunicipio) {
            mapView.removeMarker(m);
        }
        marcadoresMunicipio.clear();

        List<Zona> zonasMunicipio = sistemaGestionDesastres.getZonas().stream()
                .filter(z -> z.getMunicipio().equals(municipio))
                .toList();

        for (Zona z : zonasMunicipio) {
            Marker marcador;
            switch (z.getTipo()) {
                case REFUGIO -> marcador = Marker.createProvided(Marker.Provided.RED);
                case CENTRO_AYUDA -> marcador = Marker.createProvided(Marker.Provided.BLUE);
                case CIUDAD -> marcador = Marker.createProvided(Marker.Provided.GREEN);
                default -> marcador = null;
            }
            marcador.setPosition(new Coordinate(z.getLatitud(), z.getAltitud()));
            marcador.setVisible(true);
            mapView.addMarker(marcador);
            marcadoresMunicipio.add(marcador);
        }
    }

    /**
     *
     * @param c
     * @param zoom
     */
    private void centrarEn(Coordinate c, int zoom) {
        if (c == null || mapView == null) return;
        if (!mapView.initializedProperty().get()) {
            mapView.initializedProperty().addListener((obs, was, ready) -> {
                if (ready) { mapView.setCenter(c); mapView.setZoom(zoom); }
            });
            return;
        }
        mapView.setCenter(c);
        mapView.setZoom(zoom);
    }

    /**
     *
     * @param m
     * @return
     */
    private Coordinate centroDeMunicipio(Municipio m) {
        if (m == null) return null;
        List<Zona> zonasMunicipio = sistemaGestionDesastres.getZonas().stream()
                .filter(z -> z.getMunicipio() == m)
                .toList();

        List<Zona> ciudades = zonasMunicipio.stream()
                .filter(z -> z.getTipo() == TipoZona.CIUDAD)
                .toList();

        List<Zona> base = ciudades.isEmpty() ? zonasMunicipio : ciudades;

        double lat = base.stream().mapToDouble(Zona::getLatitud).average().orElse(Double.NaN);
        double lon = base.stream().mapToDouble(Zona::getAltitud).average().orElse(Double.NaN);
        return new Coordinate(lat, lon);
    }
    @FXML
    void btnActualizarAvanceEvacuaciones(ActionEvent event) {

    }

    @FXML
    void btnActualizarInventarioAdmin(ActionEvent event) {
        tablaGestionInventario.getItems().clear();

        ArbolDistribuccion arbol = sistemaGestionDesastres.getArbolDistribuccion();
        if (arbol == null) {
            System.out.println("Árbol no inicializado.");
            return;
        }

        List<Recurso> recursos = arbol.obtenerTodosLosRecursosDetalle();

        for (Recurso r : recursos) {
            tablaGestionInventario.getItems().add(
                    new RecursoInventarioView(
                            r.getTipo().name(),
                            r.getCantidad(),
                            r.getEstado().name()
                    )
            );
        }
    }

    @FXML
    void btnActualizarRecursosDistribuidos(ActionEvent event) {
        if (sistemaGestionDesastres == null) return;

        Map<TipoRecurso, Integer> totalesRecursos = sistemaGestionDesastres.getRecursosDistribuidos();


        if (totalesRecursos.isEmpty()) {
            graficoRecursosDistribuidos.setData(FXCollections.emptyObservableList());
            return;
        }

        int totalGeneral = totalesRecursos.values().stream().mapToInt(Integer::intValue).sum();


        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        for (Map.Entry<TipoRecurso, Integer> entry : totalesRecursos.entrySet()) {
            TipoRecurso tipo = entry.getKey();
            int cantidad = entry.getValue();
            double porcentaje = (cantidad * 100.0) / totalGeneral;

            datos.add(new PieChart.Data(tipo.toString() + " (" + String.format("%.1f%%", porcentaje) + ")", cantidad));
        }

        graficoRecursosDistribuidos.setTitle("Recursos Distribuidos por Tipo");
        graficoRecursosDistribuidos.setData(datos);
        graficoRecursosDistribuidos.setLabelsVisible(true);
        graficoRecursosDistribuidos.setLegendVisible(true);
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
    /**
     * --------------------------------- GETTERS Y SETTERS -----------------------------------------------
     */
    public SistemaGestionDesastres getSistemaGestionDesastres() {
        return sistemaGestionDesastres;
    }

    public void setSistemaGestionDesastres(SistemaGestionDesastres sistemaGestionDesastres) {
        this.sistemaGestionDesastres = sistemaGestionDesastres;
    }

}