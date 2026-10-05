package es.unjfsc.planificador.compartido.ui;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.util.Map;

/**
 * Paleta de colores, tipografías y componentes base replicando el CSS del
 * prototipo HTML.
 */
public final class Tema {

    public static final Color FONDO = new Color(0x14151C);
    public static final Color SUPERFICIE = new Color(0x1E2029);
    public static final Color PROFUNDO = new Color(0x0E0F14);
    public static final Color CARRIL = new Color(0x191B23);
    public static final Color CARRIL_ACTIVO = new Color(0x241F3D);
    public static final Color BORDE = new Color(0x23252F);
    public static final Color BORDE_VISIBLE = new Color(0x343747);
    public static final Color BORDE_CAMPO = new Color(0x3C4054);
    public static final Color TEXTO = new Color(0xECECF3);
    public static final Color TEXTO_SUAVE = new Color(0x9B9DAE);
    public static final Color TEXTO_TENUE = new Color(0x6A6C7C);
    public static final Color ACENTO = new Color(0x7C5CFF);
    public static final Color ACENTO_OSCURO = new Color(0x6741F0);
    public static final Color VERDE = new Color(0x34D399);
    public static final Color NARANJA = new Color(0xFB923C);
    public static final Color AMARILLO = new Color(0xFBBF24);
    public static final Color AZUL_CLARO = new Color(0x38BDF8);
    public static final Color VISIBLE = new Color(0x4A4D63);

    public static final String AUTO = "\uD83D\uDE99";
    public static final String BANDERA = "\uD83C\uDFC1";
    public static final String RELOJ = "\u23F1";
    public static final String ENGRANAJE = "\u2699";
    public static final String RAYO = "\u26A1";
    public static final String RELOJ_SANDIA = "\u23F3";
    public static final String MAS = "\u2795";
    public static final String MENOS = "\u2796";
    public static final String REPRODUCIR = "\u25B6";
    public static final String PAUSA = "\u23F8";
    public static final String REINICIAR = "\u27F2";

    private static final Map<String, Color> COLORES_AUTO = Map.of(
            "A", new Color(0x7C5CFF),
            "B", new Color(0x2DD4BF),
            "C", new Color(0xFB923C),
            "D", new Color(0xF472B6),
            "E", new Color(0xFACC15),
            "F", new Color(0x38BDF8));

    private static final Map<String, Color> COLORES_AUTO_SUAVES = Map.of(
            "A", new Color(0xC4B5FD),
            "B", new Color(0x99F6E4),
            "C", new Color(0xFED7AA),
            "D", new Color(0xFBCFE8),
            "E", new Color(0xFEF08A),
            "F", new Color(0xBAE6FD));

    private static final String[] FUENTES_EMOJI = {
            "Segoe UI Emoji", "Apple Color Emoji", "Noto Color Emoji", "Segoe UI Symbol"};

    private Tema() {
    }

    // ------------------------------------------------------------------
    // Estilos globales
    // ------------------------------------------------------------------

