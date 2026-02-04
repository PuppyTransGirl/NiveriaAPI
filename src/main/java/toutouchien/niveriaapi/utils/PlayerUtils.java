package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

/**
 * Utility class for player-related operations.
 */
@SuppressWarnings("deprecation")
@NullMarked
public class PlayerUtils {
    private PlayerUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Checks if a player is vanished based on their metadata.
     *
     * @param player The player to check.
     * @return True if the player is vanished, false otherwise.
     */
    public static boolean isVanished(Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        List<MetadataValue> metadata = player.getMetadata("vanished");
        for (MetadataValue metadatum : metadata) {
            Object value = metadatum.value();
            if ((value instanceof Boolean bool && bool)
                    || (value instanceof TriState triState && triState == TriState.TRUE))
                return true;
        }

        return false;
    }

    /**
     * Retrieves a collection of all non-vanished players currently online.
     *
     * @return A collection of non-vanished players.
     */
    public static Collection<? extends Player> nonVanishedPlayers() {
        Set<Player> list = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isVanished(player))
                list.add(player);
        }

        return list;
    }

    /**
     * Retrieves a non-vanished player by their name.
     *
     * @param name The name of the player.
     * @return The non-vanished player, or null if not found or vanished.
     */
    @Nullable
    public static Player nonVanishedPlayer(String name) {
        Preconditions.checkNotNull(name, "name cannot be null");

        Player player = Bukkit.getPlayer(name);
        if (player == null || isVanished(player))
            return null;

        return player;
    }

    /**
     * Retrieves a non-vanished player by their exact name.
     *
     * @param name The exact name of the player.
     * @return The non-vanished player, or null if not found or vanished.
     */
    @Nullable
    public static Player nonVanishedPlayerExact(String name) {
        Preconditions.checkNotNull(name, "name cannot be null");

        Player player = Bukkit.getPlayerExact(name);
        if (player == null || isVanished(player))
            return null;

        return player;
    }

    /**
     * Retrieves a non-vanished player by their UUID.
     *
     * @param uuid The UUID of the player.
     * @return The non-vanished player, or null if not found or vanished.
     */
    @Nullable
    public static Player nonVanishedPlayer(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || isVanished(player))
            return null;

        return player;
    }

    /**
     * Validates if a given string is a valid player name.
     * A valid player name is between 3 and 16 characters long and
     * contains only letters (a-z, A-Z), digits (0-9), underscores (_), or periods (.).
     *
     * @param name The player name to validate.
     * @return True if the name is valid, false otherwise.
     */
    public static boolean isValidPlayerName(String name) {
        Preconditions.checkNotNull(name, "name cannot be null");

        if (name.length() < 3 || name.length() > 16)
            return false;

        for (int i = 0, len = name.length(); i < len; ++i) {
            char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || (c == '_' || c == '.'))
                continue;

            return false;
        }

        return true;
    }
}
