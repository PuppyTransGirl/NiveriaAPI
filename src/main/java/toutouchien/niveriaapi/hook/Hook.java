package toutouchien.niveriaapi.hook;

import com.google.common.base.Preconditions;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jspecify.annotations.NullMarked;
import toutouchien.niveriaapi.NiveriaAPI;

/**
 * Abstract base class for hooks into external plugins or systems.
 */
@NullMarked
public abstract class Hook {
    protected final NiveriaAPI plugin;

    /**
     * Constructs a Hook with the specified plugin instance.
     *
     * @param plugin The NiveriaAPI plugin instance.
     */
    protected Hook(NiveriaAPI plugin) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        this.plugin = plugin;
    }

    /**
     * Called when the hook is enabled.
     */
    public void onEnable() {

    }

    /**
     * Called when the hook is disabled.
     */
    public void onDisable() {

    }

    /**
     * Called when a player joins the server.
     *
     * @param event The {@link PlayerJoinEvent}.
     */
    public void onJoin(PlayerJoinEvent event) {

    }

    /**
     * Called when a player leaves the server.
     *
     * @param event The {@link PlayerQuitEvent}.
     */
    public void onLeave(PlayerQuitEvent event) {

    }
}
