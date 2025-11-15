package com.example.pruebassistemadesastres.model;

import java.util.HashMap;
import java.util.Map;

public class NodoDistribuccion {

    private String nombre;
    private Map<TipoRecurso, Integer> inventario;
    private NodoDistribuccion izquierdo;
    private NodoDistribuccion derecho;

    public NodoDistribuccion(String nombre, Map<TipoRecurso, Integer> inventario,
                             NodoDistribuccion izquierdo, NodoDistribuccion derecho) {

        this.nombre = nombre;
        this.inventario = (inventario != null) ? inventario : new HashMap<>();
        this.izquierdo = izquierdo;
        this.derecho = derecho;
    }

    public void agregarInventario(TipoRecurso tipo, int cantidad) {
        inventario.put(tipo, inventario.getOrDefault(tipo, 0) + cantidad);
    }

    public Map<TipoRecurso, Integer> getInventario() {
        return inventario;
    }

    public NodoDistribuccion getIzquierdo() { return izquierdo; }
    public void setIzquierdo(NodoDistribuccion izquierdo) { this.izquierdo = izquierdo; }

    public NodoDistribuccion getDerecho() { return derecho; }
    public void setDerecho(NodoDistribuccion derecho) { this.derecho = derecho; }

    public String getNombre() { return nombre; }

    public void mostrarInventario() {
        System.out.println("Inventario de " + nombre + ":");
        inventario.forEach((tipo, cantidad) ->
                System.out.println("  " + tipo + " = " + cantidad));
    }

    public void mostrar(String prefijo) {
        System.out.println(prefijo + "Nodo: " + nombre);
        inventario.forEach((tipo, cantidad) ->
                System.out.println(prefijo + "  " + tipo + ": " + cantidad)
        );

        if (izquierdo != null) {
            System.out.println(prefijo + "Izquierdo:");
            izquierdo.mostrar(prefijo + "    ");
        }

        if (derecho != null) {
            System.out.println(prefijo + "Derecho:");
            derecho.mostrar(prefijo + "    ");
        }
    }
}
