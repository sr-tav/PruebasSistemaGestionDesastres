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

    //Dijkstra
    public Ruta calcularRutaMasCorta(Zona origen, Zona destino) {

        Map<Zona, Double> distancia = new HashMap<>();
        Map<Zona, Zona> anterior = new HashMap<>();

        for (Zona z : adyacencia.keySet()) {
            distancia.put(z, Double.POSITIVE_INFINITY);
        }
        distancia.put(origen, 0.0);

        PriorityQueue<Zona> cola = new PriorityQueue<>(Comparator.comparingDouble(distancia::get));
        cola.add(origen);

        while (!cola.isEmpty()) {
            Zona actual = cola.poll();

            if (actual.equals(destino)) break;

            for (Camino camino : salidas(actual)) {
                Zona vecino = camino.getHasta();
                double nuevaDistancia = distancia.get(actual) + camino.getDistanciaKm();

                if (nuevaDistancia < distancia.get(vecino)) {
                    distancia.put(vecino, nuevaDistancia);
                    anterior.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        if (!anterior.containsKey(destino)) {
            return null;
        }

        List<Zona> paradas = new ArrayList<>();
        Zona actual = destino;
        paradas.add(destino);

        while (anterior.containsKey(actual)) {
            actual = anterior.get(actual);
            paradas.add(actual);
        }

        Collections.reverse(paradas);

        Ruta ruta = new Ruta("Ruta más corta: " + origen.getNombre() + " → " + destino.getNombre());
        for (Zona z : paradas) {
            ruta.agregarParada(z);
        }

        ruta.setDistanciaTotalKm(distancia.get(destino));

        double tiempoHoras = distancia.get(destino) / 50.0;
        ruta.setDuracionHoras(tiempoHoras);

        return ruta;
    }
}
