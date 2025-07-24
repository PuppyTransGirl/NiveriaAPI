package toutouchien.niveriaapi.hook.impl.luckperms;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface for managing and caching metadata values associated with players.
 * This allows efficient retrieval of metadata values such as integers, strings,
 * booleans, and enums while also providing cache invalidation mechanisms.
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
    boolean booleanMeta(@NotNull Player player, @NotNull String metaKey, boolean defaultValue);

    /**
     * Retrieves a double metadata value for the specified player.
     *
     * @param player       The player whose metadata is being queried.
     * @param metaKey      The key associated with the metadata value.
     * @param defaultValue The default value to return if the metadata is not found.
     * @return The double metadata value, or the default value if not found.
     */
    double doubleMeta(@NotNull Player player, @NotNull String metaKey, double defaultValue);

    /**
     * Retrieves an enum metadata value for the specified player.
     *
     * @param player       The player whose metadata is being queried.
     * @param metaKey      The key associated with the metadata value.
     * @param defaultValue The default value to return if the metadata is not found or invalid.
     * @param <T>          The enum type.
     * @return The enum metadata value, or the default value if not found or invalid.
     */
    @Nullable
    <T extends Enum<T>> T enumMeta(@NotNull Player player, @NotNull String metaKey, @NotNull T defaultValue);

    /**
     * Retrieves an enum metadata value for the specified player.
     *
     * @param player       The player whose metadata is being queried.
     * @param metaKey      The key associated with the metadata value.
     * @param enumClass    The enum class to which the metadata value should be converted.
     * @param defaultValue The default value to return if the metadata is not found or invalid.
     *                     May be null.
     * @param <T>          The enum type.
     * @return The enum metadata value, or the default value if not found or invalid.
     */
    @Nullable
    <T extends Enum<T>> T enumMeta(@NotNull Player player, @NotNull String metaKey, @NotNull Class<T> enumClass, @Nullable T defaultValue);

    /**
     * Retrieves an integer metadata value for the specified player.
     *
     * @param player       The player whose metadata is being queried.
     * @param metaKey      The key associated with the metadata value.
     * @param defaultValue The default value to return if the metadata is not found.
     * @return The integer metadata value, or the default value if not found.
     */
    int integerMeta(@NotNull Player player, @NotNull String metaKey, int defaultValue);

    /**
     * Retrieves a string metadata value for the specified player.
     *
     * @param player       The player whose metadata is being queried.
     * @param metaKey      The key associated with the metadata value.
     * @param defaultValue The default value to return if the metadata is not found.
     *                     May be null.
     * @return The string metadata value, or the default value (possibly null) if not found.
     */
    @Nullable
    String stringMeta(@NotNull Player player, @NotNull String metaKey, @Nullable String defaultValue);

    /**
     * Invalidates the cached metadata value for a specific player and key.
     * This forces a recalculation the next time the value is requested.
     *
     * @param player  The player whose metadata cache should be invalidated.
     * @param metaKey The specific metadata key to invalidate.
     */
    void invalidateCache(@NotNull Player player, @NotNull String metaKey);

    /**
     * Invalidates the entire cached metadata for a specific player.
     * This forces a recalculation the next time any meta value is requested.
     *
     * @param player The player whose entire metadata cache should be cleared.
     */
    void invalidateCache(@NotNull Player player);
}