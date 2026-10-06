package es.unjfsc.planificador.compartido.vista.comun;

import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Descripción de un parámetro editable de un proceso (C, T, prioridad, ...).
 *
 * <p>El panel de configuración dibuja una columna por parámetro sin conocer ningún
 * algoritmo: cada caso declara los suyos desde su {@link LogicaVisual}.</p>
 *
 * @param titulo      etiqueta corta que acompaña al campo
 * @param descripcion texto explicativo que aparece como ayuda
 * @param lector      extrae el valor actual del proceso
 * @param escritor    aplica el valor editado al proceso
 * @param minimo      valor mínimo admitido
 */
public record ParametroProceso(String titulo, String descripcion,
                               Function<Proceso, Integer> lector,
                               BiConsumer<Proceso, Integer> escritor,
                               int minimo) {

    /** C · tiempo de llegada al sistema. */
    public static ParametroProceso llegada() {
        return new ParametroProceso("C", "C · tiempo de llegada al sistema",
                Proceso::getLlegada, Proceso::setLlegada, 0);
    }

    /** T · ráfaga de CPU. */
    public static ParametroProceso rafaga() {
        return new ParametroProceso("T", "T · ráfaga de CPU",
                Proceso::getRafaga, Proceso::setRafaga, 1);
    }
}