package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for managing text colors used in the Niveria API.
 */
public class ColorUtils {
    private ColorUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets the primary color used in the Niveria API.
     *
     * @return The primary TextColor.
     */
    @NotNull
    public static TextColor primaryColor() {
        return TextColor.fromHexString("#FC67FA");
    }

    /**
     * Gets the secondary color used in the Niveria API.
     *
     * @return The secondary TextColor.
     */
    @NotNull
    public static TextColor secondaryColor() {
        return TextColor.fromHexString("#F4C4F3");
    }

    /**
     * Gets the default color used for the server MOTD.
     *
     * @return The default MOTD TextColor.
     */
    @NotNull
    public static TextColor defaultMotdColor() {
        return TextColor.color(8421504);
    }
}
