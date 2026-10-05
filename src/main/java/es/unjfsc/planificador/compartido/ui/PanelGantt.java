package es.unjfsc.planificador.compartido.ui;

import es.unjfsc.planificador.compartido.modelo.Proceso;
import es.unjfsc.planificador.compartido.motor.Planificador;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Diagrama de Gantt en forma de matriz: una columna por unidad de tiempo,
 * una fila por proceso. Marca la llegada con 'X' y la ejecución con el color
 * del proceso.
 */
public class PanelGantt extends JPanel {

    private final Planificador planificador;
    private final JPanel matriz = new JPanel();

    public PanelGantt(Planificador planificador) {
        super(new BorderLayout(0, 8));
        this.planificador = planificador;
        setBackground(Tema.PROFUNDO);
        setOpaque(false);
        setBorder(Tema.bordeRelleno(Tema.BORDE_VISIBLE, 12, 8, 8));

        matriz.setOpaque(false);
        matriz.setLayout(new BoxLayout(matriz, BoxLayout.Y_AXIS));

        add(crearLeyenda(), BorderLayout.SOUTH);
        add(matriz, BorderLayout.CENTER);
    }

    public void actualizar() {
        matriz.removeAll();

        int columnas = planificador.totalColumnasGantt();
        List<Proceso> procesos = planificador.getProcesosOrdenados();
        Map<String, Set<Integer>> ejecucion = planificador.mapaEjecucion();

        matriz.add(crearFilaReloj(columnas));

        for (Proceso proceso : procesos) {
            matriz.add(crearFilaProceso(proceso, columnas, ejecucion));
            matriz.add(javax.swing.Box.createVerticalStrut(1));
        }

        matriz.revalidate();
        matriz.repaint();
    }

    private JPanel crearFilaReloj(int columnas) {
        GridLayout disposicion = new GridLayout(1, columnas + 1);
        disposicion.setHgap(1);
        JPanel fila = new JPanel(disposicion);
        fila.setOpaque(false);

        fila.add(crearCelda("reloj", Tema.SUPERFICIE, Tema.AZUL_CLARO, Tema.fuenteNegrita(10), 34));

        for (int t = 0; t < columnas; t++) {
            fila.add(crearCelda(String.valueOf(t), Tema.SUPERFICIE, Tema.TEXTO_SUAVE, Tema.fuenteNegrita(11), 20));
        }
        return fila;
    }

    private JPanel crearFilaProceso(Proceso proceso, int columnas, Map<String, Set<Integer>> ejecucion) {
        GridLayout disposicion = new GridLayout(1, columnas + 1);
        disposicion.setHgap(1);
        JPanel fila = new JPanel(disposicion);
        fila.setOpaque(false);

        fila.add(crearCelda(proceso.getId(), Tema.SUPERFICIE, Tema.colorAuto(proceso.getId()),
                Tema.fuenteNegrita(12), 34));

        Set<Integer> instantes = ejecucion.getOrDefault(proceso.getId(), Set.of());

        for (int t = 0; t < columnas; t++) {
            if (instantes.contains(t)) {
                Color fondo = Tema.colorAutoSuave(proceso.getId());
                ComponentesRedondeados.Etiqueta celda = crearCelda("", fondo, Tema.FONDO, Tema.fuenteNegrita(11), 20);
                celda.setToolTipText("Auto " + proceso.getId() + " ejecuta en t=" + t);
                fila.add(celda);
            } else if (t == proceso.getLlegada()) {
                ComponentesRedondeados.Etiqueta celda = crearCelda("X", Tema.PROFUNDO, Tema.TEXTO, Tema.fuenteNegrita(13), 20);
                celda.setToolTipText("Auto " + proceso.getId() + " llega en t=" + proceso.getLlegada());
                fila.add(celda);
            } else {
                fila.add(crearCelda("", Tema.PROFUNDO, Tema.TEXTO_TENUE, Tema.fuente(11), 20));
            }
        }
        return fila;
    }

    /** Leyenda fija: solo el significado de 'X', el color ya identifica cada proceso. */
    private JPanel crearLeyenda() {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        leyenda.setOpaque(false);

        ComponentesRedondeados.Etiqueta cajaX = Tema.etiquetaCentrada("X", Tema.SUPERFICIE, Tema.TEXTO, Tema.fuenteNegrita(10));
        Tema.estiloCelda(cajaX);
        cajaX.setPreferredSize(new Dimension(16, 16));
        leyenda.add(cajaX);
        leyenda.add(Tema.etiqueta("llegada al sistema \u00B7 el color de cada fila indica su ejecución",
                Tema.TEXTO_TENUE, Tema.fuente(11)));
        return leyenda;
    }

    private static ComponentesRedondeados.Etiqueta crearCelda(String texto, Color fondo, Color color,
                                                             java.awt.Font fuente, int anchoMinimo) {
        ComponentesRedondeados.Etiqueta celda = Tema.etiquetaCentrada(texto, fondo, color, fuente);
        Tema.estiloCelda(celda);
        celda.setPreferredSize(new Dimension(anchoMinimo, 20));
        celda.setMinimumSize(new Dimension(anchoMinimo, 20));
        return celda;
    }
}