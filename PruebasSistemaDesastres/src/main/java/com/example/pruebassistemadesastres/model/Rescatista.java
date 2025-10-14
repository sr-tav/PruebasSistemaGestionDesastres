package com.example.pruebassistemadesastres.model;

public class Rescatista extends Persona{
    private final TipoEquipo especialidad;

    public Rescatista(String nombre, TipoEquipo especialidad) {
        super(nombre);
        this.especialidad = especialidad;
    }
    public TipoEquipo getEspecialidad() { return especialidad; }
}
