package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.format.TextColor;

public class ColorUtils {
    private ColorUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static TextColor primaryColor() {
        return TextColor.fromHexString("#FC67FA");
    }

    public static TextColor secondaryColor() {
        return TextColor.fromHexString("#F4C4F3");
    }

    public static TextColor defaultMotdColor() {
        return TextColor.color(8421504);
    }
}
