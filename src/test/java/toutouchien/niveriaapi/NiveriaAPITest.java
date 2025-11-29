package toutouchien.niveriaapi;

import org.junit.jupiter.api.*;
import org.mockbukkit.mockbukkit.MockBukkit;
import toutouchien.niveriaapi.mock.MockBukkitHelper;
import toutouchien.niveriaapi.mock.ServerMock;

class NiveriaAPITest {
    public NiveriaAPI plugin;
    public ServerMock server;

    @BeforeEach
    void setUp() {
        this.server = MockBukkitHelper.safeMock();

        this.plugin = MockBukkit.load(NiveriaAPI.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    @DisplayName("Test if NiveriaAPI plugin is enabled")
    void testPluginIfEnabled() {
        Assertions.assertTrue(plugin.isEnabled());
    }
}
