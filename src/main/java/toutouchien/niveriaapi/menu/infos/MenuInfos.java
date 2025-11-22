package toutouchien.niveriaapi.menu.infos;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class MenuInfos {
    private final Player player;
    private OfflinePlayer playerForInfos;

    public MenuInfos(@NotNull Player player) {
        this.player = player;
    }

    public MenuInfos(@NotNull Player player, OfflinePlayer playerForInfos) {
        this.player = player;
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

    public void playerForInfos(OfflinePlayer playerForInfos) {
        this.playerForInfos = playerForInfos;
    }

    public OfflinePlayer playerForInfos() {
        return playerForInfos;
    }
}
