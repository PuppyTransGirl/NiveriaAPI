package toutouchien.niveriaapi.hook.impl;

import com.google.common.base.Preconditions;
import de.bluecolored.bluemap.api.BlueMapAPI;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;

import java.util.Optional;
import java.util.UUID;

/**
 * Hook into BlueMap to manage player visibility on the map.
 */
@NullMarked
public class BlueMapHook extends Hook {
    private boolean enabled;
    @Nullable
    private BlueMapAPI blueMap;

    /**
     * Constructs a new BlueMapHook.
     *
     * @param plugin The NiveriaAPI plugin instance.
     */
    public BlueMapHook(NiveriaAPI plugin) {
        super(plugin);
    }


    @Override
    public void onEnable() {
        Optional<BlueMapAPI> optional = BlueMapAPI.getInstance();
        if (optional.isEmpty()) {
            this.plugin.getSLF4JLogger().warn("BlueMap not found, disabling hook");
            return;
        }

        this.blueMap = optional.get();
        this.plugin.getSLF4JLogger().info("Hooked into BlueMap");
        this.enabled = true;
    }

    @Override
    public void onDisable() {
        this.enabled = false;
        this.plugin.getSLF4JLogger().info("Unhooked from BlueMap");
    }

    /**
     * Sets the visibility of a player on BlueMap.
     *
     * @param uuid   The UUID of the player.
     * @param hidden {@code true} to hide the player, {@code false} to show.
     */
    public void setHidden(UUID uuid, boolean hidden) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");

        if (!this.enabled)
            return;

        this.blueMap.getWebApp().setPlayerVisibility(uuid, !hidden);
    }

    /**
     * Sets the visibility of a player on BlueMap.
     *
     * @param player The player.
     * @param hidden {@code true} to hide the player, {@code false} to show.
     */
    public void setHidden(Player player, boolean hidden) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.setHidden(player.getUniqueId(), hidden);
    }
}
