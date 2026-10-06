package es.unjfsc.planificador.compartido.vista.casos.srtf;

import es.unjfsc.planificador.compartido.nucleo.algoritmos.srtf.PlanificadorSRTF;
import es.unjfsc.planificador.compartido.vista.comun.VentanaSimulacion;

/** Ventana de la simulación con planificación SRTF. */
public class VentanaSRTF extends VentanaSimulacion {

    public VentanaSRTF() {
        super(new PlanificadorSRTF(), new LogicaVisualSRTF());
    }
}