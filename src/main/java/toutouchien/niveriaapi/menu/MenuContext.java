package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Represents the context of a menu, managing navigation history and data storage.
 * <p>
 * This class allows for tracking previous menus in a stack-like manner,
 * enabling users to navigate back through their menu history.
 * It also provides a key-value store for persisting data across menu interactions.
 */
public class MenuContext {
    private static final int MAX_PREVIOUS_MENUS = 64;

    private final Deque<Menu> previousMenus;
    private final Object2ObjectMap<String, Object> data;

    private Menu menu;
    private boolean wasPreviousMenuCall;
    private boolean firstMenuSet = true;

    /**
     * Constructs a new MenuContext for the specified menu.
     *
     * @param menu the menu associated with this context
     * @throws NullPointerException if menu is null
     */
    public MenuContext(@NotNull Menu menu) {
        this.previousMenus = new ArrayDeque<>();
        this.data = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());

        this.menu = menu;
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
     * Returns the previous menu in the navigation stack.
     *
     * @return the previous menu instance, or null if there is none
     */
    @Nullable
    public Menu previousMenu() {
        if (this.previousMenus.isEmpty())
            return null;

        this.wasPreviousMenuCall = true;
        return previousMenus.pollLast();
    }

    /**
     * Checks if there is a previous menu in the navigation stack.
     *
     * @return true if there is a previous menu, false otherwise
     */
    public boolean hasPreviousMenu() {
        return !this.previousMenus.isEmpty();
    }

    /**
     * Sets the menu associated with this context.
     *
     * @param menu the menu instance to set
     * @throws NullPointerException if menu is null
     */
    void menu(@NotNull Menu menu) {
        Preconditions.checkNotNull(menu, "menu cannot be null");
        if (this.firstMenuSet) {
            this.firstMenuSet = false;
            return;
        }

        lastMenu();

        this.menu = menu;
        this.wasPreviousMenuCall = false;
        this.firstMenuSet = false;
    }

    /**
     * Stores the current menu in the previous menus stack.
     */
    private void lastMenu() {
        if (this.wasPreviousMenuCall || !this.menu.canGoBackToThisMenu())
            return;

        this.previousMenus.add(this.menu);
        if (this.previousMenus.size() > MAX_PREVIOUS_MENUS)
            this.previousMenus.removeFirst();
    }

    /**
     * Stores a value in the context's data map.
     *
     * @param key   the key to associate with the value
     * @param value the value to store
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
        this.clear();
    }
}
