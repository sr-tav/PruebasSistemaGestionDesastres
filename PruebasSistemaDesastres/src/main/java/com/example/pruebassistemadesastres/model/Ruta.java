package com.example.pruebassistemadesastres.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ruta extends EntidadBase {

    private final String nombre;
    private final List<Zona> paradas = new ArrayList<>();
    private double distanciaTotalKm;
    private double duracionHoras;
    private boolean transitable;

    public Ruta(String nombre) {
        this.nombre = nombre;
        this.transitable = true;
    }

    public String getNombre() { return nombre; }
    public List<Zona> getParadas() { return Collections.unmodifiableList(paradas); }
    public void agregarParada(Zona z) { paradas.add(z); }
    public double getDistanciaTotalKm() { return distanciaTotalKm; }
    public void setDistanciaTotalKm(double d) { this.distanciaTotalKm = d; }
    public double getDuracionHoras() { return duracionHoras; }
    public void setDuracionHoras(double h) { this.duracionHoras = h; }
    public boolean isTransitable() { return transitable; }
    public void setTransitable(boolean t) { this.transitable = t; }
}
