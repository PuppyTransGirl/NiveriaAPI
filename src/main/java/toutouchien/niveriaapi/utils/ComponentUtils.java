package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for serializing and deserializing Adventure Components
 * using MiniMessage and JSON formats.
 */
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
    @NotNull
    public static String serializeMM(@NotNull Component component) {
        Preconditions.checkNotNull(component, "component cannot be null");

        return miniMessage.serialize(component);
    }

    /**
     * Deserializes a MiniMessage string into an Adventure Component.
     *
     * @param input the MiniMessage string to deserialize
     * @return the deserialized Component
     */
    @NotNull
    public static Component deserializeMM(@NotNull String input) {
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
    @NotNull
    public static String serializeJson(@NotNull Component component) {
        Preconditions.checkNotNull(component, "component cannot be null");

        return jsonSerializer.serialize(component);
    }

    /**
     * Deserializes a JSON string into an Adventure Component.
     *
     * @param input the JSON string to deserialize
     * @return the deserialized Component
     */
    @NotNull
    public static Component deserializeJson(@NotNull String input) {
        Preconditions.checkNotNull(input, "input cannot be null");

        return jsonSerializer.deserialize(input);
    }
}
