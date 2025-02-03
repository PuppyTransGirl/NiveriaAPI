package toutouchien.niveriaapi.menu;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MenuContext {
    private Menu menu;
    private final Map<String, Object> data;

    public MenuContext(@NotNull Menu menu) {
        this.menu = menu;
        this.data = new HashMap<>();
    }

    public @Nullable Menu menu() {
        return menu;
    }

    public @Nullable Player viewer() {
        return menu.viewer();
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }

    public boolean has(String key) {
        return data.containsKey(key);
    }

    public void remove(String key) {
        data.remove(key);
    }

    public void clear() {
        data.clear();
    }

    public void close() {
        menu = null;
        data.clear();
    }
}