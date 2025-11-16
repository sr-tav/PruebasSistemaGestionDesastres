package com.example.pruebassistemadesastres.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodoDistribuccion {
    private String nombre;
    private List<Recurso> recursos; // recursos con estado
    private NodoDistribuccion izquierdo;
    private NodoDistribuccion derecho;

    public NodoDistribuccion(String nombre) {
        this.nombre = nombre;
        this.recursos = new ArrayList<>();
        this.izquierdo = null;
        this.derecho = null;
    }

    public String getNombre() {
        return nombre;
    }

    public void agregarRecurso(Recurso recurso) {
        recursos.add(recurso);
    }

    public List<Recurso> getRecursos() {
        return recursos;
    }

    public NodoDistribuccion getIzquierdo() {
        return izquierdo;
    }

    public void setIzquierdo(NodoDistribuccion izquierdo) {
        this.izquierdo = izquierdo;
    }

    public NodoDistribuccion getDerecho() {
        return derecho;
    }

    public void setDerecho(NodoDistribuccion derecho) {
        this.derecho = derecho;
    }

    public void mostrar(String prefijo) {
        System.out.println(prefijo + "Nodo: " + nombre);

        for (Recurso r : recursos) {
            System.out.println(prefijo + "  " + r.getTipo() +
                    " = " + r.getCantidad() +
                    " (" + r.getEstado() + ")");
        }

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
