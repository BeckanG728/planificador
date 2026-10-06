package es.unjfsc.planificador.compartido.nucleo.motor;

import es.unjfsc.planificador.compartido.nucleo.modelo.EstadoProceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.ResumenMetricas;
import es.unjfsc.planificador.compartido.nucleo.modelo.Segmento;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Esqueleto común de los motores de planificación de un único motor de CPU.
 *
 * <p>Esta clase no depende de Swing: contiene el estado y el avance de la simulación. Cada
 * algoritmo aporta únicamente su política, implementando {@link #seleccionar()} y, si es
 * apropiativo, {@link #debeCeder(Proceso)} y {@link #hayTrabajoPendiente(Proceso)}. Los que
 * necesitan datos propios del proceso usan {@link #crearProceso(Proceso)}, y los que llevan
 * estado propio entre paso y paso usan {@link #alReiniciar()}, {@link #alLlegar(Proceso)} y
 * {@link #alCeder(Proceso, boolean)}.</p>
 *
 * <p>Los procesos iniciales y las plantillas opcionales los recibe cada caso, de modo que el
 * esqueleto no conoce ningún algoritmo concreto.</p>
 */
public abstract class PlanificadorBase implements Planificador {

    private final List<Proceso> procesosIniciales;
    private final List<Proceso> procesosExtra;
    private final int maximoProcesos;

    protected final List<Proceso> procesos = new ArrayList<>();
    protected final List<Segmento> segmentos = new ArrayList<>();
    protected final Set<String> llegados = new HashSet<>();

    protected int reloj;
    protected String motorPropietario;
    protected int cambiosContexto;
    protected int unidadesEnTurno;
    protected boolean ejecutando;
    protected boolean pausado;
    protected double velocidad = 1;
    protected int indiceExtra;

    /**
     * Saliente cuyo turno terminó en este instante y cuya devolución a la cola se difiere
     * hasta después de admitir las llegadas, para que un proceso que llega justo cuando
     * acaba el quantum pase antes que el saliente.
     */
    private Proceso pendienteDeReencolar;

    protected PlanificadorBase(List<Proceso> procesosIniciales, List<Proceso> procesosExtra, int maximoProcesos) {
        this.procesosIniciales = List.copyOf(procesosIniciales);
        this.procesosExtra = List.copyOf(procesosExtra);
        this.maximoProcesos = maximoProcesos;
        restablecerProcesos();
    }

    // ------------------------------------------------------------------
    // Política del algoritmo
    // ------------------------------------------------------------------

    /** Elige el próximo proceso apto según la política del algoritmo. */
    protected abstract Proceso seleccionar();

    /**
     * Indica si el proceso cede el motor tras la unidad recién ejecutada.
     * Por defecto solo cede cuando agota su ráfaga.
     */
    protected boolean debeCeder(Proceso proceso) {
        return proceso.getTiempoRestante() == 0;
    }

    /**
     * Indica si al proceso le queda trabajo por hacer. Determina si la cesión fue una
     * interrupción (vuelve a la cola) o una terminación. Por defecto, solo al agotar la ráfaga.
     */
    protected boolean hayTrabajoPendiente(Proceso proceso) {
        return proceso.getTiempoRestante() > 0;
    }

    /**
     * Crea la copia de trabajo de un proceso de la plantilla. Por defecto devuelve un
     * {@link Proceso} plano; los algoritmos con datos propios lo sobreescriben para clonar
     * también esos datos.
     *
     * <p>Se invoca desde el constructor de esta clase, así que la implementación no debe
     * leer campos del algoritmo: solo datos de la plantilla.</p>
     */
    protected Proceso crearProceso(Proceso plantilla) {
        return new Proceso(plantilla);
    }

    /** Permite al algoritmo reiniciar el estado propio que lleva entre ejecuciones. */
    protected void alReiniciar() {
    }

    /** Avisa de que un proceso acaba de entrar al sistema y está en la cola de aptos. */
    protected void alLlegar(Proceso proceso) {
    }

    /**
     * Avisa de que un proceso suelta el motor.
     *
     * @param terminado true si la cesión fue la última, false si fue una interrupción
     */
    protected void alCeder(Proceso proceso, boolean terminado) {
    }

    /**
     * Suelta el motor sin despachar a nadie: el proceso terminado pasa a terminado y el
     * interrumpido vuelve a la cola. El despacho ocurre después, en un solo punto del
     * esqueleto, para que un algoritmo apropiativo pueda ceder entre llegadas del mismo
     * instante y aun así despachar con la cola completa.
     */
    protected void cederTurno(Proceso saliente) {
        if (saliente != null) {
            boolean terminado = !hayTrabajoPendiente(saliente);
            soltarTurno(saliente, terminado);
            if (terminado) {
                alCeder(saliente, true);
            } else {
                reencolarPendiente();
                pendienteDeReencolar = saliente;
            }
        }
        motorPropietario = null;
        unidadesEnTurno = 0;
    }

    /**
     * Devuelve a la cola al saliente interrumpido cuyo reencolado quedó diferido. Se invoca
     * después de admitir las llegadas del instante y antes de despachar.
     */
    private void reencolarPendiente() {
        if (pendienteDeReencolar != null) {
            Proceso saliente = pendienteDeReencolar;
            pendienteDeReencolar = null;
            alCeder(saliente, false);
        }
    }

    // ------------------------------------------------------------------
    // Ciclo de vida de la simulación
    // ------------------------------------------------------------------

    /** Restaura la lista inicial de procesos y reinicia el estado. */
    @Override
    public void restablecerProcesos() {
        procesos.clear();
        for (Proceso inicial : procesosIniciales) {
            procesos.add(crearProceso(inicial));
        }
        indiceExtra = 0;
        reiniciar();
    }

    /** Reinicia el estado de la simulación conservando los procesos configurados. */
    @Override
    public void reiniciar() {
        segmentos.clear();
        llegados.clear();
        reloj = 0;
        motorPropietario = null;
        cambiosContexto = 0;
        unidadesEnTurno = 0;
        pendienteDeReencolar = null;
        ejecutando = false;
        pausado = false;
        for (Proceso proceso : procesos) {
            proceso.reiniciar();
        }
        alReiniciar();
    }

    @Override
    public void iniciar() {
        if (ejecutando) {
            return;
        }
        reiniciar();
        for (Proceso proceso : procesos) {
            if (proceso.getLlegada() == 0) {
                llegados.add(proceso.getId());
                alLlegar(proceso);
            }
        }
        programarSiguiente();
        ejecutando = true;
        pausado = false;
    }

    @Override
    public void pausar() {
        if (ejecutando) {
            pausado = true;
        }
    }

    @Override
    public void continuar() {
        if (ejecutando) {
            pausado = false;
        }
    }

    /** Detiene la simulación dejando el estado actual visible en la vista. */
    @Override
    public void detener() {
        ejecutando = false;
        pausado = false;
    }

    @Override
    public void establecerVelocidad(double velocidad) {
        this.velocidad = Math.max(0.1, velocidad);
    }

    /** Periodo base del temporizador: 500 ms a velocidad 1x. */
    @Override
    public int periodoTemporizador() {
        return (int) Math.max(20, Math.round(500 / velocidad));
    }

    // ------------------------------------------------------------------
    // Núcleo de la simulación
    // ------------------------------------------------------------------

    /**
     * Avanza una unidad de tiempo (un tick de simulación).
     *
     * <p>En cada instante ocurre en este orden: el dueño ejecuta su unidad, entran las
     * llegadas del instante, el que agotó su turno vuelve al final de la cola (detrás de
     * esas llegadas) y por último se despacha. Así un proceso que llega en {@code t} suspende
     * a quien llevaba ejecutando desde {@code t-1}, y el despacho se hace siempre con la cola
     * completa, incluida la llegada de ese mismo instante.</p>
     *
     * @return true si todos los procesos han terminado
     */
    @Override
    public boolean avanzar() {
        if (!ejecutando || pausado) {
            return false;
        }

        String propietarioAnterior = motorPropietario;
        reloj++;

        for (Proceso proceso : procesos) {
            proceso.ajustarTiempoEspera(reloj);
        }

        if (motorPropietario != null) {
            Proceso enEjecucion = buscarProceso(motorPropietario);
            if (enEjecucion != null && enEjecucion.getTiempoRestante() > 0) {
                enEjecucion.ejecutarUnidad();
                unidadesEnTurno++;
                registrarSegmento(enEjecucion.getId());
                if (debeCeder(enEjecucion)) {
                    cederTurno(enEjecucion);
                }
            } else {
                cederTurno(enEjecucion);
            }
        }

        for (Proceso proceso : procesos) {
            if (proceso.getLlegada() == reloj
                    && !llegados.contains(proceso.getId())
                    && proceso.getTiempoRestante() > 0
                    && !proceso.isTerminado()) {
                llegados.add(proceso.getId());
                if (proceso.getEstado() != EstadoProceso.EJECUTANDO) {
                    proceso.setEstado(EstadoProceso.ESPERANDO);
                }
                alLlegar(proceso);
            }
        }

        reencolarPendiente();

        if (motorPropietario == null) {
            programarSiguiente();
        }

        if (propietarioAnterior != null && motorPropietario != null
                && !propietarioAnterior.equals(motorPropietario)) {
            cambiosContexto++;
        }

        return todosTerminados();
    }

    private void programarSiguiente() {
        if (motorPropietario != null) {
            return;
        }
        Proceso elegido = seleccionar();

        if (elegido == null) {
            motorPropietario = null;
            return;
        }

        motorPropietario = elegido.getId();
        unidadesEnTurno = 0;
        elegido.setEstado(EstadoProceso.EJECUTANDO);
        if (elegido.getTiempoInicio() == null) {
            elegido.setTiempoInicio(reloj);
        }
    }

    /** Un proceso es apto cuando le queda ráfaga y ya ha llegado al sistema. */
    protected boolean esApto(Proceso proceso) {
        return proceso.getTiempoRestante() > 0
                && !proceso.isTerminado()
                && proceso.getLlegada() <= reloj;
    }

    private boolean todosTerminados() {
        return !procesos.isEmpty() && procesos.stream().allMatch(p -> p.getTiempoRestante() == 0);
    }

    /** Termina el proceso si ya no le queda trabajo; si fue interrumpido, vuelve a la cola. */
    private void soltarTurno(Proceso proceso, boolean terminado) {
        if (terminado) {
            proceso.setEstado(EstadoProceso.TERMINADO);
            proceso.setTiempoFinalizacion(reloj);
        } else {
            proceso.setEstado(EstadoProceso.ESPERANDO);
        }
    }

    private void registrarSegmento(String procesoId) {
        int inicio = reloj - 1;
        if (!segmentos.isEmpty()) {
            Segmento ultimo = segmentos.get(segmentos.size() - 1);
            if (ultimo.procesoId().equals(procesoId) && ultimo.fin() == inicio) {
                segmentos.set(segmentos.size() - 1, new Segmento(ultimo.inicio(), reloj, procesoId));
                return;
            }
        }
        segmentos.add(new Segmento(inicio, reloj, procesoId));
    }

    // ------------------------------------------------------------------
    // Gestión de procesos
    // ------------------------------------------------------------------

    /**
     * Plantilla del siguiente proceso opcional (id, llegada y ráfaga por defecto).
     * No consume la plantilla: solo la consulta.
     *
     * @return la plantilla, o null si ya no hay procesos opcionales disponibles
     */
    @Override
    public Proceso getSiguienteProcesoExtra() {
        if (indiceExtra >= procesosExtra.size()) {
            return null;
        }
        return crearProceso(procesosExtra.get(indiceExtra));
    }

    /**
     * Crea el siguiente proceso opcional a partir de la plantilla (D, E, ...)
     * con los valores indicados.
     *
     * @return el proceso creado, o null si no quedan plantillas disponibles
     */
    @Override
    public Proceso crearProcesoExtra(int llegada, int rafaga) {
        if (indiceExtra >= procesosExtra.size() || procesos.size() >= maximoProcesos) {
            return null;
        }
        Proceso plantilla = procesosExtra.get(indiceExtra);
        indiceExtra++;
        Proceso nuevo = crearProceso(plantilla);
        nuevo.setLlegada(llegada);
        nuevo.setRafaga(rafaga);
        return nuevo;
    }

    @Override
    public int maximoProcesos() {
        return maximoProcesos;
    }

    @Override
    public int minimoProcesos() {
        return procesosIniciales.size();
    }

    /** Agrega un proceso; si la simulación está detenida, reinicia el estado. */
    @Override
    public void agregarProceso(Proceso proceso) {
        if (procesos.size() >= maximoProcesos) {
            throw new IllegalStateException("Máximo " + maximoProcesos + " procesos alcanzado.");
        }
        procesos.add(proceso);

        if (!ejecutando) {
            reiniciar();
            return;
        }
        if (proceso.getLlegada() <= reloj) {
            llegados.add(proceso.getId());
            alLlegar(proceso);
            reencolarPendiente();
            if (motorPropietario == null) {
                programarSiguiente();
            }
        }
    }

    /** Quita el último proceso agregado, solo con la simulación detenida. */
    @Override
    public boolean quitarProceso() {
        if (ejecutando || procesos.size() <= minimoProcesos()) {
            return false;
        }
        procesos.remove(procesos.size() - 1);
        indiceExtra = Math.max(0, indiceExtra - 1);
        reiniciar();
        return true;
    }

    // ------------------------------------------------------------------
    // Consultas para la vista
    // ------------------------------------------------------------------

    @Override
    public List<Proceso> getProcesos() {
        return List.copyOf(procesos);
    }

    /** Procesos ordenados por identificador, como en las pistas y el Gantt. */
    @Override
    public List<Proceso> getProcesosOrdenados() {
        return procesos.stream().sorted(Comparator.comparing(Proceso::getId)).toList();
    }

    @Override
    public Proceso buscarProceso(String id) {
        return procesos.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    @Override
    public int getReloj() {
        return reloj;
    }

    @Override
    public String getMotorPropietario() {
        return motorPropietario;
    }

    /** Número de cambios de contexto entre procesos. */
    public int getCambiosContexto() {
        return cambiosContexto;
    }

    @Override
    public boolean isEjecutando() {
        return ejecutando;
    }

    @Override
    public boolean isPausado() {
        return pausado;
    }

    public double getVelocidad() {
        return velocidad;
    }

    /** Segmentos del diagrama de Gantt, en orden de ejecución. */
    public List<Segmento> getSegmentos() {
        return List.copyOf(segmentos);
    }

    /** Instantes t en los que cada proceso ocupa el motor. */
    @Override
    public Map<String, Set<Integer>> mapaEjecucion() {
        Map<String, Set<Integer>> mapa = new LinkedHashMap<>();
        for (Proceso proceso : procesos) {
            mapa.put(proceso.getId(), new HashSet<>());
        }
        for (Segmento segmento : segmentos) {
            Set<Integer> instantes = mapa.get(segmento.procesoId());
            if (instantes == null) {
                continue;
            }
            for (int t = segmento.inicio(); t < segmento.fin(); t++) {
                instantes.add(t);
            }
        }
        return mapa;
    }

    /** Número de columnas del diagrama de Gantt. */
    @Override
    public int totalColumnasGantt() {
        int ultimaEjecucion = segmentos.stream().mapToInt(Segmento::fin).max().orElse(0);
        int ultimaLlegada = procesos.stream().mapToInt(Proceso::getLlegada).max().orElse(0);
        return Math.max(ultimaEjecucion, Math.max(ultimaLlegada, reloj)) + 1;
    }

    @Override
    public ResumenMetricas getResumen() {
        return ResumenMetricas.calcular(procesos, cambiosContexto);
    }
}