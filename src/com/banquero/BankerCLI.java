package com.banquero;

import java.util.Arrays;
import java.util.Scanner;

public class BankerCLI {

    public static void ejecutarCLI() {
        Scanner escaner = new Scanner(System.in);
        System.out.println("==========================================================");
        System.out.println("  SIMULADOR DEL ALGORITMO DEL BANQUERO Y DETECCION DE INTERBLOQUEOS");
        System.out.println("==========================================================");

        int numProcesos = 5;
        int numRecursos = 3;
        int[] disponible = {3, 3, 2};
        int[][] maximo = {
            {7, 5, 3},
            {3, 2, 2},
            {9, 0, 2},
            {2, 2, 2},
            {4, 3, 3}
        };
        int[][] asignacion = {
            {0, 1, 0},
            {2, 0, 0},
            {3, 0, 2},
            {2, 1, 1},
            {0, 0, 2}
        };

        BankerAlgorithm banquero = new BankerAlgorithm(numProcesos, numRecursos, disponible, maximo, asignacion);

        boolean ejecutando = true;
        while (ejecutando) {
            System.out.println("\n--- MENU PRINCIPAL ---");
            System.out.println("1. Mostrar Estado del Sistema (Disponible, Maximo, Asignacion, Necesidad)");
            System.out.println("2. Ejecutar Algoritmo de Seguridad (Safety)");
            System.out.println("3. Simular Solicitud de Recursos (Resource-Request)");
            System.out.println("4. Ejecutar Deteccion de Interbloqueos (Deadlock Detection)");
            System.out.println("5. Cargar Ejemplo Personalizado");
            System.out.println("6. Salir");
            System.out.print("Seleccione una opcion (1-6): ");

            String entrada = escaner.nextLine().trim();
            switch (entrada) {
                case "1":
                    mostrarEstado(banquero);
                    break;

                case "2":
                    BankerAlgorithm.ResultadoSeguridad seguridad = banquero.comprobarSeguridad();
                    System.out.println(seguridad.log);
                    break;

                case "3":
                    System.out.print("Ingrese ID del proceso (0 a " + (banquero.getNumProcesos() - 1) + "): ");
                    try {
                        int idProceso = Integer.parseInt(escaner.nextLine().trim());
                        if (idProceso < 0 || idProceso >= banquero.getNumProcesos()) {
                            System.out.println("ID de proceso invalido.");
                            break;
                        }
                        int[] sol = new int[banquero.getNumRecursos()];
                        System.out.println("Ingrese los recursos solicitados para los " + banquero.getNumRecursos() + " tipos (separados por espacio): ");
                        String[] partes = escaner.nextLine().trim().split("\\s+");
                        if (partes.length != banquero.getNumRecursos()) {
                            System.out.println("Cantidad de recursos no coincide.");
                            break;
                        }
                        for (int r = 0; r < banquero.getNumRecursos(); r++) {
                            sol[r] = Integer.parseInt(partes[r]);
                        }
                        BankerAlgorithm.ResultadoSolicitud resSolicitud = banquero.solicitarRecursos(idProceso, sol);
                        System.out.println("\n" + resSolicitud.mensaje);
                        if (resSolicitud.resultadoSeguridad != null) {
                            System.out.println(resSolicitud.resultadoSeguridad.log);
                        }
                    } catch (Exception e) {
                        System.out.println("Entrada no valida: " + e.getMessage());
                    }
                    break;

                case "4":
                    BankerAlgorithm.ResultadoInterbloqueo interbloqueo = banquero.detectarInterbloqueo(null);
                    System.out.println(interbloqueo.log);
                    break;

                case "5":
                    banquero = ingresarDatosPersonalizados(escaner);
                    break;

                case "6":
                    ejecutando = false;
                    System.out.println("Saliendo de la simulacion CLI!");
                    break;

                default:
                    System.out.println("Opcion invalida. Intente de nuevo.");
            }
        }
    }

    public static void mostrarEstado(BankerAlgorithm banquero) {
        System.out.println("\n--- ESTADO ACTUAL DEL SISTEMA ---");
        System.out.println("Disponible: " + Arrays.toString(banquero.getDisponible()));
        System.out.println("\nProceso |  Maximo | Asignacion | Necesidad");
        System.out.println("--------+---------+------------+----------");
        int[][] maximo = banquero.getMaximo();
        int[][] asignacion = banquero.getAsignacion();
        int[][] necesidad = banquero.getNecesidad();

        for (int i = 0; i < banquero.getNumProcesos(); i++) {
            System.out.printf("  P%-4d | %-7s | %-10s | %-8s\n", 
                i, 
                Arrays.toString(maximo[i]), 
                Arrays.toString(asignacion[i]), 
                Arrays.toString(necesidad[i]));
        }
    }

    private static BankerAlgorithm ingresarDatosPersonalizados(Scanner escaner) {
        try {
            System.out.print("Numero de procesos: ");
            int np = Integer.parseInt(escaner.nextLine().trim());
            System.out.print("Numero de tipos de recursos: ");
            int nr = Integer.parseInt(escaner.nextLine().trim());

            int[] disp = new int[nr];
            System.out.println("Ingrese vector Disponible (" + nr + " valores separados por espacio): ");
            String[] dispStr = escaner.nextLine().trim().split("\\s+");
            for (int i = 0; i < nr; i++) disp[i] = Integer.parseInt(dispStr[i]);

            int[][] max = new int[np][nr];
            System.out.println("Ingrese Matriz MAXIMO (" + np + " filas, cada una con " + nr + " valores):");
            for (int i = 0; i < np; i++) {
                System.out.print("Maximo P" + i + ": ");
                String[] partes = escaner.nextLine().trim().split("\\s+");
                for (int j = 0; j < nr; j++) max[i][j] = Integer.parseInt(partes[j]);
            }

            int[][] asig = new int[np][nr];
            System.out.println("Ingrese Matriz ASIGNACION (" + np + " filas, cada una con " + nr + " valores):");
            for (int i = 0; i < np; i++) {
                System.out.print("Asignacion P" + i + ": ");
                String[] partes = escaner.nextLine().trim().split("\\s+");
                for (int j = 0; j < nr; j++) asig[i][j] = Integer.parseInt(partes[j]);
            }

            System.out.println("Datos personalizados cargados exitosamente.");
            return new BankerAlgorithm(np, nr, disp, max, asig);
        } catch (Exception e) {
            System.out.println("Error al ingresar datos. Se mantendra la simulacion previa: " + e.getMessage());
            return new BankerAlgorithm(5, 3, new int[]{3, 3, 2}, 
                new int[][]{{7,5,3},{3,2,2},{9,0,2},{2,2,2},{4,3,3}}, 
                new int[][]{{0,1,0},{2,0,0},{3,0,2},{2,1,1},{0,0,2}});
        }
    }
}
