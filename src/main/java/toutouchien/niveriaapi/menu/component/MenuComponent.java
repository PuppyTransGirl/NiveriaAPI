package toutouchien.niveriaapi.menu.component;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

/**
 * Abstract base class for all menu components.
 * <p>
 * Components represent individual UI elements within a menu that can be rendered,
 * positioned, and interacted with. Each component has a position, size, visibility,
 * and enabled state, and can handle click events.
 */
@NullMarked
public abstract class MenuComponent {
    private final String id;

    private boolean visible = true;
    private boolean enabled = true;

    private int x = 0;
    private int y = 0;

    /**
     * Constructs a new MenuComponent with the specified ID.
     *
     * @param id the unique identifier for this component, or null for a default ID
     */
    protected MenuComponent(@Nullable String id) {
        this.id = id;
    }

    /**
     * Called when this component is added to a menu.
     * <p>
     * Override this method to perform initialization logic when the component
     * becomes part of a menu.
     *
     * @param context the menu context
     */
    public void onAdd(MenuContext context) {

    }

    /**
     * Called when this component is removed from a menu.
     * <p>
     * Override this method to perform cleanup logic when the component
     * is no longer part of a menu.
     *
     * @param context the menu context
     */
    public void onRemove(MenuContext context) {

    }

    /**
     * Handles click events on this component.
     * <p>
     * Override this method to define custom behavior when the component is clicked.
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    public void onClick(NiveriaInventoryClickEvent event, MenuContext context) {

    }

    /**
     * Returns a map of slot indices to ItemStacks that this component should display.
     * <p>
     * This method must be implemented by subclasses to define what items
     * the component renders at each slot.
     *
     * @param context the menu context
     * @return a map from slot indices to ItemStacks
     */
    public abstract Int2ObjectMap<ItemStack> items(MenuContext context);

    /**
     * Returns the set of inventory slot indices that this component occupies.
     * <p>
     * This method must be implemented by subclasses to define which slots
     * the component uses for rendering.
     *
     * @param context the menu context
     * @return a set of slot indices
     */
    public abstract IntSet slots(MenuContext context);

    /**
     * Renders this component to the menu's inventory.
     * <p>
     * This method retrieves the component's items and slots, then places
     * the items into the appropriate inventory slots.
     *
     * @param context the menu context
     * @throws NullPointerException if context is null
     */
    public void render(MenuContext context) {
        Preconditions.checkNotNull(context, "context cannot be null");

        if (!this.visible())
            return;

        Int2ObjectMap<ItemStack> items = this.items(context);
        IntSet slots = this.slots(context);

        for (int slot : slots) {
            ItemStack item = items.get(slot);
            context.menu().getInventory().setItem(slot, item);
        }
    }

    /**
     * Sets the position of this component within the menu grid.
     *
     * @param x the x-coordinate (0-8 for standard inventory width)
     * @param y the y-coordinate (0+ for inventory height)
     * @throws IllegalArgumentException if x or y is negative
     */
    public void position(@NonNegative int x, @NonNegative int y) {
        Preconditions.checkArgument(x >= 0, "x cannot be negative: %s", x);
        Preconditions.checkArgument(y >= 0, "y cannot be negative: %s", y);

        this.x = x;
        this.y = y;
    }

    /**
     * Sets the visibility state of this component.
     *
     * @param visible true to make the component visible, false to hide it
     */
    public void visible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Sets the enabled state of this component.
     *
     * @param enabled true to enable the component, false to disable it
     */
    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the unique identifier of this component.
     *
     * @return the component ID
     */
    @Nullable
    public String id() {
        return id;
    }

    /**
     * Returns the x-coordinate of this component's position.
     *
     * @return the x-coordinate (0-based)
     */
    @NonNegative
    public int x() {
        return x;
    }

    /**
     * Returns the y-coordinate of this component's position.
     *
     * @return the y-coordinate (0-based)
     */
    @NonNegative
    public int y() {
        return y;
    }

    /**
     * Returns the width of this component in inventory slots.
     *
     * @return the component width (must be positive)
     */
    @Positive
    public abstract int width();

    /**
     * Returns the height of this component in inventory rows.
     *
     * @return the component height (must be positive)
     */
    @Positive
    public abstract int height();

    /**
     * Returns the inventory slot index for this component's top-left position.
     *
     * @return the slot index calculated from x and y coordinates
     */
    @NonNegative
    public int slot() {
        return y * 9 + x;
    }

    /**
     * Returns whether this component is currently visible.
     *
     * @return true if visible, false otherwise
     */
    public boolean visible() {
        return visible;
    }

    /**
     * Returns whether this component is currently enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean enabled() {
        return enabled;
    }

    /**
     * Returns whether this component can be interacted with.
     * <p>
     * A component is interactable when it is both visible and enabled.
     *
     * @return true if the component is interactable, false otherwise
     */
    public boolean interactable() {
        return this.visible && this.enabled;
    }

    protected static class Builder<T> {
        protected String id;

        /**
         * Sets the ID for this component.
         *
         * @param id the unique identifier for the component
         * @return this builder for method chaining
         * @throws NullPointerException if id is null
         */
        @SuppressWarnings("unchecked")
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public T id(String id) {
            Preconditions.checkNotNull(id, "id cannot be null");

            this.id = id;
            return (T) this;
        }
    }

    /**
     * Converts an inventory slot index to its x-coordinate.
     *
     * @param slot the slot index
     * @return the x-coordinate (0-8)
     */
    @NonNegative
    public static int toX(int slot) {
        return slot % 9;
    }

    /**
     * Converts an inventory slot index to its y-coordinate.
     *
     * @param slot the slot index
     * @return the y-coordinate (0+)
     */
    @NonNegative
    public static int toY(int slot) {
        return slot / 9;
    }

    /**
     * Converts x and y coordinates to an inventory slot index.
     *
     * @param x the x-coordinate
     * @param y the y-coordinate
     * @return the slot index
     */
    @NonNegative
    public static int toSlot(int x, int y) {
        return y * 9 + x;
    }
}
