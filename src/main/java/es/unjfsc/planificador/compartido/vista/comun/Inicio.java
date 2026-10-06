package es.unjfsc.planificador.compartido.vista.comun;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.util.function.Supplier;

/**
 * Arranque común de las ventanas de simulación.
 *
 * <p>Cada caso aporta su propia ventana mediante la fábrica que recibe.</p>
 */
public final class Inicio {

    private Inicio() {
    }

    public static void lanzar(Supplier<? extends JFrame> fabrica) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (ReflectiveOperationException | UnsupportedLookAndFeelException ignored) {
            }
            Tema.aplicarEstilosGlobales();
            fabrica.get().setVisible(true);
        });
    }
}