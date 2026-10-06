package es.unjfsc.planificador.compartido.vista.comun;

import es.unjfsc.planificador.compartido.nucleo.modelo.MetricaProceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.ResumenMetricas;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;
import java.util.Locale;

/**
 * Panel de métricas finales: tabla por proceso (C, T, E, F, P) y promedios.
 */
public class PanelMetricas extends JPanel {

    private static final String[] COLUMNAS = {"Auto", "C", "T", "E", "F", "P"};
    private static final int ALTO_ENCABEZADO = 24;

    private final DefaultTableModel modelo = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };

    private final JTable tabla = new JTable(modelo);
    private final JLabel esperaPromedio = crearValor();
    private final JLabel finPromedio = crearValor();
    private final JLabel penalizacionPromedio = crearValor();
    private final JLabel cambiosContexto = crearValor();
    private final JScrollPane desplazamiento;

    public PanelMetricas() {
        super(new BorderLayout());
        setBackground(Tema.PROFUNDO);
        setOpaque(false);
        setBorder(Tema.bordeRelleno(Tema.BORDE_VISIBLE, 12, 8, 8));

        tabla.setBackground(Tema.PROFUNDO);
        tabla.setForeground(Tema.TEXTO);
        tabla.setRowHeight(20);
        tabla.setShowGrid(false);
        tabla.setIntercellSpacing(new Dimension(0, 0));
        tabla.setFillsViewportHeight(false);
        tabla.setSelectionBackground(Tema.ACENTO);
        tabla.setSelectionForeground(Color.WHITE);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setFont(Tema.fuente(12));
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tabla.getTableHeader().setBackground(Tema.SUPERFICIE);
        tabla.getTableHeader().setForeground(Tema.TEXTO_SUAVE);
        tabla.getTableHeader().setFont(Tema.fuenteNegrita(11));
        tabla.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE));
        tabla.getTableHeader().setReorderingAllowed(false);

        DefaultTableCellRenderer renderizador = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tabla, Object valor, boolean seleccionada,
                                                          boolean foco, int fila, int columna) {
                Component componente = super.getTableCellRendererComponent(tabla, valor, seleccionada, foco, fila, columna);
                componente.setBackground(seleccionada ? Tema.ACENTO : Tema.PROFUNDO);
                componente.setForeground(seleccionada ? Color.WHITE : Tema.TEXTO);
                componente.setFont(columna == 0 ? Tema.fuenteNegrita(12) : Tema.fuente(12));
                setHorizontalAlignment(columna == 0 ? LEFT : CENTER);
                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Tema.BORDE));
                return componente;
            }
        };
        tabla.setDefaultRenderer(Object.class, renderizador);

        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setBackground(Tema.PROFUNDO);
        contenedor.setOpaque(false);

        desplazamiento = new JScrollPane(tabla);
        desplazamiento.setBorder(Tema.bordeRelleno(Tema.BORDE, 8, 1, 1));
        desplazamiento.getViewport().setBackground(Tema.PROFUNDO);
        desplazamiento.getVerticalScrollBar().setUnitIncrement(16);
        contenedor.add(desplazamiento, BorderLayout.CENTER);

        JPanel resumen = Tema.panel(Tema.PROFUNDO);
        resumen.setOpaque(false);
        resumen.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 0));
        resumen.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 2));
        resumen.add(crearResumen("Espera prom.", esperaPromedio));
        resumen.add(crearResumen("Fin prom.", finPromedio));
        resumen.add(crearResumen("Penaliz. prom.", penalizacionPromedio));
        resumen.add(crearResumen("Cambios contexto", cambiosContexto));

        contenedor.add(resumen, BorderLayout.SOUTH);
        add(contenedor, BorderLayout.CENTER);
    }

    /**
     * La tabla se ajusta a su altura mínima y solo recibe scroll propio si hay
     * más procesos que filas caben sin holgura.
     */
    private void ajustarAlturaTabla(int filas) {
        int alto = Math.max(1, filas) * tabla.getRowHeight() + ALTO_ENCABEZADO + 8;
        desplazamiento.setPreferredSize(new Dimension(400, alto));
        desplazamiento.setMinimumSize(new Dimension(200, alto));
        revalidate();
        repaint();
    }

    private static JLabel crearValor() {
        return Tema.etiqueta("0", Tema.AZUL_CLARO, Tema.fuenteNegrita(12));
    }

    private static JPanel crearResumen(String titulo, JLabel valor) {
        JPanel grupo = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        grupo.setOpaque(false);
        grupo.add(Tema.etiqueta(titulo, Tema.TEXTO_SUAVE, Tema.fuente(11)));
        grupo.add(valor);
        return grupo;
    }

    public void actualizar(ResumenMetricas resumen) {
        modelo.setRowCount(0);
        List<MetricaProceso> filas = resumen.filas();
        for (MetricaProceso metrica : filas) {
            modelo.addRow(new Object[]{
                    metrica.procesoId(),
                    metrica.llegada(),
                    metrica.rafaga(),
                    String.format(Locale.ROOT, "%.1f", (double) metrica.espera()),
                    String.format(Locale.ROOT, "%.1f", (double) metrica.finalizacion()),
                    String.format(Locale.ROOT, "%.2f", metrica.penalizacion())});
        }

        esperaPromedio.setText(String.format(Locale.ROOT, "%.2f", resumen.esperaPromedio()));
        finPromedio.setText(String.format(Locale.ROOT, "%.2f", resumen.finalizacionPromedio()));
        penalizacionPromedio.setText(String.format(Locale.ROOT, "%.2f", resumen.penalizacionPromedio()));
        cambiosContexto.setText(String.valueOf(resumen.cambiosContexto()));
        ajustarAlturaTabla(filas.size());
    }
}