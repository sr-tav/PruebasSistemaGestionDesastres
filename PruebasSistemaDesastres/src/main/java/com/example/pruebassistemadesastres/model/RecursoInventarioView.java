package com.example.pruebassistemadesastres.model;

public class RecursoInventarioView {
    private String recurso;
    private int cantidad;
    private String estado;

    public RecursoInventarioView(String recurso, int cantidad, String estado) {
        this.recurso = recurso;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    public String getRecurso() { return recurso; }
    public int getCantidad() { return cantidad; }
    public String getEstado() { return estado; }
}
