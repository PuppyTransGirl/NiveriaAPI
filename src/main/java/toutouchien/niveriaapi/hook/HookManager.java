package toutouchien.niveriaapi.hook;

import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import toutouchien.niveriaapi.NiveriaAPI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HookManager {
    private final Map<String, Hook> hooks;
    private final NiveriaAPI plugin;

    public HookManager(NiveriaAPI plugin) {
        this.plugin = plugin;
        this.hooks = new HashMap<>();
        Arrays.stream(HookType.values()).forEach(this::registerHook);
    }

    public void registerHook(HookType type) {
        try {
            Plugin hookPlugin = this.plugin.getServer().getPluginManager().getPlugin(type.pluginName());
            if (hookPlugin == null) {
                this.plugin.getSLF4JLogger().warn("{} not found, disabling hook", type.pluginName());
                return;
            }

            Hook hook = type.hookClass().getConstructor(NiveriaAPI.class).newInstance(this.plugin);
            this.hooks.put(type.name(), hook);
        } catch (Exception e) {
            this.plugin.getSLF4JLogger().warn("Could not instantiate hook '{}' [{}] for plugin '{}'. Hook disabled.",
                    type.name(), type.hookClass().getSimpleName(), type.pluginName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends Hook> T hook(HookType hookType) {
        Hook hook = this.hooks.get(hookType.name());
        if (hook == null)
            return null;

        return (T) hook;
    }

    public void onEnable() {
        this.hooks.values().forEach(Hook::onEnable);
    }

    public void onDisable() {
        this.hooks.values().forEach(Hook::onDisable);
    }

    public void onJoin(PlayerJoinEvent event) {
        this.hooks.values().forEach(hook -> hook.onJoin(event));
    }

    public void onLeave(PlayerQuitEvent event) {
        this.hooks.values().forEach(hook -> hook.onLeave(event));
    }
}
