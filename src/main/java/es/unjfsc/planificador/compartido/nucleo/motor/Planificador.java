package es.unjfsc.planificador.compartido.nucleo.motor;

import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.ResumenMetricas;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Planificador {

    void restablecerProcesos();

    void iniciar();

    void pausar();

    void continuar();

    void detener();

    boolean avanzar();

    // Catálogo de procesos

    List<Proceso> getProcesos();

    List<Proceso> getProcesosOrdenados();

    // Estado para la vista

    int getReloj();

    String getMotorPropietario();

    boolean isEjecutando();

    boolean isPausado();

    Map<String, Set<Integer>> mapaEjecucion();

    int totalColumnasGantt();

    ResumenMetricas getResumen();
}