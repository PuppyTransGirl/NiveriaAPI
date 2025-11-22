package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

public class ComponentUtils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final JSONComponentSerializer jsonSerializer = JSONComponentSerializer.json();

    private ComponentUtils() {
        throw new IllegalStateException("Utility class");
    }

    // MiniMessage
    public static String serializeMM(Component component) {
        return miniMessage.serialize(component);
    }

    public static Component deserializeMM(String input) {
        return miniMessage.deserialize(input);
    }

    // JSON
    public static String serializeJson(Component component) {
        return jsonSerializer.serialize(component);
    }

    public static Component deserializeJson(String input) {
        return jsonSerializer.deserialize(input);
    }
}
