package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context object for a menu that provides access to the menu instance,
 * the player, and a key-value storage for component state.
 * <p>
 * This class is thread-safe for data storage operations.
 */
public class MenuContext {
    private final Menu menu;
    private final Object2ObjectMap<String, Object> data;

    /**
     * Constructs a new menu context for the specified menu.
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
     * Returns the menu this context belongs to.
     *
     * @return the menu
     */
    @NotNull
    public Menu menu() {
        return menu;
    }

    /**
     * Returns the player viewing the menu.
     *
     * @return the player
     */
    @NotNull
    public Player player() {
        return this.menu.player();
    }

    /**
     * Stores a value associated with the specified key.
     *
     * @param key   the key to store the value under
     * @param value the value to store (can be null)
     * @throws NullPointerException if key is null
     */
    public void set(@NotNull String key, @Nullable Object value) {
        Preconditions.checkNotNull(key, "key cannot be null");

        this.data.put(key, value);
    }

    /**
     * Retrieves the value associated with the specified key.
     *
     * @param key the key to retrieve the value for
     * @return the value, or null if not found
     * @throws NullPointerException if key is null
     */
    @Nullable
    public Object get(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        return this.data.get(key);
    }

    /**
     * Checks if a value exists for the specified key.
     *
     * @param key the key to check
     * @return true if the key exists, false otherwise
     * @throws NullPointerException if key is null
     */
    public boolean has(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        return this.data.containsKey(key);
    }

    /**
     * Removes the value associated with the specified key.
     *
     * @param key the key to remove
     * @throws NullPointerException if key is null
     */
    public void remove(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        this.data.remove(key);
    }

    /**
     * Clears all stored data.
     */
    public void clear() {
        this.data.clear();
    }

    /**
     * Closes the context and clears all stored data.
     * <p>
     * This should be called when the menu is closed.
     */
    public void close() {
        this.data.clear();
    }
}
