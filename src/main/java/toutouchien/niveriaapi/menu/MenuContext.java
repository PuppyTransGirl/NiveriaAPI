package toutouchien.niveriaapi.menu;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class MenuContext {
    private final Menu menu;
    private final Object2ObjectMap<String, Object> data;

    public MenuContext(@NotNull Menu menu) {
        this.menu = menu;
        this.data = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    }

    @NotNull
    public Menu menu() {
        return menu;
    }

    public Player player() {
        return this.menu.player();
    }

    public void set(String key, Object value) {
        this.data.put(key, value);
    }

    public Object get(String key) {
        return this.data.get(key);
    }

    public boolean has(String key) {
        return this.data.containsKey(key);
    }

    public void remove(String key) {
        this.data.remove(key);
    }

    public void clear() {
        this.data.clear();
    }

    public void close() {
        this.data.clear();
    }
}
