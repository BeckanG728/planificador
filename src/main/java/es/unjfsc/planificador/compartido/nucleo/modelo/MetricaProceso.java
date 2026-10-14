package es.unjfsc.planificador.compartido.nucleo.modelo;


public record MetricaProceso(String procesoId, int llegada, int rafaga, int espera, int finalizacion) {

    public static MetricaProceso de(Proceso proceso, int reloj) {
        if (proceso.getTiempoFinalizacion() != null) {
            int fin = proceso.getTiempoFinalizacion() - proceso.getLlegada();
            int espera = Math.max(0, fin - proceso.getRafaga());
            return new MetricaProceso(proceso.getId(), proceso.getLlegada(), proceso.getRafaga(),
                    espera, fin);
        }
        if (reloj <= proceso.getLlegada()) {
            return new MetricaProceso(proceso.getId(), proceso.getLlegada(), proceso.getRafaga(), 0, 0);
        }
        int ejecutado = proceso.getRafaga() - proceso.getTiempoRestante();
        int espera = Math.max(0, reloj - proceso.getLlegada() - ejecutado);
        int fin = Math.max(0, reloj - proceso.getLlegada());
        return new MetricaProceso(proceso.getId(), proceso.getLlegada(), proceso.getRafaga(),
                espera, fin);
    }
}
