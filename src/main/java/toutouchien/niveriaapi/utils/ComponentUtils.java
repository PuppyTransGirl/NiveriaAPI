package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ComponentUtils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final JSONComponentSerializer jsonSerializer = JSONComponentSerializer.json();
    private static final LegacyComponentSerializer legacyAmpersandTextSerializer = LegacyComponentSerializer.legacyAmpersand();
    private static final LegacyComponentSerializer legacySectionTextSerializer = LegacyComponentSerializer.legacySection();
    private static final PlainTextComponentSerializer plainTextSerializer = PlainTextComponentSerializer.plainText();

    private ComponentUtils() {
        throw new IllegalStateException("Utility class");
    }

    // MiniMessage
    public static String serializeMiniMessage(Component component) {
        return miniMessage.serialize(component);
    }

    public static Component deserializeMiniMessage(String input) {
        return miniMessage.deserialize(input);
    }

    // JSON
    public static String serializeJson(Component component) {
        return jsonSerializer.serialize(component);
    }

    public static Component deserializeJson(String input) {
        return jsonSerializer.deserialize(input);
    }

    // Legacy Text
    public static String serializeLegacyAmpersandText(Component component) {
        return legacyAmpersandTextSerializer.serialize(component);
    }

    public static Component deserializeLegacyAmpersandText(String input) {
        return legacyAmpersandTextSerializer.deserialize(input);
    }

    public static String serializeLegacySectionText(Component component) {
        return legacySectionTextSerializer.serialize(component);
    }

    public static Component deserializeLegacySectionText(String input) {
        return legacySectionTextSerializer.deserialize(input);
    }

    // Plain Text
    public static String serializePlainText(Component component) {
        return plainTextSerializer.serialize(component);
    }
}
