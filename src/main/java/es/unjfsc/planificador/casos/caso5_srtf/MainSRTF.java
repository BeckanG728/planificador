package es.unjfsc.planificador.casos.caso5_srtf;

import es.unjfsc.planificador.compartido.ui.Inicio;

/**
 * Punto de entrada de la simulación SRTF.
 */
public class MainSRTF {

    public static void main(String[] args) {
        Inicio.lanzar(VentanaSRTF::new);
    }
}