package es.unjfsc.planificador.casos.caso5_srtf;

import es.unjfsc.planificador.compartido.ui.VentanaSimulacion;

/** Ventana de la simulación con planificación SRTF. */
public class VentanaSRTF extends VentanaSimulacion {

    public VentanaSRTF() {
        super(new PlanificadorSRTF(), new LogicaVisualSRTF());
    }
}