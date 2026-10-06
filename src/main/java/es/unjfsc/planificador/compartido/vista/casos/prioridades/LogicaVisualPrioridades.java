package es.unjfsc.planificador.compartido.vista.casos.prioridades;

import es.unjfsc.planificador.compartido.nucleo.algoritmos.prioridades.PlanificadorPrioridades;
import es.unjfsc.planificador.compartido.nucleo.algoritmos.prioridades.ProcesoPrioridad;
import es.unjfsc.planificador.compartido.vista.comun.LogicaVisual;
import es.unjfsc.planificador.compartido.vista.comun.ParametroProceso;

import java.util.ArrayList;
import java.util.List;

/** Presentación del caso de prioridades: número menor es mayor prioridad, con el modo vigente en la insignia. */
public class LogicaVisualPrioridades implements LogicaVisual {

    private final PlanificadorPrioridades planificador;

    public LogicaVisualPrioridades(PlanificadorPrioridades planificador) {
        this.planificador = planificador;
    }

    @Override
    public String nombre() {
        return "Prioridades";
    }

    @Override
    public String insignia() {
        return "Prioridades \u00B7 " + (planificador.isApropiativo() ? "apropiativo" : "no apropiativo");
    }

    @Override
    public String descripcion() {
        return "Planificación por prioridades: se despacha al apto de menor número de "
                + "prioridad y a igual prioridad al que llegó primero. En modo apropiativo, "
                + "un proceso de mejor prioridad desaloja al que ocupa el motor.";
    }

    /** Añade la columna de prioridad a los parámetros comunes C y T. */
    @Override
    public List<ParametroProceso> parametros() {
        List<ParametroProceso> parametros = new ArrayList<>(LogicaVisual.super.parametros());
        parametros.add(new ParametroProceso(
                "P",
                "P \u00B7 prioridad, 1 es la más alta",
                proceso -> ((ProcesoPrioridad) proceso).getPrioridad(),
                (proceso, valor) -> ((ProcesoPrioridad) proceso).setPrioridad(valor),
                1));
        return List.copyOf(parametros);
    }
}