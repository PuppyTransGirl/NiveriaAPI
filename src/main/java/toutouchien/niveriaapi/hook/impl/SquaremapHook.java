package toutouchien.niveriaapi.hook.impl;

import com.google.common.base.Preconditions;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;
import xyz.jpenilla.squaremap.api.Squaremap;
import xyz.jpenilla.squaremap.api.SquaremapProvider;

import java.util.UUID;

public class SquaremapHook extends Hook {
    private boolean enabled;
    private Squaremap squaremap;

    public SquaremapHook(@NotNull NiveriaAPI plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        this.squaremap = SquaremapProvider.get();
        this.plugin.getSLF4JLogger().info("Hooked into Squaremap");
        this.enabled = true;
    }

    @Override
    public void onDisable() {
        this.enabled = false;
        this.plugin.getSLF4JLogger().info("Unhooked from Squaremap");
    }

    public void setHidden(@NotNull UUID uuid, boolean hidden, boolean persistent) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");

        if (!this.enabled)
            return;

        this.squaremap.playerManager().hidden(uuid, hidden, persistent);
    }

    public void setHidden(@NotNull Player player, boolean hidden, boolean persistent) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.setHidden(player.getUniqueId(), hidden, persistent);
    }
}
