package toutouchien.niveriaapi.hook;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.hook.impl.*;

public enum HookType {
    BlueMapHook(BlueMapHook.class, "BlueMap"),
    DynmapHook(DynmapHook.class, "dynmap"),
    ItemsAdderHook(ItemsAdderHook.class, "ItemsAdder"),
    LandsHook(LandsHook.class, "Lands"),
    LuckpermsHook(LuckPermsHook.class, "Luckperms"),
    PlaceholderAPIHook(PlaceholderAPIHook.class, "PlaceholderAPI"),
    SquaremapHook(SquaremapHook.class, "squaremap"),
    WorldGuardHook(WorldGuardHook.class, "WorldGuard");

    private final Class<? extends Hook> hookClazz;
    private final String pluginName;

    HookType(@NotNull Class<? extends Hook> hookClazz, @NotNull String pluginName) {
        Preconditions.checkNotNull(hookClazz, "hookClazz cannot be null");
        Preconditions.checkNotNull(pluginName, "pluginName cannot be null");

        this.hookClazz = hookClazz;
        this.pluginName = pluginName;
    }

    @NotNull
    public Class<? extends Hook> hookClass() {
        return hookClazz;
    }

    @NotNull
    public String pluginName() {
        return pluginName;
    }
}