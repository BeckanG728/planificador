package es.unjfsc.planificador.compartido.vista.casos.sjf;

import es.unjfsc.planificador.compartido.nucleo.algoritmos.sjf.PlanificadorSJF;
import es.unjfsc.planificador.compartido.vista.comun.VentanaSimulacion;

/** Ventana de la simulación con planificación SJF. */
public class VentanaSJF extends VentanaSimulacion {

    public VentanaSJF() {
        super(new PlanificadorSJF(), new LogicaVisualSJF());
    }
}