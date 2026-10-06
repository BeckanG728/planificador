package es.unjfsc.planificador.compartido.vista.casos.roundrobin;

import es.unjfsc.planificador.compartido.nucleo.algoritmos.roundrobin.PlanificadorRoundRobin;
import es.unjfsc.planificador.compartido.nucleo.motor.Planificador;
import es.unjfsc.planificador.compartido.vista.comun.ComponentesRedondeados;
import es.unjfsc.planificador.compartido.vista.comun.Tema;
import es.unjfsc.planificador.compartido.vista.comun.VentanaSimulacion;

import javax.swing.JComponent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana de la simulación con planificación Round Robin.
 *
 * <p>Añade a la cabecera el campo del quantum, entre los botones de transporte y los de
 * velocidad. Se habilita solo con la simulación detenida, igual que los parámetros de los
 * procesos, así que el valor vigente se toma al empezar la siguiente simulación.</p>
 */
public class VentanaRoundRobin extends VentanaSimulacion {

    public VentanaRoundRobin() {
        this(new PlanificadorRoundRobin());
    }

    private VentanaRoundRobin(PlanificadorRoundRobin planificador) {
        super(planificador, new LogicaVisualRoundRobin(planificador));
    }

    @Override
    protected List<JComponent> controlesDelCaso(Planificador planificador) {
        ComponentesRedondeados.Campo campo = Tema.campoNumero(2);
        campo.setText(String.valueOf(((PlanificadorRoundRobin) planificador).getQuantum()));
        campo.setToolTipText("Q \u00B7 unidades de CPU por turno");
        campo.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.selectAll();
            }

            @Override
            public void focusLost(FocusEvent e) {
                aplicarQuantum(campo, planificador);
            }
        });
        campo.addActionListener(e -> aplicarQuantum(campo, planificador));

        List<JComponent> controles = new ArrayList<>();
        controles.add(Tema.etiqueta("Q", Tema.TEXTO_SUAVE, Tema.fuenteNegrita(11)));
        controles.add(campo);
        return controles;
    }

    private static void aplicarQuantum(ComponentesRedondeados.Campo campo, Planificador planificador) {
        if (!campo.isEnabled()) {
            return;
        }
        PlanificadorRoundRobin rr = (PlanificadorRoundRobin) planificador;
        try {
            rr.establecerQuantum(Integer.parseInt(campo.getText().trim()));
        } catch (NumberFormatException ex) {
            campo.setText(String.valueOf(rr.getQuantum()));
            return;
        }
        campo.setText(String.valueOf(rr.getQuantum()));
    }
}