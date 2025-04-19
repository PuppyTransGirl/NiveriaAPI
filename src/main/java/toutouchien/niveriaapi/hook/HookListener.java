package toutouchien.niveriaapi.hook;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HookListener implements Listener {
	private final HookManager hookManager;

	public HookListener(HookManager hookManager) {
		this.hookManager = hookManager;
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
