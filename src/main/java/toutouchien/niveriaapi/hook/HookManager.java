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

/**
 * Central manager for external plugin hooks used by {@link NiveriaAPI}.
 * <p>
 * On construction, it iterates over all {@link HookType} values and attempts
 * to register a hook implementation for each one whose target plugin is
 * currently loaded.
 * <p>
 * The manager also forwards lifecycle and player events to all active hooks.
 */
public class HookManager {
    private final Map<String, Hook> hooks;
    private final NiveriaAPI plugin;

    /**
     * Creates a new {@code HookManager} for the given plugin and attempts to
     * auto-register all hooks defined in {@link HookType}.
     *
     * @param plugin owning {@link NiveriaAPI} instance
     */
    public HookManager(@NotNull NiveriaAPI plugin) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        this.plugin = plugin;
        this.hooks = new HashMap<>();
        Arrays.stream(HookType.values()).forEach(this::registerHook);
    }

    /**
     * Registers (instantiates) a hook for the given {@link HookType} if the
     * corresponding external plugin is present.
     * <p>
     * The method:
     * <ol>
     *     <li>Checks if {@code type.pluginName()} is loaded.</li>
     *     <li>Reflectively instantiates {@code type.hookClass()} with a
     *         single-argument constructor taking {@link NiveriaAPI}.</li>
     *     <li>Stores the hook instance in this manager if successful.</li>
     * </ol>
     * If any step fails, a warning is logged and the hook is not registered.
     *
     * @param type hook type to register
     */
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
            this.plugin.getSLF4JLogger().warn("Could not instantiate hook '{}' [{}] for plugin '{}'. Hook disabled.", type.name(), type.hookClass().getSimpleName(), type.pluginName(), e);
        }
    }

    /**
     * Returns the hook instance for the given {@link HookType}, if present.
     *
     * @param hookType type of hook to retrieve
     * @param <T>      compile-time hook type (extends {@link Hook})
     * @return hook instance or {@code null} if not registered or failed to load
     * @throws ClassCastException if the stored hook cannot be cast to {@code T}
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends Hook> T hook(@NotNull HookType hookType) {
        Preconditions.checkNotNull(hookType, "hookType cannot be null");

        Hook hook = this.hooks.get(hookType.name());
        if (hook == null) {
            return null;
        }

        return (T) hook;
    }

    /**
     * Invokes {@link Hook#onEnable()} on all registered hooks.
     * <p>
     * Should be called from the plugin's {@code onEnable()}.
     */
    public void onEnable() {
        this.hooks.values().forEach(Hook::onEnable);
    }

    /**
     * Invokes {@link Hook#onDisable()} on all registered hooks.
     * <p>
     * Should be called from the plugin's {@code onDisable()}.
     */
    public void onDisable() {
        this.hooks.values().forEach(Hook::onDisable);
    }

    /**
     * Forwards a {@link PlayerJoinEvent} to all registered hooks by calling
     * {@link Hook#onJoin(PlayerJoinEvent)}.
     *
     * @param event player join event
     */
    public void onJoin(PlayerJoinEvent event) {
        this.hooks.values().forEach(hook -> hook.onJoin(event));
    }

    /**
     * Forwards a {@link PlayerQuitEvent} to all registered hooks by calling
     * {@link Hook#onLeave(PlayerQuitEvent)}.
     *
     * @param event player quit event
     */
    public void onLeave(PlayerQuitEvent event) {
        this.hooks.values().forEach(hook -> hook.onLeave(event));
    }
}