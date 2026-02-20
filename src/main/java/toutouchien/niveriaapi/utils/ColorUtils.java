package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.format.TextColor;
import org.jspecify.annotations.NullMarked;

/**
 * Utility class for managing text colors used in the Niveria API.
 */
@NullMarked
public final class ColorUtils {
    private ColorUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Gets the primary color used in the Niveria API.
     *
     * @return The primary TextColor.
     */
    public static TextColor primaryColor() {
        return TextColor.fromHexString("#FC67FA");
    }

    /**
     * Gets the secondary color used in the Niveria API.
     *
     * @return The secondary TextColor.
     */
    public static TextColor secondaryColor() {
        return TextColor.fromHexString("#F4C4F3");
    }

    /**
     * Gets the default color used for the server MOTD.
     *
     * @return The default MOTD TextColor.
     */
    public static TextColor defaultMotdColor() {
        return TextColor.color(8421504);
    }
}
