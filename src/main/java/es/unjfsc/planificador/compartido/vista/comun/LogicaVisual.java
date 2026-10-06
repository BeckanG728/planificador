package es.unjfsc.planificador.compartido.vista.comun;

import es.unjfsc.planificador.compartido.nucleo.motor.Planificador;

import java.util.List;

/**
 * Presentación de un algoritmo de planificación: lo que la vista necesita saber
 * para etiquetarlo, sin acoplarse a una implementación concreta.
 *
 * <p>Cada caso aporta su propia implementación dentro de su paquete.</p>
 */
public interface LogicaVisual {

    /** Nombre corto del algoritmo, usado en el título de la ventana. */
    String nombre();

    /** Texto de la insignia de la cabecera. */
    String insignia();

    /** Texto de la insignia según el estado actual del motor; por defecto, la fija. */
    default String insignia(Planificador planificador) {
        return insignia();
    }

    /** Descripción del algoritmo; por defecto, la propia insignia. */
    default String descripcion() {
        return insignia();
    }

    /**
     * Parámetros editables de cada proceso, en el orden en que se muestran.
     * Por defecto, solo C y T.
     */
    default List<ParametroProceso> parametros() {
        return List.of(ParametroProceso.llegada(), ParametroProceso.rafaga());
    }
}