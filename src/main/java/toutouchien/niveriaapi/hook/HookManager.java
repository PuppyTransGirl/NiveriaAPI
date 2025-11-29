package toutouchien.niveriaapi.hook;

import com.google.common.base.Preconditions;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class HookManager {
    private final Map<String, Hook> hooks;
    private final NiveriaAPI plugin;

    public HookManager(@NotNull NiveriaAPI plugin) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        this.plugin = plugin;
        this.hooks = new HashMap<>();
        Arrays.stream(HookType.values()).forEach(this::registerHook);
    }

    public void registerHook(@NotNull HookType type) {
        Preconditions.checkNotNull(type, "type cannot be null");

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
    @Nullable
    public <T extends Hook> T hook(@NotNull HookType hookType) {
        Preconditions.checkNotNull(hookType, "hookType cannot be null");

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
