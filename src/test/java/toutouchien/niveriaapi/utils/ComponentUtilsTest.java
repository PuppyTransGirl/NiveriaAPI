package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.mock.MockBukkitHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComponentUtilsTest {
    @BeforeEach
    void setUp() {
        MockBukkitHelper.safeMock();
        MockBukkit.load(NiveriaAPI.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkitHelper.safeUnmock();
    }

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
