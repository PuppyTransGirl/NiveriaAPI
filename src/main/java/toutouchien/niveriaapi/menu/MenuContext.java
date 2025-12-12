package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context object that provides state management and access to menu-related data.
 * <p>
 * This class serves as a bridge between menu components and the menu itself,
 * providing a thread-safe key-value store for sharing data between components
 * and offering convenient access to the menu and player instances.
 */
public class MenuContext {
    private final Menu menu;
    private final Object2ObjectMap<String, Object> data;

    /**
     * Constructs a new MenuContext for the specified menu.
     *
     * @param menu the menu this context belongs to
     * @throws NullPointerException if menu is null
     */
    public MenuContext(@NotNull Menu menu) {
        Preconditions.checkNotNull(menu, "menu cannot be null");

        this.menu = menu;
        this.data = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    }

    /**
     * Returns the menu associated with this context.
     *
     * @return the menu instance
     */
    @NotNull
    public Menu menu() {
        return menu;
    }

    /**
     * Returns the player associated with this context's menu.
     *
     * @return the player who owns the menu
     */
    @NotNull
    public Player player() {
        return this.menu.player();
    }

    /**
     * Stores a value in the context's data map.
     *
     * @param key   the key to associate with the value
     * @param value the value to store, or null to remove the key
     * @throws NullPointerException if key is null
     */
    public void set(@NotNull String key, @Nullable Object value) {
        Preconditions.checkNotNull(key, "key cannot be null");

        this.data.put(key, value);
    }

    /**
     * Retrieves a value from the context's data map.
     *
     * @param key the key to look up
     * @return the value associated with the key, or null if not found
     * @throws NullPointerException if key is null
     */
    @Nullable
    public Object get(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        return this.data.get(key);
    }

    /**
     * Checks if a key exists in the context's data map.
     *
     * @param key the key to check for
     * @return true if the key exists, false otherwise
     * @throws NullPointerException if key is null
     */
    public boolean has(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        return this.data.containsKey(key);
    }

    /**
     * Removes a key and its associated value from the context's data map.
     *
     * @param key the key to remove
     * @throws NullPointerException if key is null
     */
    public void remove(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        this.data.remove(key);
    }

    /**
     * Removes all key-value pairs from the context's data map.
     */
    public void clear() {
        this.data.clear();
    }

    /**
     * Closes the context and clears all stored data.
     * <p>
     * This method should be called when the menu is being destroyed
     * to ensure proper cleanup of resources.
     */
    public void close() {
        this.data.clear();
    }
}
