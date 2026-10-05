package es.unjfsc.planificador.casos.caso1_fifo;

import es.unjfsc.planificador.compartido.ui.LogicaVisual;

/** Presentación del caso FIFO: First Come, First Served. */
public class LogicaVisualFIFO implements LogicaVisual {

    @Override
    public String nombre() {
        return "FIFO";
    }

    @Override
    public String insignia() {
        return "FCFS \u00B7 FIFO";
    }

    @Override
    public String descripcion() {
        return "First Come, First Served: el motor ejecuta cada proceso hasta "
                + "terminar su r\u00E1faga y despacha al que lleva m\u00E1s tiempo esperando.";
    }
}