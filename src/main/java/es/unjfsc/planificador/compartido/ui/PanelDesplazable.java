package es.unjfsc.planificador.compartido.ui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.Scrollable;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

/**
 * Panel que ocupa todo el ancho del viewport y expone desplazamiento vertical
 * suave dentro de un JScrollPane.
 */
public class PanelDesplazable extends JPanel implements Scrollable {

    public PanelDesplazable(int eje) {
        setLayout(new BoxLayout(this, eje));
        setOpaque(false);
    }

    public PanelDesplazable(LayoutManager layout) {
        super(layout);
        setOpaque(false);
    }

    @Override
    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    @Override
    public int getScrollableUnitIncrement(Rectangle rectVisible, int orientacion, int direccion) {
        return 16;
    }

    @Override
    public int getScrollableBlockIncrement(Rectangle rectVisible, int orientacion, int direccion) {
        return rectVisible.height;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        return true;
    }

    @Override
    public boolean getScrollableTracksViewportHeight() {
        return false;
    }
}