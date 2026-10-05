package es.unjfsc.planificador.casos.caso3_prioridades;

import es.unjfsc.planificador.compartido.ui.Inicio;

/**
 * Punto de entrada de la simulación por prioridades.
 */
public class MainPrioridades {

    public static void main(String[] args) {
        Inicio.lanzar(VentanaPrioridades::new);
    }
}