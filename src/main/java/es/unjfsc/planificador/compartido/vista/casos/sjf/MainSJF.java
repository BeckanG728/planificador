package es.unjfsc.planificador.compartido.vista.casos.sjf;

import es.unjfsc.planificador.compartido.vista.comun.Inicio;

/**
 * Punto de entrada de la simulación SJF.
 */
public class MainSJF {

    public static void main(String[] args) {
        Inicio.lanzar(VentanaSJF::new);
    }
}