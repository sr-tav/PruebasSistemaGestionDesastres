package com.example.pruebassistemadesastres.model;

public class Recurso extends EntidadBase {
    private final String idRecurso;
    private final TipoRecurso tipo;
    private int cantidad;
    private EstadoRecurso estado;

    public Recurso(String idRecurso, TipoRecurso tipo, int cantidad, EstadoRecurso estado) {
        this.idRecurso = idRecurso;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    public String getIdRecurso() { return idRecurso; }
    public TipoRecurso getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public EstadoRecurso getEstado() { return estado; }
    public void setEstado(EstadoRecurso estado) { this.estado = estado; }
}
