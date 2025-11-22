package toutouchien.niveriaapi.utils.game;

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
import toutouchien.niveriaapi.utils.PlayerUtils;

import java.util.Collection;
import java.util.UUID;

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

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    @DisplayName("isVanished should return true regardless of the number of true values as the vanished metadata")
    void isVanished_shouldReturnTrueRegardlessNumberOfTrueValuesAsVanishedMetadata(int loopTimes) {
        for (int i = 0; i < loopTimes; i++) {
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
    @DisplayName("isVanished should throw an NullPointerException when passed null")
    void isVanished_shouldThrowWhenPassedNull() {
        assertThrows(NullPointerException.class, () -> PlayerUtils.isVanished(null));
    }

    @Test
    @DisplayName("nonVanishedPlayers should return all online players when none are vanished")
    void nonVanishedPlayers_shouldReturnAllWhenNoneVanished() {
        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertEquals(2, result.size());
        assertTrue(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayers should exclude a player when vanished (boolean true)")
    void nonVanishedPlayers_shouldExcludePlayerWhenVanishedBooleanTrue() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertEquals(1, result.size());
        assertFalse(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayers should include players with boolean false only")
    void nonVanishedPlayers_shouldIncludePlayersWithBooleanFalseOnly() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, false));
        player2.setMetadata("vanished", new FixedMetadataValue(plugin, false));

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertEquals(2, result.size());
        assertTrue(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayers should support TriState and exclude TRUE, include FALSE/NOT_SET")
    void nonVanishedPlayers_shouldSupportTriState() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.TRUE));
        player2.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.FALSE));

        Collection<? extends Player> result1 = PlayerUtils.nonVanishedPlayers();

        assertEquals(1, result1.size());
        assertFalse(result1.contains(player1));
        assertTrue(result1.contains(player2));

        player2.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.NOT_SET));

        Collection<? extends Player> result2 = PlayerUtils.nonVanishedPlayers();

        assertEquals(1, result2.size());
        assertFalse(result2.contains(player1));
        assertTrue(result2.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayers should exclude a player if any plugin reports TRUE even if others FALSE")
    void nonVanishedPlayers_shouldExcludeIfAnyPluginReportsTrue() {
        PluginMock otherPlugin = MockBukkit.createMockPlugin();

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        player1.setMetadata("vanished", new FixedMetadataValue(otherPlugin, false));

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertFalse(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    @DisplayName("nonVanishedPlayers should exclude a player regardless of the number of TRUE values")
    void nonVanishedPlayers_shouldExcludeRegardlessOfNumberOfTrues(int loopTimes) {
        for (int i = 0; i < loopTimes; i++) {
            PluginMock somePlugin = MockBukkit.createMockPlugin();
            player1.setMetadata("vanished", new FixedMetadataValue(somePlugin, true));
        }

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertFalse(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5, 10})
    @DisplayName("nonVanishedPlayers should exclude a player with repeated TriState.TRUE values")
    void nonVanishedPlayers_shouldExcludeWithRepeatedTriStateTrue(int loopTimes) {
        for (int i = 0; i < loopTimes; i++) {
            PluginMock somePlugin = MockBukkit.createMockPlugin();
            player1.setMetadata("vanished", new FixedMetadataValue(somePlugin, TriState.TRUE));
        }

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertFalse(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayers should include players when metadata is not Boolean/TriState")
    void nonVanishedPlayers_shouldIncludeWhenMetadataIsNotBooleanOrTriState() {
        PluginMock pluginA = MockBukkit.createMockPlugin();
        PluginMock pluginB = MockBukkit.createMockPlugin();
        PluginMock pluginC = MockBukkit.createMockPlugin();
        PluginMock pluginD = MockBukkit.createMockPlugin();

        player1.setMetadata("vanished", new FixedMetadataValue(pluginA, "true"));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginB, 1));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginC, null));
        player1.setMetadata("vanished", new FixedMetadataValue(pluginD, new Object()));

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertTrue(result.contains(player1));
        assertTrue(result.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayers should return only the first player when second is vanished")
    void nonVanishedPlayers_shouldReturnOnlyFirstWhenSecondVanished() {
        player2.setMetadata("vanished", new FixedMetadataValue(plugin, true));

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertEquals(1, result.size());
        assertTrue(result.contains(player1));
        assertFalse(result.contains(player2));
    }

    @Test
    @DisplayName("nonVanishedPlayers should return empty when all are vanished")
    void nonVanishedPlayers_shouldReturnEmptyWhenAllVanished() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        player2.setMetadata("vanished", new FixedMetadataValue(plugin, true));

        Collection<? extends Player> result = PlayerUtils.nonVanishedPlayers();

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should throw NullPointerException when name is null")
    void nonVanishedPlayerByName_shouldThrowWhenNameIsNull() {
        assertThrows(NullPointerException.class, () -> PlayerUtils.nonVanishedPlayer((String) null));
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should return null when player not online")
    void nonVanishedPlayerByName_shouldReturnNullWhenNotOnline() {
        assertNull(PlayerUtils.nonVanishedPlayer("Ghost"));
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should return the player when online and not vanished")
    void nonVanishedPlayerByName_shouldReturnPlayerWhenNotVanished() {
        assertEquals(player1, PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should return null when the player is vanished (boolean true)")
    void nonVanishedPlayerByName_shouldReturnNullWhenVanishedBooleanTrue() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertNull(PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should support TriState values")
    void nonVanishedPlayerByName_shouldSupportTriState() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.TRUE));
        assertNull(PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.FALSE));
        assertEquals(player1, PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));

        player1.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.NOT_SET));
        assertEquals(player1, PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should return null if any plugin reports TRUE even if others FALSE")
    void nonVanishedPlayerByName_shouldReturnNullIfAnyPluginReportsTrue() {
        PluginMock otherPlugin = MockBukkit.createMockPlugin();
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        player1.setMetadata("vanished", new FixedMetadataValue(otherPlugin, false));

        assertNull(PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));
    }

    @Test
    @DisplayName("nonVanishedPlayer(String) should return the player when metadata is not Boolean/TriState")
    void nonVanishedPlayerByName_shouldReturnWhenMetadataIsNotBooleanOrTriState() {
        PluginMock pluginA = MockBukkit.createMockPlugin();
        player1.setMetadata("vanished", new FixedMetadataValue(pluginA, "true"));

        assertEquals(player1, PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    @DisplayName("nonVanishedPlayer(String) should return null regardless of number of TRUE values")
    void nonVanishedPlayerByName_shouldReturnNullRegardlessNumberOfTrues(int loopTimes) {
        for (int i = 0; i < loopTimes; i++) {
            PluginMock somePlugin = MockBukkit.createMockPlugin();
            player1.setMetadata("vanished", new FixedMetadataValue(somePlugin, true));
        }

        assertNull(PlayerUtils.nonVanishedPlayer("PuppyTransGirl"));
    }

    @Test
    @DisplayName("nonVanishedPlayerExact(String) should throw NullPointerException when name is null")
    void nonVanishedPlayerExact_shouldThrowWhenNameIsNull() {
        assertThrows(NullPointerException.class, () -> PlayerUtils.nonVanishedPlayerExact(null));
    }

    @Test
    @DisplayName("nonVanishedPlayerExact(String) should return null when player not online")
    void nonVanishedPlayerExact_shouldReturnNullWhenNotOnline() {
        assertNull(PlayerUtils.nonVanishedPlayerExact("Ghost"));
    }

    @Test
    @DisplayName("nonVanishedPlayerExact(String) should return the player when exact and not vanished")
    void nonVanishedPlayerExact_shouldReturnPlayerWhenExactAndNotVanished() {
        assertEquals(player1, PlayerUtils.nonVanishedPlayerExact("PuppyTransGirl"));
    }

    @Test
    @DisplayName("nonVanishedPlayerExact(String) should return null when the player is vanished")
    void nonVanishedPlayerExact_shouldReturnNullWhenVanished() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertNull(PlayerUtils.nonVanishedPlayerExact("PuppyTransGirl"));
    }

    @Test
    @DisplayName("nonVanishedPlayerExact(String) should support TriState values")
    void nonVanishedPlayerExact_shouldSupportTriState() {
        player2.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.TRUE));
        assertNull(PlayerUtils.nonVanishedPlayerExact("Toutouchien"));

        player2.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.FALSE));
        assertEquals(player2, PlayerUtils.nonVanishedPlayerExact("Toutouchien"));

        player2.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.NOT_SET));
        assertEquals(player2, PlayerUtils.nonVanishedPlayerExact("Toutouchien"));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 10})
    @DisplayName("nonVanishedPlayerExact(String) should return null regardless of number of TRUE values")
    void nonVanishedPlayerExact_shouldReturnNullRegardlessNumberOfTrues(int loopTimes) {
        for (int i = 0; i < loopTimes; i++) {
            PluginMock somePlugin = MockBukkit.createMockPlugin();
            player2.setMetadata("vanished", new FixedMetadataValue(somePlugin, true));
        }

        assertNull(PlayerUtils.nonVanishedPlayerExact("Toutouchien"));
    }

    @Test
    @DisplayName("nonVanishedPlayer(UUID) should throw NullPointerException when uuid is null")
    void nonVanishedPlayerByUuid_shouldThrowWhenUuidIsNull() {
        assertThrows(NullPointerException.class, () -> PlayerUtils.nonVanishedPlayer((UUID) null));
    }

    @Test
    @DisplayName("nonVanishedPlayer(UUID) should return null when player not online")
    void nonVanishedPlayerByUuid_shouldReturnNullWhenNotOnline() {
        assertNull(PlayerUtils.nonVanishedPlayer(UUID.randomUUID()));
    }

    @Test
    @DisplayName("nonVanishedPlayer(UUID) should return the player when online and not vanished")
    void nonVanishedPlayerByUuid_shouldReturnPlayerWhenNotVanished() {
        assertEquals(player1, PlayerUtils.nonVanishedPlayer(player1.getUniqueId()));
    }

    @Test
    @DisplayName("nonVanishedPlayer(UUID) should return null when the player is vanished")
    void nonVanishedPlayerByUuid_shouldReturnNullWhenVanished() {
        player1.setMetadata("vanished", new FixedMetadataValue(plugin, true));
        assertNull(PlayerUtils.nonVanishedPlayer(player1.getUniqueId()));
    }

    @Test
    @DisplayName("nonVanishedPlayer(UUID) should support TriState values")
    void nonVanishedPlayerByUuid_shouldSupportTriState() {
        player2.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.TRUE));
        assertNull(PlayerUtils.nonVanishedPlayer(player2.getUniqueId()));

        player2.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.FALSE));
        assertEquals(player2, PlayerUtils.nonVanishedPlayer(player2.getUniqueId()));

        player2.setMetadata("vanished", new FixedMetadataValue(plugin, TriState.NOT_SET));
        assertEquals(player2, PlayerUtils.nonVanishedPlayer(player2.getUniqueId()));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10})
    @DisplayName("nonVanishedPlayer(UUID) should return null regardless of number of TRUE values")
    void nonVanishedPlayerByUuid_shouldReturnNullRegardlessNumberOfTrues(int loopTimes) {
        for (int i = 0; i < loopTimes; i++) {
            PluginMock somePlugin = MockBukkit.createMockPlugin();
            player1.setMetadata("vanished", new FixedMetadataValue(somePlugin, true));
        }

        assertNull(PlayerUtils.nonVanishedPlayer(player1.getUniqueId()));
    }

    @Test
    @DisplayName("isValidPlayerName should throw NullPointerException when null")
    void isValidPlayerName_shouldThrowWhenNull() {
        assertThrows(NullPointerException.class, () -> PlayerUtils.isValidPlayerName(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"abc", "NameOK_123", "abcdefghijklmnop", "A1_", "___"})
    @DisplayName("isValidPlayerName should return true for valid names")
    void isValidPlayerName_shouldReturnTrueForValidNames(String name) {
        assertTrue(PlayerUtils.isValidPlayerName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "a", "ab", "abcdefghijklmnopq"})
    @DisplayName("isValidPlayerName should return false for too short or too long names")
    void isValidPlayerName_shouldReturnFalseForInvalidLengths(String name) {
        assertFalse(PlayerUtils.isValidPlayerName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ab c", "abc ", " abc", "ab\tc", "ab\nc", "ab\rc"})
    @DisplayName("isValidPlayerName should return false for names with spaces/control chars")
    void isValidPlayerName_shouldReturnFalseForSpacesOrControl(String name) {
        assertFalse(PlayerUtils.isValidPlayerName(name));
    }

    @ParameterizedTest
    @ValueSource(strings = {"Jörg", "Renée", "名字", "abc\u007F"})
    @DisplayName("isValidPlayerName should return false for non-ASCII characters")
    void isValidPlayerName_shouldReturnFalseForNonAscii(String name) {
        assertFalse(PlayerUtils.isValidPlayerName(name));
    }
}
