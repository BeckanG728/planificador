package es.unjfsc.planificador.compartido.nucleo.modelo;

/**
 * Proceso de la simulación: equivalente a un auto de la carrera.
 *
 * <p>Configuración: identificador, instante de llegada (C) y ráfaga de CPU (T).
 * Estado de ejecución: unidades restantes, estado, tiempo de espera,
 * instante de inicio e instante de finalización.</p>
 */
public class Proceso {

    private final String id;
    private int llegada;
    private int rafaga;

    private int tiempoRestante;
    private EstadoProceso estado;
    private int tiempoEspera;
    private Integer tiempoInicio;
    private Integer tiempoFinalizacion;

    public Proceso(String id, int llegada, int rafaga) {
        this.id = id;
        setLlegada(llegada);
        setRafaga(rafaga);
        reiniciar();
    }

    /** Copia de otro proceso con su misma configuración y su estado inicial. */
    public Proceso(Proceso otro) {
        this(otro.id, otro.llegada, otro.rafaga);
    }

    public String getId() {
        return id;
    }

    public int getLlegada() {
        return llegada;
    }

    public void setLlegada(int llegada) {
        this.llegada = Math.max(0, llegada);
    }

    public int getRafaga() {
        return rafaga;
    }

    public void setRafaga(int rafaga) {
        this.rafaga = Math.max(1, rafaga);
    }

    public int getTiempoRestante() {
        return tiempoRestante;
    }

    public void setTiempoRestante(int tiempoRestante) {
        this.tiempoRestante = Math.max(0, tiempoRestante);
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
    }

    public int getTiempoEspera() {
        return tiempoEspera;
    }

    public Integer getTiempoInicio() {
        return tiempoInicio;
    }

    public void setTiempoInicio(Integer tiempoInicio) {
        this.tiempoInicio = tiempoInicio;
    }

    public Integer getTiempoFinalizacion() {
        return tiempoFinalizacion;
    }

    public void setTiempoFinalizacion(Integer tiempoFinalizacion) {
        this.tiempoFinalizacion = tiempoFinalizacion;
    }

    /** Unidades de CPU ya ejecutadas por el proceso. */
    public int getEjecutado() {
        return Math.max(0, rafaga - tiempoRestante);
    }

    /** true si el proceso ya no tiene trabajo pendiente. */
    public boolean isTerminado() {
        return estado == EstadoProceso.TERMINADO;
    }

    /**
     * Suma una unidad al tiempo de espera si el proceso sigue en espera.
     *
     * <p>Un proceso solo espera a partir del instante siguiente al de su llegada,
     * de modo que la espera medida cumple {@code E = F - C - T}.</p>
     *
     * @param reloj instante actual de la simulación
     */
    public void ajustarTiempoEspera(int reloj) {
        if (estado == EstadoProceso.ESPERANDO && tiempoRestante > 0 && llegada < reloj) {
            tiempoEspera++;
        }
    }

    /** Consume una unidad de CPU. */
    public void ejecutarUnidad() {
        if (tiempoRestante > 0) {
            tiempoRestante--;
        }
    }

    /** Restaura el estado inicial del proceso, conservando su configuración. */
    public void reiniciar() {
        tiempoRestante = rafaga;
        estado = EstadoProceso.ESPERANDO;
        tiempoEspera = 0;
        tiempoInicio = null;
        tiempoFinalizacion = null;
    }

    @Override
    public String toString() {
        return "Proceso[" + id + ", C=" + llegada + ", T=" + rafaga + "]";
    }
}