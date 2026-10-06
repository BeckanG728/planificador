package es.unjfsc.planificador.compartido.vista.comun;

import es.unjfsc.planificador.compartido.nucleo.modelo.Proceso;

import javax.swing.JComponent;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/**
 * Dibuja la pista de un carril: segmentos completados, marcas de tiempo,
 * el auto en su posición actual y el indicador de "en ejecución".
 */
public class PistaCarril extends JComponent {

    private static final int ALTO = 38;
    private static final int TAMANO_EMOJI = 18;
    private static final int MARGEN_DERECHO = 24;

    private Proceso proceso;
    private boolean enEjecucion;

    public PistaCarril() {
        setOpaque(false);
        setPreferredSize(new Dimension(400, ALTO));
    }

    public void actualizar(Proceso proceso, boolean enEjecucion) {
        this.proceso = proceso;
        this.enEjecucion = enEjecucion;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (proceso == null) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int anchoUtil = Math.max(1, getWidth() - MARGEN_DERECHO);
        int totalT = Math.max(1, proceso.getRafaga());
        int ejecutadas = Math.min(totalT, proceso.getEjecutado());
        Color color = Tema.colorAuto(proceso.getId());

        FontMetrics metricas = g2.getFontMetrics(Tema.fuenteEmoji(TAMANO_EMOJI));
        int lineaBase = ALTO - 4;
        int mediaY = lineaBase - metricas.getAscent() - 4;
        double anchoSegmento = (double) anchoUtil / totalT;

        g2.setColor(Tema.BORDE_VISIBLE);
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(0, mediaY, anchoUtil, mediaY);

        g2.setColor(color);
        for (int i = 0; i < ejecutadas; i++) {
            int x1 = (int) Math.round(i * anchoSegmento);
            int x2 = (int) Math.round((i + 1) * anchoSegmento);
            g2.drawLine(x1, mediaY, x2, mediaY);
        }

        int radio = Math.min(5, Math.max(3, (int) (anchoSegmento / 3)));
        for (int i = 0; i < totalT; i++) {
            int cx = (int) Math.round((i + 1) * anchoSegmento);
            g2.setColor(i < ejecutadas ? color : Tema.BORDE_VISIBLE);
            g2.fillOval(cx - radio, mediaY - radio, radio * 2, radio * 2);
        }

        dibujarAuto(g2, metricas, (int) Math.round(ejecutadas * anchoSegmento), lineaBase);
        g2.dispose();
    }

    private void dibujarAuto(Graphics2D g2, FontMetrics metricas, int x, int lineaBase) {
        int xAuto = Math.min(Math.max(x, 12), Math.max(12, getWidth() - MARGEN_DERECHO));

        int desvio = 0;
        if (enEjecucion) {
            long fase = System.currentTimeMillis() / 150;
            desvio = (fase % 2 == 0) ? -2 : 2;
        }

        g2.setFont(Tema.fuenteEmoji(TAMANO_EMOJI));
        int anchoTexto = metricas.stringWidth(Tema.AUTO);
        g2.drawString(Tema.AUTO, xAuto - anchoTexto / 2, lineaBase + desvio);
    }
}