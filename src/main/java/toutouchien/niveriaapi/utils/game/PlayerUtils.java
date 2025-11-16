package toutouchien.niveriaapi.utils.game;

import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
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
        for (MetadataValue metadatum : metadata) {
            Object value = metadatum.value();
            if ((value instanceof Boolean bool && bool)
                    || (value instanceof TriState triState && triState == TriState.TRUE))
                return true;
        }

        return false;
	}

	public static Collection<? extends Player> nonVanishedPlayers() {
        List<Player> list = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isVanished(player))
                list.add(player);
        }

        return list;
	}

	public static Player nonVanishedPlayer(@NotNull String name) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");

		Player player = Bukkit.getPlayer(name);
        if (player == null || isVanished(player))
            return null;

        return player;
	}

	public static Player nonVanishedPlayerExact(@NotNull String name) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");

		Player player = Bukkit.getPlayerExact(name);
        if (player == null || isVanished(player))
            return null;

        return player;
	}

	public static Player nonVanishedPlayer(@NotNull UUID uuid) {
        if (uuid == null)
            throw new IllegalArgumentException("uuid cannot be null");

		Player player = Bukkit.getPlayer(uuid);
        if (player == null || isVanished(player))
            return null;

        return player;
	}

    public static boolean isValidPlayerName(@NotNull String name) {
        if (name == null)
            throw new IllegalArgumentException("name cannot be null");

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
