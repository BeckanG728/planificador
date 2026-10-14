package es.unjfsc.planificador.compartido.nucleo.algoritmos.apropiativos;

import es.unjfsc.planificador.compartido.nucleo.mock.MockProcesos;
import es.unjfsc.planificador.compartido.nucleo.modelo.EstadoProceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.ResumenMetricas;
import es.unjfsc.planificador.compartido.nucleo.motor.CPU;
import es.unjfsc.planificador.compartido.nucleo.motor.Planificador;

import java.util.*;

/**
 * Round Robin: cola circular con quantum. Cada turno ejecuta hasta Q unidades
 * y luego vuelve al final de la cola.
 */
public class PlanificadorRoundRobin implements Planificador {

    /**
     * Quantum con el que arranca la simulación.
     */
    public static final int QUANTUM_INICIAL = 2;

    private final List<Proceso> procesos = new ArrayList<>();
    private final Queue<Proceso> cola = new ArrayDeque<>();
    private final CPU cpu = new CPU();

    private int reloj;
    private int cambiosContexto;
    private int unidadesEnTurno;
    private boolean ejecutando;
    private boolean pausado;
    private int quantum = QUANTUM_INICIAL;

    public PlanificadorRoundRobin() {
        restablecerProcesos();
    }

    /**
     * Un proceso es apto cuando ya llegó y aún le queda ráfaga.
     */
    private boolean esApto(Proceso proceso) {
        return proceso.getTiempoRestante() > 0
               && !proceso.isTerminado()
               && proceso.getLlegada() <= reloj;
    }

    /**
     * Suelta el CPU: si terminó pasa a terminado, si agotó el quantum
     * vuelve al final de la cola. Los nuevos de este instante ya entraron
     * antes, así que el saliente queda detrás de ellos.
     */
    private void cederTurno(Proceso saliente) {
        if (saliente != null) {
            if (saliente.getTiempoRestante() == 0) {
                saliente.setEstado(EstadoProceso.TERMINADO);
                saliente.setTiempoFinalizacion(reloj);
            } else {
                saliente.setEstado(EstadoProceso.ESPERANDO);
                cola.offer(saliente);
            }
        }
        cpu.liberar();
        unidadesEnTurno = 0;
    }

    @Override
    public void restablecerProcesos() {
        procesos.clear();
        procesos.addAll(MockProcesos.nuevos());
        reiniciar();
    }

    private void reiniciar() {
        cpu.reiniciar();
        cola.clear();
        reloj = 0;
        cambiosContexto = 0;
        unidadesEnTurno = 0;
        ejecutando = false;
        pausado = false;
        for (Proceso proceso : procesos) {
            proceso.reiniciar();
        }
    }

    @Override
    public void iniciar() {
        if (ejecutando) {
            return;
        }
        reiniciar();
        for (Proceso proceso : procesos) {
            if (proceso.getLlegada() == 0) {
                cola.offer(proceso);
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

    @Override
    public void detener() {
        ejecutando = false;
        pausado = false;
    }

    /**
     * Un tick [reloj-1, reloj): el dueño ejecuta 1, los nuevos entran al final de
     * la cola, el saliente (quantum o fin) vuelve detrás de ellos y, si el CPU
     * quedó libre, se despacha al siguiente (rige desde el próximo tick, así nadie
     * ejecuta antes de haber llegado).
     */
    @Override
    public boolean avanzar() {
        if (!ejecutando || pausado) {
            return false;
        }

        String propietarioAnterior = cpu.getDuenoId();
        reloj++;

        if (!cpu.estaLibre()) {
            Proceso enEjecucion = cpu.getDueno();
            if (enEjecucion != null && enEjecucion.getTiempoRestante() > 0) {
                cpu.ejecutar(reloj);
                unidadesEnTurno++;
            }
        }

        for (Proceso proceso : procesos) {
            if (proceso.getLlegada() == reloj
                && proceso.getTiempoRestante() > 0
                && !proceso.isTerminado()
                && !proceso.getId().equals(cpu.getDuenoId())
                && !cola.contains(proceso)) {
                proceso.setEstado(EstadoProceso.ESPERANDO);
                cola.offer(proceso);
            }
        }

        if (!cpu.estaLibre()) {
            Proceso saliente = cpu.getDueno();
            if (saliente == null
                || saliente.getTiempoRestante() == 0
                || unidadesEnTurno >= quantum) {
                cederTurno(saliente);
            }
        }

        if (cpu.estaLibre()) {
            programarSiguiente();
        }

        if (propietarioAnterior != null && cpu.getDuenoId() != null
            && !propietarioAnterior.equals(cpu.getDuenoId())) {
            cambiosContexto++;
        }

        return todosTerminados();
    }

    private void programarSiguiente() {
        if (!cpu.estaLibre()) {
            return;
        }
        Proceso elegido = null;
        while (!cola.isEmpty()) {
            Proceso candidato = cola.peek();
            if (candidato != null && esApto(candidato)) {
                elegido = cola.poll();
                break;
            }
            cola.poll();
        }
        if (elegido == null) {
            return;
        }
        cpu.asignar(elegido);
        unidadesEnTurno = 0;
        elegido.setEstado(EstadoProceso.EJECUTANDO);
        if (elegido.getTiempoInicio() == null) {
            elegido.setTiempoInicio(reloj);
        }
    }

    private boolean todosTerminados() {
        return !procesos.isEmpty() && procesos.stream().allMatch(p -> p.getTiempoRestante() == 0);
    }

    @Override
    public List<Proceso> getProcesos() {
        return List.copyOf(procesos);
    }

    @Override
    public List<Proceso> getProcesosOrdenados() {
        return procesos.stream().sorted(Comparator.comparing(Proceso::getId)).toList();
    }

    @Override
    public int getReloj() {
        return reloj;
    }

    @Override
    public String getMotorPropietario() {
        return cpu.getDuenoId();
    }

    @Override
    public boolean isEjecutando() {
        return ejecutando;
    }

    @Override
    public boolean isPausado() {
        return pausado;
    }

    @Override
    public Map<String, Set<Integer>> mapaEjecucion() {
        return cpu.mapaEjecucion(procesos.stream().map(Proceso::getId).toList());
    }

    @Override
    public int totalColumnasGantt() {
        int ultimaEjecucion = cpu.finMaximo();
        int ultimaLlegada = procesos.stream().mapToInt(Proceso::getLlegada).max().orElse(0);
        return Math.max(ultimaEjecucion, Math.max(ultimaLlegada, reloj)) + 1;
    }

    @Override
    public ResumenMetricas getResumen() {
        return ResumenMetricas.calcular(procesos, reloj, cambiosContexto);
    }

    /**
     * Fija el quantum en unidades de CPU. Solo tiene efecto antes de iniciar.
     */
    public void establecerQuantum(int valor) {
        this.quantum = Math.max(1, valor);
    }

    public int getQuantum() {
        return quantum;
    }
}
