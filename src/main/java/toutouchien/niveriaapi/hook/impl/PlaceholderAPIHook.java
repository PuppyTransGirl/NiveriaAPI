package toutouchien.niveriaapi.hook.impl;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;
import toutouchien.niveriaapi.hook.impl.placeholderapi.CustomPlaceholder;

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

	public String replacePlaceholders(String text) {
		if (!this.enabled)
			return text;

		return PlaceholderAPI.setPlaceholders(null, text);
	}

	public String replacePlaceholders(Player player, String text) {
		if (!this.enabled)
			return text;

		return PlaceholderAPI.setPlaceholders(player, text);
	}

	public void registerCustomPlaceholder(CustomPlaceholder customPlaceholder) {
		customPlaceholder.register();
	}
}
