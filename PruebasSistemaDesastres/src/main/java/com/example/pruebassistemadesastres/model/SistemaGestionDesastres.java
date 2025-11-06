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
        Municipio tebaida = new Municipio("La Tebaida", 35189);
        Municipio circasia = new Municipio("Circasia", 29789);
        Municipio salento = new Municipio("Salento", 9846);
        Municipio filandia = new Municipio("Filandia", 13412);
        Municipio cordoba = new  Municipio("Cordoba", 5954);
        Municipio buenavista = new  Municipio("Buenavista", 3527);
        Municipio pijao = new Municipio("Pijao", 5960);
        Municipio genova = new Municipio("Genova", 7809);




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

        //ZONA TEBAIDA
        Zona zCentroTebaida = new Zona(
                "Centro La Tebaida", TipoZona.CIUDAD, tebaida, 35000, 4,
                4.452408340649018, -75.78860194545679
        );

        Zona zRefugioTebaida = new Zona(
                "Coliso de la Tebaida", TipoZona.REFUGIO, tebaida, 1000, 2,
                4.4564173065870705, -75.78937640228851
        );

        Zona zHospitalTebaida = new Zona(
                "Hospital Pio X", TipoZona.CENTRO_AYUDA, tebaida, 0, 1,
                4.451956501660902, -75.78222867713673
        );

        Zona zBomberosTebaida = new Zona(
                "Cuerpo de Bomberos La Tebaida", TipoZona.CENTRO_AYUDA, tebaida, 0, 2,
                4.451953114093611, -75.78381194607292
        );

        Zona zColegioTebaida = new Zona(
                "Instituto Tebaida", TipoZona.REFUGIO, tebaida, 800, 3,
                4.452524336390497, -75.79143645083546
        );

        Zona zParqueTebaida = new Zona(
                "Parque Principal La Tebaida", TipoZona.REFUGIO, tebaida, 500, 1,
                4.453779866416768, -75.78929191963513
        );


        s.agregarZona(zCentroTebaida);
        s.agregarZona(zRefugioTebaida);
        s.agregarZona(zHospitalTebaida);
        s.agregarZona(zBomberosTebaida);
        s.agregarZona(zColegioTebaida);
        s.agregarZona(zParqueTebaida);


        s.getGrafo().conectar(zCentroTebaida, zHospitalTebaida, 0.8, true, 10);
        s.getGrafo().conectar(zCentroTebaida, zRefugioTebaida, 1.0, false, 8);
        s.getGrafo().conectar(zHospitalTebaida, zBomberosTebaida, 0.6, true, 9);
        s.getGrafo().conectar(zBomberosTebaida, zColegioTebaida, 1.5, true, 7);
        s.getGrafo().conectar(zColegioTebaida, zParqueTebaida, 1.2, true, 6);
        s.getGrafo().conectar(zParqueTebaida, zCentroTebaida, 1.0, true, 8);

        //Zona Circasia
        Zona zCentroCircasia = new Zona("Centro Circasia", TipoZona.CIUDAD, circasia, 29789, 4, 4.61980613315615, -75.63619390377012
        );

        Zona zRefugioCircasia = new Zona("Coliseo de Circasia", TipoZona.REFUGIO, circasia, 800, 2, 4.621861915311156, -75.63732955597916
        );

        Zona zHospitalCircasia = new Zona("Hospital San Vicente de Paul", TipoZona.CENTRO_AYUDA, circasia, 0, 1,
                4.618896178627092, -75.63732879091812
        );

        Zona zBomberosCircasia = new Zona(
                "Cuerpo de Bomberos Circasia", TipoZona.CENTRO_AYUDA, circasia, 0, 2,
                4.616888201366161, -75.63478486236014
        );

        Zona zColegioCircasia = new Zona(
                "Institución Educativa Luis Eduardo Calvo", TipoZona.REFUGIO, circasia, 700, 3,
                4.621956943477734, -75.63466958453377
        );

        Zona zParqueCircasia = new Zona(
                "Parque Principal Circasia", TipoZona.REFUGIO, circasia, 400, 1,
                4.62038120976319, -75.63439016559786
        );

        s.agregarZona(zCentroCircasia);
        s.agregarZona(zRefugioCircasia);
        s.agregarZona(zHospitalCircasia);
        s.agregarZona(zBomberosCircasia);
        s.agregarZona(zColegioCircasia);
        s.agregarZona(zParqueCircasia);


        s.getGrafo().conectar(zCentroCircasia, zHospitalCircasia, 0.7, true, 10);
        s.getGrafo().conectar(zCentroCircasia, zRefugioCircasia, 1.0, false, 8);
        s.getGrafo().conectar(zHospitalCircasia, zBomberosCircasia, 0.6, true, 9);
        s.getGrafo().conectar(zBomberosCircasia, zColegioCircasia, 1.2, true, 7);
        s.getGrafo().conectar(zColegioCircasia, zParqueCircasia, 1.0, true, 6);
        s.getGrafo().conectar(zParqueCircasia, zCentroCircasia, 0.9, true, 8);

        //ZONA SALENTO
        Zona zCentroSalento = new Zona(
                "Centro Salento", TipoZona.CIUDAD, salento, 9846, 4,
                4.638256988779316, -75.568815222479029);

        Zona zRefugioSalento = new Zona(
                "Coliseo Salento", TipoZona.REFUGIO, salento, 500, 2,
                4.638558329785101, -75.56981064289566
        );

        Zona zHospitalSalento = new Zona(
                "Hospital San Vicente de Paul", TipoZona.CENTRO_AYUDA, salento, 0, 1,
                4.6364403790107085, -75.57066734512179
        );

        Zona zBomberosSalento = new Zona(
                "Cuerpo de Bomberos Salento", TipoZona.CENTRO_AYUDA, salento, 0, 2,
                4.640112805952863, -75.57190294782289
        );

        Zona zColegioSalento = new Zona(
                "Liceo Quindio", TipoZona.REFUGIO, salento, 400, 3,
                4.640166566765212, -75.57068460593389
        );

        Zona zParqueSalento = new Zona(
                "Parque Principal Salento", TipoZona.REFUGIO, salento, 300, 1,
                4.637204737655874, -75.56978060697949
        );

        s.agregarZona(zCentroSalento);
        s.agregarZona(zRefugioSalento);
        s.agregarZona(zHospitalSalento);
        s.agregarZona(zBomberosSalento);
        s.agregarZona(zColegioSalento);
        s.agregarZona(zParqueSalento);

        s.getGrafo().conectar(zCentroSalento, zHospitalSalento, 0.5, true, 10);
        s.getGrafo().conectar(zCentroSalento, zRefugioSalento, 0.8, false, 8);
        s.getGrafo().conectar(zHospitalSalento, zBomberosSalento, 0.4, true, 9);
        s.getGrafo().conectar(zBomberosSalento, zColegioSalento, 0.7, true, 7);
        s.getGrafo().conectar(zColegioSalento, zParqueSalento, 0.6, true, 6);
        s.getGrafo().conectar(zParqueSalento, zCentroSalento, 0.5, true, 8);

        // ZONA FILANDIA

        Zona zCentroFilandia = new Zona("Centro Filandia", TipoZona.CIUDAD, filandia, 13412, 4, 4.673635116175152, -75.65704846099914
        );

        Zona zRefugioFilandia = new Zona("Casa de la Cultura", TipoZona.REFUGIO, filandia, 800, 2, 4.674598032282073, -75.65774126406795
        );

        Zona zHospitalFilandia = new Zona("Hospital San Vicente de Paul-Filandia", TipoZona.CENTRO_AYUDA, filandia, 0, 1, 4.672400814631654, -75.6617514229001
        );

        Zona zBomberosFilandia = new Zona("Cuerpo de Bomberos Voluntarios Filandia", TipoZona.CENTRO_AYUDA, filandia, 0, 2, 4.671171669583376, -75.65755880634279
        );

        Zona zColegioFilandia = new Zona("Institución Educativa Liceo Andino de la Santísima Trinidad", TipoZona.REFUGIO, filandia, 700, 3, 4.672705421145436, -75.65584122252575
        );

        Zona zParqueFilandia = new Zona("Parque Principal de Filandia", TipoZona.REFUGIO, filandia, 400, 1, 4.674794981083568, -75.65815929931857
        );

        s.agregarZona(zCentroFilandia);
        s.agregarZona(zRefugioFilandia);
        s.agregarZona(zHospitalFilandia);
        s.agregarZona(zBomberosFilandia);
        s.agregarZona(zColegioFilandia);
        s.agregarZona(zParqueFilandia);

        s.getGrafo().conectar(zCentroFilandia, zHospitalFilandia, 0.6, true, 10);
        s.getGrafo().conectar(zCentroFilandia, zRefugioFilandia, 0.9, false, 8);
        s.getGrafo().conectar(zHospitalFilandia, zBomberosFilandia, 0.4, true, 9);
        s.getGrafo().conectar(zBomberosFilandia, zColegioFilandia, 0.8, true, 7);
        s.getGrafo().conectar(zColegioFilandia, zParqueFilandia, 0.7, true, 6);
        s.getGrafo().conectar(zParqueFilandia, zCentroFilandia, 0.5, true, 8);

        //ZONA CORDOBA
        Zona zCentroCordoba = new Zona(
                "Centro Córdoba", TipoZona.CIUDAD, cordoba, 5954, 4,4.391107579325129, -75.68630075761499
        );

        Zona zRefugioCordoba = new Zona(
                "Casa de la cultura Cordoba", TipoZona.REFUGIO, cordoba, 800, 2, 4.390068762643359, -75.68448328526303
        );

        Zona zHospitalCordoba = new Zona(
                "Hospital San Roque Córdoba", TipoZona.CENTRO_AYUDA, cordoba, 0, 1, 4.389874996376682, -75.68706653501403
        );

        Zona zBomberosCordoba = new Zona(
                "Cuerpo de Bomberos Voluntarios de Córdoba", TipoZona.CENTRO_AYUDA, cordoba, 0, 2, 4.39017513963827, -75.68603840915831
        );

        Zona zColegioCordoba = new Zona(
                "Institucion Educativa Jose Maria Cordoba", TipoZona.REFUGIO, cordoba, 600, 3, 4.3925509839196035, -75.6872934261599
        );

        Zona zParqueCordoba = new Zona(
                "Parque Principal Córdoba", TipoZona.REFUGIO, cordoba, 500, 1, 4.390228386542014, -75.68550731757038
        );


        s.agregarZona(zCentroCordoba);
        s.agregarZona(zRefugioCordoba);
        s.agregarZona(zHospitalCordoba);
        s.agregarZona(zBomberosCordoba);
        s.agregarZona(zColegioCordoba);
        s.agregarZona(zParqueCordoba);

        s.getGrafo().conectar(zCentroCordoba, zHospitalCordoba, 0.6, true, 9);
        s.getGrafo().conectar(zCentroCordoba, zRefugioCordoba, 0.8, false, 8);
        s.getGrafo().conectar(zHospitalCordoba, zBomberosCordoba, 0.5, true, 9);
        s.getGrafo().conectar(zBomberosCordoba, zColegioCordoba, 1.0, true, 7);
        s.getGrafo().conectar(zColegioCordoba, zParqueCordoba, 0.9, true, 6);
        s.getGrafo().conectar(zParqueCordoba, zCentroCordoba, 0.7, true, 8);

        //ZONA BUENAVISTA

        Zona zCentroBuenavista = new Zona(
                "Centro Buenavista", TipoZona.CIUDAD, buenavista, 3527, 4,
                4.360224731266342, -75.73876879303484
        );

        Zona zRefugioBuenavista = new Zona(
                "Coliseo de Bunavusta", TipoZona.REFUGIO, buenavista, 600, 2,
                4.361253146429546, -75.74146897528607
        );

        Zona zHospitalBuenavista = new Zona("Hospital San Camilo", TipoZona.CENTRO_AYUDA, buenavista, 0, 1, 4.360307129936257, -75.73977593707554
        );

        Zona zBomberosBuenavista = new Zona("Cuerpo de Bomberos Voluntarios de Buenavista", TipoZona.CENTRO_AYUDA, buenavista, 0, 2, 4.360470894137083, -75.73995404799814
        );

        Zona zColegioBuenavista = new Zona("Instituto Buenavista", TipoZona.REFUGIO, buenavista, 500, 3, 4.360955811592284, -75.74133081224039
        );

        Zona zParqueBuenavista = new Zona("Parque Principal Buenavista", TipoZona.REFUGIO, buenavista, 400, 1, 4.359470162222283, -75.73819903261159
        );


        s.agregarZona(zCentroBuenavista);
        s.agregarZona(zRefugioBuenavista);
        s.agregarZona(zHospitalBuenavista);
        s.agregarZona(zBomberosBuenavista);
        s.agregarZona(zColegioBuenavista);
        s.agregarZona(zParqueBuenavista);

        s.getGrafo().conectar(zCentroBuenavista, zHospitalBuenavista, 0.6, true, 9);
        s.getGrafo().conectar(zCentroBuenavista, zRefugioBuenavista, 0.9, false, 8);
        s.getGrafo().conectar(zHospitalBuenavista, zBomberosBuenavista, 0.5, true, 9);
        s.getGrafo().conectar(zBomberosBuenavista, zColegioBuenavista, 1.0, true, 7);
        s.getGrafo().conectar(zColegioBuenavista, zParqueBuenavista, 0.8, true, 6);
        s.getGrafo().conectar(zParqueBuenavista, zCentroBuenavista, 0.7, true, 8);

        // ZONA PIJAO

        Zona zCentroPijao = new Zona("Centro Pijao", TipoZona.CIUDAD, pijao, 5960, 4, 4.333440416778879, -75.7038804115483
        );

        Zona zRefugioPijao = new Zona("Coliseo de Pijao", TipoZona.REFUGIO, pijao, 700, 2, 4.328410280401068, -75.7061594095711
        );

        Zona zHospitalPijao = new Zona("Hospital Santa Ana de Pijao", TipoZona.CENTRO_AYUDA, pijao, 0, 1, 4.332917697831475, -75.7058323105764
        );

        Zona zBomberosPijao = new Zona("Cuerpo de Bomberos Voluntarios de Pijao", TipoZona.CENTRO_AYUDA, pijao, 0, 2, 4.335797010901282, -75.70326112457927
        );

        Zona zColegioPijao = new Zona("Institución Educativa Santa Teresita", TipoZona.REFUGIO, pijao, 600, 3, 4.335248035567887, -75.7037961440746
        );

        Zona zParquePijao = new Zona("Parque Principal de Pijao", TipoZona.REFUGIO, pijao, 400, 1, 4.334114545311118, -75.7040323782889
        );


        s.agregarZona(zCentroPijao);
        s.agregarZona(zRefugioPijao);
        s.agregarZona(zHospitalPijao);
        s.agregarZona(zBomberosPijao);
        s.agregarZona(zColegioPijao);
        s.agregarZona(zParquePijao);


        s.getGrafo().conectar(zCentroPijao, zHospitalPijao, 0.6, true, 10);
        s.getGrafo().conectar(zCentroPijao, zRefugioPijao, 1.0, false, 8);
        s.getGrafo().conectar(zHospitalPijao, zBomberosPijao, 0.5, true, 9);
        s.getGrafo().conectar(zBomberosPijao, zColegioPijao, 1.2, true, 7);
        s.getGrafo().conectar(zColegioPijao, zParquePijao, 0.9, true, 6);
        s.getGrafo().conectar(zParquePijao, zCentroPijao, 0.7, true, 8);


        Zona zCentroGenova = new Zona("Centro Génova", TipoZona.CIUDAD, genova, 7809, 4, 4.207780804095296, -75.7893081565009
        );

        Zona zRefugioGenova = new Zona(" Instituto Genova", TipoZona.REFUGIO, genova, 1000, 2, 4.204287386927032, -75.79380665257369
        );

        Zona zHospitalGenova = new Zona("Hospital de Genova", TipoZona.CENTRO_AYUDA, genova, 0, 1, 4.209468941112951, -75.78739526463603
        );

        Zona zBomberosGenova = new Zona("Cuerpo de Bomberos Génova", TipoZona.CENTRO_AYUDA, genova, 0, 2, 4.2066234765622, -75.7912364530711
        );

        Zona zColegioGenova = new Zona("Colegio San vicente de Paul-Genova", TipoZona.REFUGIO, genova, 800, 3, 4.207359599878561, -75.7906491523019
        );

        Zona zParqueGenova = new Zona("Parque Principal Génova", TipoZona.REFUGIO, genova, 500, 1, 4.206389114094996, -75.7905448502346
        );


        s.agregarZona(zCentroGenova);
        s.agregarZona(zRefugioGenova);
        s.agregarZona(zHospitalGenova);
        s.agregarZona(zBomberosGenova);
        s.agregarZona(zColegioGenova);
        s.agregarZona(zParqueGenova);


        s.getGrafo().conectar(zCentroGenova, zHospitalGenova, 0.8, true, 10);
        s.getGrafo().conectar(zCentroGenova, zRefugioGenova, 1.0, false, 8);
        s.getGrafo().conectar(zHospitalGenova, zBomberosGenova, 0.6, true, 9);
        s.getGrafo().conectar(zBomberosGenova, zColegioGenova, 1.5, true, 7);
        s.getGrafo().conectar(zColegioGenova, zParqueGenova, 1.2, true, 6);
        s.getGrafo().conectar(zParqueGenova, zCentroGenova, 1.0, true, 8);







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
