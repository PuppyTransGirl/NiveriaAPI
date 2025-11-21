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

    public static TextColor errorColor() {
        return TextColor.fromHexString("#F52F2E");
    }

    public static TextColor infoColor() {
        return TextColor.fromHexString("#364CD2");
    }

    public static TextColor successColor() {
        return TextColor.fromHexString("#36C835");
    }

    public static TextColor warnColor() {
        return TextColor.fromHexString("#FF6501");
    }
}
