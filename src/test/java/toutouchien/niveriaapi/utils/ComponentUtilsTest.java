package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentUtilsTest {
    @Test
    @DisplayName("MiniMessage serialization and deserialization")
    void testMiniMessageSerializationDeserialization() {
        String miniMessageString = "<red>Hello, <bold>World!</bold></red> Hehe :3";

        Component component = ComponentUtils.deserializeMM(miniMessageString);
        String serialized = ComponentUtils.serializeMM(component);

        assertEquals(miniMessageString, serialized);
    }

    @Test
    @DisplayName("JSON serialization and deserialization")
    void testJsonSerializationDeserialization() {
        String jsonString = "{\"extra\":[{\"bold\":true,\"color\":\"red\",\"text\":\"World!\"},\" Hehe :3\"],\"text\":\"Hello, \"}";

        Component component = ComponentUtils.deserializeJson(jsonString);
        String serialized = ComponentUtils.serializeJson(component);

        assertEquals(jsonString, serialized);
    }
}
