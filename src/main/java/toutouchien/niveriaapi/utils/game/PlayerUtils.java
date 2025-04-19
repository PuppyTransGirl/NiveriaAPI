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
		Player player = Bukkit.getPlayer(name);
		return player == null ? null : isVanished(player) ? null : player;
	}

	public static Player nonVanishedPlayerExact(@NotNull String name) {
		Player playerExact = Bukkit.getPlayerExact(name);
		return playerExact == null ? null : isVanished(playerExact) ? null : playerExact;
	}

	public static Player nonVanishedPlayer(@NotNull UUID uuid) {
		Player player = Bukkit.getPlayer(uuid);
		return player == null ? null : isVanished(player) ? null : player;
	}

	public static boolean isValidPlayerName(@NotNull String playerName) {
		return playerName.length() <= 16 && playerName.chars().filter(i -> i <= 32 || i >= 127).findAny().isEmpty(); // Minecraft code
	}
}
