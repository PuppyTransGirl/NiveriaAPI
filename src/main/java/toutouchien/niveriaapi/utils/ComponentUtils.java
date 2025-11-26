package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.jetbrains.annotations.NotNull;

public class ComponentUtils {
    private static final MiniMessage miniMessage = MiniMessage.miniMessage();
    private static final JSONComponentSerializer jsonSerializer = JSONComponentSerializer.json();

    private ComponentUtils() {
        throw new IllegalStateException("Utility class");
    }

    // MiniMessage
    @NotNull
    public static String serializeMM(@NotNull Component component) {
        Preconditions.checkNotNull(component, "component cannot be null");

        return miniMessage.serialize(component);
    }

    @NotNull
    public static Component deserializeMM(@NotNull String input) {
        Preconditions.checkNotNull(input, "input cannot be null");

        return miniMessage.deserialize(input);
    }

    // JSON
    @NotNull
    public static String serializeJson(@NotNull Component component) {
        Preconditions.checkNotNull(component, "component cannot be null");

        return jsonSerializer.serialize(component);
    }

    @NotNull
    public static Component deserializeJson(@NotNull String input) {
        Preconditions.checkNotNull(input, "input cannot be null");

        return jsonSerializer.deserialize(input);
    }
}
