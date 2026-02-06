package de.ralfrosenkranz.moltbook.demo.swing;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.intellijthemes.FlatAllIJThemes;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

import javax.swing.*;


/**
 * Swing demo client that exposes all functions available in moltbook-java-client.
 *
 * Package: demo.swing (subpackage of demo as requested).
 */
public final class MoltbookSwingClient {

    /**
     * Toggle the Look & Feel here.
     *
     * This list intentionally includes all freely available FlatLaf LAFs:
     *  - Core FlatLaf LAFs (Light/Dark/IntelliJ/Darcula)
     *  - FlatLaf macOS themes (Light/Dark)
     *  - FlatLaf IntelliJ Themes (via flatlaf-intellij-themes)
     */
    public static final String LAF_SYSTEM = "System";

    // Core FlatLaf
    public static final String LAF_FLAT_LIGHT = "Flat Light";
    public static final String LAF_FLAT_DARK = "Flat Dark";
    public static final String LAF_FLAT_INTELLIJ = "Flat IntelliJ";
    public static final String LAF_FLAT_DARCULA = "Flat Darcula";

    // FlatLaf macOS themes
    public static final String LAF_FLAT_MACOS_LIGHT = "Flat macOS Light";
    public static final String LAF_FLAT_MACOS_DARK = "Flat macOS Dark";

    // IntelliJ Themes (flatlaf-intellij-themes)
    public static final String IJ_ARC = "Arc";
    public static final String IJ_ARC_DARK = "Arc Dark";
    public static final String IJ_CARBON = "Carbon";
    public static final String IJ_COBALT_2 = "Cobalt 2";
    public static final String IJ_CYAN_LIGHT = "Cyan Light";
    public static final String IJ_CYAN_DARK = "Cyan Dark";
    public static final String IJ_DARK_FLAT = "Dark Flat";
    public static final String IJ_DRACULA = "Dracula";
    public static final String IJ_GRADIANTO_DARK_FUCHSIA = "Gradianto Dark Fuchsia";
    public static final String IJ_GRADIANTO_DEEP_OCEAN = "Gradianto Deep Ocean";
    public static final String IJ_GRADIANTO_MIDNIGHT_BLUE = "Gradianto Midnight Blue";
    public static final String IJ_GRADIANTO_NATURE_GREEN = "Gradianto Nature Green";
    public static final String IJ_GRAY = "Gray";
    public static final String IJ_GRUVBOX_DARK_HARD = "Gruvbox Dark Hard";
    public static final String IJ_GRUVBOX_DARK_MEDIUM = "Gruvbox Dark Medium";
    public static final String IJ_GRUVBOX_DARK_SOFT = "Gruvbox Dark Soft";
    public static final String IJ_HIBERBEE_DARK = "Hiberbee Dark";
    public static final String IJ_HIGH_CONTRAST = "High Contrast";
    public static final String IJ_LIGHT_FLAT = "Light Flat";
    public static final String IJ_MATERIAL_DARK = "Material Design Dark";
    public static final String IJ_MATERIAL_LIGHT = "Material Design Light";
    public static final String IJ_MONOCAI = "Monocai";
    public static final String IJ_MOONLIGHT = "Moonlight";
    public static final String IJ_ONE_DARK = "One Dark";
    public static final String IJ_SOLARIZED_DARK = "Solarized Dark";
    public static final String IJ_SOLARIZED_LIGHT = "Solarized Light";
    public static final String IJ_SPACEGRAY = "Spacegray";
    public static final String IJ_VUESION = "Vuesion";

    /** Selected Look & Feel. */
    private static final String SELECTED_LAF = LAF_FLAT_MACOS_DARK;

