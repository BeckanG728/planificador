package es.unjfsc.planificador.compartido.vista.comun;

import es.unjfsc.planificador.compartido.nucleo.modelo.EstadoProceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;
import es.unjfsc.planificador.compartido.nucleo.motor.Planificador;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.List;

/**
 * Ventana de la simulación: reagrupa controles, pista de carrera, diagrama de Gantt
 * y métricas, y conecta los eventos con el planificador.
 *
 * <p>Es común a todos los casos; cada algoritmo aporta su motor y su {@link LogicaVisual}.</p>
 */
public abstract class VentanaSimulacion extends JFrame {

    private final Planificador planificador;
    private final LogicaVisual logicaVisual;
    private final Timer temporizador;

    private final JButton botonIniciar = Tema.boton(Tema.REPRODUCIR + " Iniciar", true);
    private final JButton botonPausar = Tema.boton(Tema.PAUSA + " Pausar");
    private final JButton botonReiniciar = Tema.boton(Tema.REINICIAR + " Reiniciar");
    private final Map<Double, ComponentesRedondeados.Boton> botonesVelocidad = new LinkedHashMap<>();
    private double velocidad = 1;

    private final JPanel carriles = Tema.panel(Tema.FONDO);
    private final JPanel cola = Tema.panel(Tema.FONDO);
    private final JLabel reloj = Tema.etiqueta("0", Tema.AZUL_CLARO, Tema.fuenteNegrita(13));
    private final JLabel motorActual = Tema.etiqueta("\u2014", Tema.TEXTO, Tema.fuenteNegrita(13));
    private final JLabel insignia = Tema.etiquetaRedondeada("", Tema.ACENTO, Color.WHITE, Tema.fuente(11), 8);
    private final List<JComponent> controlesCaso = new ArrayList<>();

    private final PanelConfiguracion panelConfiguracion;
    private final PanelGantt panelGantt;
    private final PanelMetricas panelMetricas = new PanelMetricas();

    private final Map<String, PanelCarril> vistasCarril = new HashMap<>();

    protected VentanaSimulacion(Planificador planificador, LogicaVisual logicaVisual) {
        super("CPU Racing \u00B7 " + logicaVisual.nombre());
        this.planificador = planificador;
        this.logicaVisual = logicaVisual;

        temporizador = new Timer(planificador.periodoTemporizador(), this::manejarTick);
        temporizador.setRepeats(true);

        panelConfiguracion = new PanelConfiguracion(planificador, logicaVisual,
                this::abrirDialogoNuevoAuto, this::quitarProceso);
        panelGantt = new PanelGantt(planificador);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(820, 480));
        setSize(1000, 700);
        getContentPane().setBackground(Tema.FONDO);

        JPanel contenido = Tema.panel(Tema.FONDO);
        contenido.setLayout(new BorderLayout(0, 8));
        contenido.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        contenido.add(crearCabecera(), BorderLayout.NORTH);
        contenido.add(crearDesplazamiento(), BorderLayout.CENTER);

