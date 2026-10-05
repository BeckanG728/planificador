package es.unjfsc.planificador.compartido.ui;

import javax.swing.border.AbstractBorder;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

/**
 * Borde con contorno redondeado. Opcionalmente rellena el fondo del componente
 * con las mismas esquinas redondeadas.
 *
 * <p>Atención al orden de pintado de Swing: el borde se dibuja despues de
 * {@code paintComponent} y antes de {@code paintChildren}. Por eso el relleno
 * solo puede usarse en contenedores cuyo contenido son hijos; en componentes
 * con texto (botones, campos, etiquetas) el relleno taparía el texto y hay que
 * dejar que el componente sea opaco.</p>
 */
public class BordeRedondeado extends AbstractBorder {

    private final Color color;
    private final int radio;
    private final int grosor;
    private final boolean rellenar;

    public BordeRedondeado(Color color, int radio) {
        this(color, radio, 1, false);
    }

    public BordeRedondeado(Color color, int radio, boolean rellenar) {
        this(color, radio, 1, rellenar);
    }

    public BordeRedondeado(Color color, int radio, int grosor, boolean rellenar) {
        this.color = color;
        this.radio = radio;
        this.grosor = grosor;
        this.rellenar = rellenar;
    }

    @Override
    public void paintBorder(Component componente, Graphics g, int x, int y, int ancho, int alto) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        Color fondo = componente.getBackground();
        if (rellenar && fondo != null) {
            g2.setColor(fondo);
            g2.fillRoundRect(x, y, ancho - 1, alto - 1, radio * 2, radio * 2);
        }

        if (color != null && grosor > 0) {
            g2.setColor(color);
            g2.setStroke(new BasicStroke(grosor));
            g2.drawRoundRect(x + grosor, y + grosor, ancho - grosor * 2 - 1, alto - grosor * 2 - 1,
                    radio * 2, radio * 2);
        }

        g2.dispose();
    }

    @Override
    public Insets getBorderInsets(Component componente, Insets insets) {
        insets.left = grosor;
        insets.top = grosor;
        insets.right = grosor;
        insets.bottom = grosor;
        return insets;
    }

    @Override
    public Insets getBorderInsets(Component componente) {
        return new Insets(grosor, grosor, grosor, grosor);
    }

    @Override
    public boolean isBorderOpaque() {
        return false;
    }
}