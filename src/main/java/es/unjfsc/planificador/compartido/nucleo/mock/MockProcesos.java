package es.unjfsc.planificador.compartido.nucleo.mock;

import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;

import java.util.List;

public final class MockProcesos {

    private static final List<Proceso> PLANTILLAS = List.of(
            new Proceso("A", 0, 5, 3),
            new Proceso("B", 3, 2, 1),
            new Proceso("C", 1, 3, 5),
            new Proceso("D", 2, 2, 4),
            new Proceso("E", 4, 4, 2));

    private MockProcesos() {
    }
    
    public static List<Proceso> nuevos() {
        return PLANTILLAS.stream().map(Proceso::new).toList();
    }
}
