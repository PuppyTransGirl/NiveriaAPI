package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;

public class ColorUtils {
    private ColorUtils() {
        throw new IllegalStateException("Utility class");
    }

    @NotNull
    public static TextColor primaryColor() {
        return TextColor.fromHexString("#FC67FA");
    }

    @NotNull
    public static TextColor secondaryColor() {
        return TextColor.fromHexString("#F4C4F3");
    }

    @NotNull
    public static TextColor defaultMotdColor() {
        return TextColor.color(8421504);
    }
}
