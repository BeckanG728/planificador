package es.unjfsc.planificador.compartido.vista.comun;

import es.unjfsc.planificador.compartido.nucleo.modelo.EstadoProceso;
import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Carril de una pista: punto de estado, identificador, pista de progreso
 * y distintivo del motor cuando el proceso tiene el turno.
 */
public class PanelCarril extends JPanel {

    private static final int RADIO = 10;

    private final PuntoEstado puntoEstado = new PuntoEstado();
    private final JLabel etiquetaId = new JLabel();
    private final PistaCarril pista = new PistaCarril();
    private final JPanel distintivoMotor = crearDistintivoMotor();

    private boolean activo;

    public PanelCarril() {
        super(new BorderLayout(10, 0));
        setOpaque(false);

        JPanel etiqueta = new JPanel();
        etiqueta.setOpaque(false);
        etiqueta.setLayout(new BoxLayout(etiqueta, BoxLayout.X_AXIS));
        etiqueta.setPreferredSize(new Dimension(44, 0));

        etiquetaId.setFont(Tema.fuenteNegrita(14));
        etiqueta.add(puntoEstado);
        etiqueta.add(javax.swing.Box.createHorizontalStrut(8));
        etiqueta.add(etiquetaId);

        add(etiqueta, BorderLayout.WEST);
        add(pista, BorderLayout.CENTER);
        add(distintivoMotor, BorderLayout.EAST);

        setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 6));
    }

    private static JPanel crearDistintivoMotor() {
        JPanel distintivo = new JPanel(new BorderLayout());
        distintivo.setOpaque(false);
        distintivo.add(
                Tema.etiquetaRedondeada("MOTOR", Tema.AMARILLO, Tema.FONDO, Tema.fuenteNegrita(11), 7),
                BorderLayout.CENTER);
        return distintivo;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO * 2, RADIO * 2);
        if (activo) {
            g2.setColor(Tema.ACENTO);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIO * 2, RADIO * 2);
        }
        g2.dispose();
        super.paintComponent(g);
    }

    public void actualizar(Proceso proceso, String motorPropietario) {
        activo = motorPropietario != null && motorPropietario.equals(proceso.getId());
        setBackground(activo ? Tema.CARRIL_ACTIVO : Tema.CARRIL);

        puntoEstado.color = colorEstado(proceso.getEstado());
        puntoEstado.repaint();
        etiquetaId.setText(proceso.getId());
        etiquetaId.setForeground(Tema.colorAuto(proceso.getId()));

        pista.actualizar(proceso, activo);

        distintivoMotor.setVisible(activo);
        revalidate();
        repaint();
    }

    private static Color colorEstado(EstadoProceso estado) {
        return switch (estado) {
            case ESPERANDO -> Tema.TEXTO_TENUE;
            case EJECUTANDO -> Tema.ACENTO;
            case TERMINADO -> Tema.VERDE;
        };
    }

    /** Círculo sólido usado como indicador de estado del proceso. */
    private static class PuntoEstado extends JComponent {

        private Color color = Tema.TEXTO_TENUE;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(8, 8);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

        @Override
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g2.dispose();
        }
    }
}