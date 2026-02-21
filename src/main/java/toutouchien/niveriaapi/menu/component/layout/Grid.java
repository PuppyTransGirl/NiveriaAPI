package toutouchien.niveriaapi.menu.component.layout;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

/**
 * A layout component that arranges child components in a rectangular grid.
 * <p>
 * The Grid component provides a flexible container for organizing menu components
 * with optional border and fill items. Components are positioned within the grid
 * using slot indices or x/y coordinates. The grid handles rendering priority as:
 * slot components → border → fill.
 */
@NullMarked
public class Grid extends MenuComponent {
    private final ObjectList<MenuComponent> slotComponents;

    @Nullable private final ItemStack border;
    @Nullable private final ItemStack fill;

    /**
     * Constructs a new Grid with the specified configuration.
     *
     * @param builder the builder containing the builder configuration
     */
    private Grid(Builder builder) {
        super(builder);
        this.slotComponents = new ObjectArrayList<>(builder.slotComponents);

        this.border = builder.border;
        this.fill = builder.fill;
    }

    /**
     * Creates a new Grid builder instance.
     *
     * @return a new Grid.Builder for constructing grids
     */
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * Called when this grid is added to a menu.
     * <p>
     * Propagates the onAdd event to all child components.
     *
     * @param context the menu context
     */
    @Override
    public void onAdd(MenuContext context) {
        this.slotComponents.forEach(component -> {
            component.onAdd(context);

            String addedID = component.id();
            if (addedID != null)
                context.menu().registerComponentID(addedID, component);
        });
    }

    /**
     * Called when this grid is removed from a menu.
     * <p>
     * Cleans up all child components and unregisters their IDs from the menu.
     *
     * @param context the menu context
     */
    @Override
    public void onRemove(MenuContext context) {
        this.slotComponents.forEach(component -> {
            component.onRemove(context);

            String removedID = component.id();
            if (removedID != null)
                context.menu().unregisterComponentID(removedID);
        });
    }

    /**
     * Handles click events within this grid.
     * <p>
     * Delegates click events to the appropriate child component based on
     * which slots the component occupies. Only processes clicks if the grid
     * is interactable.
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    @Override
    public void onClick(NiveriaInventoryClickEvent event, MenuContext context) {
        if (!this.interactable())
            return;

        for (MenuComponent component : this.slotComponents) {
            if (component.slots(context).contains(event.getSlot())) {
                component.onClick(event, context);
                break;
            }
        }
    }

    /**
     * Returns the items to be displayed in this grid.
     * <p>
     * Rendering priority: slot components → border → fill.
     * Child components take precedence, followed by border decoration,
     * and finally fill items for any remaining empty slots.
     *
     * @param context the menu context
     * @return a map from slot indices to ItemStacks
     */
    @Override
    public Int2ObjectMap<ItemStack> items(MenuContext context) {
        Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();

        for (MenuComponent slotComponent : this.slotComponents)
            items.putAll(slotComponent.items(context));

        if (this.border == null && this.fill == null)
            return items;

        for (int y = 0; y < this.height; y++) {
            for (int x = 0; x < this.width; x++) {
                int slot = toSlot(x + this.x(), y + this.y());
                if (items.containsKey(slot))
                    continue;

                if (this.border != null && this.border(x + this.x(), y + this.y()))
                    items.put(slot, this.border);
                else if (this.fill != null)
                    items.put(slot, this.fill);
            }
        }

        return items;
    }

    /**
     * Determines if the specified coordinates represent a border position.
     *
     * @param x the absolute x-coordinate
     * @param y the absolute y-coordinate
     * @return true if the position is on the grid border, false otherwise
     */
    private boolean border(int x, int y) {
        return x == this.x()
                || x == this.x() + this.width - 1
                || y == this.y()
                || y == this.y() + this.height - 1;
    }

    /**
     * Builder class for constructing Grid instances with a fluent interface.
     */
    public static class Builder extends MenuComponent.Builder<Builder> {
        private final ObjectList<MenuComponent> slotComponents = new ObjectArrayList<>();
        @Nullable
        private ItemStack border;
        @Nullable
        private ItemStack fill;

        /**
         * Adds a component to the grid at the specified slot index.
         *
         * @param slot      the slot index where the component should be placed
         * @param component the component to add
         * @return this builder for method chaining
         * @throws IllegalArgumentException if slot is negative, component is null,
         *                                  or component doesn't fit within grid bounds
         */
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder add(@NonNegative int slot, MenuComponent component) {
            Preconditions.checkArgument(slot >= 0, "slot cannot be negative: %s", slot);
            Preconditions.checkNotNull(component, "component cannot be null");

            component.position(toX(slot), toY(slot));

            // Check that the component fits inside the grid
            int compX = component.x();
            int compY = component.y();
            int compWidth = component.width();
            int compHeight = component.height();

            Preconditions.checkArgument(
                    compX >= 0 && compY >= 0 &&
                            compX + compWidth <= this.width &&
                            compY + compHeight <= this.height,
                    "MenuComponent %s does not fit inside the grid of size %sx%s at position (%s, %s) with size %sx%s. (Have you set the grid size before adding components ?)",
                    component.getClass().getSimpleName(),
                    this.width, this.height,
                    compX, compY,
                    compWidth, compHeight
            );

            slotComponents.add(component);
            return this;
        }

        /**
         * Adds a component to the grid at the specified x/y coordinates.
         *
         * @param x         the x-coordinate (0-based)
         * @param y         the y-coordinate (0-based)
         * @param component the component to add
         * @return this builder for method chaining
         * @throws IllegalArgumentException if coordinates are negative or component is null
         */
        @Contract(value = "_, _, _ -> this", mutates = "this")
        public Builder add(@NonNegative int x, @NonNegative int y, MenuComponent component) {
            Preconditions.checkArgument(x >= 0, "x cannot be negative: %s", x);
            Preconditions.checkArgument(y >= 0, "y cannot be negative: %s", y);
            Preconditions.checkNotNull(component, "component cannot be null");

            return add(y * this.width + x, component);
        }

        /**
         * Sets the border ItemStack for this grid.
         *
         * @param border the ItemStack to use for border decoration
         * @return this builder for method chaining
         * @throws NullPointerException if border is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder border(ItemStack border) {
            Preconditions.checkNotNull(border, "border cannot be null");

            this.border = border;
            return this;
        }

        /**
         * Sets the fill ItemStack for this grid.
         *
         * @param fill the ItemStack to use for empty space filling
         * @return this builder for method chaining
         * @throws NullPointerException if fill is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder fill(ItemStack fill) {
            Preconditions.checkNotNull(fill, "fill cannot be null");

            this.fill = fill;
            return this;
        }

        /**
         * Builds and returns the configured Grid instance.
         *
         * @return a new Grid with the specified configuration
         */
        public Grid build() {
            return new Grid(this);
        }
    }
}
