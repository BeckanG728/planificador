package es.unjfsc.planificador.compartido.nucleo.modelo;

/**
 * Métricas de un proceso:
 * C = llegada, T = ráfaga, E = espera, F = tiempo de finalización medido desde la llegada
 * (instante de fin - C), P = penalización.
 * La penalización se calcula como F / T.
 */
public record MetricaProceso(String procesoId, int llegada, int rafaga, int espera, int finalizacion,
                             double penalizacion) {

    public static MetricaProceso de(Proceso proceso) {
        int fin = proceso.getTiempoFinalizacion() == null ? 0 : proceso.getTiempoFinalizacion() - proceso.getLlegada();
        double penalizacion = proceso.getRafaga() > 0 ? (double) fin / proceso.getRafaga() : 0;
        return new MetricaProceso(proceso.getId(), proceso.getLlegada(), proceso.getRafaga(),
                proceso.getTiempoEspera(), fin, penalizacion);
    }
}