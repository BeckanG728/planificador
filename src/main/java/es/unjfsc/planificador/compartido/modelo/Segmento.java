package es.unjfsc.planificador.compartido.modelo;

/**
 * Intervalo continuo de ejecución de un proceso: [inicio, fin).
 * El instante 'fin' es exclusivo, igual que en el prototipo HTML.
 */
public record Segmento(int inicio, int fin, String procesoId) {

    public int duracion() {
        return fin - inicio;
    }
}