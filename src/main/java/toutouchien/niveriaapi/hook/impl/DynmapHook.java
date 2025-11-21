package toutouchien.niveriaapi.hook.impl;

import org.bukkit.entity.Player;
import org.dynmap.DynmapAPI;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;

public class DynmapHook extends Hook {
    private boolean enabled;
    private DynmapAPI dynmap;

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

    public void hidden(Player player, boolean hidden) {
        if (!this.enabled)
            return;

        this.dynmap.setPlayerVisiblity(player, !hidden);
    }
}
