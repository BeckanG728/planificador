package es.unjfsc.planificador.compartido.nucleo.algoritmos.fifo;

import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;
import es.unjfsc.planificador.compartido.nucleo.motor.PlanificadorBase;

import java.util.Comparator;
import java.util.List;

/**
 * Planificador FIFO (FCFS, no apropiativo) de un único motor de CPU.
 *
 * <p>El motor lo recibe el primer proceso que llega y lo ejecuta hasta
 * terminar su ráfaga; después lo cede al siguiente apto.</p>
 */
public class PlanificadorFIFO extends PlanificadorBase {

    private static final int MAXIMO_PROCESOS = 5;

    private static final List<Proceso> PROCESOS_INICIALES = List.of(
            new Proceso("A", 0, 8),
            new Proceso("B", 1, 4),
            new Proceso("C", 2, 2));

    private static final List<Proceso> PROCESOS_EXTRA = List.of(
            new Proceso("D", 3, 5),
            new Proceso("E", 4, 6));

    public PlanificadorFIFO() {
        super(PROCESOS_INICIALES, PROCESOS_EXTRA, MAXIMO_PROCESOS);
    }

    /** El apto que lleva más tiempo esperando; a igual espera, el de menor identificador. */
    @Override
    protected Proceso seleccionar() {
        return procesos.stream()
                .filter(this::esApto)
                .min(Comparator.comparingInt(Proceso::getLlegada).thenComparing(Proceso::getId))
                .orElse(null);
    }
}