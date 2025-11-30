package toutouchien.niveriaapi.cooldown;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.utils.Task;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * A comprehensive cooldown management system for Bukkit plugins using Keys,
 * with database persistence enabled by default.
 * <p>
 * This system allows tracking cooldowns for players or any UUID-based entity with
 * Key identifiers, automatic cleanup, flexible time specifications, and persistence
 * via a {@link CooldownDatabase} implementation. Cooldowns are persistent by default
 * unless explicitly set as non-persistent.
 * <p>
 * Example usage:
 * <pre>{@code
 * CooldownManager cooldownManager = NiveriaAPI.instance().cooldownManager();
 *
 * // Create cooldown keys
 * Key fireballKey = Key.key("plugin_name", "ability_fireball");
 * Key teleportKey = Key.key("plugin_name", "ability_teleport");
 * Key sprintKey = Key.key("plugin_name", "ability_sprint");
 *
 * // Register a persistent cooldown (default behavior)
 * cooldownManager.setCooldown(player, fireballKey, Duration.ofSeconds(30));
 *
 * // Register another persistent cooldown (explicitly persistent)
 * cooldownManager.setCooldown(player, teleportKey, Duration.ofHours(1), true);
 *
 * // Register a non-persistent cooldown (in-memory only)
 * cooldownManager.setCooldown(player, sprintKey, Duration.ofSeconds(5), false);
 *
 * // Check if player is in cooldown
 * if (cooldownManager.inCooldown(player, fireballKey)) {
 *     long remainingSeconds = cooldownManager.remainingTime(player, fireballKey).getSeconds();
 *     player.sendMessage(Component.text("You must wait " + remainingSeconds + " seconds to use this ability again!", NamedTextColor.RED));
 *     return;
 * }
 *
 * // Execute ability code
 * }
 * </pre>
 */
public class CooldownManager {
    private static final long DEFAULT_CLEANUP_MINUTES = 3L;
    private static final long DATABASE_CLEANUP_MINUTES = 15L; // Less frequent DB cleanup

    private final NiveriaAPI plugin;
    private final Map<CompositeKey, Cooldown> cooldowns = new ConcurrentHashMap<>();
    private final ScheduledTask cleanupTask;
    private final ScheduledTask databaseCleanupTask;
    private final CooldownDatabase database;

    /**
     * Constructs a new CooldownManager.
     *
     * @param plugin   The main plugin instance.
     * @param database The database implementation for persistent cooldowns. Must not be null.
     */
    public CooldownManager(@NotNull NiveriaAPI plugin, @NotNull CooldownDatabase database) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkNotNull(database, "database cannot be null");

        this.plugin = plugin;
        this.database = database;
        loadPersistentCooldowns();

        this.cleanupTask = Task.asyncRepeat(ignored -> cleanupExpiredCooldowns(), plugin, DEFAULT_CLEANUP_MINUTES, DEFAULT_CLEANUP_MINUTES, TimeUnit.MINUTES);
        this.databaseCleanupTask = Task.asyncRepeat(ignored -> cleanupDatabaseCooldowns(), plugin, DATABASE_CLEANUP_MINUTES, DATABASE_CLEANUP_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Loads persistent cooldowns from the database into memory.
     * Should be called on startup. Runs asynchronously.
     */
    private void loadPersistentCooldowns() {
        Task.async(task -> {
            List<Cooldown> persistentCooldowns = database.loadAllCooldowns();
            persistentCooldowns.forEach(cooldown -> {
                if (cooldown.expired())
                    return;

                cooldowns.put(new CompositeKey(cooldown.key(), cooldown.uuid()), cooldown);
            });

            plugin.getLogger().info("Loaded " + persistentCooldowns.size() + " active persistent cooldowns.");
        }, plugin);
    }

    /**
     * Sets a cooldown for a UUID.
     *
     * @param uuid       The UUID.
     * @param key        The cooldown identifier.
     * @param duration   The cooldown duration.
     * @param persistent Whether this cooldown should be saved to the database (true by default).
     * @return The created cooldown, or null if the duration is zero or negative.
     */
    public Cooldown setCooldown(@NotNull UUID uuid, @NotNull Key key, @NotNull Duration duration, boolean persistent) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(duration, "duration cannot be null");

        if (duration.isZero() || duration.isNegative())
            return null;

        Cooldown cooldown = new Cooldown(uuid, key, duration.toMillis() + System.currentTimeMillis(), persistent);
        cooldowns.put(new CompositeKey(key, uuid), cooldown);

        if (persistent)
            database.saveCooldown(cooldown);

        return cooldown;
    }

