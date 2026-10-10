package com.banquero;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BankerAlgorithm {
    private int numProcesos;
    private int numRecursos;
    private int[] disponible;
    private int[][] maximo;
    private int[][] asignacion;
    private int[][] necesidad;

    public BankerAlgorithm(int numProcesos, int numRecursos, int[] disponible, int[][] maximo, int[][] asignacion) {
        this.numProcesos = numProcesos;
        this.numRecursos = numRecursos;
        this.disponible = Arrays.copyOf(disponible, numRecursos);
        this.maximo = copiarMatriz(maximo);
        this.asignacion = copiarMatriz(asignacion);
        this.necesidad = new int[numProcesos][numRecursos];
        calcularNecesidad();
    }

    private void calcularNecesidad() {
        for (int i = 0; i < numProcesos; i++) {
            for (int j = 0; j < numRecursos; j++) {
                this.necesidad[i][j] = this.maximo[i][j] - this.asignacion[i][j];
            }
        }
    }

    public static class ResultadoSeguridad {
        public boolean esSeguro;
        public List<Integer> secuenciaSegura;
        public String log;

        public ResultadoSeguridad(boolean esSeguro, List<Integer> secuenciaSegura, String log) {
            this.esSeguro = esSeguro;
            this.secuenciaSegura = secuenciaSegura;
            this.log = log;
        }
    }

    public static class ResultadoSolicitud {
        public boolean concedido;
        public String mensaje;
        public ResultadoSeguridad resultadoSeguridad;

        public ResultadoSolicitud(boolean concedido, String mensaje, ResultadoSeguridad resultadoSeguridad) {
            this.concedido = concedido;
            this.mensaje = mensaje;
            this.resultadoSeguridad = resultadoSeguridad;
        }
    }

    public static class ResultadoInterbloqueo {
        public boolean hayInterbloqueo;
        public List<Integer> procesosInterbloqueados;
        public String log;

        public ResultadoInterbloqueo(boolean hayInterbloqueo, List<Integer> procesosInterbloqueados, String log) {
            this.hayInterbloqueo = hayInterbloqueo;
            this.procesosInterbloqueados = procesosInterbloqueados;
            this.log = log;
        }
    }

    public ResultadoSeguridad comprobarSeguridad() {
        StringBuilder log = new StringBuilder();
        log.append("=== ALGORITMO DE SEGURIDAD ===\n");

        int[] trabajo = Arrays.copyOf(disponible, numRecursos);
        boolean[] finalizado = new boolean[numProcesos];
        List<Integer> secuenciaSegura = new ArrayList<>();

        log.append("Vector Disponible inicial (Trabajo): ").append(Arrays.toString(trabajo)).append("\n\n");

        int contador = 0;
        while (contador < numProcesos) {
            boolean procesoEncontrado = false;

            for (int i = 0; i < numProcesos; i++) {
                if (!finalizado[i]) {
                    boolean puedeAsignar = true;
                    for (int j = 0; j < numRecursos; j++) {
                        if (necesidad[i][j] > trabajo[j]) {
                            puedeAsignar = false;
                            break;
                        }
                    }

                    if (puedeAsignar) {
                        log.append("[OK] Proceso P").append(i).append(" puede ejecutarse.\n");
                        log.append("     Necesidad[P").append(i).append("] = ").append(Arrays.toString(necesidad[i]))
                           .append(" <= Trabajo = ").append(Arrays.toString(trabajo)).append("\n");

                        for (int j = 0; j < numRecursos; j++) {
                            trabajo[j] += asignacion[i][j];
                        }
                        finalizado[i] = true;
                        secuenciaSegura.add(i);
                        procesoEncontrado = true;
                        contador++;

                        log.append("     Nuevo Trabajo tras liberar Asignacion[P").append(i).append("]: ")
                           .append(Arrays.toString(trabajo)).append("\n\n");
                        break;
                    }
                }
            }

            if (!procesoEncontrado) {
                break;
            }
        }

        boolean esSeguro = (secuenciaSegura.size() == numProcesos);
        if (esSeguro) {
            log.append("EL SISTEMA ESTA EN UN ESTADO SEGURO.\n");
            log.append("Secuencia segura de ejecucion: <");
            for (int i = 0; i < secuenciaSegura.size(); i++) {
                log.append("P").append(secuenciaSegura.get(i));
                if (i < secuenciaSegura.size() - 1) log.append(", ");
            }
            log.append(">\n");
        } else {
            log.append("EL SISTEMA NO ESTA EN UN ESTADO SEGURO (Posible Interbloqueo).\n");
            log.append("Procesos que no pudieron finalizar: ");
            for (int i = 0; i < numProcesos; i++) {
                if (!finalizado[i]) {
                    log.append("P").append(i).append(" ");
                }
            }
            log.append("\n");
        }

        return new ResultadoSeguridad(esSeguro, secuenciaSegura, log.toString());
    }

    public ResultadoSolicitud solicitarRecursos(int idProceso, int[] solicitud) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SOLICITUD DE RECURSOS PARA P").append(idProceso).append(" ===\n");
        sb.append("Vector de solicitud: ").append(Arrays.toString(solicitud)).append("\n");

        for (int j = 0; j < numRecursos; j++) {
            if (solicitud[j] > necesidad[idProceso][j]) {
                return new ResultadoSolicitud(false, 
                    "Error: El proceso P" + idProceso + " ha solicitado mas recursos de los declarados en su Necesidad maxima (" + 
                    Arrays.toString(necesidad[idProceso]) + ").", null);
            }
        }

        for (int j = 0; j < numRecursos; j++) {
            if (solicitud[j] > disponible[j]) {
                return new ResultadoSolicitud(false, 
                    "Espera: Recursos no disponibles. P" + idProceso + " debe esperar. Solicitud = " + 
                    Arrays.toString(solicitud) + " > Disponible = " + Arrays.toString(disponible), null);
            }
        }

        for (int j = 0; j < numRecursos; j++) {
            disponible[j] -= solicitud[j];
            asignacion[idProceso][j] += solicitud[j];
            necesidad[idProceso][j] -= solicitud[j];
        }

        ResultadoSeguridad seguridad = comprobarSeguridad();

        if (seguridad.esSeguro) {
            return new ResultadoSolicitud(true, 
                "Solicitud CONCEDIDA a P" + idProceso + ". El sistema permanece en un estado seguro.", seguridad);
        } else {
            for (int j = 0; j < numRecursos; j++) {
                disponible[j] += solicitud[j];
                asignacion[idProceso][j] -= solicitud[j];
                necesidad[idProceso][j] += solicitud[j];
            }
            return new ResultadoSolicitud(false, 
                "Solicitud RECHAZADA. Conceder la solicitud llevaria al sistema a un estado NO SEGURO. P" + idProceso + " debe esperar.", seguridad);
        }
    }

    public ResultadoInterbloqueo detectarInterbloqueo(int[][] matrizSolicitud) {
        StringBuilder log = new StringBuilder();
        log.append("=== ALGORITMO DE DETECCION DE INTERBLOQUEOS ===\n");

        int[][] sol = (matrizSolicitud != null) ? matrizSolicitud : this.necesidad;
        int[] trabajo = Arrays.copyOf(disponible, numRecursos);
        boolean[] finalizado = new boolean[numProcesos];

        for (int i = 0; i < numProcesos; i++) {
            boolean tieneAsignacion = false;
            for (int j = 0; j < numRecursos; j++) {
                if (asignacion[i][j] != 0) {
                    tieneAsignacion = true;
                    break;
                }
            }
            if (!tieneAsignacion) {
                finalizado[i] = true;
            }
        }

        log.append("Trabajo inicial: ").append(Arrays.toString(trabajo)).append("\n");

        while (true) {
            boolean encontrado = false;
            for (int i = 0; i < numProcesos; i++) {
                if (!finalizado[i]) {
                    boolean puedeSatisfacer = true;
                    for (int j = 0; j < numRecursos; j++) {
                        if (sol[i][j] > trabajo[j]) {
                            puedeSatisfacer = false;
                            break;
                        }
                    }

                    if (puedeSatisfacer) {
                        for (int j = 0; j < numRecursos; j++) {
                            trabajo[j] += asignacion[i][j];
                        }
                        finalizado[i] = true;
                        encontrado = true;
                        log.append("[OK] P").append(i).append(" satisface sus solicitudes/necesidad y libera recursos. Nuevo Trabajo: ")
                           .append(Arrays.toString(trabajo)).append("\n");
                    }
                }
            }
            if (!encontrado) break;
        }

        List<Integer> interbloqueados = new ArrayList<>();
        for (int i = 0; i < numProcesos; i++) {
            if (!finalizado[i]) {
                interbloqueados.add(i);
            }
        }

        boolean hayInterbloqueo = !interbloqueados.isEmpty();
        if (hayInterbloqueo) {
            log.append("\nATENCION: DETECTADO INTERBLOQUEO (DEADLOCK)!\n");
            log.append("Los siguientes procesos estan interbloqueados: ");
            for (int p : interbloqueados) {
                log.append("P").append(p).append(" ");
            }
            log.append("\n");
        } else {
            log.append("\nNO SE DETECTARON INTERBLOQUEOS. Todos los procesos pueden finalizar correctamente.\n");
        }

        return new ResultadoInterbloqueo(hayInterbloqueo, interbloqueados, log.toString());
    }

    public int getNumProcesos() { return numProcesos; }
    public int getNumRecursos() { return numRecursos; }
    public int[] getDisponible() { return Arrays.copyOf(disponible, numRecursos); }
    public int[][] getMaximo() { return copiarMatriz(maximo); }
    public int[][] getAsignacion() { return copiarMatriz(asignacion); }
    public int[][] getNecesidad() { return copiarMatriz(necesidad); }

    public void setDisponible(int[] nuevoDisponible) {
        this.disponible = Arrays.copyOf(nuevoDisponible, numRecursos);
    }

    public void setValoresMatriz(int[][] nuevoMaximo, int[][] nuevaAsignacion) {
        this.maximo = copiarMatriz(nuevoMaximo);
        this.asignacion = copiarMatriz(nuevaAsignacion);
        calcularNecesidad();
    }

    private static int[][] copiarMatriz(int[][] matriz) {
        int[][] copia = new int[matriz.length][];
        for (int i = 0; i < matriz.length; i++) {
            copia[i] = Arrays.copyOf(matriz[i], matriz[i].length);
        }
        return copia;
    }
}
