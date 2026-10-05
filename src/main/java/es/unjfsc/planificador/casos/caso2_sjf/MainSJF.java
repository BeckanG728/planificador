package es.unjfsc.planificador.casos.caso2_sjf;

import es.unjfsc.planificador.compartido.ui.Inicio;

/**
 * Punto de entrada de la simulación SJF.
 */
public class MainSJF {

    public static void main(String[] args) {
        Inicio.lanzar(VentanaSJF::new);
    }
}