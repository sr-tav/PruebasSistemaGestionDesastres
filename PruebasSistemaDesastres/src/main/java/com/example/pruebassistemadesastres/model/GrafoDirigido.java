package com.example.pruebassistemadesastres.model;

import java.util.*;

public class GrafoDirigido {
    private final Map<Zona, List<Camino>> adyacencia = new HashMap<>();

    public void agregarZona(Zona z) {
        adyacencia.putIfAbsent(z, new ArrayList<>());
    }
    public void conectar(Zona desde, Zona hasta, double distanciaKm, boolean pavimentado, double capacidadTon) {
        agregarZona(desde); agregarZona(hasta);
        adyacencia.get(desde).add(new Camino(desde, hasta, distanciaKm, pavimentado, capacidadTon));
    }

    public List<Camino> salidas(Zona z) {
        return adyacencia.getOrDefault(z, List.of());
    }
    public Set<Zona> zonas() {
        return adyacencia.keySet();
    }
}
