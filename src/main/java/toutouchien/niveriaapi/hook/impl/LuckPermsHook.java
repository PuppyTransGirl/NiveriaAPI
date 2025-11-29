package toutouchien.niveriaapi.hook.impl;

import net.luckperms.api.LuckPerms;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;
import toutouchien.niveriaapi.hook.impl.luckperms.LuckPermsMetaCache;
import toutouchien.niveriaapi.hook.impl.luckperms.MetaCache;
import toutouchien.niveriaapi.hook.impl.luckperms.SimpleMetaCache;

public class LuckPermsHook extends Hook {
    private boolean enabled;
    private MetaCache metaCache;

    public LuckPermsHook(@NotNull NiveriaAPI plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider == null) {
            this.plugin.getSLF4JLogger().warn("LuckPerms not found, disabling hook");
            this.metaCache = new SimpleMetaCache();
            return;
        }

        LuckPerms luckPerms = provider.getProvider();
        this.metaCache = new LuckPermsMetaCache(this.plugin, luckPerms);

        this.plugin.getSLF4JLogger().info("Hooked into LuckPerms");
        this.enabled = true;
    }

    @Override
    public void onDisable() {
        this.enabled = false;
        this.plugin.getSLF4JLogger().info("Unhooked from LuckPerms");
    }

    @NotNull
    public MetaCache metaCache() {
        return metaCache;
    }
}
