package toutouchien.niveriaapi.utils.game;

import net.kyori.adventure.util.TriState;
import org.bukkit.metadata.FixedMetadataValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.mock.MockBukkitHelper;
import toutouchien.niveriaapi.mock.ServerMock;

import static org.junit.jupiter.api.Assertions.*;

class PlayerUtilsTest {
    public NiveriaAPI plugin;
    private ServerMock server;

    private PlayerMock player1;
    private PlayerMock player2;

    @BeforeEach
    void setUp() {
        this.server = MockBukkitHelper.safeMock();

        this.player1 = this.server.addPlayer("PuppyTransGirl");
        this.player2 = this.server.addPlayer("Toutouchien");

        this.plugin = MockBukkit.load(NiveriaAPI.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkitHelper.safeUnmock();
    }

    @Test
    @DisplayName("isVanished should return false when the player isn't vanished (has no metadata)")
    void isVanished_shouldReturnFalseWhenNotVanished() {
        assertFalse(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished should return true when the player is vanished (has metadata)")
    void isVanished_shouldReturnTrueWhenVanished() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertTrue(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished should return false as the vanished metadata")
    void isVanished_shouldReturnFalseAsVanishedMetadata() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, false));
        assertFalse(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished should support net.kyori.adventure.util.TriState")
    void isVanished_shouldSupportTriState() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.TRUE));
        assertTrue(PlayerUtils.isVanished(player1));

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.FALSE));
        assertFalse(PlayerUtils.isVanished(player1));

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.NOT_SET));
        assertFalse(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished should ignore other plugins false values as the vanished metadata")
    void isVanished_shouldIgnoreOtherPluginsFalseValuesAsVanishedMetadata() {
        PluginMock otherPlugin = MockBukkit.createMockPlugin();

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        player1.setMetadata("vanished", new FixedMetadataValue(otherPlugin, false));

        assertTrue(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished should return true regardless of the number of true values as the vanished metadata")
    void isVanished_shouldReturnTrueRegardlessNumberOfTrueValuesAsVanishedMetadata() {
        for (int i = 0; i < 10; i++) {
            PluginMock somePlugin = MockBukkit.createMockPlugin();
            player1.setMetadata("vanished", new FixedMetadataValue(somePlugin, true));
        }

        assertTrue(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished should return false if the value is not a boolean as the vanished metadata")
    void isVanished_shouldReturnFalseIfValueIsNotABooleanAsVanishedMetadata() {
        PluginMock pluginA = MockBukkit.createMockPlugin();
        PluginMock pluginB = MockBukkit.createMockPlugin();
        PluginMock pluginC = MockBukkit.createMockPlugin();
        PluginMock pluginD = MockBukkit.createMockPlugin();

        player1.setMetadata("vanished", new FixedMetadataValue(pluginA, "true"));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginB, 1));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginC, null));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginD, new Object()));

        assertFalse(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished should throw an IllegalArgumentException when passed null")
    void isVanished_shouldThrowIllegalArgumentExceptionWhenPassedNull() {
        assertThrows(IllegalArgumentException.class, () -> PlayerUtils.isVanished(null));
    }
}
