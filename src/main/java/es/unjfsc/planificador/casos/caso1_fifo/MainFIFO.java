package es.unjfsc.planificador.casos.caso1_fifo;

import es.unjfsc.planificador.compartido.ui.Inicio;

/**
 * Punto de entrada de la simulación FIFO.
 */
public class MainFIFO {

    public static void main(String[] args) {
        Inicio.lanzar(VentanaFIFO::new);
    }
}