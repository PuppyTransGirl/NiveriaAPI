package toutouchien.niveriaapi.menu.component.container;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.Arrays;
import java.util.function.Function;

/**
 * A container component that displays multiple components across paginated pages.
 * <p>
 * The Paginator component organizes a list of child components into pages, displaying
 * a subset of components based on the current page and the configured page size.
 * It provides navigation buttons for moving between pages, including back/next buttons
 * and optional first/last page buttons. Navigation buttons can have different appearances
 * when disabled (at first/last page).
 */
@NullMarked
public class Paginator extends MenuComponent {
    private final ObjectList<MenuComponent> components;

    private Function<MenuContext, ItemStack> firstPageItem, lastPageItem, backItem, nextItem;
    @Nullable
    private Function<MenuContext, ItemStack> offBackItem, offNextItem, offFirstPageItem, offLastPageItem;

    private final int width, height;
    private final IntList layoutSlots;

    private int page;
    @Nullable
    private ObjectList<MenuComponent> cachedPageComponents;

    /**
     * Constructs a new Paginator with the specified configuration.
     *
     * @param builder the builder containing the paginator configuration
     */
    private Paginator(Builder builder) {
        super(builder.id);
        this.components = builder.components;

        this.backItem = builder.backItem;
        this.nextItem = builder.nextItem;
        this.offBackItem = builder.offBackItem;
        this.offNextItem = builder.offNextItem;

        this.firstPageItem = builder.firstPageItem;
        this.lastPageItem = builder.lastPageItem;
        this.offFirstPageItem = builder.offFirstPageItem;
        this.offLastPageItem = builder.offLastPageItem;

        this.width = builder.width;
        this.height = builder.height;

        this.page = builder.page;

        this.layoutSlots = new IntArrayList(width * height);

        // Initial calculation of layout slots
        this.updateLayoutSlots();
    }

    /**
     * Called when this paginator is added to a menu.
     * <p>
     * Propagates the onAdd event to all child components if the paginator is visible.
     *
     * @param context the menu context
     */
    @Override
    public void onAdd(MenuContext context) {
        this.components.forEach(component -> {
            component.onAdd(context);

            String addedID = component.id();
            if (addedID != null)
                context.menu().registerComponentID(addedID, component);
        });
    }

    /**
     * Called when this paginator is removed from a menu.
     * <p>
     * Cleans up all child components and unregisters their IDs from the menu.
     *
     * @param context the menu context
     */
    @Override
    public void onRemove(MenuContext context) {
        this.components.forEach(component -> {
            component.onRemove(context);

            String removedID = component.id();
            if (removedID != null)
                context.menu().unregisterComponentID(removedID);
        });
    }

