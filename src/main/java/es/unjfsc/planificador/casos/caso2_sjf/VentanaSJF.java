package es.unjfsc.planificador.casos.caso2_sjf;

import es.unjfsc.planificador.compartido.ui.VentanaSimulacion;

/** Ventana de la simulación con planificación SJF. */
public class VentanaSJF extends VentanaSimulacion {

    public VentanaSJF() {
        super(new PlanificadorSJF(), new LogicaVisualSJF());
    }
}