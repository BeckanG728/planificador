package es.unjfsc.planificador.compartido.vista.casos.srtf;

import es.unjfsc.planificador.compartido.vista.comun.LogicaVisual;

/** Presentación del caso SRTF: Shortest Remaining Time First. */
public class LogicaVisualSRTF implements LogicaVisual {

    @Override
    public String nombre() {
        return "SRTF";
    }

    @Override
    public String insignia() {
        return "SRTF \u00B7 apropiativo";
    }

    @Override
    public String descripcion() {
        return "Shortest Remaining Time First: se despacha al proceso con menos tiempo "
                + "restante y se suspende al dueño en cuanto entra uno más corto.";
    }
}