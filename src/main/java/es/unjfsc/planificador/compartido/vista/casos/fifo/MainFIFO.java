package es.unjfsc.planificador.compartido.vista.casos.fifo;

import es.unjfsc.planificador.compartido.vista.comun.Inicio;

/**
 * Punto de entrada de la simulación FIFO.
 */
public class MainFIFO {

    public static void main(String[] args) {
        Inicio.lanzar(VentanaFIFO::new);
    }
}