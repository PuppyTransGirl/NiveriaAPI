package toutouchien.niveriaapi.hook;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import toutouchien.niveriaapi.NiveriaAPI;

public abstract class Hook {
	protected final NiveriaAPI plugin;

	public Hook(NiveriaAPI plugin) {
		this.plugin = plugin;
	}

	public void onEnable() {

	}

	public void onDisable() {

	}

	public void onJoin(PlayerJoinEvent event) {

	}

	public void onLeave(PlayerQuitEvent event) {

	}
}
