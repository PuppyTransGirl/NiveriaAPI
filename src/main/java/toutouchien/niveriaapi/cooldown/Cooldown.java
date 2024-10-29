package toutouchien.niveriaapi.cooldown;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class Cooldown {
	private final UUID uuid;
	private final long expirationTime;

	public Cooldown(@NotNull UUID uuid, long cooldown) {
		Objects.requireNonNull(uuid, "UUID must not be null");

		this.uuid = uuid;
		this.expirationTime = System.currentTimeMillis() + cooldown;
	}

	public Cooldown(@NotNull Player player, long cooldown) {
		Objects.requireNonNull(player, "Player must not be null");

		this.uuid = player.getUniqueId();
		this.expirationTime = System.currentTimeMillis() + cooldown;
	}

	@NotNull
	public UUID uuid() {
		return uuid;
	}

	@Nullable
	public Player player() {
		return Bukkit.getPlayer(uuid);
	}

	public long expirationTime() {
		return expirationTime;
	}
}
