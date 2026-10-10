package com.banquero;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

public class BankerGUI extends JFrame {
    private BankerAlgorithm banquero;
    private JTable tablaMaximo;
    private JTable tablaAsignacion;
    private JTable tablaNecesidad;
    private JTextField campoTextoDisponible;
    private JTextArea areaTextoLog;
    private DefaultTableModel modeloMaximo;
    private DefaultTableModel modeloAsignacion;
    private DefaultTableModel modeloNecesidad;

    public BankerGUI() {
        super("Simulador del Algoritmo del Banquero y Deteccion de Interbloqueos");
        inicializarDatosPorDefecto();
        configurarUI();
    }

    private void inicializarDatosPorDefecto() {
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
        banquero = new BankerAlgorithm(numProcesos, numRecursos, disponible, maximo, asignacion);
    }

    private void configurarUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel panelCabecera = new JPanel(new BorderLayout());
        panelCabecera.setBackground(new Color(24, 43, 73));
        JLabel etiquetaTitulo = new JLabel("Simulador del Algoritmo del Banquero", SwingConstants.CENTER);
        etiquetaTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        etiquetaTitulo.setForeground(Color.WHITE);
        etiquetaTitulo.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        panelCabecera.add(etiquetaTitulo, BorderLayout.CENTER);
        add(panelCabecera, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel panelEntradaDisponible = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelEntradaDisponible.setBorder(BorderFactory.createTitledBorder("Recursos Disponibles (Vector Disponible)"));
        panelEntradaDisponible.add(new JLabel("Disponible (ej: 3 3 2):"));
        campoTextoDisponible = new JTextField(15);
        campoTextoDisponible.setFont(new Font("Monospaced", Font.BOLD, 14));
        panelEntradaDisponible.add(campoTextoDisponible);

        JButton botonActualizarDisponible = new JButton("Actualizar Disponible");
        botonActualizarDisponible.addActionListener(this::alActualizarDisponible);
        panelEntradaDisponible.add(botonActualizarDisponible);

        JButton botonReiniciarEjemplo = new JButton("Cargar Ejemplo Tipico");
        botonReiniciarEjemplo.setBackground(new Color(220, 235, 252));
        botonReiniciarEjemplo.addActionListener(e -> {
            inicializarDatosPorDefecto();
            refrescarTablas();
            areaTextoLog.setText("Ejemplo clasico cargado exitosamente (5 procesos, 3 tipos de recursos).\n");
        });
        panelEntradaDisponible.add(botonReiniciarEjemplo);

        panelCentral.add(panelEntradaDisponible, BorderLayout.NORTH);

        JPanel panelTablas = new JPanel(new GridLayout(1, 3, 10, 10));

        modeloMaximo = crearModeloTabla("Maximo");
        tablaMaximo = new JTable(modeloMaximo);
        panelTablas.add(crearPanelTabla("Matriz Maxima (Maximo)", tablaMaximo));

        modeloAsignacion = crearModeloTabla("Asignacion");
        tablaAsignacion = new JTable(modeloAsignacion);
        panelTablas.add(crearPanelTabla("Matriz Asignada (Asignacion)", tablaAsignacion));

        modeloNecesidad = crearModeloTabla("Necesidad");
        tablaNecesidad = new JTable(modeloNecesidad);
        tablaNecesidad.setEnabled(false);
        panelTablas.add(crearPanelTabla("Matriz Necesidad (Necesidad = Maximo - Asignacion)", tablaNecesidad));

        panelCentral.add(panelTablas, BorderLayout.CENTER);

        JPanel panelAcciones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        panelAcciones.setBorder(BorderFactory.createTitledBorder("Acciones del Algoritmo"));

        JButton botonVerificarSeguridad = new JButton("Verificar Estado Seguro");
        botonVerificarSeguridad.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botonVerificarSeguridad.setBackground(new Color(212, 237, 218));
        botonVerificarSeguridad.addActionListener(e -> ejecutarVerificacionSeguridad());
        panelAcciones.add(botonVerificarSeguridad);

        JButton botonSolicitarRecursos = new JButton("Simular Solicitud de Recursos");
        botonSolicitarRecursos.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botonSolicitarRecursos.setBackground(new Color(255, 243, 205));
        botonSolicitarRecursos.addActionListener(e -> abrirDialogoSolicitud());
        panelAcciones.add(botonSolicitarRecursos);

        JButton botonDetectarInterbloqueo = new JButton("Detectar Interbloqueos");
        botonDetectarInterbloqueo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        botonDetectarInterbloqueo.setBackground(new Color(248, 215, 218));
        botonDetectarInterbloqueo.addActionListener(e -> ejecutarDeteccionInterbloqueo());
        panelAcciones.add(botonDetectarInterbloqueo);

        panelCentral.add(panelAcciones, BorderLayout.SOUTH);

        add(panelCentral, BorderLayout.CENTER);

        JPanel panelLog = new JPanel(new BorderLayout());
        panelLog.setBorder(BorderFactory.createTitledBorder("Registro de Ejecucion (Logs y Explicacion Paso a Paso)"));

        areaTextoLog = new JTextArea(10, 80);
        areaTextoLog.setFont(new Font("Monospaced", Font.PLAIN, 13));
        areaTextoLog.setEditable(false);
        areaTextoLog.setMargin(new Insets(8, 8, 8, 8));
        JScrollPane panelDesplazamientoLog = new JScrollPane(areaTextoLog);
        panelLog.add(panelDesplazamientoLog, BorderLayout.CENTER);

        add(panelLog, BorderLayout.SOUTH);

        refrescarTablas();
    }

