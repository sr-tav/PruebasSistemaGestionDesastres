package com.example.pruebassistemadesastres.model;

import java.util.ArrayList;
import java.util.List;

public class ColaPrioridadEvacuacion {
    private final List<Evacuacion> cola;

    public ColaPrioridadEvacuacion() {
        this.cola = new ArrayList<>();
    }

    public void encolar(Evacuacion ev) {
        int i = 0;
        while (i < cola.size() &&
                ev.getOrigen().getNivelRiesgo() <= cola.get(i).getOrigen().getNivelRiesgo()) {
            i++;
        }
        cola.add(i, ev);
        System.out.println("Evacuación planificada: " + ev.getPersonas() +
                " personas desde " + ev.getOrigen().getNombre() +
                " hacia " + ev.getDestino().getNombre() +
                " (riesgo: " + ev.getOrigen().getNivelRiesgo() + ")");
    }

    public Evacuacion desencolar() {
        if (cola.isEmpty()) {
            System.out.println("No hay evacuaciones pendientes.");
            return null;
        }

        Evacuacion ev = cola.remove(0);
        System.out.println("Procesando evacuación de " + ev.getPersonas() +
                " personas desde " + ev.getOrigen().getNombre() +
                " hacia " + ev.getDestino().getNombre());
        return ev;
    }

    public Evacuacion verSiguiente() {
        if (cola.isEmpty()) return null;
        return cola.get(0);
    }

    public void mostrarCola() {
        if (cola.isEmpty()) {
            System.out.println("No hay evacuaciones registradas.");
            return;
        }

        System.out.println("Evacuaciones pendientes (ordenadas por prioridad):");
        for (Evacuacion e : cola) {
            System.out.println(" - " + e.getOrigen().getNombre() + " → " +
                    e.getDestino().getNombre() +
                    " | Personas: " + e.getPersonas() +
                    " | Riesgo: " + e.getOrigen().getNivelRiesgo());
        }
    }

    public boolean estaVacia() {
        return cola.isEmpty();
    }

    public int tamaño() {
        return cola.size();
    }


}
