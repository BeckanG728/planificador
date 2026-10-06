package es.unjfsc.planificador.compartido.nucleo.modelo;

import java.util.List;

/**
 * Resumen de métricas de la simulación: filas por proceso y promedios.
 */
public record ResumenMetricas(List<MetricaProceso> filas, double esperaPromedio, double finalizacionPromedio,
                              double penalizacionPromedio, int cambiosContexto) {

    public static ResumenMetricas calcular(List<Proceso> procesos, int cambiosContexto) {
        List<MetricaProceso> filas = procesos.stream().map(MetricaProceso::de).toList();
        if (filas.isEmpty()) {
            return new ResumenMetricas(List.of(), 0, 0, 0, cambiosContexto);
        }
        int n = filas.size();
        double espera = filas.stream().mapToDouble(MetricaProceso::espera).sum() / n;
        double fin = filas.stream().mapToDouble(MetricaProceso::finalizacion).sum() / n;
        double penalizacion = filas.stream().mapToDouble(MetricaProceso::penalizacion).sum() / n;
        return new ResumenMetricas(filas, espera, fin, penalizacion, cambiosContexto);
    }
}