        setContentPane(contenido);
        registrarEventos();
        actualizarTodo();
        setLocationRelativeTo(null);
    }

    /**
     * Envoltorio con scroll para que todo el cuerpo sea accesible en ventanas pequeñas.
     */
    private JScrollPane crearDesplazamiento() {
        JScrollPane desplazamiento = new JScrollPane(crearCuerpo(),
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        desplazamiento.setBorder(BorderFactory.createEmptyBorder());
        desplazamiento.getViewport().setBackground(Tema.FONDO);
        desplazamiento.getVerticalScrollBar().setUnitIncrement(16);
        desplazamiento.getVerticalScrollBar().setBlockIncrement(160);
        desplazamiento.getHorizontalScrollBar().setUnitIncrement(16);
        return desplazamiento;
    }

    // ------------------------------------------------------------------
    // Construcción de la interfaz
    // ------------------------------------------------------------------

    private JPanel crearCabecera() {
        JPanel cabecera = new JPanel(new BorderLayout(12, 0));
        cabecera.setOpaque(false);

        JPanel titulo = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titulo.setOpaque(false);
        titulo.add(Tema.etiqueta(Tema.BANDERA + " CPU Racing", Tema.TEXTO, Tema.fuenteNegrita(19)));
        insignia.setText(logicaVisual.insignia(planificador));
        insignia.setToolTipText(logicaVisual.descripcion());
        titulo.add(insignia);

        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(crearControles(), BorderLayout.EAST);
        return cabecera;
    }

    /**
     * Controles que el algoritmo necesita en la cabecera, además de los comunes.
     * Por defecto no hay ninguno. La invoca el constructor, así que la implementación
     * no debe leer campos de la subclase.
     */
    protected List<JComponent> controlesDelCaso(Planificador planificador) {
        return List.of();
    }

    private JPanel crearControles() {
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        controles.setOpaque(false);

        controles.add(botonIniciar);
        controles.add(botonPausar);
        controles.add(botonReiniciar);

        controlesCaso.clear();
        controlesCaso.addAll(controlesDelCaso(planificador));
        if (!controlesCaso.isEmpty()) {
            controles.add(javax.swing.Box.createHorizontalStrut(4));
            for (JComponent control : controlesCaso) {
                controles.add(control);
            }
        }

        controles.add(javax.swing.Box.createHorizontalStrut(10));

        for (double valor : new double[]{0.5, 1, 2}) {
            ComponentesRedondeados.Boton boton = Tema.botonCompacto(valor + "x");
            boton.setToolTipText("Velocidad de la simulación");
            botonesVelocidad.put(valor, boton);
            controles.add(boton);
        }

        return controles;
    }

    private JPanel crearCuerpo() {
        JPanel cuerpo = new PanelDesplazable(BoxLayout.Y_AXIS);

        cuerpo.add(crearAreaCarrera());
        cuerpo.add(javax.swing.Box.createVerticalStrut(8));
        cuerpo.add(panelGantt);
        cuerpo.add(javax.swing.Box.createVerticalStrut(8));
        cuerpo.add(panelMetricas);
        return cuerpo;
    }

    /**
     * Pistas a la izquierda y parámetros de los autos a la derecha.
     */
    private JPanel crearAreaCarrera() {
        JPanel area = Tema.panel(Tema.PROFUNDO);
        area.setLayout(new BorderLayout(8, 0));
        area.setBorder(Tema.bordeRelleno(Tema.BORDE_VISIBLE, 12, 8, 8));

        JPanel izquierda = Tema.panel(Tema.PROFUNDO);
        izquierda.setOpaque(false);
        izquierda.setLayout(new BorderLayout(0, 6));

        carriles.setOpaque(false);
        carriles.setLayout(new BoxLayout(carriles, BoxLayout.Y_AXIS));

        izquierda.add(carriles, BorderLayout.CENTER);
        izquierda.add(crearPie(), BorderLayout.SOUTH);

        area.add(izquierda, BorderLayout.CENTER);
        area.add(panelConfiguracion, BorderLayout.EAST);
        return area;
    }

    private JPanel crearPie() {
        JPanel pie = new JPanel(new BorderLayout(12, 0));
        pie.setOpaque(false);

        JPanel espera = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        espera.setOpaque(false);
        cola.setOpaque(false);
        espera.add(Tema.etiqueta(Tema.RELOJ_SANDIA, Tema.TEXTO_TENUE, Tema.fuenteEmoji(11)));
        espera.add(cola);
        pie.add(espera, BorderLayout.CENTER);

        JPanel info = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        info.setOpaque(false);
        info.add(Tema.etiqueta(Tema.RELOJ + " Tiempo", Tema.TEXTO_TENUE, Tema.fuenteEmoji(11)));
        info.add(reloj);
        info.add(Tema.etiqueta("Motor", Tema.TEXTO_TENUE, Tema.fuente(11)));
        info.add(motorActual);
        pie.add(info, BorderLayout.EAST);

        return pie;
    }

    private void registrarEventos() {
        botonIniciar.addActionListener(this::iniciarSimulacion);
        botonPausar.addActionListener(this::alternarPausa);
        botonReiniciar.addActionListener(e -> reiniciarSimulacion());

        for (Map.Entry<Double, ComponentesRedondeados.Boton> entrada : botonesVelocidad.entrySet()) {
            ComponentesRedondeados.Boton boton = entrada.getValue();
            boton.addActionListener(e -> establecerVelocidad(entrada.getKey()));
        }
    }

    // ------------------------------------------------------------------
    // Acciones
    // ------------------------------------------------------------------

    private void iniciarSimulacion(ActionEvent evento) {
        planificador.iniciar();
        temporizador.setDelay(planificador.periodoTemporizador());
        temporizador.start();
        actualizarTodo();
    }

    private void alternarPausa(ActionEvent evento) {
        if (planificador.isPausado()) {
            planificador.continuar();
        } else {
            planificador.pausar();
        }
        actualizarBotones();
    }

    private void reiniciarSimulacion() {
        temporizador.stop();
        planificador.restablecerProcesos();
        panelConfiguracion.reconstruir();
        actualizarTodo();
    }

    private void establecerVelocidad(double valor) {
        velocidad = valor;
        planificador.establecerVelocidad(valor);
        temporizador.setDelay(planificador.periodoTemporizador());
        actualizarBotones();
    }

    private void abrirDialogoNuevoAuto() {
        if (planificador.getProcesos().size() >= planificador.maximoProcesos()) {
            JOptionPane.showMessageDialog(this,
                    "Máximo " + planificador.maximoProcesos() + " autos alcanzado.", "Límite alcanzado",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Proceso sugerencia = planificador.getSiguienteProcesoExtra();
        String id = sugerencia == null ? "?" : sugerencia.getId();
        int llegadaSugerida = sugerencia == null ? 3 : sugerencia.getLlegada();
        int rafagaSugerida = sugerencia == null ? 5 : sugerencia.getRafaga();

        boolean estabaCorriendo = planificador.isEjecutando() && !planificador.isPausado();
        if (estabaCorriendo) {
            planificador.pausar();
            actualizarBotones();
        }

        DialogoNuevoAuto dialogo = new DialogoNuevoAuto(this, id, llegadaSugerida, rafagaSugerida);
        dialogo.setVisible(true);

        if (dialogo.isConfirmado()) {
            Proceso nuevo = planificador.crearProcesoExtra(dialogo.getLlegada(), dialogo.getRafaga());
            if (nuevo != null) {
                planificador.agregarProceso(nuevo);
                panelConfiguracion.reconstruir();
            }
        }

        if (estabaCorriendo && planificador.isEjecutando()) {
            planificador.continuar();
        }
        actualizarTodo();
    }

    private void quitarProceso() {
        if (planificador.quitarProceso()) {
            panelConfiguracion.reconstruir();
            actualizarTodo();
        }
    }

    // ------------------------------------------------------------------
    // Simulación y refresco
    // ------------------------------------------------------------------

    private void manejarTick(ActionEvent evento) {
        if (planificador.avanzar()) {
            temporizador.stop();
            planificador.detener();
        }
        actualizarTodo();
    }

    private void actualizarTodo() {
        actualizarBotones();
        actualizarCarriles();
        actualizarCola();
        reloj.setText(String.valueOf(planificador.getReloj()));
        insignia.setText(logicaVisual.insignia(planificador));
        motorActual.setText(planificador.getMotorPropietario() == null
                ? "\u2014"
                : Tema.AUTO + " " + planificador.getMotorPropietario());
        panelGantt.actualizar();
        panelMetricas.actualizar(planificador.getResumen());
    }

    private void actualizarBotones() {
        boolean ejecutando = planificador.isEjecutando();
        botonIniciar.setEnabled(!ejecutando);
        botonPausar.setEnabled(ejecutando);
        botonPausar.setText(planificador.isPausado() ? Tema.REPRODUCIR + " Continuar" : Tema.PAUSA + " Pausar");
        for (Map.Entry<Double, ComponentesRedondeados.Boton> entrada : botonesVelocidad.entrySet()) {
            Tema.activarBotonCompacto(entrada.getValue(), entrada.getKey() == velocidad);
        }
        for (JComponent control : controlesCaso) {
            control.setEnabled(!ejecutando);
        }
        panelConfiguracion.actualizarBotones();
    }

    private void actualizarCarriles() {
        List<Proceso> procesos = planificador.getProcesosOrdenados();
        List<String> ids = procesos.stream().map(Proceso::getId).toList();

        for (String id : new ArrayList<>(vistasCarril.keySet())) {
            if (!ids.contains(id)) {
                vistasCarril.remove(id).setVisible(false);
            }
        }
        carriles.removeAll();

        for (Proceso proceso : procesos) {
            PanelCarril carril = vistasCarril.computeIfAbsent(proceso.getId(), id -> {
                PanelCarril nuevo = new PanelCarril();
                nuevo.setAlignmentX(LEFT_ALIGNMENT);
                return nuevo;
            });
            carril.setVisible(true);
            carril.actualizar(proceso, planificador.getMotorPropietario());
            carriles.add(carril);
            carriles.add(javax.swing.Box.createVerticalStrut(9));
        }

        carriles.revalidate();
        carriles.repaint();
    }

    private void actualizarCola() {
        cola.removeAll();
        cola.setLayout(new BoxLayout(cola, BoxLayout.X_AXIS));

        List<Proceso> esperando = planificador.getProcesos().stream()
                .filter(p -> p.getEstado() == EstadoProceso.ESPERANDO)
                .toList();

        if (esperando.isEmpty()) {
            cola.add(Tema.etiqueta("\u2014", Tema.VISIBLE, Tema.fuente(13)));
        } else {
            for (Proceso proceso : esperando) {
                cola.add(crearChip(proceso));
                cola.add(javax.swing.Box.createHorizontalStrut(6));
            }
        }

        cola.revalidate();
        cola.repaint();
    }

    private static JPanel crearChip(Proceso proceso) {
        JPanel chip = Tema.panel(Tema.SUPERFICIE);
        chip.setOpaque(false);
        chip.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 1));
        chip.setBorder(Tema.bordeRelleno(Tema.BORDE_CAMPO, 8, 2, 8));
        chip.add(Tema.etiqueta(Tema.AUTO, Tema.TEXTO, Tema.fuenteEmoji(13)));
        chip.add(Tema.etiqueta(proceso.getId(), Tema.colorAuto(proceso.getId()), Tema.fuenteNegrita(13)));
        chip.setAlignmentX(LEFT_ALIGNMENT);
        return chip;
    }
}