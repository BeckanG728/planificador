package es.unjfsc.planificador.compartido.vista.casos.prioridades;

import es.unjfsc.planificador.compartido.nucleo.algoritmos.prioridades.PlanificadorPrioridades;
import es.unjfsc.planificador.compartido.nucleo.motor.Planificador;
import es.unjfsc.planificador.compartido.vista.comun.ComponentesRedondeados;
import es.unjfsc.planificador.compartido.vista.comun.Tema;
import es.unjfsc.planificador.compartido.vista.comun.VentanaSimulacion;

import javax.swing.JComponent;
import java.util.List;

/**
 * Ventana de la simulación con planificación por prioridades.
 *
 * <p>Añade a la cabecera un selector de dos botones, apropiativo o no apropiativo. Se
 * deshabilita con la simulación en curso, igual que los demás controles del caso, así que
 * el modo elegido rige desde la siguiente simulación.</p>
 */
public class VentanaPrioridades extends VentanaSimulacion {

    public VentanaPrioridades() {
        this(new PlanificadorPrioridades());
    }

    private VentanaPrioridades(PlanificadorPrioridades planificador) {
        super(planificador, new LogicaVisualPrioridades(planificador));
    }

    @Override
    protected List<JComponent> controlesDelCaso(Planificador planificador) {
        PlanificadorPrioridades prioridades = (PlanificadorPrioridades) planificador;

        ComponentesRedondeados.Boton noApropiativo = Tema.botonCompacto("No apropiativo");
        noApropiativo.setToolTipText("El proceso en el motor lo conserva hasta terminar su ráfaga");
        ComponentesRedondeados.Boton apropiativo = Tema.botonCompacto("Apropiativo");
        apropiativo.setToolTipText("Un proceso de mejor prioridad desaloja al que ocupa el motor");

        Runnable pintar = () -> {
            Tema.activarBotonCompacto(noApropiativo, !prioridades.isApropiativo());
            Tema.activarBotonCompacto(apropiativo, prioridades.isApropiativo());
        };
        noApropiativo.addActionListener(e -> {
            prioridades.establecerApropiativo(false);
            pintar.run();
        });
        apropiativo.addActionListener(e -> {
            prioridades.establecerApropiativo(true);
            pintar.run();
        });
        pintar.run();

        return List.of(noApropiativo, apropiativo);
    }
}