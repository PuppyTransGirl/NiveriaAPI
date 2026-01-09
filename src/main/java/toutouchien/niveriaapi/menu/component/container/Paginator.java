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
import org.jetbrains.annotations.NotNull;
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
public class Paginator extends MenuComponent {
    private final ObjectList<MenuComponent> components;

    private Function<MenuContext, ItemStack> backItem, nextItem, offBackItem, offNextItem;
    private Function<MenuContext, ItemStack> firstPageItem, lastPageItem, offFirstPageItem, offLastPageItem;

    private final int width, height;
    private final IntList layoutSlots;

    private int page;
    private ObjectList<MenuComponent> cachedPageComponents;

    /**
     * Constructs a new Paginator with the specified parameters.
     *
     * @param id               the unique identifier for this paginator
     * @param components       the list of components to paginate
     * @param backItem         function providing the back button item when enabled
     * @param nextItem         function providing the next button item when enabled
     * @param offBackItem      function providing the back button item when disabled
     * @param offNextItem      function providing the next button item when disabled
     * @param firstPageItem    function providing the first page button item when enabled
     * @param lastPageItem     function providing the last page button item when enabled
     * @param offFirstPageItem function providing the first page button item when disabled
     * @param offLastPageItem  function providing the last page button item when disabled
     * @param width            the width of each page in slots
     * @param height           the height of each page in rows
     * @param page             the initial page index (0-based)
     */
    private Paginator(
            String id,
            ObjectList<MenuComponent> components,
            Function<MenuContext, ItemStack> backItem, Function<MenuContext, ItemStack> nextItem,
            Function<MenuContext, ItemStack> offBackItem, Function<MenuContext, ItemStack> offNextItem,
            Function<MenuContext, ItemStack> firstPageItem, Function<MenuContext, ItemStack> lastPageItem,
            Function<MenuContext, ItemStack> offFirstPageItem, Function<MenuContext, ItemStack> offLastPageItem,
            int width, int height,
            int page
    ) {
        super(id);
        this.components = components;
        this.backItem = backItem;
        this.nextItem = nextItem;
        this.offBackItem = offBackItem;
        this.offNextItem = offNextItem;
        this.firstPageItem = firstPageItem;
        this.lastPageItem = lastPageItem;
        this.offFirstPageItem = offFirstPageItem;
        this.offLastPageItem = offLastPageItem;
        this.width = width;
        this.height = height;
        this.page = page;
        this.layoutSlots = new IntArrayList(width * height);

        // Initial calculation of layout slots
        this.updateLayoutSlots();
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
    public void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context) {
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
    @NotNull
    @Override
    public Int2ObjectMap<ItemStack> items(@NotNull MenuContext context) {
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
    @NotNull
    @Override
    public IntSet slots(@NotNull MenuContext context) {
        // Return all slots controlled by the paginator grid
        return new IntOpenHashSet(this.layoutSlots);
    }

    @Override
    public void render(@NotNull MenuContext context) {
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
    @NotNull
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
    @NotNull
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
    @NotNull
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
    @NotNull
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
    @NotNull
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
     * @param component the component to add
     * @return this paginator for method chaining
     * @throws NullPointerException if component is null
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator add(@NotNull MenuComponent component) {
        Preconditions.checkNotNull(component, "component cannot be null");

        this.components.add(component);
        return this;
    }

    /**
     * Adds multiple components to the paginator.
     *
     * @param components the list of components to add
     * @return this paginator for method chaining
     * @throws NullPointerException if components is null
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator addAll(@NotNull ObjectList<MenuComponent> components) {
        Preconditions.checkNotNull(components, "components cannot be null");

        this.components.addAll(components);
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
    @NotNull
    @Contract(value = "_, _ -> this", mutates = "this")
    public Paginator remove(@NotNull MenuContext context, int slot) {
        ObjectList<MenuComponent> pageComponents = this.currentPageComponents();
        for (int i = 0; i < pageComponents.size(); i++) {
            if (i >= this.layoutSlots.size()) break;

            MenuComponent component = pageComponents.get(i);
            int targetSlot = this.layoutSlots.getInt(i);

            // Temporarily position to check slots
            component.position(MenuComponent.toX(targetSlot), MenuComponent.toY(targetSlot));

            if (component.slots(context).contains(slot)) {
                this.components.remove(component);
                return this;
            }
        }
        return this;
    }

    /**
     * Removes a specific component from the paginator.
     *
     * @param component the component to remove
     * @return this paginator for method chaining
     * @throws NullPointerException if component is null
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator remove(@NotNull MenuComponent component) {
        Preconditions.checkNotNull(component, "component cannot be null");

        this.components.remove(component);
        return this;
    }

    /**
     * Removes multiple components at the specified indexes from the paginator.
     *
     * @param indexes the set of indexes of components to remove
     * @return this paginator for method chaining
     * @throws NullPointerException if indexes is null
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator removeAll(@NotNull IntSet indexes) {
        Preconditions.checkNotNull(indexes, "indexes cannot be null");

        int[] sorted = indexes.toIntArray();
        Arrays.sort(sorted);
        for (int i = sorted.length - 1; i >= 0; i--)
            this.components.remove(sorted[i]);

        return this;
    }

    /**
     * Removes multiple components from the paginator.
     *
     * @param components the set of components to remove
     * @return this paginator for method chaining
     * @throws NullPointerException if components is null
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator removeAll(@NotNull ObjectSet<MenuComponent> components) {
        Preconditions.checkNotNull(components, "components cannot be null");

        this.components.removeAll(components);
        return this;
    }

    /**
     * Clears all components from the paginator.
     *
     * @return this paginator for method chaining
     */
    @NotNull
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator backItem(@NotNull ItemStack backItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator nextItem(@NotNull ItemStack nextItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offBackItem(@NotNull ItemStack offBackItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offNextItem(@NotNull ItemStack offNextItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator firstPageItem(@NotNull ItemStack firstPageItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator lastPageItem(@NotNull ItemStack lastPageItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offFirstPageItem(@NotNull ItemStack offFirstPageItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offLastPageItem(@NotNull ItemStack offLastPageItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator backItem(@NotNull Function<MenuContext, ItemStack> backItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator nextItem(@NotNull Function<MenuContext, ItemStack> nextItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offBackItem(@NotNull Function<MenuContext, ItemStack> offBackItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offNextItem(@NotNull Function<MenuContext, ItemStack> offNextItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator firstPageItem(@NotNull Function<MenuContext, ItemStack> firstPageItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator lastPageItem(@NotNull Function<MenuContext, ItemStack> lastPageItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offFirstPageItem(@NotNull Function<MenuContext, ItemStack> offFirstPageItem) {
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
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Paginator offLastPageItem(@NotNull Function<MenuContext, ItemStack> offLastPageItem) {
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
    @NotNull
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
    @NotNull
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * Builder class for constructing Paginator instances with a fluent interface.
     */
    public static class Builder extends MenuComponent.Builder {
        private final ObjectList<MenuComponent> components = new ObjectArrayList<>();

        private Function<MenuContext, ItemStack> backItem = context -> new ItemStack(Material.ARROW);
        private Function<MenuContext, ItemStack> nextItem = context -> new ItemStack(Material.ARROW);
        private Function<MenuContext, ItemStack> offBackItem, offNextItem;

        private Function<MenuContext, ItemStack> firstPageItem, lastPageItem;
        private Function<MenuContext, ItemStack> offFirstPageItem, offLastPageItem;

        private int page;

        private int width = 1;
        private int height = 1;

        /**
         * Adds a component to the paginator.
         *
         * @param component the component to add
         * @return this builder for method chaining
         * @throws NullPointerException if component is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder add(@NotNull MenuComponent component) {
            Preconditions.checkNotNull(component, "component cannot be null");

            this.components.add(component);
            return this;
        }

        /**
         * Adds multiple components to the paginator.
         *
         * @param components the list of components to add
         * @return this builder for method chaining
         * @throws NullPointerException if components is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder addAll(@NotNull ObjectList<MenuComponent> components) {
            Preconditions.checkNotNull(components, "components cannot be null");

            this.components.addAll(components);
            return this;
        }

        /**
         * Sets the ItemStack for the enabled back button.
         *
         * @param backItem the ItemStack for the back button
         * @return this builder for method chaining
         * @throws NullPointerException if backItem is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder backItem(@NotNull ItemStack backItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder nextItem(@NotNull ItemStack nextItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offBackItem(@NotNull ItemStack offBackItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offNextItem(@NotNull ItemStack offNextItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder firstPageItem(@NotNull ItemStack firstPageItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder lastPageItem(@NotNull ItemStack lastPageItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offFirstPageItem(@NotNull ItemStack offFirstPageItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offLastPageItem(@NotNull ItemStack offLastPageItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder backItem(@NotNull Function<MenuContext, ItemStack> backItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder nextItem(@NotNull Function<MenuContext, ItemStack> nextItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offBackItem(@NotNull Function<MenuContext, ItemStack> offBackItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offNextItem(@NotNull Function<MenuContext, ItemStack> offNextItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder firstPageItem(@NotNull Function<MenuContext, ItemStack> firstPageItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder lastPageItem(@NotNull Function<MenuContext, ItemStack> lastPageItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offFirstPageItem(@NotNull Function<MenuContext, ItemStack> offFirstPageItem) {
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
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offLastPageItem(@NotNull Function<MenuContext, ItemStack> offLastPageItem) {
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
        @NotNull
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
        @NotNull
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
        @NotNull
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
        @NotNull
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
        @NotNull
        public Paginator build() {
            return new Paginator(
                    this.id,
                    this.components,
                    this.backItem,
                    this.nextItem,
                    this.offBackItem,
                    this.offNextItem,
                    this.firstPageItem,
                    this.lastPageItem,
                    this.offFirstPageItem,
                    this.offLastPageItem,
                    this.width,
                    this.height,
                    this.page
            );
        }
    }
}