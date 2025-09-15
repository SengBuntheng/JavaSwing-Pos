package forms;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A utility class to centralize all UI styling constants and methods
 * for a professional and consistent application theme.
 */
public class UITheme {

    // --- Primary Colors ---
    public static final Color PRIMARY_GREEN = new Color(1, 126, 81);
    public static final Color BACKGROUND = new Color(240, 243, 248);
    public static final Color PANEL_BG = Color.WHITE;
    public static final Color ACCENT_GREEN = new Color(236, 248, 245);

    // --- Text Colors ---
    public static final Color TEXT_DARK = new Color(33, 37, 41);
    public static final Color TEXT_LIGHT = Color.GRAY;

    // --- Action/Status Colors ---
    public static final Color BORDER_COLOR = new Color(222, 226, 230);
    public static final Color BORDER_HOVER_COLOR = new Color(13, 110, 253);
    public static final Color CANCEL_COLOR = new Color(220, 53, 69);
    public static final Color HOLD_COLOR = new Color(255, 193, 7);

    // --- Fonts ---
    public static final Font FONT_MAIN = new Font("Segoe UI", Font.PLAIN, 15);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_PRICE = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 24);

    // --- Borders ---
    public static final Border BORDER_NORMAL = BorderFactory.createLineBorder(BORDER_COLOR);
    public static final Border BORDER_HOVER = BorderFactory.createLineBorder(PRIMARY_GREEN, 2);

    /**
     * Applies a consistent style to a primary action button.
     * @param button The button to style.
     */
    public static void stylePrimaryButton(JButton button) {
        button.setBackground(PRIMARY_GREEN);
        button.setForeground(Color.WHITE);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(12, 25, 12, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Applies a consistent style to a secondary action button (e.g., Cancel, Hold).
     * @param button The button to style.
     * @param color The border and text color for the button.
     */
    public static void styleSecondaryButton(JButton button, Color color) {
        button.setBackground(PANEL_BG);
        button.setForeground(color);
        button.setFont(FONT_BOLD);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(color));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    /**
     * Sets global UI settings for a professional look and feel.
     * Should be called once at the start of the application.
     */
    public static void applyProfessionalLook() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Enable anti-aliasing for smoother text
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }
}