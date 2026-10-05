package es.unjfsc.planificador.compartido.motor;

import es.unjfsc.planificador.compartido.modelo.Proceso;
import es.unjfsc.planificador.compartido.modelo.ResumenMetricas;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Contrato común de los motores de planificación.
 *
 * <p>Expone el ciclo de vida de la simulación, el catálogo de procesos y el estado que la vista
 * necesita para pintarse. Cada algoritmo vive en su propio paquete y aporta únicamente la política
 * de selección y cesión mediante {@link PlanificadorBase}.
 */
public interface Planificador {

    // Ciclo de vida de la simulación

    void restablecerProcesos();

    void reiniciar();

    void iniciar();

    void pausar();

    void continuar();

    void detener();

    void establecerVelocidad(double velocidad);

    int periodoTemporizador();

    boolean avanzar();

    // Catálogo de procesos

    List<Proceso> getProcesos();

    List<Proceso> getProcesosOrdenados();

    Proceso buscarProceso(String id);

    int maximoProcesos();

    int minimoProcesos();

    Proceso getSiguienteProcesoExtra();

    Proceso crearProcesoExtra(int llegada, int rafaga);

    void agregarProceso(Proceso proceso);

    boolean quitarProceso();

    // Estado para la vista

    int getReloj();

    String getMotorPropietario();

    boolean isEjecutando();

    boolean isPausado();

    Map<String, Set<Integer>> mapaEjecucion();

    int totalColumnasGantt();

    ResumenMetricas getResumen();
}