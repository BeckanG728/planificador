package es.unjfsc.planificador.casos.caso3_prioridades;

import es.unjfsc.planificador.compartido.modelo.Proceso;
import es.unjfsc.planificador.compartido.motor.PlanificadorBase;

import java.util.Comparator;
import java.util.List;

/**
 * Planificador por prioridades de un único motor de CPU, en modo apropiativo o no apropiativo.
 *
 * <p>Para despachar escoge, entre los aptos, el de menor prioridad numérica. Dos procesos con
 * la misma prioridad se rompen a favor del de ráfaga más corta; si también empatan en ráfaga,
 * gana el que llegó primero.</p>
 *
 * <p>En modo no apropiativo (el de por defecto) el proceso que ocupa el motor lo conserva
 * hasta terminar su ráfaga. En modo apropiativo lo cede en cuanto hay un apto de prioridad
 * estrictamente mejor; una prioridad igual no lo desaloja.</p>
 */
public class PlanificadorPrioridades extends PlanificadorBase {

    private static final int MAXIMO_PROCESOS = 5;

    private static final List<Proceso> PROCESOS_INICIALES = List.of(
            new ProcesoPrioridad("A", 0, 8, 5),
            new ProcesoPrioridad("B", 1, 4, 1),
            new ProcesoPrioridad("C", 2, 2, 3),
            new ProcesoPrioridad("D", 3, 5, 2),
            new ProcesoPrioridad("E", 4, 6, 4));

    private static final List<Proceso> PROCESOS_EXTRA = List.of();

    private boolean apropiativo;

    public PlanificadorPrioridades() {
        super(PROCESOS_INICIALES, PROCESOS_EXTRA, MAXIMO_PROCESOS);
    }

    @Override
    protected Proceso crearProceso(Proceso plantilla) {
        return new ProcesoPrioridad((ProcesoPrioridad) plantilla);
    }

    /** El apto de mayor prioridad; a igualdad, el de ráfaga más corta; luego, el que llegó primero. */
    @Override
    protected Proceso seleccionar() {
        return procesos.stream()
                .filter(this::esApto)
                .min(Comparator.comparingInt((Proceso proceso) -> ((ProcesoPrioridad) proceso).getPrioridad())
                        .thenComparingInt(Proceso::getRafaga)
                        .thenComparingInt(Proceso::getLlegada)
                        .thenComparing(Proceso::getId))
                .orElse(null);
    }

    /**
     * Cede el motor al agotar la ráfaga o, en modo apropiativo, cuando hay un apto con una
     * prioridad estrictamente mejor que la del proceso en ejecución.
     *
     * <p>{@code esApto} ya cuenta las llegadas del instante actual, así que un proceso que
     * llega en {@code t} desaloja al que ejecutó la unidad {@code t-1..t}.</p>
     */
    @Override
    protected boolean debeCeder(Proceso proceso) {
        if (!hayTrabajoPendiente(proceso)) {
            return true;
        }
        if (!apropiativo) {
            return false;
        }
        int prioridadActual = prioridad(proceso);
        return procesos.stream()
                .filter(otro -> otro != proceso && esApto(otro))
                .anyMatch(otro -> prioridad(otro) < prioridadActual);
    }

    private static int prioridad(Proceso proceso) {
        return ((ProcesoPrioridad) proceso).getPrioridad();
    }

    /**
     * Elige entre apropiativo y no apropiativo. Solo tiene efecto en la próxima simulación:
     * la ventana lo bloquea mientras hay una en curso.
     */
    public void establecerApropiativo(boolean apropiativo) {
        this.apropiativo = apropiativo;
    }

    public boolean isApropiativo() {
        return apropiativo;
    }
}