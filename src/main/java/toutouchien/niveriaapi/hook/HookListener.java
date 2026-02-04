package toutouchien.niveriaapi.hook;

import com.google.common.base.Preconditions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.jspecify.annotations.NullMarked;

/**
 * Listener for player join and leave events to notify the HookManager.
 */
@NullMarked
public class HookListener implements Listener {
    private final HookManager hookManager;

    /**
     * Constructs a HookListener with the specified HookManager.
     *
     * @param hookManager The HookManager instance.
     */
    public HookListener(HookManager hookManager) {
        Preconditions.checkNotNull(hookManager, "hookManager cannot be null");

        this.hookManager = hookManager;
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        this.hookManager.onEnable();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.hookManager.onJoin(event);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        this.hookManager.onLeave(event);
    }
}
