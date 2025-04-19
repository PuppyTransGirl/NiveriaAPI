package toutouchien.niveriaapi.cooldown;

import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a cooldown with associated player/UUID and expiration time.
 * This class is immutable and thread-safe.
 */
public class Cooldown {
	private final UUID uuid;
	private final Key key;
	private final long expirationTime;
	private final boolean persistent;

	/**
	 * Creates a new cooldown for a UUID with a specified duration in milliseconds.
	 *
	 * @param uuid The UUID
	 * @param expirationTime The exact system time (in milliseconds) when the cooldown expires
	 * @throws NullPointerException if uuid is null
	 */
	public Cooldown(@NotNull UUID uuid, @NotNull Key key, long expirationTime, boolean persistent) {
		Objects.requireNonNull(uuid, "UUID must not be null");

		this.uuid = uuid;
		this.key = key;
		this.expirationTime = expirationTime;
		this.persistent = persistent;
	}

	/**
	 * Creates a new cooldown for a player with a specified duration in milliseconds.
	 *
	 * @param player The player
	 * @param expirationTime The exact system time (in milliseconds) when the cooldown expires
	 * @throws NullPointerException if player is null
	 */
	public Cooldown(@NotNull Player player, @NotNull Key key, long expirationTime, boolean persistent) {
		Objects.requireNonNull(player, "Player must not be null");

		this.uuid = player.getUniqueId();
		this.key = key;
		this.expirationTime = expirationTime;
		this.persistent = persistent;
	}

	/**
	 * Creates a new cooldown for a UUID with a specified duration.
	 *
	 * @param uuid The UUID
	 * @param duration The cooldown duration
	 * @throws NullPointerException if uuid or duration is null
	 */
	public Cooldown(@NotNull UUID uuid, @NotNull Key key, @NotNull Duration duration, boolean persistent) {
		Objects.requireNonNull(uuid, "UUID must not be null");
		Objects.requireNonNull(duration, "Duration must not be null");

		this.uuid = uuid;
		this.key = key;
		this.expirationTime = System.currentTimeMillis() + duration.toMillis();
		this.persistent = persistent;
	}

	/**
	 * Creates a new cooldown for a player with a specified duration.
	 *
	 * @param player The player
	 * @param duration The cooldown duration
	 * @throws NullPointerException if player or duration is null
	 */
	public Cooldown(@NotNull Player player, @NotNull Key key, @NotNull Duration duration, boolean persistent) {
		Objects.requireNonNull(player, "Player must not be null");
		Objects.requireNonNull(duration, "Duration must not be null");

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
	@NotNull
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
	@NotNull
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
	@NotNull
	public Duration remainingTime() {
		long remaining = expirationTime - System.currentTimeMillis();
		return remaining > 0 ? Duration.ofMillis(remaining) : Duration.ZERO;
	}

	/**
	 * Gets the total duration of this cooldown.
	 *
	 * @return The total duration
	 */
	@NotNull
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