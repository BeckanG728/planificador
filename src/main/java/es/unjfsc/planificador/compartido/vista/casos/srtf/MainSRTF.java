package es.unjfsc.planificador.compartido.vista.casos.srtf;

import es.unjfsc.planificador.compartido.vista.comun.Inicio;

/**
 * Punto de entrada de la simulación SRTF.
 */
public class MainSRTF {

    public static void main(String[] args) {
        Inicio.lanzar(VentanaSRTF::new);
    }
}