package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import net.kyori.adventure.util.TriState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PlayerUtils {
    private PlayerUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isVanished(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        List<MetadataValue> metadata = player.getMetadata("vanished");
        for (MetadataValue metadatum : metadata) {
            Object value = metadatum.value();
            if ((value instanceof Boolean bool && bool)
                    || (value instanceof TriState triState && triState == TriState.TRUE))
                return true;
        }

        return false;
    }

    @NotNull
    public static Collection<? extends Player> nonVanishedPlayers() {
        Set<Player> list = new HashSet<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isVanished(player))
                list.add(player);
        }

        return list;
    }

    @Nullable
    public static Player nonVanishedPlayer(@NotNull String name) {
        Preconditions.checkNotNull(name, "name cannot be null");

        Player player = Bukkit.getPlayer(name);
        if (player == null || isVanished(player))
            return null;

        return player;
    }

    @Nullable
    public static Player nonVanishedPlayerExact(@NotNull String name) {
        Preconditions.checkNotNull(name, "name cannot be null");

        Player player = Bukkit.getPlayerExact(name);
        if (player == null || isVanished(player))
            return null;

        return player;
    }

    @Nullable
    public static Player nonVanishedPlayer(@NotNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || isVanished(player))
            return null;

        return player;
    }

    public static boolean isValidPlayerName(@NotNull String name) {
        Preconditions.checkNotNull(name, "name cannot be null");

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
