package com.example.pruebassistemadesastres.model;

public class OperadorEmergencia extends Persona {
    String clave;
    public OperadorEmergencia(String nombre, String clave) {
        super(nombre);
        this.clave = clave;
    }
}
