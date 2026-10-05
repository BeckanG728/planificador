package es.unjfsc.planificador.casos.caso4_roundrobin;

import es.unjfsc.planificador.compartido.ui.Inicio;

/**
 * Punto de entrada de la simulación Round Robin.
 */
public class MainRoundRobin {

    public static void main(String[] args) {
        Inicio.lanzar(VentanaRoundRobin::new);
    }
}