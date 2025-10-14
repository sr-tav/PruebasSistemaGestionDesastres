package com.example.pruebassistemadesastres.model;

import java.util.ArrayList;
import java.util.List;

public class SistemaGestionDesastres {
    private static SistemaGestionDesastres instancia;
    private final GrafoDirigido grafo = new GrafoDirigido();
    private final List<Zona> zonas = new ArrayList<>();
    private final List<Recurso> recursos = new ArrayList<>();
    private final List<Equipo> equipos = new ArrayList<>();
    private final List<Rescatista> rescatistas = new ArrayList<>();
    private final List<OperadorEmergencia> operadores = new ArrayList<>();
    private final List<Admin> administradores = new ArrayList<>();
    private final List<Ruta> rutas = new ArrayList<>();
    private final List<Evacuacion> evacuaciones = new ArrayList<>();


    private SistemaGestionDesastres() {}


    public static SistemaGestionDesastres getInstancia() {
        if (instancia == null) instancia = new SistemaGestionDesastres();
        return instancia;
    }


    // --- getters ---
    public GrafoDirigido getGrafo() { return grafo; }
    public List<Zona> getZonas() { return zonas; }
    public List<Recurso> getRecursos() { return recursos; }
    public List<Equipo> getEquipos() { return equipos; }
    public List<Rescatista> getRescatistas() { return rescatistas; }
    public List<OperadorEmergencia> getOperadores() { return operadores; }
    public List<Admin> getAdministradores() { return administradores; }
    public List<Ruta> getRutas() { return rutas; }
    public List<Evacuacion> getEvacuaciones() { return evacuaciones; }


    public void agregarZona(Zona z) { zonas.add(z); grafo.agregarZona(z); }
    public void agregarRuta(Ruta r) { rutas.add(r); }
    public void agregarRecurso(Recurso r) { recursos.add(r); }
    public void agregarEquipo(Equipo e) { equipos.add(e); }
    public void agregarRescatista(Rescatista r) { rescatistas.add(r); }
    public void agregarOperador(OperadorEmergencia o) { operadores.add(o); }
    public void agregarAdmin(Admin a) { administradores.add(a); }
    public void agregarEvacuacion(Evacuacion e) { evacuaciones.add(e); }

    /**
     * Metodo para crear un sistema de gestion con datos quemados
     * @return
     */
    public static SistemaGestionDesastres cargarDatosQuemados() {
        SistemaGestionDesastres s = SistemaGestionDesastres.getInstancia();


        Municipio calarca = new Municipio("Calarcá", 75000);
        Municipio armenia = new Municipio("Armenia", 295000);


        Zona zCiudad = new Zona("Ciudad Calarcá", TipoZona.CIUDAD, calarca, 50000, 4);
        Zona zRefugio = new Zona("Refugio La María", TipoZona.REFUGIO, calarca, 1200, 2);
        Zona zCentro = new Zona("Centro de Ayuda Caficultor", TipoZona.CENTRO_AYUDA, armenia, 0, 1);
        Zona zHospital = new Zona("Hospital San José", TipoZona.CENTRO_AYUDA, calarca, 0, 1);


        s.agregarZona(zCiudad); s.agregarZona(zRefugio); s.agregarZona(zCentro); s.agregarZona(zHospital);


        zCiudad.getInventario().put(TipoRecurso.AGUA, 6000);
        zCiudad.getInventario().put(TipoRecurso.ALIMENTO, 4000);
        zRefugio.getInventario().put(TipoRecurso.ALIMENTO, 1500);
        zRefugio.getInventario().put(TipoRecurso.MEDICINA, 300);


        Rescatista r1 = new Rescatista("Ana Gómez",  TipoEquipo.MEDICO);
        Rescatista r2 = new Rescatista("Luis Patiño",  TipoEquipo.BOMBERO);
        Rescatista r3 = new Rescatista("Sara Naranjo",  TipoEquipo.LOGISTICA);
        s.agregarRescatista(r1); s.agregarRescatista(r2); s.agregarRescatista(r3);
        s.getZonas().get(0).getRescatistas().add(r2);
        s.getZonas().get(3).getRescatistas().add(r1);
        s.getZonas().get(1).getRescatistas().add(r3);


        Equipo e1 = new Equipo( TipoEquipo.MEDICO, EstadoEquipo.DISPONIBLE);
        Equipo e2 = new Equipo( TipoEquipo.BOMBERO, EstadoEquipo.DISPONIBLE);
        Equipo e3 = new Equipo( TipoEquipo.LOGISTICA, EstadoEquipo.MANTENIMIENTO);
        s.agregarEquipo(e1); s.agregarEquipo(e2); s.agregarEquipo(e3);
        s.getZonas().get(3).getEquipos().add(e1);
        s.getZonas().get(0).getEquipos().add(e2);
        s.getZonas().get(1).getEquipos().add(e3);


        s.agregarRecurso(new Recurso("R-AG-01", TipoRecurso.AGUA, 2000, EstadoRecurso.DISPONIBLE));
        s.agregarRecurso(new Recurso("R-AL-01", TipoRecurso.ALIMENTO, 3500, EstadoRecurso.EN_RUTA));
        s.agregarRecurso(new Recurso("R-ME-01", TipoRecurso.MEDICINA, 600, EstadoRecurso.DISPONIBLE));


        s.getGrafo().conectar(zCiudad, zHospital, 4.2, true, 10);
        s.getGrafo().conectar(zCiudad, zRefugio, 7.8, false, 7);
        s.getGrafo().conectar(zHospital, zRefugio, 5.0, true, 8);
        s.getGrafo().conectar(zCentro, zCiudad, 18.2, true, 15);
        s.getGrafo().conectar(zCentro, zHospital, 16.0, true, 15);


        Ruta rEv1 = new Ruta("Ruta Evacuación 1");
        rEv1.agregarParada(zCiudad);
        rEv1.agregarParada(zHospital);
        rEv1.agregarParada(zRefugio);
        rEv1.setDistanciaTotalKm(9.2);
        rEv1.setDuracionHoras(0.45);
        s.agregarRuta(rEv1);


        Evacuacion ev = new Evacuacion(zCiudad, zRefugio, 220);
        ev.setRuta(rEv1);
        ev.setTiempoEstimadoHoras(0.5);
        s.agregarEvacuacion(ev);


        s.agregarOperador(new OperadorEmergencia("Operador 1", "OP-01"));
        s.agregarOperador(new OperadorEmergencia("Operador 2", "OP-02"));
        s.agregarAdmin(new Admin("Admin SGDN", "AD-01"));
        return s;
    }
}
