package com.example.pruebassistemadesastres.model;

public class ArbolDistribuccion {
    private NodoDistribuccion raiz;

    public ArbolDistribuccion(NodoDistribuccion raiz) {
        this.raiz = raiz;
    }

    public NodoDistribuccion getRaiz() {
        return raiz;
    }

    public void mostrarArbol() {
        if (raiz != null) {
            raiz.mostrar("");
        } else {
            System.out.println("El árbol está vacío.");
        }
    }

    public void recorrerPreorden(NodoDistribuccion nodo) {
        if (nodo != null) {
            System.out.println("Nodo: " + nodo.getNombre());
            nodo.getInventario().forEach((tipo, cantidad) ->
                    System.out.println("  " + tipo + ": " + cantidad)
            );
            recorrerPreorden(nodo.getIzquierdo());
            recorrerPreorden(nodo.getDerecho());
        }
    }



}
