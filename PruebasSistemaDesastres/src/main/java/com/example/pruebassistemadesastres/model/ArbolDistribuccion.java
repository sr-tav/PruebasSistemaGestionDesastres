package com.example.pruebassistemadesastres.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArbolDistribuccion {
    private NodoDistribuccion raiz;
    private List<NodoDistribuccion> nodos = new ArrayList<>();

    public ArbolDistribuccion() {}

    public NodoDistribuccion getRaiz() {
        return raiz;
    }


    public void poblarDesdeZonas(List<Zona> zonas) {
        for (Zona z : zonas) {
            NodoDistribuccion nodo = new NodoDistribuccion(z.getNombre());
            nodos.add(nodo);
        }

        // formamos la raíz si hay datos
        if (!nodos.isEmpty()) {
            raiz = nodos.get(0);
        }

        // Enlazamos de manera simple en árbol binario
        for (int i = 0; i < nodos.size(); i++) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;

            if (left < nodos.size())
                nodos.get(i).setIzquierdo(nodos.get(left));

            if (right < nodos.size())
                nodos.get(i).setDerecho(nodos.get(right));
        }
    }

    public void distribuirRecursosDetalle(List<Recurso> recursos) {
        if (nodos.isEmpty()) return;

        int index = 0;
        for (Recurso r : recursos) {
            nodos.get(index).agregarRecurso(r);
            index = (index + 1) % nodos.size();
        }
    }

    public List<Recurso> obtenerTodosLosRecursosDetalle() {
        List<Recurso> lista = new ArrayList<>();

        for (NodoDistribuccion nodo : nodos) {
            lista.addAll(nodo.getRecursos());
        }

        return lista;
    }

    public void setRaiz(NodoDistribuccion raiz) {
        this.raiz = raiz;
    }

    public List<NodoDistribuccion> getNodos() {
        return nodos;
    }

    public void setNodos(List<NodoDistribuccion> nodos) {
        this.nodos = nodos;
    }
}
