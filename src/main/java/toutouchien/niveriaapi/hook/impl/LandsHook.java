package toutouchien.niveriaapi.hook.impl;

import com.google.common.base.Preconditions;
import me.angeschossen.lands.api.LandsIntegration;
import me.angeschossen.lands.api.land.Area;
import me.angeschossen.lands.api.player.LandPlayer;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
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

    public boolean hasRoleFlag(@NotNull Player player, @NotNull Location location, @NotNull RoleFlags roleFlag) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(roleFlag, "roleFlag cannot be null");

        Area area = this.lands.getArea(location);
        if (area == null)
            return true;

        return area.hasRoleFlag(player.getUniqueId(), roleFlag.flag());
    }

    public boolean hasNaturalFlag(@NotNull Location location, @NotNull NaturalFlags naturalFlag) {
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(naturalFlag, "naturalFlag cannot be null");

        Area area = this.lands.getArea(location);
        if (area == null)
            return true;

        return area.hasNaturalFlag(naturalFlag.flag());
    }

    public boolean hasPlayerFlag(@NotNull Player player, @NotNull PlayerFlags playerFlag) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(playerFlag, "playerFlag cannot be null");

        LandPlayer landPlayer = this.lands.getLandPlayer(player.getUniqueId());
        if (landPlayer == null)
            return true;

        return landPlayer.hasFlag(playerFlag.flag());
    }
}
