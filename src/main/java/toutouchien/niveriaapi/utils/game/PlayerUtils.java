package toutouchien.niveriaapi.utils.game;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class PlayerUtils {
	private PlayerUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isVanished(@NotNull Player player) {
        if (player == null)
            throw new IllegalArgumentException("player cannot be null");

		List<MetadataValue> metadata = player.getMetadata("vanished");
		return metadata.stream().anyMatch(MetadataValue::asBoolean);
	}

	public static Collection<? extends Player> nonVanishedPlayers() {
		return Bukkit.getOnlinePlayers().stream()
				.filter(player -> {
					List<MetadataValue> metadata = player.getMetadata("vanished");
					return metadata.stream().anyMatch(value -> !value.asBoolean());
				})
				.toList();
	}

	public static Player nonVanishedPlayer(@NotNull String name) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");

		Player player = Bukkit.getPlayer(name);
		return player == null ? null : isVanished(player) ? null : player;
	}

	public static Player nonVanishedPlayerExact(@NotNull String name) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");

		Player playerExact = Bukkit.getPlayerExact(name);
		return playerExact == null ? null : isVanished(playerExact) ? null : playerExact;
	}

	public static Player nonVanishedPlayer(@NotNull UUID uuid) {
        if (uuid == null)
            throw new IllegalArgumentException("uuid cannot be null");

		Player player = Bukkit.getPlayer(uuid);
		return player == null ? null : isVanished(player) ? null : player;
	}

	public static boolean isValidPlayerName(@NotNull String name) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");

		return name.length() >= 3 && name.length() <= 16 && name.chars().filter(i -> i <= 32 || i >= 127).findAny().isEmpty(); // Minecraft code
	}
}
