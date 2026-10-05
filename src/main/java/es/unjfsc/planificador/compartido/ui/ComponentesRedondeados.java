package es.unjfsc.planificador.compartido.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Componentes con fondo y contorno redondeados de verdad.
 *
 * <p>Un {@link javax.swing.border.Border} no sirve para esto: Swing dibuja el
 * borde despues de {@code paintComponent}, asi que taparia el texto. Estos
 * componentes son no opacos y pintan su fondo redondeado antes de delegar en
 * {@code super.paintComponent}, de modo que el texto queda encima y las
 * esquinas muestran al contenedor padre.</p>
 */
public final class ComponentesRedondeados {

    private ComponentesRedondeados() {
    }

    /** Pinta el fondo y el contorno redondeados de un componente. */
    public static void pintar(Graphics g, int ancho, int alto, int radio, Color fondo, Color borde) {
        if (ancho <= 0 || alto <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        if (fondo != null) {
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, ancho - 1, alto - 1, radio * 2, radio * 2);
        }
        if (borde != null) {
            g2.setColor(borde);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, ancho - 1, alto - 1, radio * 2, radio * 2);
        }
        g2.dispose();
    }

    /** Botón con fondo redondeado. */
    public static class Boton extends JButton {

        private int radio;
        private Color colorBorde;

        public Boton(String texto, int radio, int rellenoVertical, int rellenoHorizontal) {
            super(texto);
            this.radio = radio;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorder(BorderFactory.createEmptyBorder(rellenoVertical, rellenoHorizontal,
                    rellenoVertical, rellenoHorizontal));
        }

        public void setColorBorde(Color colorBorde) {
            this.colorBorde = colorBorde;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            pintar(g, getWidth(), getHeight(), radio, getBackground(), colorBorde);
            super.paintComponent(g);
        }
    }

    /** Campo de texto con fondo redondeado. */
    public static class Campo extends JTextField {

        private final int radio;

        public Campo(int columnas, int radio, int rellenoVertical, int rellenoHorizontal) {
            super(columnas);
            this.radio = radio;
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(rellenoVertical, rellenoHorizontal,
                    rellenoVertical, rellenoHorizontal));
        }

        @Override
        protected void paintComponent(Graphics g) {
            pintar(g, getWidth(), getHeight(), radio, getBackground(), Tema.BORDE_CAMPO);
            super.paintComponent(g);
        }
    }

    /** Etiqueta con fondo redondeado. */
    public static class Etiqueta extends JLabel {

        private int radio;
        private Color colorBorde;

        public Etiqueta(String texto, int radio) {
            super(texto, JLabel.CENTER);
            this.radio = radio;
            this.colorBorde = Tema.BORDE;
            setOpaque(false);
        }

        public void setRadio(int radio) {
            this.radio = radio;
            revalidate();
            repaint();
        }

        public void setColorBorde(Color colorBorde) {
            this.colorBorde = colorBorde;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            pintar(g, getWidth(), getHeight(), radio, getBackground(), colorBorde);
            super.paintComponent(g);
        }
    }
}