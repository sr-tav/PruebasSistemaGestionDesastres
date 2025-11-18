package com.example.pruebassistemadesastres.model;

import com.sothawo.mapjfx.Coordinate;

public interface EquipoListener {
    /** Nueva posición del equipo en el mapa. */
    void onEquipoPosUpdate(Equipo equipo, Coordinate pos);

    /** Cambio de estado de misión (ida, atendiendo, regreso, etc.). */
    void onEquipoEstadoUpdate(Equipo equipo, EstadoMision estado);

    /** Rutas listas (polilíneas calculadas). Útil para dibujar la línea en el mapa si quieres. */
    void onEquipoRutasListas(Equipo equipo);
}
