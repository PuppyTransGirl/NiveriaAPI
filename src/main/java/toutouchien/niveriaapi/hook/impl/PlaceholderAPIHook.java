package toutouchien.niveriaapi.hook.impl;

import com.google.common.base.Preconditions;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;

/**
 * Hook for integrating with PlaceholderAPI to replace placeholders in strings.
 */
@NullMarked
public class PlaceholderAPIHook extends Hook {
    private boolean enabled;

    public PlaceholderAPIHook(NiveriaAPI plugin) {
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

    /**
     * Replaces placeholders in the given text without a specific player context.
     *
     * @param text The text containing placeholders.
     * @return The text with placeholders replaced.
     */
    public String replacePlaceholders(String text) {
        Preconditions.checkNotNull(text, "text cannot be null");

        if (!this.enabled)
            return text;

        return PlaceholderAPI.setPlaceholders(null, text);
    }

    /**
     * Replaces placeholders in the given text for a specific player.
     *
     * @param player The player context for placeholder replacement.
     * @param text   The text containing placeholders.
     * @return The text with placeholders replaced.
     */
    public String replacePlaceholders(Player player, String text) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(text, "text cannot be null");

        if (!this.enabled)
            return text;

        return PlaceholderAPI.setPlaceholders(player, text);
    }
}
