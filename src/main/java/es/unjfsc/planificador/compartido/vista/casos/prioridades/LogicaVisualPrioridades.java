package es.unjfsc.planificador.compartido.vista.casos.prioridades;

import es.unjfsc.planificador.compartido.nucleo.algoritmos.apropiativos.PlanificadorPrioridadesApropiativo;
import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;
import es.unjfsc.planificador.compartido.nucleo.motor.Planificador;
import es.unjfsc.planificador.compartido.vista.comun.LogicaVisual;
import es.unjfsc.planificador.compartido.vista.comun.ParametroProceso;

import java.util.ArrayList;
import java.util.List;

/**
 * Presentación del caso de prioridades: número menor es mayor prioridad.
 */
public class LogicaVisualPrioridades implements LogicaVisual {

    private final Planificador planificador;

    public LogicaVisualPrioridades(Planificador planificador) {
        this.planificador = planificador;
    }

    private boolean esApropiativo() {
        return planificador instanceof PlanificadorPrioridadesApropiativo;
    }

    @Override
    public String nombre() {
        return "Prioridades";
    }

    @Override
    public String insignia() {
        return "Prioridades \u00B7 " + (esApropiativo() ? "apropiativo" : "no apropiativo");
    }

    @Override
    public String insignia(Planificador planificador) {
        return "Prioridades \u00B7 " + (planificador instanceof PlanificadorPrioridadesApropiativo
                ? "apropiativo" : "no apropiativo");
    }

    @Override
    public String descripcion() {
        return "Planificación por prioridades: se despacha al apto de menor número de "
               + "prioridad y a igual prioridad al que llegó primero. En modo apropiativo, "
               + "un proceso de mejor prioridad desaloja al que ocupa el motor.";
    }

    /**
     * Añade la columna de prioridad a los parámetros comunes C y T.
     */
    @Override
    public List<ParametroProceso> parametros() {
        List<ParametroProceso> parametros = new ArrayList<>(LogicaVisual.super.parametros());
        parametros.add(new ParametroProceso(
                "P",
                "P \u00B7 prioridad, 1 es la más alta",
                Proceso::getPrioridad,
                Proceso::setPrioridad,
                1));
        return List.copyOf(parametros);
    }
}
