package com.example.pruebassistemadesastres.model;

public class Recurso extends EntidadBase {
    private final String idRecurso;
    private final TipoRecurso tipo;
    private int cantidad;
    private EstadoRecurso estado;

    // Metadatos para VEHICULO (ignorados para otros tipos)
    private int capacidadPasajeros = 10;   // default
    private double velocidadKmH   = 80.0;

    public Recurso(String idRecurso, TipoRecurso tipo, int cantidad, EstadoRecurso estado) {
        this.idRecurso = idRecurso;
        this.tipo = tipo;
        this.cantidad = cantidad;
        this.estado = estado;
    }

    public boolean esVehiculo() { return tipo == TipoRecurso.VEHICULO; }
    public String getIdRecurso() { return idRecurso; }
    public TipoRecurso getTipo() { return tipo; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public EstadoRecurso getEstado() { return estado; }
    public void setEstado(EstadoRecurso estado) { this.estado = estado; }
    /** Solo tiene efecto si es VEHICULO */
    public int getCapacidadPasajeros() { return capacidadPasajeros; }
    public void setCapacidadPasajeros(int capacidadPasajeros) { this.capacidadPasajeros = capacidadPasajeros; }

    /** Solo tiene efecto si es VEHICULO */
    public double getVelocidadKmH() { return velocidadKmH; }
    public void setVelocidadKmH(double velocidadKmH) { this.velocidadKmH = velocidadKmH; }

    public void cambiarEstado(EstadoRecurso nuevoEstado) {
        this.estado = nuevoEstado;
    }
}
