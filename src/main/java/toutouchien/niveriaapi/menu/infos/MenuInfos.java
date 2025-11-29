package toutouchien.niveriaapi.menu.infos;

import com.google.common.base.Preconditions;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class MenuInfos {
    private final Player player;
    private OfflinePlayer playerForInfos;

    public MenuInfos(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.player = player;
    }

    public MenuInfos(@NotNull Player player, @Nullable OfflinePlayer playerForInfos) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.player = player;
        this.playerForInfos = playerForInfos;
    }

    public void playerForInfos(@Nullable OfflinePlayer playerForInfos) {
        this.playerForInfos = playerForInfos;
    }

    @NotNull
    public Player player() {
        return player;
    }

    @NotNull
    public UUID uuid() {
        return player.getUniqueId();
    }

    @Nullable
    public OfflinePlayer playerForInfos() {
        return playerForInfos;
    }
}
