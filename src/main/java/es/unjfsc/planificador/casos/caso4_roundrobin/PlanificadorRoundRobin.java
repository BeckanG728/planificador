package es.unjfsc.planificador.casos.caso4_roundrobin;

import es.unjfsc.planificador.compartido.modelo.Proceso;
import es.unjfsc.planificador.compartido.motor.PlanificadorBase;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Planificador Round Robin (apropiativo por tiempo) de un único motor de CPU.
 *
 * <p>Los procesos se atienden por orden de llegada y cada uno ocupa el motor durante un
 * quantum de unidades; al cumplirse, vuelve al final de la cola. La cola se representa con
 * un número de turno creciente, de modo que el orden de servicio es exactamente el de una
 * cola circular FIFO alimentada con las llegadas y con las interrupciones.</p>
 *
 * <p>Un proceso que llega en {@code t} con el motor ocupado necesita una unidad para pasar a
 * listo: no compite en el despacho de {@code t} y empieza a hacerlo en {@code t+1}. Por eso
 * una llegada que coincide con el fin del quantum no desaloja al proceso que ya estaba.</p>
 */
public class PlanificadorRoundRobin extends PlanificadorBase {

    /** Quantum con el que arranca la simulación. */
    public static final int QUANTUM_INICIAL = 2;

    private static final int MAXIMO_PROCESOS = 5;

    private static final List<Proceso> PROCESOS_INICIALES = List.of(
            new ProcesoRoundRobin("A", 0, 8),
            new ProcesoRoundRobin("B", 1, 4),
            new ProcesoRoundRobin("C", 2, 2));

    private static final List<Proceso> PROCESOS_EXTRA = List.of(
            new ProcesoRoundRobin("D", 3, 5),
            new ProcesoRoundRobin("E", 4, 6));

    private int contadorSecuencia;
    private int quantum = QUANTUM_INICIAL;

    public PlanificadorRoundRobin() {
        super(PROCESOS_INICIALES, PROCESOS_EXTRA, MAXIMO_PROCESOS);
    }

    // ------------------------------------------------------------------
    // Cola circular por número de turno
    // ------------------------------------------------------------------

    @Override
    protected Proceso crearProceso(Proceso plantilla) {
        return new ProcesoRoundRobin((ProcesoRoundRobin) plantilla);
    }

    @Override
    protected void alReiniciar() {
        contadorSecuencia = 0;
        for (Proceso proceso : procesos) {
            ((ProcesoRoundRobin) proceso).setSecuencia(0);
        }
    }

    @Override
    protected void alLlegar(Proceso proceso) {
        encolar(proceso);
    }

    @Override
    protected void alCeder(Proceso proceso, boolean terminado) {
        if (!terminado) {
            encolar(proceso);
        }
    }

    /** Un proceso terminado no vuelve a la cola. */
    private void encolar(Proceso proceso) {
        ((ProcesoRoundRobin) proceso).setSecuencia(++contadorSecuencia);
    }

    /**
     * El apto con menos turno en la cola.
     *
     * <p>Los que llegan en este mismo instante solo se admiten si no hay ningún otro apto, es
     * decir, si el motor estaría libre; si no, esperan a que pase la unidad de pasar a listo.</p>
     */
    @Override
    protected Proceso seleccionar() {
        Proceso elegido = primeroEnCola(proceso -> proceso.getLlegada() < reloj);
        return elegido != null ? elegido : primeroEnCola(proceso -> true);
    }

    private Proceso primeroEnCola(Predicate<Proceso> filtro) {
        return procesos.stream()
                .filter(this::esApto)
                .filter(filtro)
                .min(Comparator.comparingInt((Proceso proceso) -> ((ProcesoRoundRobin) proceso).getSecuencia())
                        .thenComparing(Proceso::getId))
                .orElse(null);
    }

    // ------------------------------------------------------------------
    // Quantum
    // ------------------------------------------------------------------

    /**
     * Cede el motor al agotar el quantum o la ráfaga.
     *
     * <p>La cesión por quantum no la debe confundir la base con una terminación: eso lo
     * decide {@link #hayTrabajoPendiente(Proceso)}, que solo mira la ráfaga.</p>
     */
    @Override
    protected boolean debeCeder(Proceso proceso) {
        return unidadesEnTurno >= quantum || !hayTrabajoPendiente(proceso);
    }

    /**
     * Fija el quantum en unidades de CPU. Solo tiene efecto en la próxima simulación,
     * porque el valor vigente se toma al ocupar el motor.
     */
    public void establecerQuantum(int valor) {
        this.quantum = Math.max(1, valor);
    }

    public int getQuantum() {
        return quantum;
    }
}