package es.unjfsc.planificador.compartido.vista.casos.fifo;

import es.unjfsc.planificador.compartido.nucleo.algoritmos.fifo.PlanificadorFIFO;
import es.unjfsc.planificador.compartido.vista.comun.VentanaSimulacion;

/** Ventana de la simulación con planificación FIFO. */
public class VentanaFIFO extends VentanaSimulacion {

    public VentanaFIFO() {
        super(new PlanificadorFIFO(), new LogicaVisualFIFO());
    }
}