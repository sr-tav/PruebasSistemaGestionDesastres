package com.example.pruebassistemadesastres.model;

public class Persona extends EntidadBase{
    protected String nombre;

    public Persona(String nombre) {
        this.nombre = nombre;
    }
    public String getNombre() { return nombre; }

}
