package toutouchien.niveriaapi.hook.impl;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;

/**
 * Hook for integrating with the Dynmap plugin to manage player visibility on the map.
 */
@NullMarked
public class DynmapHook extends Hook {
    private boolean enabled;
    @Nullable
    private DynmapAPI dynmap;

    /**
     * Constructs a DynmapHook with the specified plugin instance.
     *
     * @param plugin The NiveriaAPI plugin instance.
     */
    public DynmapHook(NiveriaAPI plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        this.dynmap = (DynmapAPI) this.plugin.getServer().getPluginManager().getPlugin("dynmap");
        this.plugin.getSLF4JLogger().info("Hooked into Dynmap");
        this.enabled = true;
    }

    @Override
    public void onDisable() {
        this.enabled = false;
        this.plugin.getSLF4JLogger().info("Unhooked from Dynmap");
    }

    /**
     * Sets the visibility of a player on the Dynmap.
     *
     * @param player The player whose visibility is to be set.
     * @param hidden True to hide the player, false to show them.
     */
    public void hidden(Player player, boolean hidden) {
        Preconditions.checkNotNull(player, "player cannot be null");

        if (!this.enabled)
            return;

        this.dynmap.setPlayerVisiblity(player, !hidden);
    }
}