    /**
     * Sets a cooldown for a player.
     *
     * @param player     The player.
     * @param key        The cooldown identifier.
     * @param duration   The cooldown duration.
     * @param persistent Whether this cooldown should be saved to the database (true by default).
     * @return The created cooldown, or null if the duration is zero or negative.
     */
    public Cooldown setCooldown(@NotNull Player player, @NotNull Key key, @NotNull Duration duration, boolean persistent) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(duration, "duration cannot be null");

        return setCooldown(player.getUniqueId(), key, duration, persistent);
    }

    /**
     * Sets a cooldown in milliseconds.
     *
     * @param uuid           The UUID.
     * @param key            The cooldown identifier.
     * @param cooldownMillis The cooldown in milliseconds.
     * @param persistent     Whether this cooldown should be saved to the database (true by default).
     * @return The created cooldown, or null if the duration is zero or negative.
     */
    public Cooldown setCooldown(@NotNull UUID uuid, @NotNull Key key, long cooldownMillis, boolean persistent) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return setCooldown(uuid, key, Duration.ofMillis(cooldownMillis), persistent);
    }

    /**
     * Sets a cooldown for a player in milliseconds.
     *
     * @param player         The player.
     * @param key            The cooldown identifier.
     * @param cooldownMillis The cooldown in milliseconds.
     * @param persistent     Whether this cooldown should be saved to the database (true by default).
     * @return The created cooldown, or null if the duration is zero or negative.
     */
    public Cooldown setCooldown(@NotNull Player player, @NotNull Key key, long cooldownMillis, boolean persistent) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return setCooldown(player.getUniqueId(), key, cooldownMillis, persistent);
    }

    /**
     * Sets a persistent cooldown for a player.
     * This cooldown will be saved to the database.
     *
     * @param player   The player
     * @param key      The cooldown identifier
     * @param duration The cooldown duration
     * @return The created cooldown, or null if the duration is zero or negative.
     */
    public Cooldown setCooldown(@NotNull Player player, @NotNull Key key, @NotNull Duration duration) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(duration, "duration cannot be null");

        return setCooldown(player, key, duration, true);
    }

    /**
     * Sets a persistent cooldown for a UUID.
     * This cooldown will be saved to the database.
     *
     * @param uuid     The UUID
     * @param key      The cooldown identifier
     * @param duration The cooldown duration
     * @return The created cooldown, or null if the duration is zero or negative.
     */
    public Cooldown setCooldown(@NotNull UUID uuid, @NotNull Key key, @NotNull Duration duration) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(duration, "duration cannot be null");

        return setCooldown(uuid, key, duration, true);
    }

    /**
     * Sets a persistent cooldown in milliseconds.
     * This cooldown will be saved to the database.
     *
     * @param uuid           The UUID
     * @param key            The cooldown identifier
     * @param cooldownMillis The cooldown in milliseconds
     * @return The created cooldown, or null if the duration is zero or negative.
     */
    public Cooldown setCooldown(@NotNull UUID uuid, @NotNull Key key, long cooldownMillis) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return setCooldown(uuid, key, cooldownMillis, true);
    }

    /**
     * Sets a persistent cooldown for a player in milliseconds.
     * This cooldown will be saved to the database.
     *
     * @param player         The player
     * @param key            The cooldown identifier
     * @param cooldownMillis The cooldown in milliseconds
     * @return The created cooldown, or null if the duration is zero or negative.
     */
    public Cooldown setCooldown(@NotNull Player player, @NotNull Key key, long cooldownMillis) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return setCooldown(player, key, cooldownMillis, true);
    }

    /**
     * Removes a cooldown from memory.
     * If the cooldown was persistent, it also attempts to remove it from the database asynchronously.
     *
     * @param uuid The UUID.
     * @param key  The cooldown identifier.
     * @return {@code true} if a cooldown was found and removed from memory, {@code false} otherwise.
     */
    public boolean removeCooldown(@NotNull UUID uuid, @NotNull Key key) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return removeCooldown(uuid, key, true);
    }

    /**
     * Removes a cooldown from memory.
     * If the cooldown was persistent, it also attempts to remove it from the database asynchronously.
     *
     * @param player The player.
     * @param key    The cooldown identifier.
     * @return {@code true} if a cooldown was found and removed from memory, {@code false} otherwise.
     */
    public boolean removeCooldown(@NotNull Player player, @NotNull Key key) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return removeCooldown(player.getUniqueId(), key, true);
    }


    /**
     * Removes a cooldown.
     *
     * @param uuid               The UUID.
     * @param key                The cooldown identifier.
     * @param removeFromDatabase Whether to attempt to remove this cooldown from the database as well if it's persistent.
     *                           Set to false to remove from memory only.
     * @return {@code true} if a cooldown was found and removed from memory, {@code false} otherwise.
     */
    public boolean removeCooldown(@NotNull UUID uuid, @NotNull Key key, boolean removeFromDatabase) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        CompositeKey compositeKey = new CompositeKey(key, uuid);
        Cooldown removed = cooldowns.remove(compositeKey);

        if ((removed != null && removed.persistent() && removeFromDatabase) || (removed == null && removeFromDatabase))
            database.deleteCooldown(uuid, key);

        return removed != null;
    }

    /**
     * Removes a cooldown for a player.
     *
     * @param player             The player.
     * @param key                The cooldown identifier.
     * @param removeFromDatabase Whether to attempt to remove this cooldown from the database as well if it's persistent.
     *                           Set to false to remove from memory only.
     * @return {@code true} if a cooldown was found and removed from memory, {@code false} otherwise.
     */
    public boolean removeCooldown(@NotNull Player player, @NotNull Key key, boolean removeFromDatabase) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return removeCooldown(player.getUniqueId(), key, removeFromDatabase);
    }


    /**
     * Gets a cooldown by UUID and identifier.
     * Checks memory first.
     *
     * @param uuid The UUID.
     * @param key  The cooldown identifier.
     * @return The cooldown, or null if not found or expired.
     */
    @Nullable
    public Cooldown getCooldown(@NotNull UUID uuid, @NotNull Key key) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        CompositeKey compositeKey = new CompositeKey(key, uuid);
        Cooldown cooldown = cooldowns.get(compositeKey);

        if (cooldown != null && cooldown.expired()) {
            cooldowns.remove(compositeKey);
            return null;
        }

        return cooldown;
    }

    /**
     * Gets a cooldown by player and identifier.
     * Checks memory first.
     *
     * @param player The player.
     * @param key    The cooldown identifier.
     * @return The cooldown, or null if not found or expired.
     */
    @Nullable
    public Cooldown getCooldown(@NotNull Player player, @NotNull Key key) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return getCooldown(player.getUniqueId(), key);
    }

    /**
     * Checks if a UUID is in cooldown.
     *
     * @param uuid The UUID.
     * @param key  The cooldown identifier.
     * @return Whether the UUID is in cooldown.
     */
    public boolean inCooldown(@NotNull UUID uuid, @NotNull Key key) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return getCooldown(uuid, key) != null;
    }

    /**
     * Checks if a player is in cooldown.
     *
     * @param player The player.
     * @param key    The cooldown identifier.
     * @return Whether the player is in cooldown.
     */
    public boolean inCooldown(@NotNull Player player, @NotNull Key key) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return inCooldown(player.getUniqueId(), key);
    }

    /**
     * Gets the remaining time of a cooldown.
     *
     * @param uuid The UUID.
     * @param key  The cooldown identifier.
     * @return The remaining time, or Duration.ZERO if not in cooldown.
     */
    @NotNull
    public Duration remainingTime(@NotNull UUID uuid, @NotNull Key key) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        Cooldown cooldown = getCooldown(uuid, key);
        if (cooldown == null)
            return Duration.ZERO;

        return cooldown.remainingTime();
    }

    /**
     * Gets the remaining time of a cooldown for a player.
     *
     * @param player The player.
     * @param key    The cooldown identifier.
     * @return The remaining time, or Duration.ZERO if not in cooldown.
     */
    @NotNull
    public Duration remainingTime(@NotNull Player player, @NotNull Key key) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return remainingTime(player.getUniqueId(), key);
    }

    /**
     * Gets the total number of active cooldowns in memory.
     * Note: This does not necessarily reflect the number of persistent cooldowns in the database.
     *
     * @return The number of active cooldowns in memory.
     */
    public int size() {
        cleanupExpiredCooldowns();
        return cooldowns.size();
    }

    /**
     * Removes all cooldowns associated with a UUID from memory.
     * Also attempts to remove associated persistent cooldowns from the database asynchronously.
     *
     * @param uuid The UUID.
     */
    public void removeAllCooldowns(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");

        removeAllCooldowns(uuid, true);
    }

    /**
     * Removes all cooldowns associated with a player from memory.
     * Also attempts to remove associated persistent cooldowns from the database asynchronously.
     *
     * @param player The player.
     */
    public void removeAllCooldowns(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        removeAllCooldowns(player.getUniqueId(), true);
    }

    /**
     * Removes all cooldowns associated with a UUID from memory.
     *
     * @param uuid               The UUID.
     * @param removeFromDatabase Whether to attempt to remove persistent cooldowns for this UUID from the database as well.
     *                           Set to false to remove from memory only.
     */
    public void removeAllCooldowns(@NotNull UUID uuid, boolean removeFromDatabase) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");

        cooldowns.entrySet().removeIf(entry -> entry.getKey().uuid() == uuid);

        if (removeFromDatabase)
            database.deleteAllCooldowns(uuid);
    }

    /**
     * Removes all cooldowns associated with a player from memory.
     *
     * @param player             The player.
     * @param removeFromDatabase Whether to attempt to remove persistent cooldowns for this player from the database as well.
     *                           Set to false to remove from memory only.
     */
    public void removeAllCooldowns(@NotNull Player player, boolean removeFromDatabase) {
        Preconditions.checkNotNull(player, "player cannot be null");

        removeAllCooldowns(player.getUniqueId(), removeFromDatabase);
    }

    /**
     * Removes all cooldowns associated with a specific key from memory.
     * Also attempts to remove associated persistent cooldowns from the database asynchronously.
     *
     * @param key The cooldown identifier.
     * @return The number of cooldowns removed from memory.
     */
    public int removeAllCooldownsByKey(@NotNull Key key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        return removeAllCooldownsByKey(key, true);
    }

    /**
     * Removes all cooldowns associated with a specific key from memory.
     *
     * @param key                The cooldown identifier.
     * @param removeFromDatabase Whether to attempt to remove persistent cooldowns with this key from the database as well.
     *                           Set to false to remove from memory only.
     * @return The number of cooldowns removed from memory.
     */
    public int removeAllCooldownsByKey(@NotNull Key key, boolean removeFromDatabase) {
        Preconditions.checkNotNull(key, "key cannot be null");

        int count = 0;
        List<CompositeKey> keysToRemove = new ArrayList<>();
        cooldowns.forEach(((compositeKey, cooldown) -> {
            if (!compositeKey.key().equals(key))
                return;

            keysToRemove.add(compositeKey);
        }));

        for (CompositeKey compositeKey : keysToRemove) {
            Cooldown removed = cooldowns.remove(compositeKey);
            if (removed == null)
                continue;

            count++;
            if (!removed.persistent() || !removeFromDatabase)
                continue;

            database.deleteCooldown(removed.uuid(), removed.key());
        }

        return count;
    }


    /**
     * Removes all cooldowns associated with a specific namespace from memory.
     * Also attempts to remove associated persistent cooldowns from the database asynchronously.
     *
     * @param namespace The namespace.
     * @return The number of cooldowns removed from memory.
     */
    public int removeAllCooldownsByNamespace(@NotNull String namespace) {
        Preconditions.checkNotNull(namespace, "namespace cannot be null");

        return removeAllCooldownsByNamespace(namespace, true);
    }

    /**
     * Removes all cooldowns associated with a specific namespace from memory.
     *
     * @param namespace          The namespace.
     * @param removeFromDatabase Whether to attempt to remove persistent cooldowns with this namespace from the database as well.
     *                           Set to false to remove from memory only.
     * @return The number of cooldowns removed from memory.
     */
    public int removeAllCooldownsByNamespace(@NotNull String namespace, boolean removeFromDatabase) {
        Preconditions.checkNotNull(namespace, "namespace cannot be null");

        int count = 0;
        List<CompositeKey> keysToRemove = new ArrayList<>();

        // Find keys to remove from memory
        cooldowns.forEach((compositeKey, cooldown) -> {
            if (!compositeKey.key().namespace().equals(namespace))
                return;

            keysToRemove.add(compositeKey);
        });

        for (CompositeKey key : keysToRemove) {
            Cooldown removed = cooldowns.remove(key);
            if (removed == null)
                continue;

            count++;
            if (!removed.persistent() || !removeFromDatabase)
                continue;

            database.deleteCooldown(removed.uuid(), removed.key());
        }

        return count;
    }

    /**
     * Cleans up expired cooldowns from the in-memory map.
     * This runs regularly via the cleanup task.
     */
    public void cleanupExpiredCooldowns() {
        cooldowns.entrySet().removeIf(entry -> entry.getValue().expired());
    }

    /**
     * Cleans up expired persistent cooldowns from the database.
     * This runs regularly via the database cleanup task asynchronously.
     */
    private void cleanupDatabaseCooldowns() {
        database.deleteExpiredCooldowns();
    }

    /**
     * Shuts down the cooldown manager and cancels the cleanup tasks.
     * Does NOT clear cooldowns from the database by default.
     */
    public void shutdown() {
        if (cleanupTask != null && !cleanupTask.isCancelled())
            cleanupTask.cancel();

        if (databaseCleanupTask != null && !databaseCleanupTask.isCancelled())
            databaseCleanupTask.cancel();
    }

    /**
     * Composite key for the cooldown map.
     */
    private record CompositeKey(Key key, UUID uuid) {
    }
}