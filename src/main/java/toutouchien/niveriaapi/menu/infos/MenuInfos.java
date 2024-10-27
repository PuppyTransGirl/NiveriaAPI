package toutouchien.niveriaapi.menu.infos;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MenuInfos {
	private final Player player;
	private OfflinePlayer playerForInfos;

	public MenuInfos(Player player) {
		this.player = player;
	}

	public MenuInfos(Player player, OfflinePlayer playerForInfos) {
		this.player = player;
		this.playerForInfos = playerForInfos;
	}

	public Player player() {
		return player;
	}

	public UUID uuid() {
		return player.getUniqueId();
	}

	public void playerForInfos(OfflinePlayer playerForInfos) {
		this.playerForInfos = playerForInfos;
	}

	public OfflinePlayer playerForInfos() {
		return playerForInfos;
	}
}
