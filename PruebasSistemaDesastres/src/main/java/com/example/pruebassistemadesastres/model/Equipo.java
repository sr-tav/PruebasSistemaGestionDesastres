package com.example.pruebassistemadesastres.model;

import com.sothawo.mapjfx.Coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Equipo extends EntidadBase {

    private final String idEquipo = "EQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

    private final TipoEquipo tipo;         // médico, bombero, etc. (lo mantengo)
    private EstadoEquipo estado;           // DISPONIBLE/MANTENIMIENTO, etc. (sigue igual)

    // --- Misión/operación ---
    private EstadoMision estadoMision = null;
    private Zona zonaOrigen;
    private Zona zonaDestinoDesastre;
    private Zona zonaDestinoRegreso; // normalmente = origen

    private final List<Recurso> recursos = new ArrayList<>();
    private final List<Rescatista> rescatistas = new ArrayList<>();

    // Derivados del vehículo principal
    private int capacidadPasajeros = 10;
    private double velocidadKmH = 80.0;

    // --- Rutas (polilíneas OSRM) ---
    private List<Coordinate> polylineIda = List.of();
    private List<Coordinate> polylineRegreso = List.of();

    // --- Progreso/tiempos ---
    private double distanciaTotalKmIda = 0.0;
    private double distanciaRecorridaKmIda = 0.0;

    private double distanciaTotalKmRegreso = 0.0;
    private double distanciaRecorridaKmRegreso = 0.0;

    private long etaLlegadaMillis = 0L;
    private long etaRegresoMillis = 0L;

    public Equipo(TipoEquipo tipo, EstadoEquipo estado) {
        this.tipo = tipo;
        this.estado = estado;
    }

    // Validación rápida: debe tener 1 vehículo
    public Recurso getVehiculo() {
        return recursos.stream().filter(Recurso::esVehiculo).findFirst().orElse(null);
    }

    // Getters/Setters
    public String getIdEquipo() { return idEquipo; }
    public TipoEquipo getTipo() { return tipo; }
    public EstadoEquipo getEstado() { return estado; }
    public void setEstado(EstadoEquipo estado) { this.estado = estado; }

    public EstadoMision getEstadoMision() { return estadoMision; }
    public void setEstadoMision(EstadoMision estadoMision) { this.estadoMision = estadoMision; }

    public Zona getZonaOrigen() { return zonaOrigen; }
    public void setZonaOrigen(Zona zonaOrigen) { this.zonaOrigen = zonaOrigen; }

    public Zona getZonaDestinoDesastre() { return zonaDestinoDesastre; }
    public void setZonaDestinoDesastre(Zona z) { this.zonaDestinoDesastre = z; }

    public Zona getZonaDestinoRegreso() { return zonaDestinoRegreso; }
    public void setZonaDestinoRegreso(Zona z) { this.zonaDestinoRegreso = z; }

    public List<Recurso> getRecursos() { return recursos; }
    public List<Rescatista> getRescatistas() { return rescatistas; }

    public int getCapacidadPasajeros() { return capacidadPasajeros; }
    public void setCapacidadPasajeros(int cap) { this.capacidadPasajeros = cap; }

    public double getVelocidadKmH() { return velocidadKmH; }
    public void setVelocidadKmH(double v) { this.velocidadKmH = v; }

    public List<Coordinate> getPolylineIda() { return polylineIda; }
    public void setPolylineIda(List<Coordinate> p) { this.polylineIda = p; }

    public List<Coordinate> getPolylineRegreso() { return polylineRegreso; }
    public void setPolylineRegreso(List<Coordinate> p) { this.polylineRegreso = p; }

    public double getDistanciaTotalKmIda() { return distanciaTotalKmIda; }
    public void setDistanciaTotalKmIda(double d) { this.distanciaTotalKmIda = d; }

    public double getDistanciaRecorridaKmIda() { return distanciaRecorridaKmIda; }
    public void setDistanciaRecorridaKmIda(double d) { this.distanciaRecorridaKmIda = d; }

    public double getDistanciaTotalKmRegreso() { return distanciaTotalKmRegreso; }
    public void setDistanciaTotalKmRegreso(double d) { this.distanciaTotalKmRegreso = d; }

    public double getDistanciaRecorridaKmRegreso() { return distanciaRecorridaKmRegreso; }
    public void setDistanciaRecorridaKmRegreso(double d) { this.distanciaRecorridaKmRegreso = d; }

    public long getEtaLlegadaMillis() { return etaLlegadaMillis; }
    public void setEtaLlegadaMillis(long t) { this.etaLlegadaMillis = t; }

    public long getEtaRegresoMillis() { return etaRegresoMillis; }
    public void setEtaRegresoMillis(long t) { this.etaRegresoMillis = t; }
}
