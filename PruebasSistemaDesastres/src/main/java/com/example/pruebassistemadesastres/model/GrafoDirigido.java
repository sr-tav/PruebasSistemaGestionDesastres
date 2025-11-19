package com.example.pruebassistemadesastres.model;

import java.util.*;

public class GrafoDirigido {
    private final Map<Zona, List<Camino>> adyacencia = new HashMap<>();

    public void agregarZona(Zona z) {
        adyacencia.putIfAbsent(z, new ArrayList<>());
    }

    // boolean ahora significa bidireccional
    public void conectar(Zona desde, Zona hasta, double distanciaKm, boolean bidireccional, double capacidadTon) {
        agregarZona(desde);
        agregarZona(hasta);

        adyacencia.get(desde).add(new Camino(desde, hasta, distanciaKm, /*pav*/ true, capacidadTon));

        if (bidireccional) {
            adyacencia.get(hasta).add(new Camino(hasta, desde, distanciaKm, /*pav*/ true, capacidadTon));
        }
    }

    public List<Camino> salidas(Zona z) { return adyacencia.getOrDefault(z, List.of()); }
    public Set<Zona> zonas() { return adyacencia.keySet(); }
    public void eliminarZona(Zona z) { adyacencia.remove(z); }

    // --- helpers de depuración (útiles una vez) ---
    public boolean containsZona(Zona z){ return adyacencia.containsKey(z); }
    public List<Zona> vecinos(Zona z){
        return adyacencia.getOrDefault(z, List.of()).stream().map(Camino::getHasta).toList();
    }
    public int totalNodos(){ return adyacencia.size(); }
    public int totalAristas(){
        return adyacencia.values().stream().mapToInt(List::size).sum();
    }

    // Dijkstra igual que lo tienes
    public Ruta calcularRutaMasCorta(Zona origen, Zona destino) {
        Map<Zona, Double> distancia = new HashMap<>();
        Map<Zona, Zona> anterior = new HashMap<>();

        for (Zona z : adyacencia.keySet()) distancia.put(z, Double.POSITIVE_INFINITY);
        // por si origen no fue agregado explícitamente:
        distancia.putIfAbsent(origen, Double.POSITIVE_INFINITY);
        distancia.put(origen, 0.0);

        PriorityQueue<Zona> cola = new PriorityQueue<>(Comparator.comparingDouble(distancia::get));
        cola.add(origen);

        while (!cola.isEmpty()) {
            Zona actual = cola.poll();
            if (actual.equals(destino)) break;

            for (Camino camino : salidas(actual)) {
                Zona vecino = camino.getHasta();
                double nd = distancia.get(actual) + camino.getDistanciaKm();
                if (nd < distancia.getOrDefault(vecino, Double.POSITIVE_INFINITY)) {
                    distancia.put(vecino, nd);
                    anterior.put(vecino, actual);
                    cola.add(vecino);
                }
            }
        }

        if (!anterior.containsKey(destino) && !origen.equals(destino)) return null;

        List<Zona> paradas = new ArrayList<>();
        Zona cur = destino;
        paradas.add(destino);
        while (anterior.containsKey(cur)) { cur = anterior.get(cur); paradas.add(cur); }
        Collections.reverse(paradas);

        Ruta r = new Ruta("Ruta más corta: " + origen.getNombre() + " → " + destino.getNombre());
        paradas.forEach(r::agregarParada);
        double dist = distancia.getOrDefault(destino, 0.0);
        r.setDistanciaTotalKm(dist);
        r.setDuracionHoras(dist / 50.0);
        return r;
    }
}
