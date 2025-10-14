package com.example.pruebassistemadesastres.model;

public class Evacuacion extends EntidadBase {
    private final Zona origen;
    private final Zona destino;
    private Ruta ruta;
    private int personas;
    private double tiempoEstimadoHoras;

    public Evacuacion(Zona origen, Zona destino, int personas) {
        this.origen = origen;
        this.destino = destino;
        this.personas = personas;
    }

    public Zona getOrigen() { return origen; }
    public Zona getDestino() { return destino; }
    public Ruta getRuta() { return ruta; }
    public void setRuta(Ruta ruta) { this.ruta = ruta; }
    public int getPersonas() { return personas; }
    public void setPersonas(int personas) { this.personas = personas; }
    public double getTiempoEstimadoHoras() { return tiempoEstimadoHoras; }
    public void setTiempoEstimadoHoras(double t) { this.tiempoEstimadoHoras = t; }
}
