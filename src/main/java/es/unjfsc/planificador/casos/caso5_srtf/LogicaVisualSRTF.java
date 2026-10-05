package es.unjfsc.planificador.casos.caso5_srtf;

import es.unjfsc.planificador.compartido.ui.LogicaVisual;

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