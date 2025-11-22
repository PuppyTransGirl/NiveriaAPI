package toutouchien.niveriaapi.hook.impl;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.World;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;

public class WorldGuardHook extends Hook {
    private boolean enabled;
    private RegionContainer regionContainer;
    private FlagRegistry flagRegistry;

    public WorldGuardHook(NiveriaAPI plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        WorldGuard worldGuard = WorldGuard.getInstance();
        this.regionContainer = worldGuard.getPlatform().getRegionContainer();
        this.flagRegistry = worldGuard.getFlagRegistry();

        this.plugin.getSLF4JLogger().info("Hooked into WorldGuard");
        this.enabled = true;
    }

    @Override
    public void onDisable() {
        this.enabled = false;
        this.plugin.getSLF4JLogger().info("Unhooked from WorldGuard");
    }

    public void registerCustomFlag(String flagName, boolean defaultValue) {
        if (!this.enabled)
            return;

        if (flagRegistry.get(flagName) != null) {
            this.plugin.getSLF4JLogger().warn("Flag '{}' already exists, skipping registration", flagName);
            return;
        }

        StateFlag flag = new StateFlag(flagName, defaultValue);
        this.flagRegistry.register(flag);
    }

    public boolean flagValue(World world, String regionName, String flagName) {
        if (!this.enabled)
            return false;

        Flag<?> flag = this.flagRegistry.get(flagName);
        if (flag == null)
            throw new IllegalArgumentException("Flag " + flagName + " not found");

        if (!(flag instanceof StateFlag))
            throw new IllegalArgumentException("Flag " + flagName + " is not a StateFlag");

        RegionManager regions = this.regionContainer.get(BukkitAdapter.adapt(world));
        if (regions == null)
            throw new IllegalArgumentException("World " + world.getName() + " not found");

        ProtectedRegion region = regions.getRegion(regionName);
        if (region == null)
            throw new IllegalArgumentException("Region " + regionName + " not found");

        return region.getFlag(flag) == StateFlag.State.ALLOW;
    }
}
