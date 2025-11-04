package com.example.pruebassistemadesastres.model;

public class Admin extends Persona {
    private String clave;

    public Admin(String nombre, String clave) {
        super(nombre);
        this.clave = clave;
    }
    public String getClave() {
        return clave;
    }
    public void setClave(String clave) {}
}
