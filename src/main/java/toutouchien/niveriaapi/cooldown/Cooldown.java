package toutouchien.niveriaapi.cooldown;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a cooldown with associated player/UUID and expiration time.
 * This class is immutable and thread-safe.
 */
@NullMarked
public class Cooldown {
    private final UUID uuid;
    private final Key key;
    private final long expirationTime;
    private final boolean persistent;

    /**
     * Creates a new Cooldown.
     *
     * @param uuid           The UUID associated with this cooldown
     * @param key            The key associated with this cooldown
     * @param expirationTime The expiration time in milliseconds since epoch
     * @param persistent     Whether this cooldown is persistent
     */
    public Cooldown(UUID uuid, Key key, long expirationTime, boolean persistent) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        this.uuid = uuid;
        this.key = key;
        this.expirationTime = expirationTime;
        this.persistent = persistent;
    }

    /**
     * Creates a new Cooldown.
     *
     * @param player         The player associated with this cooldown
     * @param key            The key associated with this cooldown
     * @param expirationTime The expiration time in milliseconds since epoch
     * @param persistent     Whether this cooldown is persistent
     */
    public Cooldown(Player player, Key key, long expirationTime, boolean persistent) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        this.uuid = player.getUniqueId();
        this.key = key;
        this.expirationTime = expirationTime;
        this.persistent = persistent;
    }

    /**
     * Creates a new Cooldown.
     *
     * @param uuid       The UUID associated with this cooldown
     * @param key        The key associated with this cooldown
     * @param duration   The duration of the cooldown
     * @param persistent Whether this cooldown is persistent
     */
    public Cooldown(UUID uuid, Key key, Duration duration, boolean persistent) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(duration, "duration cannot be null");

        this.uuid = uuid;
        this.key = key;
        this.expirationTime = System.currentTimeMillis() + duration.toMillis();
        this.persistent = persistent;
    }

    /**
     * Creates a new Cooldown.
     *
     * @param player     The player associated with this cooldown
     * @param key        The key associated with this cooldown
     * @param duration   The duration of the cooldown
     * @param persistent Whether this cooldown is persistent
     */
    public Cooldown(Player player, Key key, Duration duration, boolean persistent) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(duration, "duration cannot be null");

        this.uuid = player.getUniqueId();
        this.key = key;
        this.expirationTime = System.currentTimeMillis() + duration.toMillis();
        this.persistent = persistent;
    }

    /**
     * Gets the UUID associated with this cooldown.
     *
     * @return The UUID
     */
    public UUID uuid() {
        return uuid;
    }

    /**
     * Gets the player associated with this cooldown, if online.
     *
     * @return The player, or null if offline
     */
    @Nullable
    public Player player() {
        return Bukkit.getPlayer(uuid);
    }

    /**
     * Gets the key associated with this cooldown.
     *
     * @return The key
     */
    public Key key() {
        return key;
    }

    /**
     * Gets the expiration time of this cooldown in milliseconds since epoch.
     *
     * @return The expiration time
     */
    public long expirationTime() {
        return expirationTime;
    }

    /**
     * Checks if this cooldown has expired.
     *
     * @return Whether this cooldown has expired
     */
    public boolean expired() {
        return System.currentTimeMillis() >= expirationTime;
    }

    /**
     * Gets the remaining time of this cooldown.
     *
     * @return The remaining time, or Duration.ZERO if expired
     */
    public Duration remainingTime() {
        long remaining = expirationTime - System.currentTimeMillis();
        return remaining > 0 ? Duration.ofMillis(remaining) : Duration.ZERO;
    }

    /**
     * Gets the total duration of this cooldown.
     *
     * @return The total duration
     */
    public Duration totalDuration() {
        return Duration.ofMillis(expirationTime - (System.currentTimeMillis() - remainingTime().toMillis()));
    }

    /**
     * Gets the percentage of time remaining for this cooldown.
     *
     * @return The percentage remaining (0-1)
     */
    public double percentageRemaining() {
        Duration remaining = remainingTime();
        Duration total = totalDuration();

        if (total.isZero())
            return 0.0;

        return (double) remaining.toMillis() / total.toMillis();
    }

    /**
     * Checks if this cooldown is persistent.
     *
     * @return Whether this cooldown is persistent
     */
    public boolean persistent() {
        return persistent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null || getClass() != o.getClass())
            return false;

        Cooldown cooldown = (Cooldown) o;
        return expirationTime == cooldown.expirationTime && Objects.equals(uuid, cooldown.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, expirationTime);
    }

    @Override
    public String toString() {
        return "Cooldown{" +
                "uuid=" + uuid +
                ", expirationTime=" + expirationTime +
                ", remaining=" + remainingTime() +
                ", expired=" + expired() +
                '}';
    }
}