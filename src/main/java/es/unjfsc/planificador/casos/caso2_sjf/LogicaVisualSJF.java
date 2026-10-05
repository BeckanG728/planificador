package es.unjfsc.planificador.casos.caso2_sjf;

import es.unjfsc.planificador.compartido.ui.LogicaVisual;

/** Presentación del caso SJF: Shortest Job First. */
public class LogicaVisualSJF implements LogicaVisual {

    @Override
    public String nombre() {
        return "SJF";
    }

    @Override
    public String insignia() {
        return "SJF \u00B7 no apropiativo";
    }

    @Override
    public String descripcion() {
        return "Shortest Job First: el motor ejecuta cada proceso hasta terminar su ráfaga "
                + "y después despacha al apto más corto.";
    }
}