package com.example.pruebassistemadesastres.model;

public class Equipo extends EntidadBase{

    private final TipoEquipo tipo;
    private EstadoEquipo estado;

    public Equipo(TipoEquipo tipo, EstadoEquipo estado) {
        this.tipo = tipo;
        this.estado = estado;
    }
    public TipoEquipo getTipo() { return tipo; }
    public EstadoEquipo getEstado() { return estado; }
    public void setEstado(EstadoEquipo estado) { this.estado = estado; }
}
