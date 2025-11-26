package toutouchien.niveriaapi.hook.impl;

import com.google.common.base.Preconditions;
import de.bluecolored.bluemap.api.BlueMapAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;

import java.util.Optional;
import java.util.UUID;

public class BlueMapHook extends Hook {
    private boolean enabled;
    private BlueMapAPI blueMap;

    public BlueMapHook(@NotNull NiveriaAPI plugin) {
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

    public void setHidden(@NotNull UUID uuid, boolean hidden) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        if (!this.enabled)
            return;

        this.blueMap.getWebApp().setPlayerVisibility(uuid, !hidden);
    }

    public void setHidden(@NotNull Player player, boolean hidden) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.setHidden(player.getUniqueId(), hidden);
    }
}
