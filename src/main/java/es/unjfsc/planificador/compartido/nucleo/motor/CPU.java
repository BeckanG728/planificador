package es.unjfsc.planificador.compartido.nucleo.motor;

import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.Segmento;

import java.util.*;

public class CPU {

    private Proceso dueno;
    private final List<Segmento> segmentos = new ArrayList<>();

    public void asignar(Proceso proceso) {
        dueno = proceso;
    }

    public void liberar() {
        dueno = null;
    }

    public boolean estaLibre() {
        return dueno == null;
    }

    public Proceso getDueno() {
        return dueno;
    }

    public String getDuenoId() {
        return dueno == null ? null : dueno.getId();
    }

    /**
     * Ejecuta una unidad del dueño y la registra como segmento [reloj-1, reloj).
     */
    public void ejecutar(int reloj) {
        if (dueno == null || dueno.getTiempoRestante() <= 0) {
            return;
        }
        dueno.ejecutarUnidad();
        registrarSegmento(dueno.getId(), reloj);
    }

    public void reiniciar() {
        dueno = null;
        segmentos.clear();
    }

    public int finMaximo() {
        return segmentos.stream().mapToInt(Segmento::fin).max().orElse(0);
    }

    public Map<String, Set<Integer>> mapaEjecucion(List<String> ids) {
        Map<String, Set<Integer>> mapa = new LinkedHashMap<>();
        for (String id : ids) {
            mapa.put(id, new HashSet<>());
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

    private void registrarSegmento(String procesoId, int reloj) {
        int inicio = reloj - 1;
        if (!segmentos.isEmpty()) {
            Segmento ultimo = segmentos.getLast();
            if (ultimo.procesoId().equals(procesoId) && ultimo.fin() == inicio) {
                segmentos.set(segmentos.size() - 1, new Segmento(ultimo.inicio(), reloj, procesoId));
                return;
            }
        }
        segmentos.add(new Segmento(inicio, reloj, procesoId));
    }
}
