package toutouchien.niveriaapi.menu.infos;

import org.bukkit.entity.Player;

import java.util.UUID;

public class MenuInfos {
	private final Player player;
	private Player adminPlayer;

	public MenuInfos(Player player) {
		this.player = player;
	}

	public MenuInfos(Player player, Player adminPlayer) {
		this.player = player;
		this.adminPlayer = adminPlayer;
	}

	public Player player() {
		return player;
	}

	public UUID uuid() {
		return player.getUniqueId();
	}

	public void adminPlayer(Player adminPlayer) {
		this.adminPlayer = adminPlayer;
	}

	public Player adminPlayer() {
		return adminPlayer;
	}
}