    private DefaultTableModel crearModeloTabla(String nombre) {
        return new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return !nombre.equalsIgnoreCase("Necesidad") && columna > 0;
            }
        };
    }

    private JPanel crearPanelTabla(String titulo, JTable tabla) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder(titulo));
        tabla.setRowHeight(22);
        p.add(new JScrollPane(tabla), BorderLayout.CENTER);
        return p;
    }

    private void refrescarTablas() {
        int np = banquero.getNumProcesos();
        int nr = banquero.getNumRecursos();

        String[] encabezados = new String[nr + 1];
        encabezados[0] = "Proceso";
        for (int j = 0; j < nr; j++) {
            encabezados[j + 1] = "Rec. " + (char) ('A' + j);
        }

        int[] disp = banquero.getDisponible();
        StringBuilder sbDisp = new StringBuilder();
        for (int i = 0; i < nr; i++) {
            sbDisp.append(disp[i]).append(i < nr - 1 ? " " : "");
        }
        campoTextoDisponible.setText(sbDisp.toString());

        modeloMaximo.setDataVector(construirVectorDatos(banquero.getMaximo()), encabezados);
        modeloAsignacion.setDataVector(construirVectorDatos(banquero.getAsignacion()), encabezados);
        modeloNecesidad.setDataVector(construirVectorDatos(banquero.getNecesidad()), encabezados);
    }

    private Object[][] construirVectorDatos(int[][] matriz) {
        Object[][] datos = new Object[matriz.length][matriz[0].length + 1];
        for (int i = 0; i < matriz.length; i++) {
            datos[i][0] = "P" + i;
            for (int j = 0; j < matriz[0].length; j++) {
                datos[i][j + 1] = matriz[i][j];
            }
        }
        return datos;
    }

    private void sincronizarMatricesDesdeTabla() {
        try {
            int np = banquero.getNumProcesos();
            int nr = banquero.getNumRecursos();
            int[][] nuevoMaximo = new int[np][nr];
            int[][] nuevaAsignacion = new int[np][nr];

            for (int i = 0; i < np; i++) {
                for (int j = 0; j < nr; j++) {
                    nuevoMaximo[i][j] = Integer.parseInt(tablaMaximo.getValueAt(i, j + 1).toString().trim());
                    nuevaAsignacion[i][j] = Integer.parseInt(tablaAsignacion.getValueAt(i, j + 1).toString().trim());
                }
            }

            banquero.setValoresMatriz(nuevoMaximo, nuevaAsignacion);
            modeloNecesidad.setDataVector(construirVectorDatos(banquero.getNecesidad()), obtenerEncabezadosTabla(nr));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en los valores de la tabla: " + ex.getMessage(), "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String[] obtenerEncabezadosTabla(int nr) {
        String[] encabezados = new String[nr + 1];
        encabezados[0] = "Proceso";
        for (int j = 0; j < nr; j++) {
            encabezados[j + 1] = "Rec. " + (char) ('A' + j);
        }
        return encabezados;
    }

    private void alActualizarDisponible(ActionEvent e) {
        try {
            String[] partes = campoTextoDisponible.getText().trim().split("\\s+");
            if (partes.length != banquero.getNumRecursos()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar " + banquero.getNumRecursos() + " numeros separados por espacio.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            int[] nuevoDisponible = new int[banquero.getNumRecursos()];
            for (int i = 0; i < partes.length; i++) {
                nuevoDisponible[i] = Integer.parseInt(partes[i]);
            }
            banquero.setDisponible(nuevoDisponible);
            areaTextoLog.setText("Vector Disponible actualizado a: " + Arrays.toString(nuevoDisponible) + "\n");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Vector Disponible invalido.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ejecutarVerificacionSeguridad() {
        sincronizarMatricesDesdeTabla();
        BankerAlgorithm.ResultadoSeguridad res = banquero.comprobarSeguridad();
        areaTextoLog.setText(res.log);
    }

    private void abrirDialogoSolicitud() {
        sincronizarMatricesDesdeTabla();
        int np = banquero.getNumProcesos();
        int nr = banquero.getNumRecursos();

        String[] opcionesProcesos = new String[np];
        for (int i = 0; i < np; i++) opcionesProcesos[i] = "P" + i;

        JComboBox<String> comboProcesos = new JComboBox<>(opcionesProcesos);
        JTextField campoSolicitud = new JTextField(10);

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        panel.add(new JLabel("Seleccionar Proceso:"));
        panel.add(comboProcesos);
        panel.add(new JLabel("Solicitud (" + nr + " valores espaciados):"));
        panel.add(campoSolicitud);

        int opcion = JOptionPane.showConfirmDialog(this, panel, "Simular Solicitud de Recursos", JOptionPane.OK_CANCEL_OPTION);
        if (opcion == JOptionPane.OK_OPTION) {
            try {
                int idProceso = comboProcesos.getSelectedIndex();
                String[] partes = campoSolicitud.getText().trim().split("\\s+");
                if (partes.length != nr) {
                    JOptionPane.showMessageDialog(this, "Se requieren " + nr + " valores.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                int[] solicitud = new int[nr];
                for (int j = 0; j < nr; j++) solicitud[j] = Integer.parseInt(partes[j]);

                BankerAlgorithm.ResultadoSolicitud res = banquero.solicitarRecursos(idProceso, solicitud);
                refrescarTablas();

                StringBuilder sb = new StringBuilder();
                sb.append(res.mensaje).append("\n\n");
                if (res.resultadoSeguridad != null) {
                    sb.append(res.resultadoSeguridad.log);
                }
                areaTextoLog.setText(sb.toString());

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Formato de solicitud no valido.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void ejecutarDeteccionInterbloqueo() {
        sincronizarMatricesDesdeTabla();
        BankerAlgorithm.ResultadoInterbloqueo res = banquero.detectarInterbloqueo(null);
        areaTextoLog.setText(res.log);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new BankerGUI().setVisible(true);
        });
    }
}
