package toutouchien.niveriaapi.cooldown;

import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.utils.base.Task;
import toutouchien.niveriaapi.utils.common.TimeUtils;

import java.time.Duration;
import java.util.*;
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
 * // Get the cooldown manager (assuming database is initialized elsewhere)
 * // CooldownManager cooldownManager = NiveriaAPI.instance().cooldownManager();
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
 * long remainingSeconds = cooldownManager.remainingTime(player, fireballKey).getSeconds();
 * MessageUtils.sendMessage(player, Component.text("You must wait " + remainingSeconds + " seconds to use this ability again!"));
 * return;
 * }
 *
 * // Execute ability code
 * }
 * </pre>
 */
public class CooldownManager {
    private static final long DEFAULT_CLEANUP_TICKS = TimeUtils.ticks(3L, TimeUnit.MINUTES);
    private static final long DATABASE_CLEANUP_TICKS = TimeUtils.ticks(15L, TimeUnit.MINUTES); // Less frequent DB cleanup

    private final NiveriaAPI plugin;
    private final Map<CompositeKey, Cooldown> cooldowns = new ConcurrentHashMap<>();
    private final BukkitTask cleanupTask;
    private final BukkitTask databaseCleanupTask;
    private final CooldownDatabase database; // Add database field

    /**
     * Constructs a new CooldownManager.
     *
     * @param plugin   The main plugin instance.
     * @param database The database implementation for persistent cooldowns. Must not be null.
     */
    public CooldownManager(@NotNull NiveriaAPI plugin, @NotNull CooldownDatabase database) {
        Objects.requireNonNull(plugin, "Plugin must not be null");
        Objects.requireNonNull(database, "CooldownDatabase must not be null");

        this.plugin = plugin;
        this.database = database;
        loadPersistentCooldowns();

        this.cleanupTask = Task.asyncRepeat(this::cleanupExpiredCooldowns, plugin, DEFAULT_CLEANUP_TICKS, DEFAULT_CLEANUP_TICKS);
        this.databaseCleanupTask = Task.asyncRepeat(this::cleanupDatabaseCooldowns, plugin, DATABASE_CLEANUP_TICKS, DATABASE_CLEANUP_TICKS);
    }

    /**
     * Loads persistent cooldowns from the database into memory.
     * Should be called on startup. Runs asynchronously.
     */
    private void loadPersistentCooldowns() {
        Task.async(() -> {
            List<Cooldown> persistentCooldowns = database.loadAllCooldowns();
            persistentCooldowns.forEach(cooldown -> {
                if (cooldown.expired())
                    return;

                cooldowns.put(new CompositeKey(cooldown.key(), cooldown.uuid()), cooldown);
            });

            plugin.getLogger().info("Loaded " + persistentCooldowns.size() + " active persistent cooldowns.");
        }, plugin);
    }

    // --- Set Cooldown Methods (New methods with persistence flag and modified defaults) ---

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
        Objects.requireNonNull(player, "Player must not be null");
        return setCooldown(player.getUniqueId(), key, duration, persistent);
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
        Objects.requireNonNull(uuid, "UUID must not be null");
        Objects.requireNonNull(key, "Cooldown key must not be null");
        Objects.requireNonNull(duration, "Duration must not be null");

        if (duration.isZero() || duration.isNegative())
            return null;

        Cooldown cooldown = new Cooldown(uuid, key, duration.toMillis() + System.currentTimeMillis(), persistent);
        cooldowns.put(new CompositeKey(key, uuid), cooldown);

        if (persistent)
            database.saveCooldown(cooldown);

