package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.jspecify.annotations.NullMarked;

/**
 * Utility class for serializing and deserializing Adventure Components
 * using MiniMessage and JSON formats.
 */
@NullMarked
public class ComponentUtils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final JSONComponentSerializer jsonSerializer = JSONComponentSerializer.json();

    private ComponentUtils() {
        throw new IllegalStateException("Utility class");
    }

    // MiniMessage

    /**
     * Serializes an Adventure Component into a MiniMessage string.
     *
     * @param component the Component to serialize
     * @return the serialized MiniMessage string
     */
    public static String serializeMM(Component component) {
        Preconditions.checkNotNull(component, "component cannot be null");

        return miniMessage.serialize(component);
    }

    /**
     * Deserializes a MiniMessage string into an Adventure Component.
     *
     * @param input the MiniMessage string to deserialize
     * @return the deserialized Component
     */
    public static Component deserializeMM(String input) {
        Preconditions.checkNotNull(input, "input cannot be null");

        return miniMessage.deserialize(input);
    }

    // JSON

    /**
     * Serializes an Adventure Component into a JSON string.
     *
     * @param component the Component to serialize
     * @return the serialized JSON string
     */
    public static String serializeJson(Component component) {
        Preconditions.checkNotNull(component, "component cannot be null");

        return jsonSerializer.serialize(component);
    }

    /**
     * Deserializes a JSON string into an Adventure Component.
     *
     * @param input the JSON string to deserialize
     * @return the deserialized Component
     */
    public static Component deserializeJson(String input) {
        Preconditions.checkNotNull(input, "input cannot be null");

        return jsonSerializer.deserialize(input);
    }
}
