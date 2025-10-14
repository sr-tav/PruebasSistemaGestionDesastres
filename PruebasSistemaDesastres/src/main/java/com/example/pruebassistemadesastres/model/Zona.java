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


    public Zona(String nombre, TipoZona tipo, Municipio municipio, int habitantes, int nivelRiesgo) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.municipio = municipio;
        this.habitantes = habitantes;
        this.nivelRiesgo = nivelRiesgo;
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
}
