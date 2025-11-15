package com.example.pruebassistemadesastres.model;

public class ArbolDistribuccion {

    private NodoDistribuccion raiz;

    public ArbolDistribuccion(String nombreRaiz) {
        this.raiz = new NodoDistribuccion(nombreRaiz);
    }

    public NodoDistribuccion getRaiz() {
        return raiz;
    }

    // Inserción basada en orden alfabético del nombre
    public void insertarNodo(String nombre) {
        raiz = insertarRec(raiz, nombre);
    }

    private NodoDistribuccion insertarRec(NodoDistribuccion actual, String nombre) {
        if (actual == null)
            return new NodoDistribuccion(nombre);

        if (nombre.compareTo(actual.getNombre()) < 0)
            actual.setIzquierdo(insertarRec(actual.getIzquierdo(), nombre));
        else
            actual.setDerecho(insertarRec(actual.getDerecho(), nombre));

        return actual;
    }

    // Buscar un nodo por nombre
    public NodoDistribuccion buscar(String nombre) {
        return buscarRec(raiz, nombre);
    }

    private NodoDistribuccion buscarRec(NodoDistribuccion actual, String nombre) {
        if (actual == null) return null;

        if (actual.getNombre().equals(nombre)) return actual;

        return nombre.compareTo(actual.getNombre()) < 0 ?
                buscarRec(actual.getIzquierdo(), nombre) :
                buscarRec(actual.getDerecho(), nombre);
    }

    // Métodos de inventario desde el árbol
    public void agregarRecurso(String nodo, TipoRecurso tipo, int cantidad) {
        NodoDistribuccion n = buscar(nodo);
        if (n != null) n.agregarInventario(tipo, cantidad);
    }

    public void retirarRecurso(String nodo, TipoRecurso tipo, int cantidad) {
        NodoDistribuccion n = buscar(nodo);
        if (n != null) n.retirarInventario(tipo, cantidad);
    }
}
