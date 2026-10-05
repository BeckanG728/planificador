package es.unjfsc.planificador.casos.caso3_prioridades;

import es.unjfsc.planificador.compartido.modelo.Proceso;

/**
 * Proceso con prioridad: además de la configuración común lleva su prioridad, donde un
 * número menor significa mayor prioridad.
 */
public class ProcesoPrioridad extends Proceso {

    private int prioridad;

    public ProcesoPrioridad(String id, int llegada, int rafaga, int prioridad) {
        super(id, llegada, rafaga);
        setPrioridad(prioridad);
    }

    public ProcesoPrioridad(ProcesoPrioridad otro) {
        super(otro);
        this.prioridad = otro.prioridad;
    }

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = Math.max(1, prioridad);
    }

    @Override
    public String toString() {
        return "Proceso[" + getId() + ", C=" + getLlegada() + ", T=" + getRafaga() + ", P=" + prioridad + "]";
    }
}