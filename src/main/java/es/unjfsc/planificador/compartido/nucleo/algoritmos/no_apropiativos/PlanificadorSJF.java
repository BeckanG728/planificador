package es.unjfsc.planificador.compartido.nucleo.algoritmos.no_apropiativos;

import es.unjfsc.planificador.compartido.nucleo.mock.MockProcesos;
import es.unjfsc.planificador.compartido.nucleo.modelo.EstadoProceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.ResumenMetricas;
import es.unjfsc.planificador.compartido.nucleo.motor.CPU;
import es.unjfsc.planificador.compartido.nucleo.motor.Planificador;

import java.util.*;

/**
 * SJF no apropiativo: al quedar libre el CPU se elige al apto con ráfaga más corta.
 */
public class PlanificadorSJF implements Planificador {

    private final List<Proceso> procesos = new ArrayList<>();
    private final CPU cpu = new CPU();

    private int reloj;
    private int cambiosContexto;
    private boolean ejecutando;
    private boolean pausado;

    public PlanificadorSJF() {
        restablecerProcesos();
    }

    /**
     * El apto de menor ráfaga; a igual ráfaga, el que llegó antes.
     */
    private Proceso seleccionar() {
        return procesos.stream()
                .filter(this::esApto)
                .min(Comparator.comparingInt(Proceso::getRafaga)
                        .thenComparingInt(Proceso::getLlegada)
                        .thenComparing(Proceso::getId))
                .orElse(null);
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
     * El dueño solo cede al terminar: pasa a terminado.
     */
    private void cederTurno(Proceso saliente) {
        if (saliente != null) {
            saliente.setEstado(EstadoProceso.TERMINADO);
            saliente.setTiempoFinalizacion(reloj);
        }
        cpu.liberar();
    }

    @Override
    public void restablecerProcesos() {
        procesos.clear();
        procesos.addAll(MockProcesos.nuevos());
        reiniciar();
    }

    private void reiniciar() {
        cpu.reiniciar();
        reloj = 0;
        cambiosContexto = 0;
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
     * Un tick [reloj-1, reloj): el dueño ejecuta 1, entran las llegadas de ese
     * instante y, si el CPU quedó libre, se despacha al siguiente (rige desde el
     * próximo tick, así nadie ejecuta antes de haber llegado).
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
                if (enEjecucion.getTiempoRestante() == 0) {
                    cederTurno(enEjecucion);
                }
            } else {
                cederTurno(enEjecucion);
            }
        }

        for (Proceso proceso : procesos) {
            if (proceso.getLlegada() == reloj
                && proceso.getTiempoRestante() > 0
                && !proceso.isTerminado()
                && !proceso.getId().equals(cpu.getDuenoId())) {
                proceso.setEstado(EstadoProceso.ESPERANDO);
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
        Proceso elegido = seleccionar();
        if (elegido == null) {
            return;
        }
        cpu.asignar(elegido);
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
}
