package es.unjfsc.planificador.casos.caso1_fifo;

import es.unjfsc.planificador.compartido.ui.VentanaSimulacion;

/** Ventana de la simulación con planificación FIFO. */
public class VentanaFIFO extends VentanaSimulacion {

    public VentanaFIFO() {
        super(new PlanificadorFIFO(), new LogicaVisualFIFO());
    }
}