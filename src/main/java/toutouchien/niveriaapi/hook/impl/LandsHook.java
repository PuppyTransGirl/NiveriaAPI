package toutouchien.niveriaapi.hook.impl;

import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.player.LandPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;
import toutouchien.niveriaapi.hook.impl.lands.NaturalFlags;
import toutouchien.niveriaapi.hook.impl.lands.PlayerFlags;
import toutouchien.niveriaapi.hook.impl.lands.RoleFlags;

public class LandsHook extends Hook {
	private boolean enabled;
	private LandsIntegration lands;

	public LandsHook(NiveriaAPI plugin) {
		super(plugin);
	}

	@Override
	public void onEnable() {
		this.lands = LandsIntegration.of(plugin);

		this.plugin.getSLF4JLogger().info("Hooked into Lands");
		this.enabled = true;
	}

	@Override
	public void onDisable() {
		this.enabled = false;
		this.plugin.getSLF4JLogger().info("Unhooked from Lands");
	}

	public boolean hasRoleFlag(Player player, Location location, RoleFlags roleFlag) {
		Area area = this.lands.getArea(location);
		if (area == null)
			return true;

		return area.hasRoleFlag(player.getUniqueId(), roleFlag.flag());
	}

	public boolean hasNaturalFlag(Location location, NaturalFlags naturalFlag) {
		Area area = this.lands.getArea(location);
		if (area == null)
			return true;

		return area.hasNaturalFlag(naturalFlag.flag());
	}

	public boolean hasPlayerFlag(Player player, PlayerFlags playerFlag) {
		LandPlayer landPlayer = this.lands.getLandPlayer(player.getUniqueId());
		if (landPlayer == null)
			return true;

		return landPlayer.hasFlag(playerFlag.flag());
	}
}
