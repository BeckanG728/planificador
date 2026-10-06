package es.unjfsc.planificador.compartido.vista.casos.roundrobin;

import es.unjfsc.planificador.compartido.nucleo.algoritmos.roundrobin.PlanificadorRoundRobin;
import es.unjfsc.planificador.compartido.vista.comun.LogicaVisual;

/** Presentación del caso Round Robin, con el quantum vigente en la insignia. */
public class LogicaVisualRoundRobin implements LogicaVisual {

    private final PlanificadorRoundRobin planificador;

    public LogicaVisualRoundRobin(PlanificadorRoundRobin planificador) {
        this.planificador = planificador;
    }

    @Override
    public String nombre() {
        return "Round Robin";
    }

    @Override
    public String insignia() {
        return "RR \u00B7 Q = " + planificador.getQuantum();
    }

    @Override
    public String descripcion() {
        return "Round Robin: cada proceso ocupa el motor un quantum de tiempo y luego vuelve "
                + "al final de la cola.";
    }
}