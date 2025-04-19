package toutouchien.niveriaapi.hook.impl.luckperms;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * Interface for managing and caching metadata values associated with players.
 * This allows efficient retrieval of metadata values such as integers, strings, booleans, and lists,
 * while also providing cache invalidation mechanisms.
 */
public interface MetaCache {

    /**
     * Retrieves a boolean metadata value for the specified player.
     *
     * @param player       The player whose metadata is being queried.
     * @param metaKey      The key associated with the metadata value.
     * @param defaultValue The default value to return if the metadata is not found.
     * @return The boolean metadata value, or the default value if not found.
     */
    boolean booleanMeta(Player player, String metaKey, boolean defaultValue);

    /**
     * Retrieves a double metadata value for the specified player.
     *
     * @param player       The player whose metadata is being queried.
     * @param metaKey      The key associated with the metadata value.
     * @param defaultValue The default value to return if the metadata is not found.
     * @return The double metadata value, or the default value if not found.
     */
    double doubleMeta(Player player, String metaKey, double defaultValue);

    /**
     * Retrieves an enum metadata value for the specified player.
     *
     * @param player       The player whose metadata is being queried.
     * @param metaKey      The key associated with the metadata value.
     * @param enumClass    The enum class to which the metadata value should be converted.
     * @param defaultValue The default value to return if the metadata is not found or invalid.
     * @param <T>          The type of the enum.
     * @return The enum metadata value, or the default value if not found or invalid.
     */
    <T extends Enum<T>> T enumMeta(Player player, String metaKey, Class<T> enumClass, T defaultValue);

    /**
     * Retrieves an integer metadata value for the specified player.
     *
     * @param player       The player whose metadata is being queried.
     * @param metaKey      The key associated with the metadata value.
     * @param defaultValue The default value to return if the metadata is not found.
     * @return The integer metadata value, or the default value if not found.
     */
    int integerMeta(Player player, String metaKey, int defaultValue);

    /**
     * Retrieves a list of string metadata values for the specified player.
     *
     * @param player  The player whose metadata is being queried.
     * @param metaKey The key associated with the metadata value.
     * @return A list of string metadata values, or an empty list if not found.
     */
    List<String> listMeta(Player player, String metaKey);

    /**
     * Retrieves a string metadata value for the specified player.
     *
     * @param player       The player whose metadata is being queried.
     * @param metaKey      The key associated with the metadata value.
     * @param defaultValue The default value to return if the metadata is not found.
     * @return The string metadata value, or the default value if not found.
     */
    String stringMeta(Player player, String metaKey, String defaultValue);

    /**
     * Invalidates the cached metadata value for a specific player and key.
     * This forces a recalculation the next time the value is requested.
     *
     * @param player  The player whose metadata cache should be invalidated.
     * @param metaKey The specific metadata key to invalidate.
     */
    void invalidateCache(Player player, String metaKey);

    /**
     * Invalidates the entire cached metadata for a specific player.
     * This forces a recalculation the next time any meta value is requested.
     *
     * @param player The player whose entire metadata cache should be cleared.
     */
    void invalidateCache(Player player);
}
