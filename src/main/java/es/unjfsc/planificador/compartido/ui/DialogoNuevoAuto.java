package es.unjfsc.planificador.compartido.ui;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

/**
 * Diálogo para agregar un proceso (auto) a la simulación.
 */
public class DialogoNuevoAuto extends JDialog {

    private final ComponentesRedondeados.Campo campoLlegada = Tema.campoNumero(4);
    private final ComponentesRedondeados.Campo campoRafaga = Tema.campoNumero(4);
    private final JButton agregar = Tema.boton("Agregar auto", true);
    private final JButton cancelar = Tema.boton("Cancelar");

    private boolean confirmado;

    public DialogoNuevoAuto(Window padre, String idSugerido, int llegadaSugerida, int rafagaSugerida) {
        super(padre, "Nuevo auto", ModalityType.APPLICATION_MODAL);
        confirmado = false;

        JPanel raiz = Tema.panel(Tema.FONDO);
        raiz.setLayout(new BorderLayout(0, 10));

        JPanel contenido = Tema.panel(Tema.FONDO);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(BorderFactory.createEmptyBorder(12, 16, 10, 16));

        contenido.add(Tema.etiqueta("Nuevo proceso", Tema.TEXTO, Tema.fuenteNegrita(16)));

        JPanel vista = Tema.panel(Tema.SUPERFICIE);
        vista.setOpaque(false);
        vista.setLayout(new BorderLayout(12, 0));
        vista.setBorder(Tema.bordeRelleno(Tema.BORDE_VISIBLE, 10, 6, 12));
        vista.add(Tema.etiqueta(Tema.AUTO, Tema.TEXTO, Tema.fuenteEmoji(22)), BorderLayout.WEST);
        vista.add(Tema.etiqueta(idSugerido, Tema.colorAuto(idSugerido), Tema.fuenteNegrita(18)),
                BorderLayout.CENTER);
        contenido.add(vista);

        campoLlegada.setText(String.valueOf(llegadaSugerida));
        campoRafaga.setText(String.valueOf(rafagaSugerida));
        contenido.add(crearCampo(Tema.RELOJ + " C \u00B7 Llegada", campoLlegada));
        contenido.add(crearCampo(Tema.RAYO + " T \u00B7 Ráfaga de CPU", campoRafaga));

        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acciones.setOpaque(false);
        acciones.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 18));
        acciones.add(cancelar);
        acciones.add(agregar);

        agregar.addActionListener(e -> {
            confirmado = true;
            dispose();
        });
        cancelar.addActionListener(e -> dispose());

        raiz.add(contenido, BorderLayout.CENTER);
        raiz.add(acciones, BorderLayout.SOUTH);
        setContentPane(raiz);

        Dimension preferida = raiz.getPreferredSize();
        setSize(new Dimension(400, preferida.height + 16));
        setLocationRelativeTo(padre);
        getRootPane().setDefaultButton(agregar);
    }

    private static JPanel crearCampo(String titulo, ComponentesRedondeados.Campo campo) {
        JPanel fila = Tema.panel(Tema.SUPERFICIE);
        fila.setOpaque(false);
        fila.setLayout(new BorderLayout(12, 0));
        fila.setBorder(Tema.bordeRelleno(Tema.BORDE, 9, 5, 10));
        fila.add(Tema.etiqueta(titulo, Tema.TEXTO_SUAVE, Tema.fuente(12)), BorderLayout.WEST);
        fila.add(campo, BorderLayout.EAST);
        return fila;
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    public int getLlegada() {
        return leerValor(campoLlegada, 0);
    }

    public int getRafaga() {
        return leerValor(campoRafaga, 1);
    }

    private static int leerValor(ComponentesRedondeados.Campo campo, int porDefecto) {
        try {
            return Integer.parseInt(campo.getText().trim());
        } catch (NumberFormatException ex) {
            return porDefecto;
        }
    }
}