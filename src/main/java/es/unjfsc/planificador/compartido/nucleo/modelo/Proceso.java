package es.unjfsc.planificador.compartido.nucleo.modelo;

public class Proceso {

    private final String id;
    private int llegada;
    private int rafaga;
    private int prioridad;

    private int tiempoRestante;
    private EstadoProceso estado;
    private Integer tiempoInicio;
    private Integer tiempoFinalizacion;

    /**
     * Proceso de los casos FIFO, SJF, SRTF y Round Robin: solo lleva C y T.
     */
    public Proceso(String id, int llegada, int rafaga) {
        this(id, llegada, rafaga, 1);
    }

    /**
     * Proceso del caso de prioridades: además de C y T, lleva su prioridad P.
     */
    public Proceso(String id, int llegada, int rafaga, int prioridad) {
        this.id = id;
        setLlegada(llegada);
        setRafaga(rafaga);
        setPrioridad(prioridad);
        reiniciar();
    }

    /**
     * Copia de otro proceso con su misma configuración y su estado inicial.
     */
    public Proceso(Proceso otro) {
        this(otro.id, otro.llegada, otro.rafaga, otro.prioridad);
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

    public int getPrioridad() {
        return prioridad;
    }

    public void setPrioridad(int prioridad) {
        this.prioridad = Math.max(1, prioridad);
    }

    public int getTiempoRestante() {
        return tiempoRestante;
    }

    public EstadoProceso getEstado() {
        return estado;
    }

    public void setEstado(EstadoProceso estado) {
        this.estado = estado;
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

    /**
     * Unidades de CPU ya ejecutadas por el proceso.
     */
    public int getEjecutado() {
        return Math.max(0, rafaga - tiempoRestante);
    }

    /**
     * true si el proceso ya no tiene trabajo pendiente.
     */
    public boolean isTerminado() {
        return estado == EstadoProceso.TERMINADO;
    }

    /**
     * Consume una unidad de CPU.
     */
    public void ejecutarUnidad() {
        if (tiempoRestante > 0) {
            tiempoRestante--;
        }
    }

    /**
     * Restaura el estado inicial del proceso, conservando su configuración.
     */
    public void reiniciar() {
        tiempoRestante = rafaga;
        estado = EstadoProceso.ESPERANDO;
        tiempoInicio = null;
        tiempoFinalizacion = null;
    }
}
