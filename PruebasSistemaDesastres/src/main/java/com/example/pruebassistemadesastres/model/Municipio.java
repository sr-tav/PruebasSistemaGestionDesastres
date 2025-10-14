package com.example.pruebassistemadesastres.model;

public class Municipio extends EntidadBase {
    private final String nombre;
    private int poblacion;

    public Municipio(String nombre, int poblacion) {
        this.nombre = nombre; this.poblacion = poblacion;
    }
    public String getNombre() { return nombre; }
    public int getPoblacion() { return poblacion; }
    public void setPoblacion(int poblacion) { this.poblacion = poblacion; }
}