    /**
     * Updates the cached list of absolute inventory slots that this paginator controls.
     * Should be called whenever the paginator's position (x, y) or dimensions change.
     */
    private void updateLayoutSlots() {
        this.layoutSlots.clear();
        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                int absX = this.x() + col;
                int absY = this.y() + row;

                this.layoutSlots.add(MenuComponent.toSlot(absX, absY));
            }
        }
    }

    @Override
    public void position(@NonNegative int x, @NonNegative int y) {
        super.position(x, y);
        // Position changed, we must re-calculate the absolute slots for the grid
        this.updateLayoutSlots();
    }

    /**
     * Handles click events within the paginator.
     * <p>
     * Iterates through the current page's components, ensures they are correctly
     * positioned, and delegates the event if the click falls within their slots.
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    @Override
    public void onClick(NiveriaInventoryClickEvent event, MenuContext context) {
        if (!this.interactable())
            return;

        ObjectList<MenuComponent> pageComponents = this.currentPageComponents();
        for (int i = 0; i < pageComponents.size(); i++) {
            if (i >= this.layoutSlots.size())
                break; // Should not happen if page size matches, but safety check

            MenuComponent component = pageComponents.get(i);
            if (component.slots(context).contains(event.slot())) {
                component.onClick(event, context);
                return;
            }
        }
    }

    /**
     * Returns the items to be displayed by this paginator for the current page.
     * <p>
     * Components are assigned to the pre-calculated layout slots. The component's
     * position is updated to match the slot, and its items are then collected.
     *
     * @param context the menu context
     * @return a map from slot indices to ItemStacks for the current page
     */
    @Override
    public Int2ObjectMap<ItemStack> items(MenuContext context) {
        Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();
        ObjectList<MenuComponent> pageComponents = this.currentPageComponents();

        for (int i = 0; i < pageComponents.size(); i++) {
            if (i >= this.layoutSlots.size()) break;

            MenuComponent component = pageComponents.get(i);
            int slot = this.layoutSlots.getInt(i);

            component.position(MenuComponent.toX(slot), MenuComponent.toY(slot));
            items.putAll(component.items(context));
        }

        return items;
    }

    /**
     * Returns the set of slots that this paginator can occupy.
     * <p>
     * Returns the pre-calculated set of all slots in the pagination grid to ensure
     * proper cleanup of the area when pages change.
     *
     * @param context the menu context
     * @return a set of all possible slot indices for this paginator
     */
    @Override
    public IntSet slots(MenuContext context) {
        // Return all slots controlled by the paginator grid
        return new IntOpenHashSet(this.layoutSlots);
    }

    @Override
    public void render(MenuContext context) {
        this.invalidateCache();
        super.render(context);
    }

    /**
     * Returns the list of components to display on the current page.
     * <p>
     * This method caches the result to avoid recalculating the component
     * list on every render and interaction. The cache is invalidated when
     * the page changes.
     *
     * @return a list of components for the current page
     */
    private ObjectList<MenuComponent> currentPageComponents() {
        if (this.cachedPageComponents != null)
            return this.cachedPageComponents;

        int maxItemsPerPage = this.width * this.height;
        int totalItems = this.components.size();
        int startIndex = this.page * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, totalItems);

        this.cachedPageComponents = new ObjectArrayList<>(this.components.subList(startIndex, endIndex));
        return this.cachedPageComponents;
    }

    /**
     * Invalidates the cached page components, forcing recalculation on next access.
     */
    private void invalidateCache() {
        this.cachedPageComponents = null;
    }

    /**
     * Creates a back navigation button for this paginator.
     * <p>
     * The button navigates to the previous page when clicked. If already on the first
     * page and no disabled item is configured, an AIR item is displayed.
     *
     * @return a Button for going to the previous page
     */
    public Button backButton() {
        return Button.create()
                .item(context -> {
                    if (this.page > 0)
                        return this.backItem.apply(context);

                    if (this.offBackItem != null)
                        return this.offBackItem.apply(context);

                    return ItemStack.of(Material.AIR);
                })
                .onClick(event -> {
                    if (this.page <= 0)
                        return;

                    this.page--;
                    this.render(event.context());
                })
                .build();
    }

    /**
     * Creates a next navigation button for this paginator.
     * <p>
     * The button navigates to the next page when clicked. If already on the last
     * page and no disabled item is configured, an AIR item is displayed.
     *
     * @return a Button for going to the next page
     */
    public Button nextButton() {
        return Button.create()
                .item(context -> {
                    if (this.page < this.maxPage())
                        return this.nextItem.apply(context);

                    if (this.offNextItem != null)
                        return this.offNextItem.apply(context);

                    return ItemStack.of(Material.AIR);
                })
                .onClick(event -> {
                    if (this.page >= this.maxPage())
                        return;

                    this.page++;
                    this.render(event.context());
                })
                .build();
    }

    /**
     * Creates a first page navigation button for this paginator.
     * <p>
     * The button navigates to the first page when clicked. If already on the first
     * page and no disabled item is configured, an AIR item is displayed.
     *
     * @return a Button for going to the first page
     */
    public Button firstPageButton() {
        return Button.create()
                .item(context -> {
                    if (this.page > 0)
                        return this.firstPageItem.apply(context);

                    if (this.offFirstPageItem != null)
                        return this.offFirstPageItem.apply(context);

                    return ItemStack.of(Material.AIR);
                })
                .onClick(event -> {
                    if (this.page <= 0)
                        return;

                    this.page = 0;
                    this.render(event.context());
                })
                .build();
    }

    /**
     * Creates a last page navigation button for this paginator.
     * <p>
     * The button navigates to the last page when clicked. If already on the last
     * page and no disabled item is configured, an AIR item is displayed.
     *
     * @return a Button for going to the last page
     */
    public Button lastPageButton() {
        return Button.create()
                .item(context -> {
                    if (this.page < this.maxPage())
                        return this.lastPageItem.apply(context);

                    if (this.offLastPageItem != null)
                        return this.offLastPageItem.apply(context);

                    return ItemStack.of(Material.AIR);
                })
                .onClick(event -> {
                    int maxPage = this.maxPage();
                    if (this.page >= maxPage)
                        return;

                    this.page = maxPage;
                    this.render(event.context());
                })
                .build();
    }

    /**
     * Calculates the maximum page index (0-based) for this paginator.
     *
     * @return the highest valid page index, or -1 if no components exist
     */
    public int maxPage() {
        int maxItemsPerPage = this.width * this.height;
        int totalItems = this.components.size();
        return (int) Math.ceil((double) totalItems / maxItemsPerPage) - 1;
    }

    /**
     * Adds a component to the paginator.
     *
     * @param context   the menu context
     * @param component the component to add
     * @return this paginator for method chaining
     * @throws NullPointerException if component is null
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    public Paginator add(MenuContext context, MenuComponent component) {
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkNotNull(component, "component cannot be null");

        this.components.add(component);
        String addedID = component.id();
        if (addedID != null)
            context.menu().registerComponentID(addedID, component);

        return this;
    }

    /**
     * Adds multiple components to the paginator.
     *
     * @param context    the menu context
     * @param components the list of components to add
     * @return this paginator for method chaining
     * @throws NullPointerException if components is null
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    public Paginator addAll(MenuContext context, ObjectList<MenuComponent> components) {
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkNotNull(components, "components cannot be null");

        for (MenuComponent component : components)
            this.add(context, component);
        return this;
    }

    /**
     * Removes a component from the paginator based on the specified slot.
     *
     * @param context the menu context
     * @param slot    the slot index of the component to remove
     * @return this paginator for method chaining
     * @throws NullPointerException if context is null
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    public Paginator remove(MenuContext context, @NonNegative int slot) {
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkArgument(slot >= 0, "slot cannot be less than 0: %s", slot);

        ObjectList<MenuComponent> pageComponents = this.currentPageComponents();
        for (int i = 0; i < pageComponents.size(); i++) {
            if (i >= this.layoutSlots.size())
                break;

            MenuComponent component = pageComponents.get(i);
            int targetSlot = this.layoutSlots.getInt(i);

            // Temporarily position to check slots
            component.position(MenuComponent.toX(targetSlot), MenuComponent.toY(targetSlot));

            if (!component.slots(context).contains(slot))
                continue;

            this.components.remove(component);
            String removedID = component.id();
            if (removedID != null)
                context.menu().unregisterComponentID(removedID);

            return this;
        }
        return this;
    }

    /**
     * Removes a specific component from the paginator.
     *
     * @param context   the menu context
     * @param component the component to remove
     * @return this paginator for method chaining
     * @throws NullPointerException if component is null
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    public Paginator remove(MenuContext context, MenuComponent component) {
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkNotNull(component, "component cannot be null");

        this.components.remove(component);
        String removedID = component.id();
        if (removedID != null)
            context.menu().unregisterComponentID(removedID);
        return this;
    }

    /**
     * Removes multiple components at the specified indexes from the paginator.
     *
     * @param context the menu context
     * @param indexes the set of indexes of components to remove
     * @return this paginator for method chaining
     * @throws NullPointerException if indexes is null
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    public Paginator removeAll(MenuContext context, IntSet indexes) {
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkNotNull(indexes, "indexes cannot be null");

        int[] sorted = indexes.toIntArray();
        Arrays.sort(sorted);
        for (int i = sorted.length - 1; i >= 0; i--) {
            int index = sorted[i];
            if (index >= this.components.size())
                break; // The next indexes will always be bigger

            MenuComponent component = this.components.get(index);
            this.remove(context, component);
        }

        return this;
    }

    /**
     * Removes multiple components from the paginator.
     *
     * @param context    the menu context
     * @param components the set of components to remove
     * @return this paginator for method chaining
     * @throws NullPointerException if components is null
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    public Paginator removeAll(MenuContext context, ObjectSet<MenuComponent> components) {
        Preconditions.checkNotNull(context, "context cannot be null");
        Preconditions.checkNotNull(components, "components cannot be null");

        for (MenuComponent component : components)
            this.remove(context, component);
        return this;
    }

    /**
     * Clears all components from the paginator.
     *
     * @return this paginator for method chaining
     */
    @Contract(value = "-> this", mutates = "this")
    public Paginator clear() {
        this.components.clear();
        return this;
    }

    /**
     * Sets the ItemStack for the enabled back button.
     *
     * @param backItem the ItemStack for the back button
     * @return this paginator for method chaining
     * @throws NullPointerException if backItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator backItem(ItemStack backItem) {
        Preconditions.checkNotNull(backItem, "backItem cannot be null");

        this.backItem = context -> backItem;
        return this;
    }

    /**
     * Sets the ItemStack for the enabled next button.
     *
     * @param nextItem the ItemStack for the next button
     * @return this paginator for method chaining
     * @throws NullPointerException if nextItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator nextItem(ItemStack nextItem) {
        Preconditions.checkNotNull(nextItem, "nextItem cannot be null");

        this.nextItem = context -> nextItem;
        return this;
    }

    /**
     * Sets the ItemStack for the disabled back button.
     *
     * @param offBackItem the ItemStack for the disabled back button
     * @return this paginator for method chaining
     * @throws NullPointerException if offBackItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offBackItem(ItemStack offBackItem) {
        Preconditions.checkNotNull(offBackItem, "offBackItem cannot be null");

        this.offBackItem = context -> offBackItem;
        return this;
    }

    /**
     * Sets the ItemStack for the disabled next button.
     *
     * @param offNextItem the ItemStack for the disabled next button
     * @return this paginator for method chaining
     * @throws NullPointerException if offNextItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offNextItem(ItemStack offNextItem) {
        Preconditions.checkNotNull(offNextItem, "offNextItem cannot be null");

        this.offNextItem = context -> offNextItem;
        return this;
    }

    /**
     * Sets the ItemStack for the enabled first page button.
     *
     * @param firstPageItem the ItemStack for the first page button
     * @return this paginator for method chaining
     * @throws NullPointerException if firstPageItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator firstPageItem(ItemStack firstPageItem) {
        Preconditions.checkNotNull(firstPageItem, "firstPageItem cannot be null");

        this.firstPageItem = context -> firstPageItem;
        return this;
    }

    /**
     * Sets the ItemStack for the enabled last page button.
     *
     * @param lastPageItem the ItemStack for the last page button
     * @return this paginator for method chaining
     * @throws NullPointerException if lastPageItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator lastPageItem(ItemStack lastPageItem) {
        Preconditions.checkNotNull(lastPageItem, "lastPageItem cannot be null");

        this.lastPageItem = context -> lastPageItem;
        return this;
    }

    /**
     * Sets the ItemStack for the disabled first page button.
     *
     * @param offFirstPageItem the ItemStack for the disabled first page button
     * @return this paginator for method chaining
     * @throws NullPointerException if offFirstPageItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offFirstPageItem(ItemStack offFirstPageItem) {
        Preconditions.checkNotNull(offFirstPageItem, "offFirstPageItem cannot be null");

        this.offFirstPageItem = context -> offFirstPageItem;
        return this;
    }

    /**
     * Sets the ItemStack for the disabled last page button.
     *
     * @param offLastPageItem the ItemStack for the disabled last page button
     * @return this paginator for method chaining
     * @throws NullPointerException if offLastPageItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offLastPageItem(ItemStack offLastPageItem) {
        Preconditions.checkNotNull(offLastPageItem, "offLastPageItem cannot be null");

        this.offLastPageItem = context -> offLastPageItem;
        return this;
    }

    /**
     * Sets a function to provide the enabled back button ItemStack.
     *
     * @param backItem function that returns the back button ItemStack
     * @return this paginator for method chaining
     * @throws NullPointerException if backItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator backItem(Function<MenuContext, ItemStack> backItem) {
        Preconditions.checkNotNull(backItem, "backItem cannot be null");

        this.backItem = backItem;
        return this;
    }

    /**
     * Sets a function to provide the enabled next button ItemStack.
     *
     * @param nextItem function that returns the next button ItemStack
     * @return this paginator for method chaining
     * @throws NullPointerException if nextItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator nextItem(Function<MenuContext, ItemStack> nextItem) {
        Preconditions.checkNotNull(nextItem, "nextItem cannot be null");

        this.nextItem = nextItem;
        return this;
    }

    /**
     * Sets a function to provide the disabled back button ItemStack.
     *
     * @param offBackItem function that returns the disabled back button ItemStack
     * @return this paginator for method chaining
     * @throws NullPointerException if offBackItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offBackItem(Function<MenuContext, ItemStack> offBackItem) {
        Preconditions.checkNotNull(offBackItem, "offBackItem cannot be null");

        this.offBackItem = offBackItem;
        return this;
    }

    /**
     * Sets a function to provide the disabled next button ItemStack.
     *
     * @param offNextItem function that returns the disabled next button ItemStack
     * @return this paginator for method chaining
     * @throws NullPointerException if offNextItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offNextItem(Function<MenuContext, ItemStack> offNextItem) {
        Preconditions.checkNotNull(offNextItem, "offNextItem cannot be null");

        this.offNextItem = offNextItem;
        return this;
    }

    /**
     * Sets a function to provide the enabled first page button ItemStack.
     *
     * @param firstPageItem function that returns the first page button ItemStack
     * @return this paginator for method chaining
     * @throws NullPointerException if firstPageItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator firstPageItem(Function<MenuContext, ItemStack> firstPageItem) {
        Preconditions.checkNotNull(firstPageItem, "firstPageItem cannot be null");

        this.firstPageItem = firstPageItem;
        return this;
    }

    /**
     * Sets a function to provide the enabled last page button ItemStack.
     *
     * @param lastPageItem function that returns the last page button ItemStack
     * @return this paginator for method chaining
     * @throws NullPointerException if lastPageItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator lastPageItem(Function<MenuContext, ItemStack> lastPageItem) {
        Preconditions.checkNotNull(lastPageItem, "lastPageItem cannot be null");

        this.lastPageItem = lastPageItem;
        return this;
    }

    /**
     * Sets a function to provide the disabled first page button ItemStack.
     *
     * @param offFirstPageItem function that returns the disabled first page button ItemStack
     * @return this paginator for method chaining
     * @throws NullPointerException if offFirstPageItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offFirstPageItem(Function<MenuContext, ItemStack> offFirstPageItem) {
        Preconditions.checkNotNull(offFirstPageItem, "offFirstPageItem cannot be null");

        this.offFirstPageItem = offFirstPageItem;
        return this;
    }

    /**
     * Sets a function to provide the disabled last page button ItemStack.
     *
     * @param offLastPageItem function that returns the disabled last page button ItemStack
     * @return this paginator for method chaining
     * @throws NullPointerException if offLastPageItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offLastPageItem(Function<MenuContext, ItemStack> offLastPageItem) {
        Preconditions.checkNotNull(offLastPageItem, "offLastPageItem cannot be null");

        this.offLastPageItem = offLastPageItem;
        return this;
    }

    /**
     * Sets the initial page index for the paginator.
     *
     * @param page the initial page index (0-based)
     * @return this paginator for method chaining
     * @throws IllegalArgumentException if page is negative
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator page(@NonNegative int page) {
        Preconditions.checkArgument(page >= 0, "page cannot be less than 0: %s", page);

        this.page = page;
        return this;
    }

    /**
     * Returns the width of this paginator in slots.
     *
     * @return the paginator width
     */
    @Positive
    @Override
    public int width() {
        return this.width;
    }

    /**
     * Returns the height of this paginator in rows.
     *
     * @return the paginator height
     */
    @Positive
    @Override
    public int height() {
        return this.height;
    }

    /**
     * Creates a new Paginator builder instance.
     *
     * @return a new Paginator.Builder for constructing paginators
     */
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * Builder class for constructing Paginator instances with a fluent interface.
     */
    public static class Builder extends MenuComponent.Builder<Builder> {
        private final ObjectList<MenuComponent> components = new ObjectArrayList<>();

        private Function<MenuContext, ItemStack> backItem = context -> new ItemStack(Material.ARROW);
        private Function<MenuContext, ItemStack> nextItem = context -> new ItemStack(Material.ARROW);

        @Nullable
        private Function<MenuContext, ItemStack> firstPageItem, lastPageItem;
        @Nullable
        private Function<MenuContext, ItemStack> offBackItem, offNextItem, offFirstPageItem, offLastPageItem;

        private int page;

        private int width = 1;
        private int height = 1;

        /**
         * Adds a component to the paginator.
         *
         * @param context   menu context
         * @param component the component to add
         * @return this builder for method chaining
         * @throws NullPointerException if component is null
         */
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder add(MenuContext context, MenuComponent component) {
            Preconditions.checkNotNull(context, "context cannot be null");
            Preconditions.checkNotNull(component, "component cannot be null");

            this.components.add(component);
            return this;
        }

        /**
         * Adds multiple components to the paginator.
         *
         * @param context   menu context
         * @param components the list of components to add
         * @return this builder for method chaining
         * @throws NullPointerException if components is null
         */
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder addAll(MenuContext context, ObjectList<MenuComponent> components) {
            Preconditions.checkNotNull(context, "context cannot be null");
            Preconditions.checkNotNull(components, "components cannot be null");

            for (MenuComponent component : components)
                this.add(context, component);
            return this;
        }

        /**
         * Sets the ItemStack for the enabled back button.
         *
         * @param backItem the ItemStack for the back button
         * @return this builder for method chaining
         * @throws NullPointerException if backItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder backItem(ItemStack backItem) {
            Preconditions.checkNotNull(backItem, "backItem cannot be null");

            this.backItem = context -> backItem;
            return this;
        }

        /**
         * Sets the ItemStack for the enabled next button.
         *
         * @param nextItem the ItemStack for the next button
         * @return this builder for method chaining
         * @throws NullPointerException if nextItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder nextItem(ItemStack nextItem) {
            Preconditions.checkNotNull(nextItem, "nextItem cannot be null");

            this.nextItem = context -> nextItem;
            return this;
        }

        /**
         * Sets the ItemStack for the disabled back button.
         *
         * @param offBackItem the ItemStack for the disabled back button
         * @return this builder for method chaining
         * @throws NullPointerException if offBackItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offBackItem(ItemStack offBackItem) {
            Preconditions.checkNotNull(offBackItem, "offBackItem cannot be null");

            this.offBackItem = context -> offBackItem;
            return this;
        }

        /**
         * Sets the ItemStack for the disabled next button.
         *
         * @param offNextItem the ItemStack for the disabled next button
         * @return this builder for method chaining
         * @throws NullPointerException if offNextItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offNextItem(ItemStack offNextItem) {
            Preconditions.checkNotNull(offNextItem, "offNextItem cannot be null");

            this.offNextItem = context -> offNextItem;
            return this;
        }

        /**
         * Sets the ItemStack for the enabled first page button.
         *
         * @param firstPageItem the ItemStack for the first page button
         * @return this builder for method chaining
         * @throws NullPointerException if firstPageItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder firstPageItem(ItemStack firstPageItem) {
            Preconditions.checkNotNull(firstPageItem, "firstPageItem cannot be null");

            this.firstPageItem = context -> firstPageItem;
            return this;
        }

        /**
         * Sets the ItemStack for the enabled last page button.
         *
         * @param lastPageItem the ItemStack for the last page button
         * @return this builder for method chaining
         * @throws NullPointerException if lastPageItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder lastPageItem(ItemStack lastPageItem) {
            Preconditions.checkNotNull(lastPageItem, "lastPageItem cannot be null");

            this.lastPageItem = context -> lastPageItem;
            return this;
        }

        /**
         * Sets the ItemStack for the disabled first page button.
         *
         * @param offFirstPageItem the ItemStack for the disabled first page button
         * @return this builder for method chaining
         * @throws NullPointerException if offFirstPageItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offFirstPageItem(ItemStack offFirstPageItem) {
            Preconditions.checkNotNull(offFirstPageItem, "offFirstPageItem cannot be null");

            this.offFirstPageItem = context -> offFirstPageItem;
            return this;
        }

        /**
         * Sets the ItemStack for the disabled last page button.
         *
         * @param offLastPageItem the ItemStack for the disabled last page button
         * @return this builder for method chaining
         * @throws NullPointerException if offLastPageItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offLastPageItem(ItemStack offLastPageItem) {
            Preconditions.checkNotNull(offLastPageItem, "offLastPageItem cannot be null");

            this.offLastPageItem = context -> offLastPageItem;
            return this;
        }

        /**
         * Sets a function to provide the enabled back button ItemStack.
         *
         * @param backItem function that returns the back button ItemStack
         * @return this builder for method chaining
         * @throws NullPointerException if backItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder backItem(Function<MenuContext, ItemStack> backItem) {
            Preconditions.checkNotNull(backItem, "backItem cannot be null");

            this.backItem = backItem;
            return this;
        }

        /**
         * Sets a function to provide the enabled next button ItemStack.
         *
         * @param nextItem function that returns the next button ItemStack
         * @return this builder for method chaining
         * @throws NullPointerException if nextItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder nextItem(Function<MenuContext, ItemStack> nextItem) {
            Preconditions.checkNotNull(nextItem, "nextItem cannot be null");

            this.nextItem = nextItem;
            return this;
        }

        /**
         * Sets a function to provide the disabled back button ItemStack.
         *
         * @param offBackItem function that returns the disabled back button ItemStack
         * @return this builder for method chaining
         * @throws NullPointerException if offBackItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offBackItem(Function<MenuContext, ItemStack> offBackItem) {
            Preconditions.checkNotNull(offBackItem, "offBackItem cannot be null");

            this.offBackItem = offBackItem;
            return this;
        }

        /**
         * Sets a function to provide the disabled next button ItemStack.
         *
         * @param offNextItem function that returns the disabled next button ItemStack
         * @return this builder for method chaining
         * @throws NullPointerException if offNextItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offNextItem(Function<MenuContext, ItemStack> offNextItem) {
            Preconditions.checkNotNull(offNextItem, "offNextItem cannot be null");

            this.offNextItem = offNextItem;
            return this;
        }

        /**
         * Sets a function to provide the enabled first page button ItemStack.
         *
         * @param firstPageItem function that returns the first page button ItemStack
         * @return this builder for method chaining
         * @throws NullPointerException if firstPageItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder firstPageItem(Function<MenuContext, ItemStack> firstPageItem) {
            Preconditions.checkNotNull(firstPageItem, "firstPageItem cannot be null");

            this.firstPageItem = firstPageItem;
            return this;
        }

        /**
         * Sets a function to provide the enabled last page button ItemStack.
         *
         * @param lastPageItem function that returns the last page button ItemStack
         * @return this builder for method chaining
         * @throws NullPointerException if lastPageItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder lastPageItem(Function<MenuContext, ItemStack> lastPageItem) {
            Preconditions.checkNotNull(lastPageItem, "lastPageItem cannot be null");

            this.lastPageItem = lastPageItem;
            return this;
        }

        /**
         * Sets a function to provide the disabled first page button ItemStack.
         *
         * @param offFirstPageItem function that returns the disabled first page button ItemStack
         * @return this builder for method chaining
         * @throws NullPointerException if offFirstPageItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offFirstPageItem(Function<MenuContext, ItemStack> offFirstPageItem) {
            Preconditions.checkNotNull(offFirstPageItem, "offFirstPageItem cannot be null");

            this.offFirstPageItem = offFirstPageItem;
            return this;
        }

        /**
         * Sets a function to provide the disabled last page button ItemStack.
         *
         * @param offLastPageItem function that returns the disabled last page button ItemStack
         * @return this builder for method chaining
         * @throws NullPointerException if offLastPageItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offLastPageItem(Function<MenuContext, ItemStack> offLastPageItem) {
            Preconditions.checkNotNull(offLastPageItem, "offLastPageItem cannot be null");

            this.offLastPageItem = offLastPageItem;
            return this;
        }

        /**
         * Sets the initial page index for the paginator.
         *
         * @param page the initial page index (0-based)
         * @return this builder for method chaining
         * @throws IllegalArgumentException if page is negative
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder page(@NonNegative int page) {
            Preconditions.checkArgument(page >= 0, "page cannot be less than 0: %s", page);

            this.page = page;
            return this;
        }

        /**
         * Sets the width of the paginator in slots.
         *
         * @param width the width in slots (must be positive)
         * @return this builder for method chaining
         * @throws IllegalArgumentException if width is less than 1
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder width(@Positive int width) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %s", width);

            this.width = width;
            return this;
        }

        /**
         * Sets the height of the paginator in rows.
         *
         * @param height the height in rows (must be positive)
         * @return this builder for method chaining
         * @throws IllegalArgumentException if height is less than 1
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder height(@Positive int height) {
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %s", height);

            this.height = height;
            return this;
        }

        /**
         * Sets both width and height of the paginator.
         *
         * @param width  the width in slots (must be positive)
         * @param height the height in rows (must be positive)
         * @return this builder for method chaining
         * @throws IllegalArgumentException if width or height is less than 1
         */
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder size(@Positive int width, @Positive int height) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %s", width);
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %s", height);

            this.width = width;
            this.height = height;
            return this;
        }

        /**
         * Builds and returns the configured Paginator instance.
         *
         * @return a new Paginator with the specified configuration
         */
        public Paginator build() {
            return new Paginator(this);
        }
    }
}