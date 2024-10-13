package toutouchien.niveriaapi.menu.infos;

import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.UUID;

public class MenuInfos {
	private final Player player;
	private Material material;

	public MenuInfos(Player player) {
		this.player = player;
	}

	public MenuInfos(Player player, Material material) {
		this.player = player;
		this.material = material;
	}

	public Player player() {
		return player;
	}

	public UUID uuid() {
		return player.getUniqueId();
	}

	public void material(Material material) {
		this.material = material;
	}

	public Material material() {
		return material;
	}
}
