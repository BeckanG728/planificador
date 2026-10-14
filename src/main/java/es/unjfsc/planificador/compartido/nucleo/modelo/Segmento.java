package es.unjfsc.planificador.compartido.nucleo.modelo;

/**
 * Intervalo continuo de ejecución de un proceso: [inicio, fin).
 * El instante 'fin' es exclusivo.
 */
public record Segmento(int inicio, int fin, String procesoId) {
}
