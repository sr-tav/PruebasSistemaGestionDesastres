package com.example.pruebassistemadesastres.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class SistemaGestionDesastres {

    //Listas principales del sistema
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

    //Obtener la instancia unica del sistema
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

    //Metodos para agregar entidades
    public void agregarZona(Zona z) { zonas.add(z); grafo.agregarZona(z); }
    public void agregarRuta(Ruta r) { rutas.add(r); }
    public void agregarRecurso(Recurso r) { recursos.add(r); }
    public void agregarEquipo(Equipo e) { equipos.add(e); }
    public void agregarRescatista(Rescatista r) { rescatistas.add(r); }
    public void agregarOperador(OperadorEmergencia o) { operadores.add(o); }
    public void agregarAdmin(Admin a) { administradores.add(a); }
    public void agregarEvacuacion(Evacuacion e) { evacuaciones.add(e); }

    //APARTADO REGISTRO Y AUTENTIFICACION
    public boolean registrarAdmin(String nombre, String clave) {
        for (Admin a : administradores) {
            if (a.getNombre().equalsIgnoreCase(nombre)) {
                return false;
            }
        }
        administradores.add(new Admin(nombre, clave));
        return true;
    }

    public boolean registrarOperador(String nombre, String clave) {
        for (OperadorEmergencia o : operadores) {
            if (o.getNombre().equalsIgnoreCase(nombre)) {
                return false;
            }
        }
        operadores.add(new OperadorEmergencia(nombre, clave));
        return true;
    }

    public String autenticar(String nombre, String clave) {
        System.out.println(nombre);
        for (Admin a : administradores) {
            System.out.println(a.getId());
            if (a.getNombre().equalsIgnoreCase(nombre)) {
                return "ADMIN";
            }
        }
        for (OperadorEmergencia o : operadores) {
            if (o.getNombre().equalsIgnoreCase(nombre)) {
                return "OPERADOR";
            }
        }
        return "NO_EXISTE";
    }

    //APARTADO ASIGNACION DE RECURSOS
    public boolean asignarRecursoAZona(TipoRecurso tipo, int cantidad, Zona zona) {
        int totalDisponible = 0;
        for (Recurso r : recursos) {
            if (r.getTipo() == tipo && r.getEstado() == EstadoRecurso.DISPONIBLE) {
                totalDisponible += r.getCantidad();
            }
        }

        if (totalDisponible < cantidad) {
            return false;
        }

        zona.getInventario().put(tipo, zona.getInventario().getOrDefault(tipo, 0) + cantidad);

        // Reducimos de los recursos disponibles
        int restante = cantidad;
        for (Recurso r : recursos) {
            if (r.getTipo() == tipo && r.getEstado() == EstadoRecurso.DISPONIBLE) {
                if (r.getCantidad() >= restante) {
                    r.setCantidad(r.getCantidad() - restante);
                    if (r.getCantidad() == 0) r.setEstado(EstadoRecurso.ASIGNADO);
                    break;
                } else {
                    restante -= r.getCantidad();
                    r.setCantidad(0);
                    r.setEstado(EstadoRecurso.ASIGNADO);
                }
            }
        }

        return true;
    }

    public void mostrarInventarioZona(Zona z) {
        System.out.println("Inventario de la zona: " + z.getNombre());
        for (TipoRecurso tipo : z.getInventario().keySet()) {
            System.out.println("- " + tipo + ": " + z.getInventario().get(tipo));
        }
    }

    /**
     * Metodo para crear un sistema de gestion con datos quemados
     * @return
     */
    public static SistemaGestionDesastres cargarDatosQuemados() {
        SistemaGestionDesastres s = SistemaGestionDesastres.getInstancia();


        Municipio calarca = new Municipio("Calarcá", 75000);
        Municipio armenia = new Municipio("Armenia", 295000);
        Municipio quimbaya = new Municipio("Quimbaya", 40000);
        Municipio montenegro = new Municipio("Montenegro", 35000);


        Zona zCiudad = new Zona("Ciudad Calarcá", TipoZona.CIUDAD, calarca, 50000, 4, 4.533338, -75.640813);
        Zona zRefugio = new Zona("Refugio La María", TipoZona.REFUGIO, calarca, 1200, 2, 4.533338, -75.640813);
        Zona zCentro = new Zona("Centro de Ayuda Caficultor", TipoZona.CENTRO_AYUDA, armenia, 0, 1,4.533338, -75.640813);
        Zona zHospital = new Zona("Hospital San José", TipoZona.CENTRO_AYUDA, calarca, 0, 1, 4.533338, -75.640813);
        s.agregarZona(zCiudad); s.agregarZona(zRefugio); s.agregarZona(zCentro); s.agregarZona(zHospital);

        //ZONA CALARCA
        Zona zRefugioCal = new Zona("Refugio La María Cal", TipoZona.REFUGIO, calarca, 1200, 2,
                4.5290, -75.6430);
        Zona zCentroCal = new Zona("Centro de Ayuda Caficultor Cal", TipoZona.CENTRO_AYUDA, armenia, 0, 1,
                4.533338, -75.640813);
        Zona zHospitalCal = new Zona("Hospital La Misericordia Cal", TipoZona.CENTRO_AYUDA, calarca, 0, 1,
                4.53331, -75.64087);
        Zona zBomberos = new Zona("Cuerpo de Bomberos Calarcá", TipoZona.CENTRO_AYUDA, calarca, 0, 2,
                4.52905, -75.63691);
        Zona zColegio = new Zona("Institución Educativa San Bernardo Cal", TipoZona.REFUGIO, calarca, 800, 3,
                4.5265, -75.6470);
        Zona zParque = new Zona("Parque Fundadores Cal", TipoZona.REFUGIO, calarca, 300, 1,
                4.522715154365355, -75.64573160595077);

        s.agregarZona(zRefugioCal);
        s.agregarZona(zCentroCal);
        s.agregarZona(zHospitalCal);
        s.agregarZona(zBomberos);
        s.agregarZona(zColegio);
        s.agregarZona(zParque);

        //Grafo
        s.getGrafo().conectar(zCiudad, zHospitalCal, 1.2, true, 10);
        s.getGrafo().conectar(zCiudad, zRefugioCal, 1.1, false, 8);
        s.getGrafo().conectar(zHospitalCal, zRefugioCal, 0.9, true, 9);
        s.getGrafo().conectar(zCiudad, zBomberos, 1.4, true, 10);
        s.getGrafo().conectar(zBomberos, zColegio, 2.0, true, 8);
        s.getGrafo().conectar(zColegio, zParque, 1.8, true, 7);
        s.getGrafo().conectar(zParque, zCentroCal, 2.5, true, 12);

        //ZONA ARMENIA
        Zona zCentroArmenia = new Zona("Centro Armenia", TipoZona.CIUDAD, armenia, 100000, 4, 4.5386, -75.6755);
        Zona zRefugioArmenia = new Zona("Refugio La Floresta", TipoZona.REFUGIO, armenia, 2000, 2, 4.5460, -75.6820);
        Zona zHospitalArmenia = new Zona("Hospital San Roque", TipoZona.CENTRO_AYUDA, armenia, 0, 1, 4.5340, -75.6770);
        Zona zCentroAyudaArmenia = new Zona("Centro de Ayuda Caficultor", TipoZona.CENTRO_AYUDA, armenia, 0, 1, 4.5400, -75.6800);
        Zona zParqueArmenia = new Zona("Parque Sucre", TipoZona.REFUGIO, armenia, 500, 2, 4.5390, -75.6765);

        // Añadir zonas al sistema
        s.agregarZona(zCentroArmenia);
        s.agregarZona(zRefugioArmenia);
        s.agregarZona(zHospitalArmenia);
        s.agregarZona(zCentroAyudaArmenia);
        s.agregarZona(zParqueArmenia);

        //GRAFO
        s.getGrafo().conectar(zCentroArmenia, zHospitalArmenia, 3.2, true, 10);
        s.getGrafo().conectar(zCentroArmenia, zRefugioArmenia, 5.0, true, 8);
        s.getGrafo().conectar(zHospitalArmenia, zRefugioArmenia, 2.5, true, 6);
        s.getGrafo().conectar(zCentroAyudaArmenia, zCentroArmenia, 4.5, true, 12);
        s.getGrafo().conectar(zCentroAyudaArmenia, zHospitalArmenia, 3.8, true, 9);
        s.getGrafo().conectar(zParqueArmenia, zCentroArmenia, 1.2, true, 5);
        s.getGrafo().conectar(zParqueArmenia, zHospitalArmenia, 2.0, true, 6);

        //ZONA QUIMBAYA
        Zona zCentroQuimbaya = new Zona("Centro Quimbaya", TipoZona.CIUDAD, quimbaya, 40000, 4, 4.622674012500528, -75.76189897952767);

        Zona zRefugioQuimbaya = new Zona("Refugio Instituto Quimbaya", TipoZona.REFUGIO, quimbaya, 1000, 2, 4.628397608254127, -75.77039223011448
        );

        Zona zHospitalQuimbaya = new Zona("Hospital Sagrado Corazón de Jesús", TipoZona.CENTRO_AYUDA, quimbaya, 0, 1, 4.62096, -75.76543
        );

        Zona zBomberosQuimbaya = new Zona("Cuerpo de Bomberos Voluntarios de Quimbaya", TipoZona.CENTRO_AYUDA, quimbaya, 0, 2, 4.6225722628017625, -75.7600676834655
        );

        Zona zColegioQuimbaya = new Zona(
                "Colegio Policarpa Salavarrieta", TipoZona.REFUGIO, quimbaya, 800, 3, 4.624768503276307, -75.76277162725293
        );

        Zona zParqueQuimbaya = new Zona(
                "Parque Principal Quimbaya", TipoZona.REFUGIO, quimbaya, 500, 1, 4.622424053944915, -75.76307781644127
        );

        s.agregarZona(zCentroQuimbaya);
        s.agregarZona(zRefugioQuimbaya);
        s.agregarZona(zHospitalQuimbaya);
        s.agregarZona(zBomberosQuimbaya);
        s.agregarZona(zColegioQuimbaya);
        s.agregarZona(zParqueQuimbaya);


        s.getGrafo().conectar(zCentroQuimbaya, zHospitalQuimbaya, 0.8, true, 10);
        s.getGrafo().conectar(zCentroQuimbaya, zRefugioQuimbaya, 1.0, false, 8);
        s.getGrafo().conectar(zHospitalQuimbaya, zBomberosQuimbaya, 0.6, true, 9);
        s.getGrafo().conectar(zBomberosQuimbaya, zColegioQuimbaya, 1.5, true, 7);
        s.getGrafo().conectar(zColegioQuimbaya, zParqueQuimbaya, 1.2, true, 6);
        s.getGrafo().conectar(zParqueQuimbaya, zCentroQuimbaya, 1.0, true, 8);

        // ZONA MONTENEGRO
        Zona zCentroMontenegro = new Zona(
                "Centro Montenegro", TipoZona.CIUDAD, montenegro, 35000, 4,
                4.5665179383512005, -75.75180899079963
        );

        Zona zRefugioMontenegro = new Zona(
                "Coliseo de Montenegro", TipoZona.REFUGIO, montenegro, 1200, 2,
                4.566721163549409, -75.74893931982308
        );

        Zona zHospitalMontenegro = new Zona(
                "Hospital Roberto Quintero Villa", TipoZona.CENTRO_AYUDA, montenegro, 0, 1,
                4.558768824248389, -75.74864005860003
        );

        Zona zBomberosMontenegro = new Zona(
                "Cuerpo de Bomberos Montenegro", TipoZona.CENTRO_AYUDA, montenegro, 0, 2,
                4.5686514560365605, -75.74701369580801);

        Zona zColegioMontenegro = new Zona(
                "Institución Educativa General Santander", TipoZona.REFUGIO, montenegro, 800, 3,
                4.566563056316675, -75.75625569646814
        );

        Zona zParqueMontenegro = new Zona(
                "Parque Principal Montenegro", TipoZona.REFUGIO, montenegro, 500, 1,
                4.567190733256212, -75.750485370500279);

        s.agregarZona(zCentroMontenegro);
        s.agregarZona(zRefugioMontenegro);
        s.agregarZona(zHospitalMontenegro);
        s.agregarZona(zBomberosMontenegro);
        s.agregarZona(zColegioMontenegro);
        s.agregarZona(zParqueMontenegro);
        
        s.getGrafo().conectar(zCentroMontenegro, zHospitalMontenegro, 0.9, true, 10);
        s.getGrafo().conectar(zCentroMontenegro, zRefugioMontenegro, 1.2, false, 8);
        s.getGrafo().conectar(zHospitalMontenegro, zBomberosMontenegro, 0.7, true, 9);
        s.getGrafo().conectar(zBomberosMontenegro, zColegioMontenegro, 1.6, true, 7);
        s.getGrafo().conectar(zColegioMontenegro, zParqueMontenegro, 1.3, true, 6);
        s.getGrafo().conectar(zParqueMontenegro, zCentroMontenegro, 0.8, true, 8);


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


        s.agregarOperador(new OperadorEmergencia("Op1", "1234"));
        s.agregarOperador(new OperadorEmergencia("Op2", "1234"));
        s.agregarAdmin(new Admin("Admin","1234"));
        return s;
    }
}
