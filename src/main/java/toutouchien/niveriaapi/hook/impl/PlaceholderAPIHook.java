package toutouchien.niveriaapi.hook.impl;

import com.google.common.base.Preconditions;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;

public class PlaceholderAPIHook extends Hook {
    private boolean enabled;

    public PlaceholderAPIHook(@NotNull NiveriaAPI plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        this.plugin.getSLF4JLogger().info("Hooked into PlaceholderAPI");
        this.enabled = true;
    }

    @Override
    public void onDisable() {
        this.enabled = false;
        this.plugin.getSLF4JLogger().info("Unhooked from PlaceholderAPI");
    }

    public String replacePlaceholders(@NotNull String text) {
        Preconditions.checkNotNull(text, "text cannot be null");

        if (!this.enabled)
            return text;

        return PlaceholderAPI.setPlaceholders(null, text);
    }

    public String replacePlaceholders(Player player, String text) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(text, "text cannot be null");

        if (!this.enabled)
            return text;

        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
