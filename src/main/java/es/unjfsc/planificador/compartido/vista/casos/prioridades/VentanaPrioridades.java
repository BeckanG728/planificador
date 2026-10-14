package es.unjfsc.planificador.compartido.vista.casos.prioridades;

import es.unjfsc.planificador.compartido.nucleo.algoritmos.apropiativos.PlanificadorPrioridadesApropiativo;
import es.unjfsc.planificador.compartido.nucleo.algoritmos.no_apropiativos.PlanificadorPrioridades;
import es.unjfsc.planificador.compartido.nucleo.motor.Planificador;
import es.unjfsc.planificador.compartido.vista.comun.ComponentesRedondeados;
import es.unjfsc.planificador.compartido.vista.comun.Tema;
import es.unjfsc.planificador.compartido.vista.comun.VentanaSimulacion;

import javax.swing.*;
import java.util.List;

/**
 * Ventana de la simulación con planificación por prioridades.
 *
 * <p>Añade a la cabecera un selector de dos botones, apropiativo o no apropiativo. Cada
 * botón abre la ventana con su propio planificador (sin modo compartido): la variante no
 * apropiativa usa {@link PlanificadorPrioridades} y la apropiativa usa
 * {@link PlanificadorPrioridadesApropiativo}. Se deshabilita con la simulación en curso,
 * igual que los demás controles del caso.</p>
 */
public class VentanaPrioridades extends VentanaSimulacion {

    public VentanaPrioridades() {
        this(new PlanificadorPrioridades());
    }

    public VentanaPrioridades(Planificador planificador) {
        super(planificador, new LogicaVisualPrioridades(planificador));
    }

    @Override
    protected List<JComponent> controlesDelCaso(Planificador planificador) {
        boolean esApropiativo = planificador instanceof PlanificadorPrioridadesApropiativo;

        ComponentesRedondeados.Boton noApropiativo = Tema.botonCompacto("No apropiativo");
        noApropiativo.setToolTipText("El proceso en el motor lo conserva hasta terminar su ráfaga");
        ComponentesRedondeados.Boton apropiativo = Tema.botonCompacto("Apropiativo");
        apropiativo.setToolTipText("Un proceso de mejor prioridad desaloja al que ocupa el motor");

        Tema.activarBotonCompacto(noApropiativo, !esApropiativo);
        Tema.activarBotonCompacto(apropiativo, esApropiativo);

        noApropiativo.addActionListener(e -> {
            if (!(getPlanificador() instanceof PlanificadorPrioridades)) {
                abrirCon(new PlanificadorPrioridades());
            }
        });
        apropiativo.addActionListener(e -> {
            if (!(getPlanificador() instanceof PlanificadorPrioridadesApropiativo)) {
                abrirCon(new PlanificadorPrioridadesApropiativo());
            }
        });

        return List.of(noApropiativo, apropiativo);
    }

    private void abrirCon(Planificador nuevo) {
        VentanaPrioridades otra = new VentanaPrioridades(nuevo);
        otra.setLocation(getLocation());
        otra.setVisible(true);
        dispose();
    }
}
