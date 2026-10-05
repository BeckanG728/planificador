package es.unjfsc.planificador.compartido.modelo;

/**
 * Estados posibles de un proceso dentro de la simulación.
 * Mapea los estados 'waiting', 'running' y 'done' del prototipo HTML.
 */
public enum EstadoProceso {
    ESPERANDO,
    EJECUTANDO,
    TERMINADO
}