package toutouchien.niveriaapi.menu.component;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Component {
    private final List<Component> children = new ArrayList<>();
    private final Map<String, Object> properties = new HashMap<>();
    private Component parent;
    private Menu menu;
    
    // Component state
    private boolean visible = true;
    private boolean enabled = true;
    private int updateInterval = -1; // -1 means no auto-update
    
    // Position and size information
    private int x = 0;
    private int y = 0;
    private int width = 1;
    private int height = 1;
    
    // Lifecycle callbacks
    private Consumer<Component> onMount;
    private Consumer<Component> onUnmount;
    private Consumer<Component> onUpdate;
    
    /**
     * Called when the component is first added to a menu
     */
    public void mount(@NotNull MenuContext context) {
        if (onMount != null)
			onMount.accept(this);

        children.forEach(child -> child.mount(context));
        menu = context.menu();
    }
    
    /**
     * Called when the component is removed from a menu
     */
    public void unmount(@NotNull MenuContext context) {
        if (onUnmount != null)
			onUnmount.accept(this);

        children.forEach(child -> child.unmount(context));
        menu = null;
    }
    
    /**
     * Called when the component needs to update its state
     */
    public void update(@NotNull MenuContext context) {
        if (onUpdate != null)
			onUpdate.accept(this);

        children.forEach(child -> child.update(context));
    }
    
    /**
     * Renders the component and its children
     */
    public abstract void render(@NotNull MenuContext context);
    
    /**
     * Handles click events on this component
     */
    public abstract void onClick(@NotNull InventoryClickEvent event, @NotNull MenuContext context);
    
    /**
     * Gets the slots occupied by this component
     */
    public abstract List<Integer> slots();
    
    /**
     * Gets the items to be displayed for this component
     */
    public abstract Map<Integer, ItemStack> items(@NotNull MenuContext context);
    
    // Builder-style methods for component configuration
    public Component position(int x, int y) {
        this.x = x;
        this.y = y;
        return this;
    }
    
    public Component size(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }
    
    public Component visible(boolean visible) {
        this.visible = visible;
        return this;
    }
    
    public Component enabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }
    
    public Component updateEvery(int ticks) {
        this.updateInterval = ticks;
        return this;
    }
    
    // Lifecycle hooks
    public Component onMount(Consumer<Component> callback) {
        this.onMount = callback;
        return this;
    }
    
    public Component onUnmount(Consumer<Component> callback) {
        this.onUnmount = callback;
        return this;
    }
    
    public Component onUpdate(Consumer<Component> callback) {
        this.onUpdate = callback;
        return this;
    }
    
    // Child management
    public Component addChild(Component child) {
        child.parent = this;
        children.add(child);
        return this;
    }
    
    public Component removeChild(Component child) {
        children.remove(child);
        child.parent = null;
        return this;
    }
    
    // Property management
    public <T> Component property(String key, T value) {
        properties.put(key, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> T property(String key) {
        return (T) properties.get(key);
    }
    
    // Getters
    public boolean visible() { return visible; }
    public boolean enabled() { return enabled; }
    public int x() { return x; }
    public int y() { return y; }
    public int width() { return width; }
    public int height() { return height; }
    public List<Component> children() { return new ArrayList<>(children); }
    public @Nullable Component parent() { return parent; }
    public int updateInterval() { return updateInterval; }
    
    /**
     * Converts slot coordinates to inventory slot number
     */
    public static int toSlot(int x, int y) {
        return y * 9 + x;
    }
    
    /**
     * Converts inventory slot number to X coordinate
     */
    public static int toX(int slot) {
        return slot % 9;
    }
    
    /**
     * Converts inventory slot number to Y coordinate
     */
    public static int toY(int slot) {
        return slot / 9;
    }
}