package com.example.pruebassistemadesastres.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Zona extends EntidadBase{
    private final String nombre;
    private final TipoZona tipo;
    private final Municipio municipio;
    private int habitantes;
    private int nivelRiesgo; // 1..5
    private final Map<TipoRecurso, Integer> inventario = new EnumMap<>(TipoRecurso.class);
    private final List<Rescatista> rescatistas = new ArrayList<>();
    private final List<Equipo> equipos = new ArrayList<>();
    private double latitud;
    private double altitud;
    private int personasTotales;
    private int personasEvacuadas;


    public Zona(String nombre, TipoZona tipo, Municipio municipio, int habitantes, int nivelRiesgo, double latitud, double altitud) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.municipio = municipio;
        this.habitantes = habitantes;
        this.nivelRiesgo = nivelRiesgo;
        this.latitud = latitud;
        this.altitud = altitud;
    }

    public String getNombre() { return nombre; }
    public TipoZona getTipo() { return tipo; }
    public Municipio getMunicipio() { return municipio; }
    public int getHabitantes() { return habitantes; }
    public void setHabitantes(int habitantes) { this.habitantes = habitantes; }
    public int getNivelRiesgo() { return nivelRiesgo; }
    public void setNivelRiesgo(int nivelRiesgo) { this.nivelRiesgo = nivelRiesgo; }
    public Map<TipoRecurso,Integer> getInventario() { return inventario; }
    public List<Rescatista> getRescatistas() { return rescatistas; }
    public List<Equipo> getEquipos() { return equipos; }
    public double getLatitud() { return latitud; }
    public void setLatitud(double latitud) { this.latitud = latitud; }
    public double getAltitud() { return altitud; }
    public void setAltitud(double altitud) { this.altitud = altitud; }
    public int getPersonasTotales() {
        return this.personasTotales;
    }

    public int getPersonasEvacuadas() {
        return this.personasEvacuadas;
    }

    public void setPersonasEvacuadas(int personasEvacuadas) {
        this.personasEvacuadas = personasEvacuadas;
    }

    public void setPersonasTotales(int personasTotales) {
        this.personasTotales = personasTotales;
    }

}
