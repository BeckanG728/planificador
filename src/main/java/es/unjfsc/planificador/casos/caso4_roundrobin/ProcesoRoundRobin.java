package es.unjfsc.planificador.casos.caso4_roundrobin;

import es.unjfsc.planificador.compartido.modelo.Proceso;

/**
 * Proceso de Round Robin: además de la configuración común lleva el número de turno que
 * le tocó en la cola.
 *
 * <p>La posición en la cola no se guarda en una estructura aparte sino como un contador
 * monotónico: el proceso que primero entró tiene el número más bajo. Encolar un proceso
 * interrumpido o recién llegado es darle el siguiente número, igual que un push al final de
 * una cola circular. Así la cola no necesita vivir dentro del motor.</p>
 */
public class ProcesoRoundRobin extends Proceso {

    private int secuencia;

    public ProcesoRoundRobin(String id, int llegada, int rafaga) {
        super(id, llegada, rafaga);
    }

    public ProcesoRoundRobin(ProcesoRoundRobin otro) {
        super(otro);
        this.secuencia = otro.secuencia;
    }

    /** Turno en la cola; el menor número es el primero en salir. */
    public int getSecuencia() {
        return secuencia;
    }

    public void setSecuencia(int secuencia) {
        this.secuencia = secuencia;
    }
}