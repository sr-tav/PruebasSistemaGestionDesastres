package com.example.pruebassistemadesastres.model;

import java.util.HashMap;
import java.util.Map;

public class NodoDistribuccion {

    private String nombre;
    private Map<TipoRecurso, Integer> inventario = new HashMap<>();
    private NodoDistribuccion izquierdo;
    private NodoDistribuccion derecho;

    public NodoDistribuccion(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }

    public NodoDistribuccion getIzquierdo() { return izquierdo; }
    public void setIzquierdo(NodoDistribuccion izquierdo) { this.izquierdo = izquierdo; }

    public NodoDistribuccion getDerecho() { return derecho; }
    public void setDerecho(NodoDistribuccion derecho) { this.derecho = derecho; }

    public Map<TipoRecurso, Integer> getInventario() { return inventario; }

    public void agregarInventario(TipoRecurso tipo, int cantidad) {
        inventario.put(tipo, inventario.getOrDefault(tipo, 0) + cantidad);
    }

    public void retirarInventario(TipoRecurso tipo, int cantidad) {
        inventario.put(tipo, inventario.getOrDefault(tipo, 0) - cantidad);
        if (inventario.get(tipo) <= 0) inventario.remove(tipo);
    }
}
