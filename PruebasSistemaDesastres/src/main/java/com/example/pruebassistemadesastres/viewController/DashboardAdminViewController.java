package com.example.pruebassistemadesastres.viewController;

import com.example.pruebassistemadesastres.model.*;
import com.sothawo.mapjfx.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
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
    private VBox legendBox;
    private ScrollPane legendScroll;
    private StackPane overlay;
    @FXML private AnchorPane PaneAdmin;
    @FXML private AnchorPane PaneEstads;
    @FXML private AnchorPane PaneInicio;
    @FXML private AnchorPane PaneRutas;
    @FXML private AnchorPane paneMapa;
    private Marker mDesastre;
    private Marker mHospital;
    private CoordinateLine ruta;
    private MapCircle zonaCritica;
    private MapCircle zonaModerada;
    private MapCircle zonaEstable;
    private MapLabel lblCalarca;
    private enum ModoMapa { COMPACTO, AMPLIO }
    private ModoMapa modoActual = ModoMapa.AMPLIO;

    /**
     *
     */
    @FXML
    private void initialize() {
        mapView = new MapView();
        mapView.setMapType(MapType.OSM);
        mapView.setMaxSize(1920, 1080);
        mapView.setMinSize(2, 2);
        paneMapa.setMinSize(2, 2);

        paneMapa.getChildren().add(mapView);
        AnchorPane.setTopAnchor(mapView, 0.0);
        AnchorPane.setRightAnchor(mapView, 0.0);
        AnchorPane.setBottomAnchor(mapView, 0.0);
        AnchorPane.setLeftAnchor(mapView, 0.0);

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
        modoAmplio();
        crearOverlayLeyenda();
    }

    private void crearOverlayLeyenda() {
        // Caja con items
        legendBox = new VBox(6);
        legendBox.setFillWidth(false);
        legendBox.setBackground(new Background(
                new BackgroundFill(Color.rgb(20,20,30,0.80),
                        new CornerRadii(10),
                        Insets.EMPTY)
        ));
        legendBox.setPadding(new Insets(8,10,8,10));
        legendBox.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 14, 0.3, 0, 2);");

        // Scroll SOLO para la leyenda
        legendScroll = new ScrollPane(legendBox);
        legendScroll.setFitToWidth(true);
        legendScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        legendScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        legendScroll.setPannable(false);

        // 🔎 Quitar todos los fondos/blancos:
        legendScroll.setBackground(Background.EMPTY);
        legendScroll.setBorder(Border.EMPTY);
        legendScroll.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        // El contenido también transparente (por si algún skin añade color)
        legendBox.setStyle(legendBox.getStyle() + "; -fx-background-color: rgba(20,20,30,0.80);");

        // Colocar en esquina inferior izquierda del mapa (sin cubrirlo con overlays)
        paneMapa.getChildren().add(legendScroll);
        legendScroll.setPrefWidth(230);
        legendScroll.setMaxWidth(230);
        AnchorPane.setLeftAnchor(legendScroll, 12.0);
        AnchorPane.setBottomAnchor(legendScroll, 12.0);
        legendScroll.maxHeightProperty().bind(paneMapa.heightProperty().multiply(0.35));

        // Truco para el viewport blanco (se necesita tras el primer layout):
        Platform.runLater(() -> {
            Region viewport = (Region) legendScroll.lookup(".viewport");
            if (viewport != null) {
                viewport.setBackground(Background.EMPTY);
                viewport.setStyle("-fx-background-color: transparent;");
            }
        });
        legendScroll.addEventFilter(javafx.scene.input.MouseEvent.DRAG_DETECTED, e -> {
            // cede el gesto al mapa inmediatamente
            legendScroll.startFullDrag(); // evita que el scrollpane intente panear
            e.consume(); // si prefieres que no haga nada la leyenda al arrastrar
        });
    }
    /**
     * Ajuste dinamico del tamaño del mapa y de los botones de navegacion entre municipios
     * @param modo
     */
    private void aplicarModoMapa(ModoMapa modo) {
        if (paneMapa.prefWidthProperty().isBound())  paneMapa.prefWidthProperty().unbind();
        if (paneMapa.prefHeightProperty().isBound()) paneMapa.prefHeightProperty().unbind();

        AnchorPane.clearConstraints(paneMapa);

        paneMapa.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        paneMapa.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        switch (modo) {
            case COMPACTO -> {
                paneMapa.setPrefSize(828, 536);
                AnchorPane.setTopAnchor(paneMapa, 115.0);
                AnchorPane.setLeftAnchor(paneMapa, 18.0);
                AnchorPane.setRightAnchor(paneMapa, null);
                AnchorPane.setBottomAnchor(paneMapa, null);

                btnDer.setLayoutX(736); btnDer.setLayoutY(222);
                btnIzq.setLayoutX(24);  btnIzq.setLayoutY(222);
            }
            case AMPLIO -> {
                paneMapa.setPrefSize(1255, 536);
                AnchorPane.setTopAnchor(paneMapa, 115.0);
                AnchorPane.setLeftAnchor(paneMapa, -2.0);
                AnchorPane.setRightAnchor(paneMapa, null);
                AnchorPane.setBottomAnchor(paneMapa, null);

                btnDer.setLayoutX(1173); btnDer.setLayoutY(222);
                btnIzq.setLayoutX(24);   btnIzq.setLayoutY(222);
            }
        }

        if (!mapView.prefWidthProperty().isBound())  mapView.prefWidthProperty().bind(paneMapa.widthProperty());
        if (!mapView.prefHeightProperty().isBound()) mapView.prefHeightProperty().bind(paneMapa.heightProperty());

        paneMapa.toFront();
        btnDer.toFront();
        btnIzq.toFront();
        paneMapa.applyCss();
        paneMapa.requestLayout();

        modoActual = modo;
    }
    public void modoCompacto(){
        aplicarModoMapa(ModoMapa.COMPACTO);
        bringOverlayToFront();
    }
    public void modoAmplio(){
        aplicarModoMapa(ModoMapa.AMPLIO);
        bringOverlayToFront();
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
        modoAmplio();
        PaneInicio.setVisible(true);
        PaneRutas.setVisible(false);
        PaneEstads.setVisible(false);
        PaneAdmin.setVisible(false);
        paneMapa.setPrefWidth(1257);
        paneMapa.setPrefHeight(536);
        prepararCacheMunicipios();
        mostrarInicioDeMunicipios();
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
        modoCompacto();
        PaneInicio.setVisible(false);
        PaneRutas.setVisible(false);
        PaneEstads.setVisible(false);
        PaneAdmin.setVisible(true);
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
        modoCompacto();
        PaneInicio.setVisible(false);
        PaneRutas.setVisible(true);
        PaneEstads.setVisible(false);
        PaneAdmin.setVisible(false);
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
        List<Zona> paradas = ruta.getParadas();
        refrescarLeyenda(paradas);
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
        modoCompacto();
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
        refrescarLeyenda(zonasMunicipio);
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
        if (sistemaGestionDesastres == null) {
            System.out.println("sistemaGestionDesastres es null");
            return;
        }
        Map<String, Double> avances = sistemaGestionDesastres.getAvanceEvacuaciones();
        System.out.println("Avances obtenidos: " + avances);
        System.out.println("Tamaño del Map: " + avances.size());
        if (avances.isEmpty()) {
            System.out.println("Avances está vacío, limpiando gráfico");
            graficoAvanceRecursos.setData(FXCollections.emptyObservableList());
            return;
        }
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : avances.entrySet()) {
            String zona = entry.getKey();
            double porcentaje = entry.getValue() * 100.0;
            System.out.println("Agregando: " + zona + " - " + porcentaje + "%");
            datos.add(new PieChart.Data(zona + " (" + String.format("%.1f%%", porcentaje) + ")", porcentaje));
        }
        System.out.println("Datos para gráfico: " + datos.size() + " sectores");
        graficoAvanceRecursos.setTitle("Avance de Evacuaciones por Zona");
        graficoAvanceRecursos.setData(datos);
        graficoAvanceRecursos.setLabelsVisible(true);
        graficoAvanceRecursos.setLegendVisible(true);
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
    private Paint colorTipo(TipoZona t) {
        return switch (t) {
            case CIUDAD       -> Color.web("#2ecc71"); // verde
            case REFUGIO      -> Color.web("#e74c3c"); // rojo
            case CENTRO_AYUDA -> Color.web("#3498db"); // azul
            default           -> Color.web("#bdc3c7"); // gris fallback
        };
    }

    private String etiquetaZona(Zona z) {
        String tipo = switch (z.getTipo()) {
            case CIUDAD -> "Ciudad";
            case REFUGIO -> "Refugio";
            case CENTRO_AYUDA -> "Centro de ayuda";
            default -> z.getTipo().name();
        };
        return tipo + " · " + z.getNombre();
    }

    private void refrescarLeyenda(List<Zona> zonas) {
        if (legendBox == null) return;
        legendBox.getChildren().clear();

        // Agrupar por tipo
        Map<TipoZona, List<Zona>> porTipo = zonas.stream()
                .collect(Collectors.groupingBy(Zona::getTipo));

        // Orden de tipos fijo (Ciudad, Refugio, Centro de ayuda)
        List<TipoZona> ordenTipos = List.of(TipoZona.CIUDAD, TipoZona.REFUGIO, TipoZona.CENTRO_AYUDA);

        for (TipoZona tipo : ordenTipos) {
            List<Zona> lista = porTipo.get(tipo);
            if (lista == null || lista.isEmpty()) continue;

            // Orden alfabético por nombre de zona
            lista = lista.stream()
                    .sorted(Comparator.comparing(Zona::getNombre, String.CASE_INSENSITIVE_ORDER))
                    .toList();

            // Cabecera (colapsable)
            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);

            Circle bullet = new Circle(6, colorTipo(tipo));
            Label lbl = new Label(switch (tipo) {
                case CIUDAD       -> "Ciudad";
                case REFUGIO      -> "Refugio";
                case CENTRO_AYUDA -> "Centro de ayuda";
                default           -> tipo.name();
            } + " (" + lista.size() + ")");
            lbl.setTextFill(Color.WHITE);
            lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: 700;");

            Label caret = new Label("▸"); // ▶/▼
            caret.setTextFill(Color.WHITE);
            caret.setStyle("-fx-font-size: 12px; -fx-opacity: 0.85;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            header.getChildren().addAll(bullet, lbl, spacer, caret);

            // Contenedor colapsable con los nombres (máx 4 y ‘+N más’)
            VBox detalles = new VBox(4);
            detalles.setVisible(false);
            detalles.setManaged(false);

            int maxMostrar = 4;
            for (int i = 0; i < Math.min(maxMostrar, lista.size()); i++) {
                Zona z = lista.get(i);
                Label item = new Label("• " + z.getNombre() + " · " + z.getMunicipio().getNombre());
                item.setTextFill(Color.WHITE);
                item.setStyle("-fx-font-size: 12px; -fx-opacity: 0.95;");
                detalles.getChildren().add(item);
                item.setOnMouseClicked(ev -> {
                    Coordinate c = new Coordinate(z.getLatitud(), z.getAltitud());
                    centrarEn(c, 15);
                });
            }
            if (lista.size() > maxMostrar) {
                int restantes = lista.size() - maxMostrar;
                Label mas = new Label("… +" + restantes + " más");
                mas.setTextFill(Color.WHITE);
                mas.setStyle("-fx-font-size: 12px; -fx-opacity: 0.75;");
                detalles.getChildren().add(mas);
            }

            // Toggle colapso
            header.setOnMouseClicked(e -> {
                boolean show = !detalles.isVisible();
                detalles.setVisible(show);
                detalles.setManaged(show);
                caret.setText(show ? "▾" : "▸");
            });

            // Meter al panel
            legendBox.getChildren().addAll(header, detalles);
        }

        legendBox.setVisible(!legendBox.getChildren().isEmpty());
        legendBox.toFront();
    }

    private void bringOverlayToFront() {
        if (overlay != null) overlay.toFront();
        if (btnDer != null) btnDer.toFront();
        if (btnIzq != null) btnIzq.toFront();
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
        prepararCacheMunicipios();
        // Si el mapa ya está inicializado, muestra de una
        if (mapView != null && mapView.initializedProperty().get()) {
            mostrarInicioDeMunicipios();
        } else {
            // Si no, espera a que esté listo una única vez
            mapView.initializedProperty().addListener((obs, was, ready) -> {
                if (ready) {
                    mostrarInicioDeMunicipios();
                }
            });
        }
    }

}