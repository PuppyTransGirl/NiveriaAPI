package toutouchien.niveriaapi.hook;

import com.google.common.base.Preconditions;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;

public abstract class Hook {
    protected final NiveriaAPI plugin;

    protected Hook(@NotNull NiveriaAPI plugin) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        this.plugin = plugin;
    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public void onJoin(PlayerJoinEvent event) {

    }

    public void onLeave(PlayerQuitEvent event) {

    }
}
