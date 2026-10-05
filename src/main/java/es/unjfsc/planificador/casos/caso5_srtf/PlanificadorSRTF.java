package es.unjfsc.planificador.casos.caso5_srtf;

import es.unjfsc.planificador.compartido.modelo.Proceso;
import es.unjfsc.planificador.compartido.motor.PlanificadorBase;

import java.util.Comparator;
import java.util.List;

/**
 * Planificador SRTF (Shortest Remaining Time First, apropiativo) de un único motor de CPU.
 *
 * <p>Se parece a SJF en que se elige el apto más corto, pero aquí la ráfaga restante sí baja
 * conforme el proceso avanza, de modo que hay que volver a comparar en cada momento. Cuando
 * llega un proceso con menos trabajo pendiente que el que está en el motor, lo suspende.</p>
 */
public class PlanificadorSRTF extends PlanificadorBase {

    private static final int MAXIMO_PROCESOS = 5;

    private static final List<Proceso> PROCESOS_INICIALES = List.of(
            new Proceso("A", 0, 8),
            new Proceso("B", 1, 4),
            new Proceso("C", 2, 2));

    private static final List<Proceso> PROCESOS_EXTRA = List.of(
            new Proceso("D", 3, 5),
            new Proceso("E", 4, 6));

    public PlanificadorSRTF() {
        super(PROCESOS_INICIALES, PROCESOS_EXTRA, MAXIMO_PROCESOS);
    }

    /** El apto con menos tiempo restante; a igualdad, el que llegó antes. */
    @Override
    protected Proceso seleccionar() {
        return procesos.stream()
                .filter(this::esApto)
                .min(Comparator.comparingInt(Proceso::getTiempoRestante)
                        .thenComparingInt(Proceso::getLlegada)
                        .thenComparing(Proceso::getId))
                .orElse(null);
    }

    /**
     * Suspende al dueño del motor si el proceso que acaba de llegar necesita menos tiempo
     * que él. En caso de empate mantiene al dueño: dos procesos empatados no se expulsan
     * mutuamente del motor.
     */
    @Override
    protected void alLlegar(Proceso proceso) {
        if (motorPropietario == null || motorPropietario.equals(proceso.getId())) {
            return;
        }
        Proceso enEjecucion = buscarProceso(motorPropietario);
        if (enEjecucion != null && proceso.getTiempoRestante() < enEjecucion.getTiempoRestante()) {
            cederTurno(enEjecucion);
        }
    }
}