    /** Aplica los colores de la paleta a los valores por defecto de Swing. */
    public static void aplicarEstilosGlobales() {
        UIManager.put("Panel.background", PROFUNDO);
        UIManager.put("Label.foreground", TEXTO);
        UIManager.put("Label.font", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        UIManager.put("Button.background", SUPERFICIE);
        UIManager.put("Button.foreground", TEXTO);
        UIManager.put("TextField.background", PROFUNDO);
        UIManager.put("TextField.foreground", TEXTO);
        UIManager.put("TextField.caretForeground", TEXTO);
        UIManager.put("TextField.selectionBackground", ACENTO);
        UIManager.put("TextField.selectionForeground", Color.WHITE);
        UIManager.put("ScrollPane.background", FONDO);
        UIManager.put("Viewport.background", PROFUNDO);
        UIManager.put("ScrollBar.background", SUPERFICIE);
        UIManager.put("ScrollBar.thumb", BORDE_CAMPO);
        UIManager.put("ScrollBar.thumbHighlight", BORDE_VISIBLE);
        UIManager.put("ScrollBar.track", PROFUNDO);
        UIManager.put("TableHeader.background", SUPERFICIE);
        UIManager.put("TableHeader.foreground", TEXTO_SUAVE);
        UIManager.put("ToolTip.background", SUPERFICIE);
        UIManager.put("ToolTip.foreground", TEXTO);

        for (String clave : new String[]{"ScrollBar.width", "ScrollBar.height"}) {
            UIManager.put(clave, 10);
        }
    }

    // ------------------------------------------------------------------
    // Colores y tipografías
    // ------------------------------------------------------------------

    public static Color colorAuto(String id) {
        return COLORES_AUTO.getOrDefault(id, ACENTO);
    }

    public static Color colorAutoSuave(String id) {
        return COLORES_AUTO_SUAVES.getOrDefault(id, ACENTO);
    }

    /** Primera fuente de emoji disponible en el sistema. */
    public static Font fuenteEmoji(float tamano) {
        int entero = Math.max(1, Math.round(tamano));
        for (String nombre : FUENTES_EMOJI) {
            Font fuente = new Font(nombre, Font.PLAIN, entero);
            if (nombre.equals(fuente.getFamily())) {
                return fuente;
            }
        }
        return new Font(Font.SANS_SERIF, Font.PLAIN, entero);
    }

    public static Font fuente(float tamano) {
        return new Font(Font.SANS_SERIF, Font.PLAIN, Math.round(tamano));
    }

    public static Font fuenteNegrita(float tamano) {
        return new Font(Font.SANS_SERIF, Font.BOLD, Math.round(tamano));
    }

    // ------------------------------------------------------------------
    // Fábricas de componentes
    // ------------------------------------------------------------------

    public static JPanel panel(Color fondo) {
        JPanel panel = new JPanel();
        panel.setBackground(fondo);
        panel.setOpaque(true);
        return panel;
    }

    /**
     * Borde redondeado que ademas pinta el fondo del componente. Reservado para
     * contenedores sin texto propio (paneles), donde el relleno se dibuja antes
     * que los hijos y por eso no los tapa. Para botones, campos y etiquetas estan
     * {@link ComponentesRedondeados}.
     */
    public static Border bordeRelleno(Color color, int radio, int rellenoVertical, int rellenoHorizontal) {
        return BorderFactory.createCompoundBorder(
                new BordeRedondeado(color, radio, 1, true),
                BorderFactory.createEmptyBorder(rellenoVertical, rellenoHorizontal, rellenoVertical, rellenoHorizontal));
    }

    public static ComponentesRedondeados.Boton boton(String texto) {
        return boton(texto, false);
    }

    public static ComponentesRedondeados.Boton boton(String texto, boolean primario) {
        ComponentesRedondeados.Boton boton = new ComponentesRedondeados.Boton(texto, 9, 5, 12);
        boton.setFont(fuenteNegrita(12));
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBackground(primario ? ACENTO : SUPERFICIE);
        boton.setForeground(Color.WHITE);
        boton.setColorBorde(primario ? ACENTO : BORDE_CAMPO);

        Color base = primario ? ACENTO : SUPERFICIE;
        Color hover = primario ? ACENTO_OSCURO : new Color(0x272A36);
        Color bordeBase = primario ? ACENTO : BORDE_CAMPO;
        Color bordeHover = primario ? ACENTO_OSCURO : new Color(0x4A4E63);

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (!boton.isEnabled()) {
                    return;
                }
                boton.setBackground(hover);
                boton.setColorBorde(bordeHover);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (!boton.isEnabled()) {
                    return;
                }
                boton.setBackground(base);
                boton.setColorBorde(bordeBase);
            }
        });
        return boton;
    }

    /** Boton pequeno de tipo "speed-btn". */
    public static ComponentesRedondeados.Boton botonCompacto(String texto) {
        ComponentesRedondeados.Boton boton = new ComponentesRedondeados.Boton(texto, 7, 3, 10);
        boton.setFont(fuente(11));
        boton.setFocusPainted(false);
        boton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        boton.setBackground(SUPERFICIE);
        boton.setForeground(TEXTO_SUAVE);
        boton.setColorBorde(BORDE_CAMPO);
        return boton;
    }

    public static void activarBotonCompacto(ComponentesRedondeados.Boton boton, boolean activo) {
        boton.setBackground(activo ? ACENTO : SUPERFICIE);
        boton.setForeground(activo ? Color.WHITE : TEXTO_SUAVE);
        boton.setColorBorde(activo ? ACENTO : BORDE_CAMPO);
    }

    public static ComponentesRedondeados.Campo campoNumero(int columnas) {
        ComponentesRedondeados.Campo campo = new ComponentesRedondeados.Campo(columnas, 7, 4, 6);
        campo.setHorizontalAlignment(JTextField.CENTER);
        campo.setFont(fuenteNegrita(12));
        campo.setForeground(TEXTO);
        campo.setBackground(SUPERFICIE);
        campo.setCaretColor(TEXTO);
        return campo;
    }

    public static JLabel etiqueta(String texto, Color color, Font fuente) {
        JLabel etiqueta = new JLabel(texto);
        etiqueta.setForeground(color);
        etiqueta.setFont(fuente);
        etiqueta.setOpaque(false);
        return etiqueta;
    }

    /** Etiqueta centrada con fondo, usada por celdas, chips y leyenda. */
    public static ComponentesRedondeados.Etiqueta etiquetaCentrada(String texto, Color fondo, Color color, Font fuente) {
        ComponentesRedondeados.Etiqueta etiqueta = new ComponentesRedondeados.Etiqueta(texto, 8);
        etiqueta.setForeground(color);
        etiqueta.setFont(fuente);
        etiqueta.setBackground(fondo);
        return etiqueta;
    }

    /** Etiqueta con fondo redondeado, usada por chips y por la insignia del motor. */
    public static JLabel etiquetaRedondeada(String texto, Color fondo, Color color, Font fuente, int radio) {
        ComponentesRedondeados.Etiqueta etiqueta =
                new ComponentesRedondeados.Etiqueta(texto, radio);
        etiqueta.setForeground(color);
        etiqueta.setFont(fuente);
        etiqueta.setBackground(fondo);
        etiqueta.setColorBorde(null);
        etiqueta.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
        return etiqueta;
    }

    /** Aplica el relleno y borde usados por las celdas del Gantt. */
    public static void estiloCelda(ComponentesRedondeados.Etiqueta componente) {
        componente.setRadio(5);
        componente.setBorder(BorderFactory.createEmptyBorder(5, 2, 5, 2));
    }
}
