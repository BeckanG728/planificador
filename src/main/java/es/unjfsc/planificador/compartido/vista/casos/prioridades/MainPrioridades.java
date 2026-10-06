package es.unjfsc.planificador.compartido.vista.casos.prioridades;

import es.unjfsc.planificador.compartido.vista.comun.Inicio;

/**
 * Punto de entrada de la simulación por prioridades.
 */
public class MainPrioridades {

    public static void main(String[] args) {
        Inicio.lanzar(VentanaPrioridades::new);
    }
}