    /**
     * Convenience: list of all choices in one place (useful for a future settings UI).
     *
     * Note: IntelliJ theme availability depends on the flatlaf-intellij-themes dependency.
     */
    public static final String[] ALL_LAF_CHOICES = {
            LAF_SYSTEM,
            LAF_FLAT_LIGHT,
            LAF_FLAT_DARK,
            LAF_FLAT_INTELLIJ,
            LAF_FLAT_DARCULA,
            LAF_FLAT_MACOS_LIGHT,
            LAF_FLAT_MACOS_DARK,
            IJ_ARC,
            IJ_ARC_DARK,
            IJ_CARBON,
            IJ_COBALT_2,
            IJ_CYAN_LIGHT,
            IJ_CYAN_DARK,
            IJ_DARK_FLAT,
            IJ_DRACULA,
            IJ_GRADIANTO_DARK_FUCHSIA,
            IJ_GRADIANTO_DEEP_OCEAN,
            IJ_GRADIANTO_MIDNIGHT_BLUE,
            IJ_GRADIANTO_NATURE_GREEN,
            IJ_GRAY,
            IJ_GRUVBOX_DARK_HARD,
            IJ_GRUVBOX_DARK_MEDIUM,
            IJ_GRUVBOX_DARK_SOFT,
            IJ_HIBERBEE_DARK,
            IJ_HIGH_CONTRAST,
            IJ_LIGHT_FLAT,
            IJ_MATERIAL_DARK,
            IJ_MATERIAL_LIGHT,
            IJ_MONOCAI,
            IJ_MOONLIGHT,
            IJ_ONE_DARK,
            IJ_SOLARIZED_DARK,
            IJ_SOLARIZED_LIGHT,
            IJ_SPACEGRAY,
            IJ_VUESION
    };


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                applyLookAndFeel(SELECTED_LAF);
            } catch (Exception ignored) {}
            AppBootstrap.bootstrapAndShow();
        });
    }

    private static void applyLookAndFeel(String selected) throws Exception {
        switch (selected) {
            case LAF_SYSTEM:
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                break;
            case LAF_FLAT_LIGHT:
                FlatLightLaf.setup();
                break;
            case LAF_FLAT_DARK:
                FlatDarkLaf.setup();
                break;
            case LAF_FLAT_INTELLIJ:
                FlatIntelliJLaf.setup();
                break;
            case LAF_FLAT_DARCULA:
                FlatDarculaLaf.setup();
                break;
            case LAF_FLAT_MACOS_LIGHT:
                FlatMacLightLaf.setup();
                break;
            case LAF_FLAT_MACOS_DARK:
                FlatMacDarkLaf.setup();
                break;

            // IntelliJ Themes
            case IJ_ARC:
            case IJ_ARC_DARK:
            case IJ_CARBON:
            case IJ_COBALT_2:
            case IJ_CYAN_LIGHT:
            case IJ_CYAN_DARK:
            case IJ_DARK_FLAT:
            case IJ_DRACULA:
            case IJ_GRADIANTO_DARK_FUCHSIA:
            case IJ_GRADIANTO_DEEP_OCEAN:
            case IJ_GRADIANTO_MIDNIGHT_BLUE:
            case IJ_GRADIANTO_NATURE_GREEN:
            case IJ_GRAY:
            case IJ_GRUVBOX_DARK_HARD:
            case IJ_GRUVBOX_DARK_MEDIUM:
            case IJ_GRUVBOX_DARK_SOFT:
            case IJ_HIBERBEE_DARK:
            case IJ_HIGH_CONTRAST:
            case IJ_LIGHT_FLAT:
            case IJ_MATERIAL_DARK:
            case IJ_MATERIAL_LIGHT:
            case IJ_MONOCAI:
            case IJ_MOONLIGHT:
            case IJ_ONE_DARK:
            case IJ_SOLARIZED_DARK:
            case IJ_SOLARIZED_LIGHT:
            case IJ_SPACEGRAY:
            case IJ_VUESION:
                if (!setupIntelliJThemeByName(selected)) {
                    // fallback
                    FlatMacDarkLaf.setup();
                }
                break;

            default:
                // fallback
                FlatMacDarkLaf.setup();
                break;
        }
    }

    private static boolean setupIntelliJThemeByName(String themeName) {
        try {
            // Keep this method resilient against small API changes between FlatLaf versions.
            // Use reflection to avoid compile-time dependency on ThemeInfo details.

            Object infosObj = FlatAllIJThemes.class.getField("INFOS").get(null);
            if (!(infosObj instanceof Object[] infos))
                return false;

            for (Object info : infos) {
                if (info == null)
                    continue;

                String name = null;
                try {
                    // some versions provide a getter
                    Object v = info.getClass().getMethod("getName").invoke(info);
                    if (v instanceof String s)
                        name = s;
                } catch (Throwable ignored) {
                    // ignore and try field access below
                }
                if (name == null) {
                    try {
                        // other versions expose a public field
                        Object v = info.getClass().getField("name").get(info);
                        if (v instanceof String s)
                            name = s;
                    } catch (Throwable ignored) {
                        // ignore
                    }
                }

                if (name != null && name.equalsIgnoreCase(themeName)) {
                    // invoke FlatAllIJThemes.setup(ThemeInfo)
                    FlatAllIJThemes.class.getMethod("setup", info.getClass()).invoke(null, info);
                    return true;
                }
            }
        } catch (Throwable ignored) {
            // dependency missing or API changed
        }
        return false;
    }
}
