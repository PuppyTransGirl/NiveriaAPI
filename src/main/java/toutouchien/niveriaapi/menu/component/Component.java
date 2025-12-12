package toutouchien.niveriaapi.menu.component;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

/**
 * Base class for all menu components.
 * <p>
 * Components are the building blocks of menus, defining what items are displayed
 * and how they respond to player interactions. Each component has a position,
 * size, visibility state, and enabled state.
 */
public abstract class Component {
    private boolean visible = true;
    private boolean enabled = true;

    private int x = 0;
    private int y = 0;

    /**
     * Renders the component by placing its items in the menu inventory.
     *
     * @param context the menu context
     * @throws NullPointerException if context is null
     */
    public void render(@NotNull MenuContext context) {
        Preconditions.checkNotNull(context, "context cannot be null");

        Int2ObjectMap<ItemStack> items = this.items(context);
        IntSet slots = this.slots(context);

        for (int slot : slots) {
            ItemStack item = items.get(slot);
            context.menu().getInventory().setItem(slot, item);
        }
    }

    /**
     * Called when the component is added to the menu.
     * <p>
     * Override this method to perform initialization, such as starting tasks.
     *
     * @param context the menu context
     */
    public void onAdd(@NotNull MenuContext context) {

    }

    /**
     * Called when the component is removed from the menu.
     * <p>
     * Override this method to perform cleanup, such as cancelling tasks.
     *
     * @param context the menu context
     */
    public void onRemove(@NotNull MenuContext context) {

    }

    /**
     * Called when the component is clicked.
     * <p>
     * Override this method to handle click interactions.
     *
     * @param event   the click event
     * @param context the menu context
     */
    public void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context) {

    }

    /**
     * Returns the items to display, mapped by their inventory slot.
     *
     * @param context the menu context
     * @return a map of slot to ItemStack
     */
    @NotNull
    public abstract Int2ObjectMap<ItemStack> items(@NotNull MenuContext context);

    /**
     * Returns the set of inventory slots occupied by this component.
     *
     * @param context the menu context
     * @return a set of slot indices
     */
    @NotNull
    public abstract IntSet slots(@NotNull MenuContext context);

    /**
     * Sets the position of this component in the menu.
     *
     * @param x the x-coordinate (0-8)
     * @param y the y-coordinate (0+)
     * @throws IllegalArgumentException if x or y is negative
     */
    public void position(@NonNegative int x, @NonNegative int y) {
        Preconditions.checkArgument(x >= 0, "x cannot be negative: %s", x);
        Preconditions.checkArgument(y >= 0, "y cannot be negative: %s", y);

        this.x = x;
        this.y = y;
    }

    /**
     * Sets the visibility of this component.
     *
     * @param visible true to make visible, false to hide
     */
    public void visible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Sets whether this component is enabled.
     *
     * @param enabled true to enable, false to disable
     */
    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the x-coordinate of this component.
     *
     * @return the x-coordinate (0-8)
     */
    @NonNegative
    public int x() {
        return x;
    }

    /**
     * Returns the y-coordinate of this component.
     *
     * @return the y-coordinate (0+)
     */
    @NonNegative
    public int y() {
        return y;
    }

    /**
     * Returns the width of this component in slots.
     *
     * @return the width (1+)
     */
    @Positive
    public abstract int width();

    /**
     * Returns the height of this component in rows.
     *
     * @return the height (1+)
     */
    @Positive
    public abstract int height();

    /**
     * Returns the inventory slot index of this component's top-left position.
     *
     * @return the slot index
     */
    @NonNegative
    public int slot() {
        return y * 9 + x;
    }

    /**
     * Returns whether this component is visible.
     *
     * @return true if visible, false otherwise
     */
    public boolean visible() {
        return visible;
    }

    /**
     * Returns whether this component is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean enabled() {
        return enabled;
    }

    /**
     * Returns whether this component can be interacted with.
     * <p>
     * A component is interactable if it is both visible and enabled.
     *
     * @return true if interactable, false otherwise
     */
    public boolean interactable() {
        return this.visible && this.enabled;
    }

    /**
     * Converts a slot index to its x-coordinate.
     *
     * @param slot the slot index
     * @return the x-coordinate (0-8)
     */
    @NonNegative
    public static int toX(int slot) {
        return slot % 9;
    }

    /**
     * Converts a slot index to its y-coordinate.
     *
     * @param slot the slot index
     * @return the y-coordinate (0+)
     */
    @NonNegative
    public static int toY(int slot) {
        return slot / 9;
    }

    /**
     * Converts x and y coordinates to a slot index.
     *
     * @param x the x-coordinate (0-8)
     * @param y the y-coordinate (0+)
     * @return the slot index
     */
    @NonNegative
    public static int toSlot(int x, int y) {
        return y * 9 + x;
    }
}
