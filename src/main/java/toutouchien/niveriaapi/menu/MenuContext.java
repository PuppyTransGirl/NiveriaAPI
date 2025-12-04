package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MenuContext {
    private final Menu menu;
    private final Object2ObjectMap<String, Object> data;

    public MenuContext(@NotNull Menu menu) {
        Preconditions.checkNotNull(menu, "menu cannot be null");

        this.menu = menu;
        this.data = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    }

    @NotNull
    public Menu menu() {
        return menu;
    }

    @NotNull
    public Player player() {
        return this.menu.player();
    }

    public void set(@NotNull String key, @Nullable Object value) {
        Preconditions.checkNotNull(key, "key cannot be null");

        this.data.put(key, value);
    }

    @Nullable
    public Object get(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        return this.data.get(key);
    }

    public boolean has(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        return this.data.containsKey(key);
    }

    public void remove(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        this.data.remove(key);
    }

    public void clear() {
        this.data.clear();
    }

    public void close() {
        this.data.clear();
    }
}
