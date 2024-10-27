package toutouchien.niveriaapi.menu.infos;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MenuInfos {
	private final OfflinePlayer player;
	private Player adminPlayer;

	public MenuInfos(OfflinePlayer player) {
		this.player = player;
	}

	public MenuInfos(OfflinePlayer player, Player adminPlayer) {
		this.player = player;
		this.adminPlayer = adminPlayer;
	}

	public OfflinePlayer player() {
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
