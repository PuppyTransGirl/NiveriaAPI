package toutouchien.niveriaapi.utils;

import net.kyori.adventure.util.TriState;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import org.mockbukkit.mockbukkit.plugin.PluginMock;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.mock.MockBukkitHelper;
import toutouchien.niveriaapi.mock.ServerMock;

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("deprecation")
class PlayerUtilsTest {

    public NiveriaAPI plugin;

    private PlayerMock player1;
    private PlayerMock player2;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkitHelper.safeMock();

        this.player1 = server.addPlayer("PuppyTransGirl");
        this.player2 = server.addPlayer("Toutouchien");

        this.plugin = MockBukkit.load(NiveriaAPI.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkitHelper.safeUnmock();
    }

    @Test
    @DisplayName("isVanished: null player throws, player without metadata is not vanished")
    void isVanished_nullAndNoMetadata() {
        assertThrows(NullPointerException.class, () -> PlayerUtils.isVanished(null));

        assertFalse(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished: supports Boolean and TriState metadata values")
    void isVanished_booleanAndTriState() {
        // Boolean true / false
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertTrue(PlayerUtils.isVanished(player1));

        player1.removeMetadata("vanished", plugin);
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, false));
        assertFalse(PlayerUtils.isVanished(player1));

        // TriState TRUE
        player1.removeMetadata("vanished", plugin);
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.TRUE));
        assertTrue(PlayerUtils.isVanished(player1));

        // TriState FALSE
        player1.removeMetadata("vanished", plugin);
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.FALSE));
        assertFalse(PlayerUtils.isVanished(player1));

        // TriState NOT_SET
        player1.removeMetadata("vanished", plugin);
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.NOT_SET));
        assertFalse(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished: TRUE from any plugin wins, non Boolean/TriState values are ignored")
    void isVanished_anyTrueFromAnyPluginAndInvalidTypes() {
        // Any TRUE from any plugin wins over FALSE
        PluginMock pluginA = MockBukkit.createMockPlugin("PluginA");
        PluginMock pluginB = MockBukkit.createMockPlugin("PluginB");

        player1.setMetadata("vanished", new FixedMetadataValue(pluginA, true));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginB, false));

        assertTrue(PlayerUtils.isVanished(player1));

        // Many TRUE values still result in vanished
        for (int i = 0; i < 5; i++) {
            PluginMock somePlugin = MockBukkit.createMockPlugin("SomePlugin" + i);
            player2.setMetadata("vanished", new FixedMetadataValue(somePlugin, true));
        }
        assertTrue(PlayerUtils.isVanished(player2));

        // Clear player1 metadata for next scenario
        player1.removeMetadata("vanished", pluginA);
        player1.removeMetadata("vanished", pluginB);

        // Non-Boolean/TriState metadata values do not mark vanished
        PluginMock pluginC = MockBukkit.createMockPlugin("PluginC");
        PluginMock pluginD = MockBukkit.createMockPlugin("PluginD");
        PluginMock pluginE = MockBukkit.createMockPlugin("PluginE");
        PluginMock pluginF = MockBukkit.createMockPlugin("PluginF");

        player1.setMetadata("vanished", new FixedMetadataValue(pluginC, "true"));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginD, 1));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginE, null));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginF, new Object()));

        assertFalse(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("nonVanishedPlayers: when nobody is vanished, all online players " + "are returned")
    void nonVanishedPlayers_allReturnedWhenNoneVanished() {
        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertEquals(2, result.size());
        assertTrue(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayers: excludes players marked vanished with " + "Boolean or TriState, and TRUE from any plugin wins")
    void nonVanishedPlayers_excludesVanishedPlayers() {
        // Boolean true excludes player1
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        Collection<? extends Player> result1 = PlayerUtils.nonVanishedPlayers();

        assertFalse(result1.contains(player1));
        assertTrue(result1.contains(player2));

        // Boolean false on both keeps both
        player1.removeMetadata("vanished", plugin);
        player2.removeMetadata("vanished", plugin);

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, false));
        player2.setMetadata("vanished", new FixedMetadataValue(plugin, false));

        Collection<? extends Player> result2 = PlayerUtils.nonVanishedPlayers();
        assertTrue(result2.contains(player1));
        assertTrue(result2.contains(player2));

        // TriState TRUE excludes, FALSE includes
        player1.removeMetadata("vanished", plugin);
        player2.removeMetadata("vanished", plugin);

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.TRUE));
        player2.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.FALSE));

        Collection<? extends Player> result3 = PlayerUtils.nonVanishedPlayers();

        assertFalse(result3.contains(player1));
        assertTrue(result3.contains(player2));

        // Any TRUE from any plugin excludes, even if another plugin says FALSE
        PluginMock otherPlugin = MockBukkit.createMockPlugin("OtherPlugin");

        player1.removeMetadata("vanished", plugin);
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        player1.setMetadata("vanished", new FixedMetadataValue(otherPlugin, false));

        Collection<? extends Player> result4 = PlayerUtils.nonVanishedPlayers();
        assertFalse(result4.contains(player1));
        assertTrue(result4.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayers: returns empty collection when all players are vanished")
    void nonVanishedPlayers_returnsEmptyWhenAllVanished() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        player2.setMetadata("vanished", new FixedMetadataValue(plugin, true));

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("nonVanishedPlayers: players with non Boolean/TriState metadata are treated as non-vanished")
    void nonVanishedPlayers_ignoresNonBooleanMetadata() {
        PluginMock pluginA = MockBukkit.createMockPlugin("PluginA");
        PluginMock pluginB = MockBukkit.createMockPlugin("PluginB");
        PluginMock pluginC = MockBukkit.createMockPlugin("PluginC");
        PluginMock pluginD = MockBukkit.createMockPlugin("PluginD");

        player1.setMetadata("vanished", new FixedMetadataValue(pluginA, "true"));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginB, 1));
        player2.setMetadata("vanished", new FixedMetadataValue(pluginC, null));
        player2.setMetadata("vanished", new FixedMetadataValue(pluginD, new Object()));

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertTrue(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayer(name): null throws NPE, offline player returns null")
    void nonVanishedPlayerByName_nullAndOffline() {
        assertThrows(NullPointerException.class, () -> PlayerUtils.nonVanishedPlayer((String) null));

        assertNull(PlayerUtils.nonVanishedPlayer("Ghost"));
    }

    @Test
    @DisplayName("nonVanishedPlayer(name): returns player when online and not vanished, null when vanished (Boolean/TriState)")
    void nonVanishedPlayerByName_respectsVanishStatus() {
        assertEquals(player1, PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertNull(PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));

        player1.removeMetadata("vanished", plugin);
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.TRUE));
        assertNull(PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));
    }

    @Test
    @DisplayName("nonVanishedPlayerExact(name): null throws NPE, offline " + "player returns null")
    void nonVanishedPlayerExact_nullAndOffline() {
        assertThrows(NullPointerException.class, () -> PlayerUtils.nonVanishedPlayerExact(null));

        assertNull(PlayerUtils.nonVanishedPlayerExact("Ghost"));
    }

    @Test
    @DisplayName("nonVanishedPlayerExact(name): returns player when exact name " + "online, null when vanished")
    void nonVanishedPlayerExact_respectsVanishStatus() {
        assertEquals(player1, PlayerUtils.nonVanishedPlayerExact("PuppyTransGirl"));

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertNull(PlayerUtils.nonVanishedPlayerExact("PuppyTransGirl"));
    }

    @Test
    @DisplayName("nonVanishedPlayer(uuid): null throws NPE, unknown UUID returns null")
    void nonVanishedPlayerByUuid_nullAndOffline() {
        assertThrows(NullPointerException.class, () -> PlayerUtils.nonVanishedPlayer((UUID) null));

        assertNull(PlayerUtils.nonVanishedPlayer(UUID.randomUUID()));
    }

    @Test
    @DisplayName("nonVanishedPlayer(uuid): returns player when online and not vanished, null when vanished")
    void nonVanishedPlayerByUuid_respectsVanishStatus() {
        assertEquals(player1, PlayerUtils.nonVanishedPlayer(player1.getUniqueId()));

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertNull(PlayerUtils.nonVanishedPlayer(player1.getUniqueId()));
    }

    @Test
    @DisplayName("isValidPlayerName: null throws NullPointerException")
    void isValidPlayerName_nullThrows() {
        assertThrows(NullPointerException.class, () -> PlayerUtils.isValidPlayerName(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "abc",
            "NameOK_123",
            "abcdefghijklmnop",
            "A1_",
            "___"
    })
    @DisplayName("isValidPlayerName: valid names return true")
    void isValidPlayerName_validNamesReturnTrue(String name) {
        assertTrue(PlayerUtils.isValidPlayerName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            // too short / too long
            "",
            "a",
            "ab",
            "abcdefghijklmnopq",
            // spaces / control chars
            "ab c",
            "abc ",
            " abc",
            "ab\tc",
            "ab\nc",
            "ab\rc",
            // non-ASCII (including DEL)
            "Jörg",
            "Renée",
            "名字",
            "abc\u007F"
    })
    @DisplayName("isValidPlayerName: invalid names return false")
    void isValidPlayerName_invalidNamesReturnFalse(String name) {
        assertFalse(PlayerUtils.isValidPlayerName(name));
    }
}