        return cooldown;
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
        Objects.requireNonNull(player, "Player must not be null");
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
        return setCooldown(player, key, cooldownMillis, true);
    }

    /**
     * Removes a cooldown from memory.
     * If the cooldown was persistent, it also attempts to remove it from the database asynchronously.
     *
     * @param uuid The UUID.
     * @param key  The cooldown identifier.
     * @return True if a cooldown was found and removed from memory, false otherwise.
     */
    public boolean removeCooldown(@NotNull UUID uuid, @NotNull Key key) {
        return removeCooldown(uuid, key, true);
    }

    /**
     * Removes a cooldown from memory.
     * If the cooldown was persistent, it also attempts to remove it from the database asynchronously.
     *
     * @param player The player.
     * @param key    The cooldown identifier.
     * @return True if a cooldown was found and removed from memory, false otherwise.
     */
    public boolean removeCooldown(@NotNull Player player, @NotNull Key key) {
        Objects.requireNonNull(player, "Player must not be null");
        return removeCooldown(player.getUniqueId(), key, true);
    }


    /**
     * Removes a cooldown.
     *
     * @param uuid                   The UUID.
     * @param key                    The cooldown identifier.
     * @param alsoRemoveFromDatabase Whether to attempt to remove this cooldown from the database as well if it's persistent.
     *                               Set to false to remove from memory only.
     * @return True if a cooldown was found and removed from memory, false otherwise.
     */
    public boolean removeCooldown(@NotNull UUID uuid, @NotNull Key key, boolean alsoRemoveFromDatabase) {
        Objects.requireNonNull(uuid, "UUID must not be null");
        Objects.requireNonNull(key, "Cooldown key must not be null");

        CompositeKey compositeKey = new CompositeKey(key, uuid);
        Cooldown removed = cooldowns.remove(compositeKey);

        if ((removed != null && removed.persistent() && alsoRemoveFromDatabase) || (removed == null && alsoRemoveFromDatabase))
            database.deleteCooldown(uuid, key);

        return removed != null;
    }

    /**
     * Removes a cooldown for a player.
     *
     * @param player                 The player.
     * @param key                    The cooldown identifier.
     * @param alsoRemoveFromDatabase Whether to attempt to remove this cooldown from the database as well if it's persistent.
     *                               Set to false to remove from memory only.
     * @return True if a cooldown was found and removed from memory, false otherwise.
     */
    public boolean removeCooldown(@NotNull Player player, @NotNull Key key, boolean alsoRemoveFromDatabase) {
        Objects.requireNonNull(player, "Player must not be null");
        return removeCooldown(player.getUniqueId(), key, alsoRemoveFromDatabase);
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
    public Cooldown getCooldown(@Nullable UUID uuid, @Nullable Key key) {
        if (uuid == null || key == null)
            return null;

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
    public Cooldown getCooldown(@Nullable Player player, @Nullable Key key) {
        if (player == null)
            return null;

        return getCooldown(player.getUniqueId(), key);
    }

    /**
     * Checks if a UUID is in cooldown.
     *
     * @param uuid The UUID.
     * @param key  The cooldown identifier.
     * @return Whether the UUID is in cooldown.
     */
    public boolean inCooldown(@Nullable UUID uuid, @Nullable Key key) {
        return getCooldown(uuid, key) != null;
    }

    /**
     * Checks if a player is in cooldown.
     *
     * @param player The player.
     * @param key    The cooldown identifier.
     * @return Whether the player is in cooldown.
     */
    public boolean inCooldown(@Nullable Player player, @Nullable Key key) {
        if (player == null)
            return false;

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
    public Duration remainingTime(@Nullable UUID uuid, @Nullable Key key) {
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
    public Duration remainingTime(@Nullable Player player, @Nullable Key key) {
        if (player == null)
            return Duration.ZERO;

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
     * @return The number of cooldowns removed from memory.
     */
    public void removeAllCooldowns(@NotNull UUID uuid) {
        removeAllCooldowns(uuid, true);
    }

    /**
     * Removes all cooldowns associated with a player from memory.
     * Also attempts to remove associated persistent cooldowns from the database asynchronously.
     *
     * @param player The player.
     * @return The number of cooldowns removed from memory.
     */
    public void removeAllCooldowns(@NotNull Player player) {
        Objects.requireNonNull(player, "Player must not be null");
        removeAllCooldowns(player.getUniqueId(), true);
    }

    /**
     * Removes all cooldowns associated with a UUID from memory.
     *
     * @param uuid                   The UUID.
     * @param alsoRemoveFromDatabase Whether to attempt to remove persistent cooldowns for this UUID from the database as well.
     *                               Set to false to remove from memory only.
     * @return The number of cooldowns removed from memory.
     */
    public void removeAllCooldowns(@NotNull UUID uuid, boolean alsoRemoveFromDatabase) {
        Objects.requireNonNull(uuid, "UUID must not be null");
        Iterator<CompositeKey> iterator = cooldowns.keySet().iterator();
        CompositeKey key;

        while (iterator.hasNext()) {
            key = iterator.next();
            if (!key.uuid().equals(uuid))
                continue;

            Cooldown removed = cooldowns.remove(key);
            if (removed != null && removed.persistent() && alsoRemoveFromDatabase)
                iterator.remove();
        }

        if (alsoRemoveFromDatabase)
            database.deleteAllCooldowns(uuid);
    }

    /**
     * Removes all cooldowns associated with a player from memory.
     *
     * @param player                 The player.
     * @param alsoRemoveFromDatabase Whether to attempt to remove persistent cooldowns for this player from the database as well.
     *                               Set to false to remove from memory only.
     * @return The number of cooldowns removed from memory.
     */
    public void removeAllCooldowns(@NotNull Player player, boolean alsoRemoveFromDatabase) {
        Objects.requireNonNull(player, "Player must not be null");
        removeAllCooldowns(player.getUniqueId(), alsoRemoveFromDatabase);
    }


    /**
     * Removes all cooldowns associated with a specific namespace from memory.
     * Also attempts to remove associated persistent cooldowns from the database asynchronously.
     *
     * @param namespace The namespace.
     * @return The number of cooldowns removed from memory.
     */
    public int removeAllCooldownsByNamespace(@NotNull String namespace) {
        return removeAllCooldownsByNamespace(namespace, true);
    }

    /**
     * Removes all cooldowns associated with a specific namespace from memory.
     *
     * @param namespace              The namespace.
     * @param alsoRemoveFromDatabase Whether to attempt to remove persistent cooldowns with this namespace from the database as well.
     *                               Set to false to remove from memory only.
     * @return The number of cooldowns removed from memory.
     */
    public int removeAllCooldownsByNamespace(@NotNull String namespace, boolean alsoRemoveFromDatabase) {
        Objects.requireNonNull(namespace, "Namespace must not be null");

        int count = 0;
        List<CompositeKey> keysToRemove = new java.util.ArrayList<>();

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
            if (!removed.persistent() || !alsoRemoveFromDatabase)
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