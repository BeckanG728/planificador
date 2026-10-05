package es.unjfsc.planificador.compartido.ui;

import es.unjfsc.planificador.compartido.modelo.Proceso;
import es.unjfsc.planificador.compartido.motor.Planificador;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel de parámetros de los procesos (autos): permite editar los parámetros que declara
 * cada algoritmo mientras la simulación está detenida y agregar o quitar procesos.
 * Se sitúa a la derecha de las pistas.
 *
 * <p>Las columnas se generan a partir de {@link LogicaVisual#parametros()}, así que el panel
 * no cambia al añadir un algoritmo: solo cambia lo que el caso declara.</p>
 */
public class PanelConfiguracion extends JPanel {

    /** Ancho con las dos columnas comunes, C y T. */
    private static final int ANCHO = 200;

    /** Espacio que suma cada columna extra (por ejemplo, la prioridad). */
    private static final int ANCHO_POR_PARAMETRO = 64;
    private static final int PARAMETROS_COMUNES = 2;

    private final Planificador planificador;
    private final List<ParametroProceso> parametros;
    private final JPanel filas = new JPanel();
    private final ComponentesRedondeados.Boton agregar = Tema.boton("Agregar", true);
    private final ComponentesRedondeados.Boton quitar = Tema.boton("Quitar");
    private final List<ComponentesRedondeados.Campo> campos = new ArrayList<>();

    private boolean editables = true;

    public PanelConfiguracion(Planificador planificador, LogicaVisual logicaVisual,
                              Runnable alAgregar, Runnable alQuitar) {
        super(new BorderLayout(0, 8));
        this.planificador = planificador;
        this.parametros = logicaVisual.parametros();
        int ancho = ANCHO + Math.max(0, parametros.size() - PARAMETROS_COMUNES) * ANCHO_POR_PARAMETRO;
        setBackground(Tema.PROFUNDO);
        setOpaque(false);
        setBorder(Tema.bordeRelleno(Tema.BORDE_VISIBLE, 12, 8, 8));
        setPreferredSize(new Dimension(ancho, 0));
        setMinimumSize(new Dimension(ancho, 0));

        agregar.setToolTipText("Agrega un proceso (máximo " + planificador.maximoProcesos() + ")");
        quitar.setToolTipText("Quita el último proceso agregado");

        JPanel acciones = new JPanel(new GridLayout(1, 2, 6, 0));
        acciones.setOpaque(false);
        acciones.add(agregar);
        acciones.add(quitar);

        filas.setOpaque(false);
        filas.setLayout(new BoxLayout(filas, BoxLayout.Y_AXIS));
        construirFilas();

        agregar.addActionListener(e -> alAgregar.run());
        quitar.addActionListener(e -> alQuitar.run());

        add(acciones, BorderLayout.NORTH);
        add(filas, BorderLayout.CENTER);
    }

    /** Vuelve a dibujar una fila por proceso del planificador. */
    public void reconstruir() {
        construirFilas();
        actualizarBotones();
    }

    private void construirFilas() {
        filas.removeAll();
        campos.clear();

        for (Proceso proceso : planificador.getProcesos()) {
            filas.add(crearFilaProceso(proceso));
            filas.add(javax.swing.Box.createVerticalStrut(6));
        }

        filas.revalidate();
        filas.repaint();
    }

    private JPanel crearFilaProceso(Proceso proceso) {
        JPanel fila = new JPanel(new BorderLayout(6, 0));
        fila.setBackground(Tema.SUPERFICIE);
        fila.setOpaque(false);
        fila.setBorder(Tema.bordeRelleno(Tema.BORDE, 9, 3, 7));
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel id = Tema.etiqueta(proceso.getId(), Tema.colorAuto(proceso.getId()), Tema.fuenteNegrita(14));
        id.setPreferredSize(new Dimension(16, 0));
        id.setHorizontalAlignment(JLabel.CENTER);
        id.setToolTipText("Proceso " + proceso.getId());

        JPanel celdas = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        celdas.setOpaque(false);
        for (ParametroProceso parametro : parametros) {
            celdas.add(crearCampo(parametro, proceso));
        }

        fila.add(id, BorderLayout.WEST);
        fila.add(celdas, BorderLayout.CENTER);
        return fila;
    }

    private JPanel crearCampo(ParametroProceso parametro, Proceso proceso) {
        ComponentesRedondeados.Campo campo = Tema.campoNumero(2);
        campo.setText(String.valueOf(parametro.lector().apply(proceso)));
        campo.setEnabled(editables);
        campo.setToolTipText(parametro.descripcion());
        campos.add(campo);

        campo.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                aplicarValor(campo, proceso, parametro);
            }
        });
        campo.addActionListener(e -> aplicarValor(campo, proceso, parametro));

        JPanel grupo = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        grupo.setOpaque(false);
        grupo.add(Tema.etiqueta(parametro.titulo(), Tema.TEXTO_SUAVE, Tema.fuenteNegrita(11)));
        grupo.add(campo);
        return grupo;
    }

    private void aplicarValor(ComponentesRedondeados.Campo campo, Proceso proceso, ParametroProceso parametro) {
        if (!editables) {
            return;
        }
        int valor;
        try {
            valor = Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException ex) {
            valor = parametro.lector().apply(proceso);
        }
        parametro.escritor().accept(proceso, Math.max(parametro.minimo(), valor));
        campo.setText(String.valueOf(parametro.lector().apply(proceso)));
    }

    /** Habilita o deshabilita la edición y refleja el estado de los botones. */
    public void actualizarBotones() {
        agregar.setEnabled(planificador.getProcesos().size() < planificador.maximoProcesos());
        quitar.setEnabled(!planificador.isEjecutando()
                && planificador.getProcesos().size() > planificador.minimoProcesos());
        boolean habilitada = !planificador.isEjecutando();
        if (habilitada != editables) {
            editables = habilitada;
            for (ComponentesRedondeados.Campo campo : campos) {
                campo.setEnabled(editables);
            }
        }
    }
}