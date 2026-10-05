package es.unjfsc.planificador.casos.caso2_sjf;

import es.unjfsc.planificador.compartido.modelo.Proceso;
import es.unjfsc.planificador.compartido.motor.PlanificadorBase;

import java.util.Comparator;
import java.util.List;

/**
 * Planificador SJF (Shortest Job First, no apropiativo) de un único motor de CPU.
 *
 * <p>El motor ejecuta cada proceso hasta terminar su ráfaga y después escoge, entre los
 * aptos, el de menor ráfaga. La ráfaga restante de un proceso no baja porque no sea
 * interrumpido, así que elegir una vez basta.</p>
 */
public class PlanificadorSJF extends PlanificadorBase {

    private static final int MAXIMO_PROCESOS = 5;

    private static final List<Proceso> PROCESOS_INICIALES = List.of(
            new Proceso("A", 0, 8),
            new Proceso("B", 1, 4),
            new Proceso("C", 2, 2));

    private static final List<Proceso> PROCESOS_EXTRA = List.of(
            new Proceso("D", 3, 5),
            new Proceso("E", 4, 6));

    public PlanificadorSJF() {
        super(PROCESOS_INICIALES, PROCESOS_EXTRA, MAXIMO_PROCESOS);
    }

    /** El apto de menor ráfaga; a igual ráfaga, el que llegó antes. */
    @Override
    protected Proceso seleccionar() {
        return procesos.stream()
                .filter(this::esApto)
                .min(Comparator.comparingInt(Proceso::getRafaga)
                        .thenComparingInt(Proceso::getLlegada)
                        .thenComparing(Proceso::getId))
                .orElse(null);
    }
}