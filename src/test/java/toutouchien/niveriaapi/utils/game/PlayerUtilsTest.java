package toutouchien.niveriaapi.utils.game;

import org.bukkit.entity.Player;
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

import java.util.Collection;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PlayerUtilsTest {
    private ServerMock server;
    private PluginMock plugin;

    private PlayerMock player1;
    private PlayerMock player2;

    @BeforeEach
    void setUp() {
        this.server = MockBukkitHelper.safeMock();
        this.plugin = MockBukkit.createMockPlugin();

        this.player1 = this.server.addPlayer("Toutouchien");
        this.player2 = this.server.addPlayer("PuppyTransGirl");

        MockBukkit.load(NiveriaAPI.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkitHelper.safeUnmock();
    }

    @Test
    @DisplayName("isVanished should return false when player has no metadata")
    void isVanished_shouldReturnFalseWhenNoMetadata() {
        assertFalse(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished should return true when metadata contains true boolean value")
    void isVanished_shouldReturnTrueWhenVanishedMetadataTrue() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertTrue(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished should ignore false boolean metadata values")
    void isVanished_shouldIgnoreFalseValues() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, false));
        assertFalse(PlayerUtils.isVanished(player1));
    }

    @Test
    @DisplayName("isVanished should throw IllegalArgumentException when player is null")
    void isVanished_shouldThrowForNullPlayer() {
        assertThrows(IllegalArgumentException.class, () -> PlayerUtils.isVanished(null));
    }

    @Test
    @DisplayName("nonVanishedPlayers should include only players without vanished=true metadata")
    void nonVanishedPlayers_shouldReturnOnlyNonVanishedPlayers() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        player2.setMetadata("vanished", new FixedMetadataValue(plugin, false));

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();
        assertFalse(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayers should handle empty player list safely")
    void nonVanishedPlayers_shouldHandleEmptyList() {
        server.getOnlinePlayers().forEach(p -> p.kickPlayer("remove"));
        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();
        assertEquals(0, result.size());
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should return null if player not found")
    void nonVanishedPlayerByName_shouldReturnNullForMissingPlayer() {
        assertNull(PlayerUtils.nonVanishedPlayer("UnknownPlayer"));
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should throw IllegalArgumentException when input is null")
    void nonVanishedPlayerByName_shouldReturnNullForNullArgument() {
        assertThrows(IllegalArgumentException.class, () -> PlayerUtils.nonVanishedPlayer((String) null));
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should return player if not vanished")
    void nonVanishedPlayerByName_shouldReturnPlayerIfNotVanished() {
        assertEquals(player1, PlayerUtils.nonVanishedPlayer("Toutouchien"));
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should return null if vanished")
    void nonVanishedPlayerByName_shouldReturnNullIfVanished() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertNull(PlayerUtils.nonVanishedPlayer("Toutouchien"));
    }

    @Test
    @DisplayName("nonVanishedPlayerExact(String) shouldn't be case sensitive")
    void nonVanishedPlayerExact_shouldntBeCaseSensitive() {
        assertEquals(player1, PlayerUtils.nonVanishedPlayerExact("toutouchien"));
        assertEquals(player1, PlayerUtils.nonVanishedPlayerExact("Toutouchien"));
    }

    @Test
    @DisplayName("nonVanishedPlayerExact(String) should return null if vanished")
    void nonVanishedPlayerExact_shouldReturnNullIfVanished() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertNull(PlayerUtils.nonVanishedPlayerExact("Toutouchien"));
    }

    @Test
    @DisplayName("nonVanishedPlayerExact(String) should throw IllegalArgumentException on null input")
    void nonVanishedPlayerExact_shouldThrowOnNull() {
        assertThrows(IllegalArgumentException.class, () -> PlayerUtils.nonVanishedPlayerExact(null));
    }

    @Test
    @DisplayName("nonVanishedPlayer(UUID) should return null if player not found")
    void nonVanishedPlayerByUUID_shouldReturnNullIfMissing() {
        UUID random = UUID.randomUUID();
        assertNull(PlayerUtils.nonVanishedPlayer(random));
    }

    @Test
    @DisplayName("nonVanishedPlayer(UUID) should return player when not vanished")
    void nonVanishedPlayerByUUID_shouldReturnPlayerIfNonVanished() {
        assertEquals(player1, PlayerUtils.nonVanishedPlayer(player1.getUniqueId()));
    }

    @Test
    @DisplayName("nonVanishedPlayer(UUID) should return null if vanished")
    void nonVanishedPlayerByUUID_shouldReturnNullIfVanished() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertNull(PlayerUtils.nonVanishedPlayer(player1.getUniqueId()));
    }

    @Test
    @DisplayName("nonVanishedPlayer(UUID) should throw IllegalArgumentException when uuid is null")
    void nonVanishedPlayerByUUID_shouldThrowWhenUUIDIsNull() {
        assertThrows(IllegalArgumentException.class, () -> PlayerUtils.nonVanishedPlayer((UUID) null));
    }

    @Test
    @DisplayName("isValidPlayerName should return false for too-long names")
    void isValidPlayerName_shouldReturnFalseForTooLong() {
        assertFalse(PlayerUtils.isValidPlayerName("ThisNameIsWayTooLongForMinecraft"));
    }

    @Test
    @DisplayName("isValidPlayerName should return false when name contains invalid characters")
    void isValidPlayerName_shouldReturnFalseForInvalidCharacters() {
        assertFalse(PlayerUtils.isValidPlayerName("Name With Space"));
        assertFalse(PlayerUtils.isValidPlayerName("Invalid§Char"));
        assertFalse(PlayerUtils.isValidPlayerName("\nNewline"));
    }

    @Test
    @DisplayName("isValidPlayerName should return true for valid short alphanumeric names")
    void isValidPlayerName_shouldReturnTrueForValid() {
        assertTrue(PlayerUtils.isValidPlayerName("Steve"));
        assertTrue(PlayerUtils.isValidPlayerName("Alex_123"));
    }

    @Test
    @DisplayName("isValidPlayerName should throw IllegalArgumentException for null input")
    void isValidPlayerName_shouldThrowForNull() {
        assertThrows(IllegalArgumentException.class, () -> PlayerUtils.isValidPlayerName(null));
    }

    @Test
    @DisplayName("isValidPlayerName should return false for empty string")
    void isValidPlayerName_shouldReturnFalseForEmpty() {
        assertFalse(PlayerUtils.isValidPlayerName(""));
    }
}
