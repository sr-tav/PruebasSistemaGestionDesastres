package com.example.pruebassistemadesastres.viewController;

import com.example.pruebassistemadesastres.model.*;
import com.sothawo.mapjfx.*;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
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
    private Zona desastreActivo;
    private Marker markerDesastre;
    private MapCircle cCritico, cModerado, cEstable;
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
                inicializarCombos();
                columnaRecurso.setCellValueFactory(new PropertyValueFactory<>("recurso"));
                columnaCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
                columnaEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
                hookOverlayReapplier();
            }
        });
        modoAmplio();
        crearOverlayLeyenda();
        crearHudMulti();
        Platform.runLater(this::sizeChartsOnce);
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
    private Marker markerVehiculo;        // el carrito
    private javafx.animation.Timeline animRuta;  // timeline de animación
    private VBox hudBox;                  // panel flotante
    private Label lblHudTitulo, lblHudETA, lblHudDist, lblHudVel;
    private ProgressBar hudProgress;

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
    void clickSimulacro(ActionEvent event) {
        if (sistemaGestionDesastres == null) return;

        // 1) Limpiar overlays previos (si había un desastre activo)
        limpiarOverlaysDesastre();

        // 2) Crear uno nuevo en el modelo
        desastreActivo = sistemaGestionDesastres.iniciarSimulacro();
        if (desastreActivo == null) return;
        // 3) Pintar marker y círculos
        Coordinate c = new Coordinate(desastreActivo.getLatitud(), desastreActivo.getAltitud());
        try {
            markerDesastre = Marker.createProvided(Marker.Provided.ORANGE);
        } catch (Throwable t) {
            markerDesastre = Marker.createProvided(Marker.Provided.RED);
        }
        markerDesastre.setPosition(c).setVisible(true);
        mapView.addMarker(markerDesastre);

        cCritico  = new MapCircle(c, 300).setFillColor(Color.color(1.0, 0.3, 0.0, 0.25)).setVisible(true);
        cModerado = new MapCircle(c, 600).setFillColor(Color.color(1.0, 0.6, 0.0, 0.18)).setVisible(true);
        cEstable  = new MapCircle(c, 900).setFillColor(Color.color(1.0, 0.8, 0.0, 0.12)).setVisible(true);
        mapView.addMapCircle(cCritico);
        mapView.addMapCircle(cModerado);
        mapView.addMapCircle(cEstable);
        centrarEn(c, 15);
        mostrarMarcadoresMunicipio(desastreActivo.getMunicipio());
        refrescarComboSalidaPorDesastre();
        conectarDesastreConTodasLasZonasMunicipio();
        detenerTodosLosEnvios();
        actualizarHudDesastre();
        iniciarCronometroDesastre();
        actualizarPanelEstadisticas();
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
    @FXML private Button btnAgregarRecurso;
    @FXML private Button btnEnviarEquipo;
    @FXML private ComboBox<Equipo> comboSeleccionarEquipo;
    @FXML private ComboBox<Zona> comboSalidaRecursos;
    private final ObservableList<RecursoInventarioView> recursosSeleccionados = FXCollections.observableArrayList();
    private Recurso vehiculoSeleccionado = null;
    // === MULTI-ENVÍOS ===
    private static class EnvioAnim {
        String id;
        Marker marker;
        Timeline timeline;
        ProgressBar progress;
        Label lblTitulo, lblETA, lblDist, lblVel;
        VBox hudRow;
        double horasTotales;
        long t0;
        List<Coordinate> coords;
        boolean enRegreso; // <—
    }
    private final Map<String, EnvioAnim> enviosActivos = new LinkedHashMap<>();
    private VBox hudLista;            // contenedor de varios HUDs
    private ScrollPane hudScroll;     // scroll del HUD
    private final List<CoordinateLine> rutasDibujadas = new ArrayList<>();
    private final Map<String, Marker> vehMarkers = new HashMap<>();
    private final Map<String, javafx.animation.Timeline> vehAnims = new HashMap<>();
    private final Map<String, List<Coordinate>> vehCoords = new HashMap<>();
    private VBox hudDesastre;
    private Label lblDesastreTitulo, lblPendientes, lblEvacuadas, lblTotales;
    private final Map<String, CoordinateLine> lineasPorEnvio = new HashMap<>();

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
        List<Coordinate> waypoints = coordsDeRuta(ruta);

        ServicioRutas.fetchPolylineDrivingAsync(waypoints).thenAccept(path -> {
            if (path == null || path.size() < 2) return;

            // DIBUJA la ruta ruteada
            Platform.runLater(() -> {
                CoordinateLine line = new CoordinateLine(path)
                        .setVisible(true)
                        .setColor(Color.web("#3399ff"))
                        .setWidth(4);
                mapView.addCoordinateLine(line);

                double latCentro = path.stream().mapToDouble(Coordinate::getLatitude).average().orElse(0);
                double lonCentro = path.stream().mapToDouble(Coordinate::getLongitude).average().orElse(0);
                mapView.setCenter(new Coordinate(latCentro, lonCentro));
                mapView.setZoom(14);

                // Leyenda
                List<Zona> paradas = ruta.getParadas();
                refrescarLeyenda(paradas);
            });
        });
    }
    @FXML
    void clickAgregarRecurso(ActionEvent event) {

    }
    @FXML
    void clickEnviarEquipo(ActionEvent event) {
        // Validaciones
        if (sistemaGestionDesastres == null) { mostrarInfo("Sistema no listo."); return; }

        Equipo equipo = comboSeleccionarEquipo.getSelectionModel().getSelectedItem();
        if (equipo == null) { mostrarInfo("Selecciona un equipo."); return; }

        Zona origen = comboSalidaRecursos.getSelectionModel().getSelectedItem();
        if (origen == null) { mostrarInfo("Selecciona la zona de salida."); return; }

        Zona destino = sistemaGestionDesastres.getDesastreActivo();
        if (destino == null) { mostrarInfo("Primero inicia un simulacro para tener un desastre activo."); return; }

        if (vehiculoSeleccionado == null) { mostrarInfo("El equipo debe llevar 1 Vehículo."); return; }
        if (recursosSeleccionados.isEmpty()) { mostrarInfo("Agrega al menos un recurso."); return; }

        // Preparar carga
        List<Recurso> carga = new ArrayList<>();
        for (RecursoInventarioView v : recursosSeleccionados) {
            TipoRecurso t = TipoRecurso.valueOf(v.getRecurso());
            Recurso r = sistemaGestionDesastres.getRecursos().stream()
                    .filter(rr -> rr.getTipo() == t && rr.getEstado() == EstadoRecurso.DISPONIBLE && rr.getCantidad() > 0)
                    .findFirst().orElse(null);
            if (r != null) {
                restarStock(r, 1);
                if (r.getCantidad() == 0) r.setEstado(EstadoRecurso.ASIGNADO);
                carga.add(new Recurso("ENV-" + r.getIdRecurso(), r.getTipo(), 1, EstadoRecurso.EN_RUTA));
            }
        }

        // Reservar vehículo
        restarStock(vehiculoSeleccionado, 1);
        Recurso vehiculoParaViajar =
                new Recurso("ENV-" + vehiculoSeleccionado.getIdRecurso(), TipoRecurso.VEHICULO, 1, EstadoRecurso.EN_RUTA);

        vehiculoParaViajar.setCapacidadPasajeros(vehiculoSeleccionado.getCapacidadPasajeros());
        vehiculoParaViajar.setVelocidadKmH(vehiculoSeleccionado.getVelocidadKmH());
        // Crear equipo
        Equipo equipoListo = sistemaGestionDesastres.crearEquipo(origen, destino, vehiculoParaViajar, List.of(), carga);
        sistemaGestionDesastres.planearYLanzarSimulacion(equipoListo);

        // Ruta lógica (nodos → waypoints OSRM)
        Ruta ruta = sistemaGestionDesastres.getGrafo().calcularRutaMasCorta(origen, sistemaGestionDesastres.getDesastreActivo());
        if (ruta == null) { mostrarInfo("No hay ruta disponible entre el origen y el desastre."); return; }

        List<Coordinate> coords = coordsDeRuta(ruta);

        // === ID ÚNICO PARA TODO EL ENVÍO ===
        final String envioId = "ENV-" + System.currentTimeMillis();
        final Recurso vehiculoRef = vehiculoParaViajar;  // para la lambda
        final Zona destinoRef = destino;

        ServicioRutas.fetchPolylineDrivingAsync(coords).thenAccept(path -> {
            if (path == null || path.size() < 2) return;

            Platform.runLater(() -> {
                // Dibuja y guarda la línea asociada a este envío
                CoordinateLine line = new CoordinateLine(path)
                        .setVisible(true)
                        .setColor(Color.web("#3399ff"))
                        .setWidth(4);
                mapView.addCoordinateLine(line);
                lineasPorEnvio.put(envioId, line);

                // Centra
                double latC = path.stream().mapToDouble(Coordinate::getLatitude).average().orElse(0);
                double lonC = path.stream().mapToDouble(Coordinate::getLongitude).average().orElse(0);
                mapView.setCenter(new Coordinate(latC, lonC));
                mapView.setZoom(14);

                // Animación sobre el MISMO path
                double vel = vehiculoRef.getVelocidadKmH();
                Runnable onLlegada = () -> llegadaDeEquipo(destinoRef, vehiculoRef);
                iniciarAnimacionRutaSingleHUD(envioId, path, vel, /*idaYVuelta*/ true, onLlegada);
            });
        });

        // Limpieza UI
        recursosSeleccionados.clear();
        vehiculoSeleccionado = null;
        comboSeleccionarEquipo.getSelectionModel().clearSelection();
        comboSalidaRecursos.getSelectionModel().clearSelection();

        btnActualizarInventarioAdmin(null);
        mostrarInfo("Equipo enviado. Observa el recorrido en el mapa.");
    }
    @FXML
    void btnAsignarEquiposAdmin(ActionEvent event) {
        // equipo seleccionado
        Equipo equipo = comboSeleccionarEquipo.getSelectionModel().getSelectedItem();
        if (equipo == null) {
            mostrarInfo("Selecciona un equipo en el combo antes de agregar recursos.");
            return;
        }

        // fila seleccionada en inventario
        RecursoInventarioView fila = tablaGestionInventario.getSelectionModel().getSelectedItem();
        if (fila == null) {
            mostrarInfo("Selecciona un recurso en la tabla de inventario.");
            return;
        }

        // tipo del recurso
        TipoRecurso tipo;
        try {
            tipo = TipoRecurso.valueOf(fila.getRecurso());
        } catch (Exception e) {
            mostrarInfo("Tipo de recurso no reconocido: " + fila.getRecurso());
            return;
        }

        // Buscar un recurso REAL disponible del sistema que coincida en tipo y estado DISPONIBLE
        Recurso recursoReal = sistemaGestionDesastres.getRecursos().stream()
                .filter(r -> r.getTipo() == tipo && r.getEstado() == EstadoRecurso.DISPONIBLE && r.getCantidad() > 0)
                .findFirst().orElse(null);

        if (recursoReal == null) {
            mostrarInfo("No hay stock DISPONIBLE de " + tipo + " para asignar.");
            return;
        }

        // Reglas:
        // - Debe existir exactamente 1 VEHICULO para poder enviar
        if (tipo == TipoRecurso.VEHICULO) {
            if (vehiculoSeleccionado != null) {
                mostrarInfo("Ya hay un vehículo seleccionado para este equipo.");
                return;
            }
            // reserva lógica de 1 unidad de vehículo para este envío
            if (recursoReal.getCantidad() <= 0) {
                mostrarInfo("No hay unidades de vehículo disponibles.");
                return;
            }
            vehiculoSeleccionado = recursoReal;
            // reflejo visual simple: agrego una “línea” VEHICULO x1 a la lista local
            recursosSeleccionados.add(new RecursoInventarioView("VEHICULO", 1, "RESERVADO"));
            mostrarInfo("Vehículo agregado.");
            return;
        }

        // Otros recursos (AGUA/ALIMENTO/MEDICINA/EQUIPO): agregamos una cantidad estándar o toda la fila
        // Para no abrir diálogos, tomamos una unidad lógica (puedes cambiar a pedir cantidad).
        recursosSeleccionados.add(new RecursoInventarioView(tipo.name(), 1, "RESERVADO"));
        mostrarInfo(tipo.name() + " agregado (x1).");
    }
    /**
     * ---------------------------------------VENTANA DE ESTADISTICAS--------------------------------------
     */
    @FXML private Button btnEstadisticas;
    @FXML private PieChart graficoAvanceRecursos;
    @FXML private PieChart graficoRecursosDistribuidos;
    @FXML private Label labelHabitantesEvacuados;
    @FXML private Label labelHabitantesPeligro;
    @FXML private Label labelHabitantesPendientes;
    @FXML private Label labelRecursosDisponibles;
    @FXML private Label labelTiempoDesastre;
    private Timeline cronometro;
    private long tInicioDesastreMs = 0L;
    private boolean chartsSizedOnce = false;
    /**
     *
     * @param event
     */
    @FXML
    void clickEstadisticas(ActionEvent event) {
        modoCompacto();
        Platform.runLater(this::sizeChartsOnce);
        PaneInicio.setVisible(false);
        PaneRutas.setVisible(false);
        PaneEstads.setVisible(true);
        PaneAdmin.setVisible(false);
        paneMapa.toBack();
    }

    private void sizeChartsOnce() {
        if (chartsSizedOnce) return;
        chartsSizedOnce = true;

        // Quita cualquier bind previo para evitar el bucle de layout
        graficoAvanceRecursos.prefWidthProperty().unbind();
        graficoAvanceRecursos.prefHeightProperty().unbind();
        graficoRecursosDistribuidos.prefWidthProperty().unbind();
        graficoRecursosDistribuidos.prefHeightProperty().unbind();

        // Fijar contenedores y anclar
        fijarContenedorChart(graficoAvanceRecursos, 360);       // ajusta 360 al alto que quieras
        fijarContenedorChart(graficoRecursosDistribuidos, 360);

        // Opcional: si esos "cards" están dentro de un VBox
        VBox.setVgrow((Region) graficoAvanceRecursos.getParent(), Priority.ALWAYS);
        VBox.setVgrow((Region) graficoRecursosDistribuidos.getParent(), Priority.ALWAYS);

        // Opcional: que el pie quede centrado
        graficoAvanceRecursos.setClockwise(true);
        graficoAvanceRecursos.setStartAngle(90);
        graficoRecursosDistribuidos.setClockwise(true);
        graficoRecursosDistribuidos.setStartAngle(90);
    }

    private void actualizarPanelEstadisticas() {
        Zona dz = (sistemaGestionDesastres != null) ? sistemaGestionDesastres.getDesastreActivo() : null;

        int tot = (dz != null) ? dz.getHabitantes() : 0;
        int eva = (dz != null) ? dz.getPersonasEvacuadas() : 0;
        int pen = Math.max(0, tot - eva);

        // Labels
        labelHabitantesPeligro.setText(String.valueOf(tot));
        labelHabitantesEvacuados.setText(String.valueOf(eva));
        labelHabitantesPendientes.setText(String.valueOf(pen));

        // Recursos disponibles
        labelRecursosDisponibles.setText(String.valueOf(contarRecursosDisponibles()));

        // Gráfico 1: Avance de evacuación (Evacuadas vs Pendientes)
        ObservableList<PieChart.Data> avance = FXCollections.observableArrayList(
                new PieChart.Data("Evacuadas", eva),
                new PieChart.Data("Pendientes", pen)
        );
        graficoAvanceRecursos.setTitle("Avance de evacuación");
        graficoAvanceRecursos.setData(avance);
        graficoAvanceRecursos.setLabelsVisible(true);
        graficoAvanceRecursos.setLegendVisible(true);

        // Gráfico 2: Uso de recursos (por tipo: “Usados” vs “Disponibles”)
        actualizarGraficoUsoRecursos();
    }
    private int contarRecursosDisponibles() {
        try {
            ArbolDistribuccion arbol = (sistemaGestionDesastres != null) ? sistemaGestionDesastres.getArbolDistribuccion() : null;
            if (arbol != null) {
                List<Recurso> todos = arbol.obtenerTodosLosRecursosDetalle();
                return (int) todos.stream()
                        .filter(r -> r.getEstado() == EstadoRecurso.DISPONIBLE)
                        .mapToInt(Recurso::getCantidad)
                        .filter(q -> q > 0)
                        .sum();
            }
        } catch (Exception ignored) {}

        // Fallback
        if (sistemaGestionDesastres == null || sistemaGestionDesastres.getRecursos() == null) return 0;
        return sistemaGestionDesastres.getRecursos().stream()
                .filter(r -> r.getEstado() == EstadoRecurso.DISPONIBLE)
                .mapToInt(Recurso::getCantidad)
                .filter(q -> q > 0)
                .sum();
    }
    private void actualizarGraficoUsoRecursos() {
        List<Recurso> base;
        ArbolDistribuccion arbol = (sistemaGestionDesastres != null) ? sistemaGestionDesastres.getArbolDistribuccion() : null;
        if (arbol != null) base = arbol.obtenerTodosLosRecursosDetalle();
        else base = (sistemaGestionDesastres != null && sistemaGestionDesastres.getRecursos()!=null)
                ? sistemaGestionDesastres.getRecursos() : List.of();

        Map<TipoRecurso, int[]> mapa = new EnumMap<>(TipoRecurso.class);
        for (Recurso r : base) {
            if (r.getCantidad() <= 0) continue;
            mapa.putIfAbsent(r.getTipo(), new int[]{0,0}); // [0]=disp, [1]=usados
            if (r.getEstado() == EstadoRecurso.DISPONIBLE) mapa.get(r.getTipo())[0] += r.getCantidad();
            else                                           mapa.get(r.getTipo())[1] += r.getCantidad();
        }

        // Construimos “torta” con la suma total de “usados” por tipo (para no saturar)
        ObservableList<PieChart.Data> datos = FXCollections.observableArrayList();
        for (Map.Entry<TipoRecurso, int[]> e : mapa.entrySet()) {
            int usados = e.getValue()[1];
            if (usados > 0)
                datos.add(new PieChart.Data(e.getKey().name() + " usados", usados));
        }
        if (datos.isEmpty()) datos.add(new PieChart.Data("Sin uso", 1));

        graficoRecursosDistribuidos.setTitle("Uso de recursos");
        graficoRecursosDistribuidos.setData(datos);
        graficoRecursosDistribuidos.setLabelsVisible(true);
        graficoRecursosDistribuidos.setLegendVisible(true);
    }
    private void iniciarCronometroDesastre() {
        detenerCronometroDesastre();
        tInicioDesastreMs = System.currentTimeMillis();
        cronometro = new Timeline(new KeyFrame(javafx.util.Duration.seconds(1), e -> {
            long trans = System.currentTimeMillis() - tInicioDesastreMs;
            labelTiempoDesastre.setText(formatoDuracion(trans));
        }));
        cronometro.setCycleCount(Animation.INDEFINITE);
        cronometro.play();
        labelTiempoDesastre.setText("00:00:00");
    }
    private void detenerCronometroDesastre() {
        if (cronometro != null) { cronometro.stop(); cronometro = null; }
        tInicioDesastreMs = 0L;
        labelTiempoDesastre.setText("0");
    }
    private String formatoDuracion(long ms) {
        long s = ms / 1000;
        long hh = s / 3600;
        long mm = (s % 3600) / 60;
        long ss = s % 60;
        return String.format("%02d:%02d:%02d", hh, mm, ss);
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
    @FXML private Button minimizarAdmin;
    @FXML private Button recursosInicio;
    @FXML private Button salirAdmin;
    @FXML private Button btnSimulacro;
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
            Marker marcador = switch (z.getTipo()) {
                case REFUGIO -> marcador = Marker.createProvided(Marker.Provided.RED);
                case CENTRO_AYUDA -> marcador = Marker.createProvided(Marker.Provided.BLUE);
                case CIUDAD -> marcador = Marker.createProvided(Marker.Provided.GREEN);
                default-> null;
            };
            if (marcador == null) continue;
            marcador.setPosition(new Coordinate(z.getLatitud(), z.getAltitud()));
            marcador.setVisible(true);
            mapView.addMarker(marcador);
            marcadoresMunicipio.add(marcador);
        }
        if (desastreActivo != null && desastreActivo.getMunicipio().equals(municipio)) {
            if (markerDesastre != null) mapView.removeMarker(markerDesastre);
            try {
                markerDesastre = Marker.createProvided(Marker.Provided.ORANGE);
            } catch (Throwable t) {
                markerDesastre = Marker.createProvided(Marker.Provided.RED);
            }
            markerDesastre.setPosition(new Coordinate(desastreActivo.getLatitud(), desastreActivo.getAltitud()));
            markerDesastre.setVisible(true);
            mapView.addMarker(markerDesastre);
        }
        List<Zona> paraLeyenda = zonasMunicipio;
        refrescarLeyenda(paraLeyenda);
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
            int cant = Math.max(0, r.getCantidad()); // clamp visual
            if (cant <= 0) continue;                 // ¡oculta en 0!
            tablaGestionInventario.getItems().add(
                    new RecursoInventarioView(
                            r.getTipo().name(),
                            cant,
                            r.getEstado().name()
                    )
            );
        }
    }

    /**
     *
     * @param event
     */
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

    /**
     *
     * @param t
     * @return
     */
    private Paint colorTipo(TipoZona t) {
        return switch (t) {
            case CIUDAD       -> Color.web("#2ecc71"); // verde
            case REFUGIO      -> Color.web("#e74c3c"); // rojo
            case CENTRO_AYUDA -> Color.web("#3498db"); // azul
            case DESASTRE     -> Color.web("#f39c12"); // naranja
            default           -> Color.web("#bdc3c7"); // gris fallback
        };
    }

    /**
     *
     * @param z
     * @return
     */
    private String etiquetaZona(Zona z) {
        String tipo = switch (z.getTipo()) {
            case CIUDAD -> "Ciudad";
            case REFUGIO -> "Refugio";
            case CENTRO_AYUDA -> "Centro de ayuda";
            default -> z.getTipo().name();
        };
        return tipo + " · " + z.getNombre();
    }

    /**
     *
     * @param zonas
     */
    private void refrescarLeyenda(List<Zona> zonas) {
        if (legendBox == null) return;
        legendBox.getChildren().clear();

        // Agrupar por tipo
        Map<TipoZona, List<Zona>> porTipo = zonas.stream()
                .collect(Collectors.groupingBy(Zona::getTipo));

        // Orden de tipos fijo (Ciudad, Refugio, Centro de ayuda)
        List<TipoZona> ordenTipos = List.of(TipoZona.DESASTRE, TipoZona.CIUDAD, TipoZona.REFUGIO, TipoZona.CENTRO_AYUDA);

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
                case DESASTRE -> "Desastre";
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
        actualizarPanelEstadisticas();
    }

    /**
     *
     */
    private void bringOverlayToFront() {
        if (overlay != null) overlay.toFront();
        if (btnDer != null) btnDer.toFront();
        if (btnIzq != null) btnIzq.toFront();
    }

    /**
     *
     */
    private void hookOverlayReapplier() {
        // Reaplica (remove+add) para sobrevivir a refrescos internos del WebView
        mapView.zoomProperty().addListener((o, a, b) -> reaplicarOverlaysDesastre());
        mapView.mapTypeProperty().addListener((o, a, b) -> reaplicarOverlaysDesastre());
    }

    /**
     *
     */
    private void reaplicarOverlaysDesastre() {
        if (desastreActivo == null) return;
        if (markerDesastre != null) {
            mapView.removeMarker(markerDesastre);
            mapView.addMarker(markerDesastre);
        }
        if (cCritico != null) { mapView.removeMapCircle(cCritico); mapView.addMapCircle(cCritico); }
        if (cModerado != null) { mapView.removeMapCircle(cModerado); mapView.addMapCircle(cModerado); }
        if (cEstable != null) { mapView.removeMapCircle(cEstable); mapView.addMapCircle(cEstable); }
        for (CoordinateLine cl : lineasPorEnvio.values()) {
            mapView.removeCoordinateLine(cl);
            mapView.addCoordinateLine(cl);
        }
    }

    /**
     *
     */
    private void limpiarOverlaysDesastre() {
        if (markerDesastre != null) { mapView.removeMarker(markerDesastre); markerDesastre = null; }
        if (cCritico != null)  { mapView.removeMapCircle(cCritico);  cCritico = null; }
        if (cModerado != null) { mapView.removeMapCircle(cModerado); cModerado = null; }
        if (cEstable != null)  { mapView.removeMapCircle(cEstable);  cEstable = null; }
    }

    /**
     *
     * @param msg
     */
    private void mostrarInfo(String msg) {
        System.out.println(msg);
        Tooltip t = new Tooltip(msg);
        t.setAutoHide(true);
        t.show(paneMapa.getScene().getWindow());
        // se oculta solo; si prefieres, usa Alert.
    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
    private static double distKm(Zona a, Zona b) {
        double R = 6371.0;
        double lat1 = Math.toRadians(a.getLatitud()), lon1 = Math.toRadians(a.getAltitud());
        double lat2 = Math.toRadians(b.getLatitud()), lon2 = Math.toRadians(b.getAltitud());
        double dlat = lat2 - lat1, dlon = lon2 - lon1;
        double h = Math.sin(dlat/2)*Math.sin(dlat/2) +
                Math.cos(lat1)*Math.cos(lat2)*Math.sin(dlon/2)*Math.sin(dlon/2);
        return 2 * R * Math.asin(Math.sqrt(h));
    }

    /**
     *
     * @param a
     * @param b
     * @return
     */
    private static double distKm(Coordinate a, Coordinate b) {
        double R = 6371.0;
        double lat1 = Math.toRadians(a.getLatitude()),  lon1 = Math.toRadians(a.getLongitude());
        double lat2 = Math.toRadians(b.getLatitude()),  lon2 = Math.toRadians(b.getLongitude());
        double dlat = lat2 - lat1, dlon = lon2 - lon1;
        double h = Math.sin(dlat/2)*Math.sin(dlat/2) +
                Math.cos(lat1)*Math.cos(lat2)*Math.sin(dlon/2)*Math.sin(dlon/2);
        return 2 * R * Math.asin(Math.sqrt(h));
    }

    /**
     *
     */
    private void conectarDesastreConTodasLasZonasMunicipio() {
        if (sistemaGestionDesastres == null || desastreActivo == null) return;

        sistemaGestionDesastres.getZonas().stream()
                .filter(z -> z.getTipo() != TipoZona.DESASTRE)
                .filter(z -> z.getMunicipio().equals(desastreActivo.getMunicipio()))
                .forEach(z -> {
                    double d = distKm(z, desastreActivo);
                    // Si tu Grafo ya evita duplicados, basta con llamar a conectar:
                    sistemaGestionDesastres.getGrafo().conectar(z, desastreActivo, d, true, 10);
                });
    }
    /**
     *
     */
    /** Suma de distancias (km) para una polilínea de coordenadas. */
    private static double longitudRutaKm(List<Coordinate> coords) {
        double d = 0;
        for (int i = 1; i < coords.size(); i++) d += distKm(coords.get(i-1), coords.get(i));
        return d;
    }

    /** Interpola un punto a lo largo de coords según fracción [0..1]. */
    /**
     *
     * @param coords
     * @param t
     * @return
     */
    private static Coordinate puntoEnRuta(List<Coordinate> coords, double t) {
        if (coords == null || coords.size() < 2) return null;
        t = Math.max(0, Math.min(1, t));

        // longitudes acumuladas
        double total = 0;
        double[] seg = new double[coords.size()-1];
        for (int i=1;i<coords.size();i++){
            double di = distKm(coords.get(i-1), coords.get(i));
            seg[i-1] = di;
            total += di;
        }
        double objetivo = t * total;

        double acum = 0;
        for (int i=0;i<seg.length;i++){
            if (objetivo <= acum + seg[i] || i == seg.length-1){
                double local = (objetivo - acum) / (seg[i] == 0 ? 1 : seg[i]);
                Coordinate a = coords.get(i);
                Coordinate b = coords.get(i+1);
                double lat = a.getLatitude()  + (b.getLatitude()  - a.getLatitude())  * local;
                double lon = a.getLongitude() + (b.getLongitude() - a.getLongitude()) * local;
                return new Coordinate(lat, lon);
            }
            acum += seg[i];
        }
        return coords.get(coords.size()-1);
    }

    private static List<Coordinate> coordsDeRuta(Ruta ruta) {
        if (ruta == null || ruta.getParadas() == null || ruta.getParadas().size() < 2) return List.of();
        return ruta.getParadas().stream()
                .map(z -> new Coordinate(z.getLatitud(), z.getAltitud()))
                .toList();
    }

    /**
     *
     * @param id
     */
    private void detenerEnvio(String id) {
        EnvioAnim e = enviosActivos.remove(id);
        if (e != null) {
            if (e.timeline != null) e.timeline.stop();
            if (e.marker != null) mapView.removeMarker(e.marker);
            if (e.hudRow != null) hudLista.getChildren().remove(e.hudRow);
        }
        Timeline t = vehAnims.remove(id);
        if (t != null) t.stop();
        Marker m = vehMarkers.remove(id);
        if (m != null) mapView.removeMarker(m);
        vehCoords.remove(id);

        if (hudLista != null && hudLista.getChildren().isEmpty()) hudScroll.setVisible(false);
        CoordinateLine cl = lineasPorEnvio.remove(id);
        if (cl != null) mapView.removeCoordinateLine(cl);
    }

    private void iniciarAnimacionRuta(String envioId,
                                      List<Coordinate> coords,
                                      double velocidadKmH,
                                      boolean idaYVuelta,
                                      Runnable onLlegadaIda) {
        // cancela animación y marcador previos de este envío (si existían)
        detenerAnimacionDe(envioId);

        vehCoords.put(envioId, coords);

        // marcador por envío
        Marker m;
        try { m = Marker.createProvided(Marker.Provided.BLUE); }
        catch (Throwable t) { m = Marker.createProvided(Marker.Provided.RED); }
        m.setVisible(true).setPosition(coords.get(0));
        mapView.addMarker(m);
        vehMarkers.put(envioId, m);

        // métricas
        double distanciaKm = longitudRutaKm(coords);
        double horas = (velocidadKmH <= 0) ? 1.0 : distanciaKm / velocidadKmH;
        long durMs = (long) Math.max(500, horas * 3_600_000);

        // crea HUD por envío
        EnvioAnim ea = crearHudFilaEnvio(envioId, velocidadKmH, distanciaKm);
        ea.marker = m;
        ea.coords = coords;
        ea.horasTotales = horas;
        ea.t0 = System.currentTimeMillis();
        enviosActivos.put(envioId, ea);

        Timeline tl = new Timeline(
                new KeyFrame(javafx.util.Duration.millis(40), e -> {
                    long el = System.currentTimeMillis() - ea.t0;
                    double t = Math.min(1.0, (double) el / durMs);

                    Coordinate p = puntoEnRuta(ea.coords, t);
                    if (p != null) ea.marker.setPosition(p);

                    double restanteH = ea.horasTotales * (1 - t);
                    int min = (int) Math.floor(restanteH * 60);
                    int seg = (int) Math.round((restanteH * 3600) % 60);
                    ea.lblETA.setText(String.format("ETA: %02d:%02d", min, seg));
                    ea.progress.setProgress(t);

                    if (t >= 1.0) {
                        if (onLlegadaIda != null) onLlegadaIda.run();

                        if (idaYVuelta) {
                            iniciarAnimacionRuta(envioId, invertida(ea.coords), velocidadKmH, false, null);
                        } else {
                            vehAnims.remove(envioId);
                        }
                    }
                })
        );
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();
        ea.timeline = tl;
        vehAnims.put(envioId, tl);
    }
    private List<Coordinate> invertida(List<Coordinate> c) {
        ArrayList<Coordinate> r = new ArrayList<>(c);
        Collections.reverse(r);
        return r;
    }
    private void detenerAnimacionDe(String envioId) {
        Timeline t = vehAnims.remove(envioId);
        if (t != null) t.stop();
        Marker m = vehMarkers.remove(envioId);
        if (m != null) mapView.removeMarker(m);
    }
    /**
     *
     */
    private void detenerTodosLosEnvios() {
        new ArrayList<>(enviosActivos.keySet()).forEach(this::detenerEnvio);
        for (String id : new ArrayList<>(vehAnims.keySet())) detenerAnimacionDe(id);
    }

    /**
     *
     */
    private void crearHudMulti() {
        hudLista = new VBox(8);
        hudLista.setPadding(new Insets(8));
        hudLista.setBackground(new Background(new BackgroundFill(
                Color.rgb(20,20,30,0.85), new CornerRadii(10), Insets.EMPTY)));
        hudLista.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 14, 0.3, 0, 2);");

        hudScroll = new ScrollPane(hudLista);
        hudScroll.setPrefWidth(260);
        hudScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        hudScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        hudScroll.setBackground(Background.EMPTY);
        hudScroll.setStyle("-fx-background-color: transparent;");

        paneMapa.getChildren().add(hudScroll);
        AnchorPane.setTopAnchor(hudScroll, 16.0);
        AnchorPane.setRightAnchor(hudScroll, 16.0);
        hudScroll.setVisible(false);
    }
    @FXML
    void btnActualizarRutas(ActionEvent event) {

    }

    @FXML
    void btnAgregarRuta(ActionEvent event) {

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
    /**
     * --------------------------------- GETTERS Y SETTERS -----------------------------------------------
     */
    public SistemaGestionDesastres getSistemaGestionDesastres() {
        return sistemaGestionDesastres;
    }

    public void setSistemaGestionDesastres(SistemaGestionDesastres sistemaGestionDesastres) {
        this.sistemaGestionDesastres = sistemaGestionDesastres;
        prepararCacheMunicipios();

        // --- Combo de equipos ---
        comboSeleccionarEquipo.setItems(
                FXCollections.observableArrayList(this.sistemaGestionDesastres.getEquipos())
        );
        comboSeleccionarEquipo.setConverter(new StringConverter<>() {
            @Override public String toString(Equipo e) {
                return (e == null) ? "" : e.getTipo().name() + " · " + e.getEstado().name();
            }
            @Override public Equipo fromString(String s) { return null; }
        });
        comboSeleccionarEquipo.setTooltip(new Tooltip(
                "Selecciona el equipo que vas a armar y enviar (requiere 1 Vehículo)."
        ));

        // --- Combo de salida (origen) ---
        List<Zona> salidas = this.sistemaGestionDesastres.getZonas().stream()
                .filter(z -> z.getTipo() != TipoZona.DESASTRE)
                .filter(z -> {
                    Zona da = this.sistemaGestionDesastres.getDesastreActivo();
                    return (da == null) || z.getMunicipio().equals(da.getMunicipio());
                })
                .toList();
        comboSalidaRecursos.setItems(FXCollections.observableArrayList(salidas));
        comboSalidaRecursos.setConverter(new StringConverter<>() {
            @Override public String toString(Zona z) { return z == null ? "" : z.getNombre(); }
            @Override public Zona fromString(String s) { return null; }
        });

        // Si el mapa ya está inicializado, muestra de una
        if (mapView != null && mapView.initializedProperty().get()) {
            mostrarInicioDeMunicipios();
        } else {
            mapView.initializedProperty().addListener((obs, was, ready) -> {
                if (ready) mostrarInicioDeMunicipios();
            });
        }
        refrescarComboSalidaPorDesastre();
    }
    private void refrescarComboSalidaPorDesastre() {
        Zona da = sistemaGestionDesastres.getDesastreActivo();
        List<Zona> salidas = sistemaGestionDesastres.getZonas().stream()
                .filter(z -> z.getTipo() != TipoZona.DESASTRE)
                .filter(z -> da == null || z.getMunicipio().equals(da.getMunicipio()))
                .toList();
        comboSalidaRecursos.setItems(FXCollections.observableArrayList(salidas));
        comboSalidaRecursos.setConverter(new StringConverter<>() {
            @Override public String toString(Zona z){ return z==null? "" : z.getNombre(); }
            @Override public Zona fromString(String s){ return null; }
        });
    }
    private void llegadaDeEquipo(Zona destinoDesastre, Recurso vehiculo) {
        // Usa la instancia “oficial” del modelo
        Zona dz = sistemaGestionDesastres.getDesastreActivo();
        if (dz == null) return;

        int capacidad = Math.max(1, vehiculo.getCapacidadPasajeros());
        int tot = dz.getHabitantes();               // o getPersonasTotales() si esa es tu fuente
        int eva = dz.getPersonasEvacuadas();
        int pen = Math.max(0, tot - eva);

        int evacuarAhora = Math.min(capacidad, pen);
        dz.setPersonasEvacuadas(eva + evacuarAhora);

        Platform.runLater(this::actualizarHudDesastre);
    }
    private void actualizarHudDesastre() {
        Zona dz = (sistemaGestionDesastres != null) ? sistemaGestionDesastres.getDesastreActivo() : null;
        if (dz == null) return;

        int tot = dz.getHabitantes(); // o getPersonasTotales()
        int eva = dz.getPersonasEvacuadas();
        int pen = Math.max(0, tot - eva);

        if (hudDesastre != null) {
            lblTotales.setText("Total: " + tot);
            lblEvacuadas.setText("Evacuadas: " + eva);
            lblPendientes.setText("Pendientes: " + pen);
        }

        actualizarPanelEstadisticas();

        // 🔔 Fin automático del desastre
        if (tot <= 0 || pen <= 0) {
            finalizarDesastre();
        }
    }
    private void finalizarDesastre() {
        detenerCronometroDesastre();
        detenerTodosLosEnvios();
        limpiarOverlaysDesastre();
        lineasPorEnvio.values().forEach(cl -> mapView.removeCoordinateLine(cl));
        lineasPorEnvio.clear();

        // Limpia marcador y estado del modelo
        markerDesastre = null;
        cCritico = cModerado = cEstable = null;

        try {
            if (sistemaGestionDesastres != null) {
                // Si tu modelo tiene un método dedicado, úsalo:
                // sistemaGestionDesastres.finalizarSimulacro();
                // De lo contrario, al menos deja el activo en null si aplica.
            }
        } catch (Exception ignored) {}

        desastreActivo = null;

        // Reset UI de estadísticas
        labelHabitantesPeligro.setText("0");
        labelHabitantesEvacuados.setText("0");
        labelHabitantesPendientes.setText("0");
        labelRecursosDisponibles.setText("0");
        graficoAvanceRecursos.setData(FXCollections.emptyObservableList());
        graficoRecursosDistribuidos.setData(FXCollections.emptyObservableList());

        // Refresca combos (por si filtraban por municipio del desastre)
        refrescarComboSalidaPorDesastre();

        mostrarInfo("Desastre finalizado.");
    }
    private EnvioAnim crearHudFilaEnvio(String envioId, double velocidadKmH, double distanciaKm) {
        if (hudLista == null) crearHudMulti();

        Label titulo = new Label("Envío " + envioId);
        titulo.setStyle("-fx-text-fill: white; -fx-font-weight: 700; -fx-font-size: 13px;");

        ProgressBar pb = new ProgressBar(0);
        pb.setPrefWidth(220);

        Label lblETA  = new Label("ETA: —");
        Label lblDist = new Label(String.format("Dist.: %.2f km", distanciaKm));
        Label lblVel  = new Label(String.format("Vel.: %.0f km/h", velocidadKmH));
        for (Label l : List.of(lblETA, lblDist, lblVel)) {
            l.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
        }

        VBox fila = new VBox(6, titulo, pb, lblETA, lblDist, lblVel);
        fila.setPadding(new Insets(10));
        fila.setBackground(new Background(new BackgroundFill(
                Color.rgb(20,20,30,0.85), new CornerRadii(10), Insets.EMPTY)));
        fila.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 14, 0.3, 0, 2);");

        hudLista.getChildren().add(fila);
        hudScroll.setVisible(true);

        EnvioAnim ea = new EnvioAnim();
        ea.id = envioId;
        ea.progress = pb;
        ea.lblTitulo = titulo;
        ea.lblETA = lblETA;
        ea.lblDist = lblDist;
        ea.lblVel = lblVel;
        ea.hudRow = fila;
        return ea;
    }
    private void iniciarAnimacionRutaSingleHUD(String envioId,
                                               List<Coordinate> path,
                                               double velocidadKmH,
                                               boolean idaYVuelta,
                                               Runnable onLlegadaIda) {
        // si ya existía, detén y borra todo (HUD incluido)
        if (enviosActivos.containsKey(envioId)) {
            detenerEnvio(envioId);
        }
        // crea HUD UNA SOLA VEZ
        double distanciaKm = longitudRutaKm(path);
        EnvioAnim ea = crearHudFilaEnvio(envioId, velocidadKmH, distanciaKm);
        ea.coords = path;
        ea.horasTotales = (velocidadKmH <= 0) ? 1.0 : (distanciaKm / velocidadKmH);
        ea.t0 = System.currentTimeMillis();
        enviosActivos.put(envioId, ea);

        // marcador
        Marker m;
        try { m = Marker.createProvided(Marker.Provided.BLUE); }
        catch (Throwable t) { m = Marker.createProvided(Marker.Provided.RED); }
        m.setVisible(true).setPosition(path.get(0));
        mapView.addMarker(m);
        ea.marker = m;

        Timeline tl = new Timeline(new KeyFrame(javafx.util.Duration.millis(40), e -> {
            long el = System.currentTimeMillis() - ea.t0;
            long durMs = (long) Math.max(500, ea.horasTotales * 3_600_000);
            double t = Math.min(1.0, (double) el / durMs);

            Coordinate p = puntoEnRuta(ea.coords, t);
            if (p != null) ea.marker.setPosition(p);

            double restanteH = ea.horasTotales * (1 - t);
            int min = (int)Math.floor(restanteH * 60);
            int seg = (int)Math.round((restanteH * 3600) % 60);
            ea.lblETA.setText(String.format("ETA: %02d:%02d", min, seg));
            ea.progress.setProgress(t);

            if (t >= 1.0) {
                // Llegada de la ida
                if (!ea.enRegreso && idaYVuelta) {
                    if (onLlegadaIda != null) onLlegadaIda.run();

                    // Preparar regreso REUSANDO MISMO HUD y MISMO MARCADOR
                    ea.enRegreso = true;
                    ea.coords = invertida(ea.coords);
                    double distKmReg = longitudRutaKm(ea.coords);
                    ea.horasTotales = (velocidadKmH <= 0) ? 1.0 : (distKmReg / velocidadKmH);
                    ea.t0 = System.currentTimeMillis();
                    ea.lblTitulo.setText("Envío " + envioId + " (regreso)");
                    ea.lblDist.setText(String.format("Dist.: %.2f km", distKmReg));
                    ea.progress.setProgress(0);
                    return; // la MISMA timeline continua ahora para el regreso
                }

                // Fin total (terminó el regreso o no había regreso)
                detenerEnvio(envioId); // borra HUD, marcador, timeline y caches
            }
        }));
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();
        ea.timeline = tl;
    }
    private void restarStock(Recurso r, int n) {
        int nueva = Math.max(0, r.getCantidad() - Math.max(0, n));
        r.setCantidad(nueva);
        if (nueva == 0 && r.getEstado() == EstadoRecurso.DISPONIBLE) {
            r.setEstado(EstadoRecurso.ASIGNADO); // o el estado que uses para “sin stock”
        }
    }
    private void fijarContenedorChart(PieChart chart, double altoPx) {
        Region parent = (Region) chart.getParent();
        // Altura estabilizada del “card”
        parent.setMinHeight(altoPx);
        parent.setPrefHeight(altoPx);
        parent.setMaxHeight(Region.USE_PREF_SIZE); // no crece más
        // El chart solo se ancla, sin binds de pref*
        AnchorPane.setTopAnchor(chart, 16.0);
        AnchorPane.setBottomAnchor(chart, 16.0);
        AnchorPane.setLeftAnchor(chart, 16.0);
        AnchorPane.setRightAnchor(chart, 16.0);
        chart.setMinSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
        chart.setMaxSize(Region.USE_COMPUTED_SIZE, Region.USE_COMPUTED_SIZE);
    }
}