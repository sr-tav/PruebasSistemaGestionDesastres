package com.example.pruebassistemadesastres.model;

public class Camino extends EntidadBase {
    private final Zona desde;
    private final Zona hasta;
    private final double distanciaKm;
    private final boolean pavimentado;
    private final double capacidadTon;
    public Camino(Zona desde, Zona hasta, double distanciaKm, boolean pavimentado, double capacidadTon) {
        this.desde = desde; this.hasta = hasta; this.distanciaKm = distanciaKm; this.pavimentado = pavimentado; this.capacidadTon = capacidadTon;
    }
    public Zona getDesde() { return desde; }
    public Zona getHasta() { return hasta; }
    public double getDistanciaKm() { return distanciaKm; }
    public boolean isPavimentado() { return pavimentado; }
    public double getCapacidadTon() { return capacidadTon; }
}
