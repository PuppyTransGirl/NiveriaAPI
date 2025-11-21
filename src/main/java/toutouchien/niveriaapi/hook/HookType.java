package toutouchien.niveriaapi.hook;

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

    HookType(Class<? extends Hook> hookClazz, String pluginName) {
        this.hookClazz = hookClazz;
        this.pluginName = pluginName;
    }

    public Class<? extends Hook> hookClass() {
        return hookClazz;
    }

    public String pluginName() {
        return pluginName;
    }
}