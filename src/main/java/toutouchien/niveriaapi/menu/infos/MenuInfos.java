package toutouchien.niveriaapi.menu.infos;

import org.bukkit.entity.Player;

import java.util.UUID;

public class MenuInfos {
	private final Player player;

	public MenuInfos(Player player) {
		this.player = player;
	}

	public Player player() {
		return player;
	}

	public UUID uuid() {
		return player.getUniqueId();
	}
}
