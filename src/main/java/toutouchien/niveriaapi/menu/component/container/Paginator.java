package toutouchien.niveriaapi.menu.component.container;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

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
public class Paginator extends Component {
    private final ObjectList<Component> components;

    private final Function<MenuContext, ItemStack> backItem, nextItem, offBackItem, offNextItem;
    private final Function<MenuContext, ItemStack> firstPageItem, lastPageItem, offFirstPageItem, offLastPageItem;

    private final int width, height;

    private int page;
    private ObjectList<Component> cachedPageComponents;

    /**
     * Constructs a new Paginator with the specified parameters.
     *
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
            ObjectList<Component> components,
            Function<MenuContext, ItemStack> backItem, Function<MenuContext, ItemStack> nextItem,
            Function<MenuContext, ItemStack> offBackItem, Function<MenuContext, ItemStack> offNextItem,
            Function<MenuContext, ItemStack> firstPageItem, Function<MenuContext, ItemStack> lastPageItem,
            Function<MenuContext, ItemStack> offFirstPageItem, Function<MenuContext, ItemStack> offLastPageItem,
            int width, int height,
            int page
    ) {
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
    }

    /**
     * Handles click events within the paginator.
     * <p>
     * This method delegates the click event to the appropriate child component
     * based on the clicked slot. If the paginator is not interactable, the event
     * is ignored.
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    @Override
    public void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context) {
        if (!this.interactable())
            return;

        int componentIndex = 0;
        for (Component component : this.currentPageComponents()) {
            int localX = (componentIndex % this.width);
            int localY = 1 + (componentIndex / this.width);

            int compX = this.x() + localX;
            int compY = this.y() + localY;

            int baseSlot = (compY - 1) * 9 + compX;

            Int2ObjectMap<ItemStack> compItems = component.items(context);
            for (Int2ObjectMap.Entry<ItemStack> entry : compItems.int2ObjectEntrySet()) {
                int innerSlot = entry.getIntKey();
                if (event.getSlot() == baseSlot + innerSlot) {
                    component.onClick(event, context);
                    return;
                }
            }

            componentIndex++;
        }
    }

    /**
     * Returns the items to be displayed by this paginator for the current page.
     * <p>
     * Only components visible on the current page are rendered. Components are
     * positioned within the paginator's area based on their index within the page.
     *
     * @param context the menu context
     * @return a map from slot indices to ItemStacks for the current page
     */
    @NotNull
    @Override
    public Int2ObjectMap<ItemStack> items(@NotNull MenuContext context) {
        Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();

        int componentIndex = 0;
        for (Component component : this.currentPageComponents()) {
            int localX = (componentIndex % this.width);
            int localY = 1 + (componentIndex / this.width);

            int compX = this.x() + localX;
            int compY = this.y() + localY;

            int baseSlot = (compY - 1) * 9 + compX;

            Int2ObjectMap<ItemStack> compItems = component.items(context);
            for (Int2ObjectMap.Entry<ItemStack> entry : compItems.int2ObjectEntrySet()) {
                int innerSlot = entry.getIntKey();
                ItemStack item = entry.getValue();
                items.put(baseSlot + innerSlot, item);
            }

            componentIndex++;
        }

        return items;
    }

    /**
     * Returns the set of slots that this paginator can occupy.
     * <p>
     * Returns all possible slots for the current page dimensions to ensure
     * proper cleanup when switching between pages with different content.
     *
     * @param context the menu context
     * @return a set of all possible slot indices for this paginator
     */
    @NotNull
    @Override
    public IntSet slots(@NotNull MenuContext context) {
        IntSet slots = new IntOpenHashSet();

        // Instead of only adding the slots of the current page, we add all the possible slots on the current page
        // If we don't do this, the menu system will not remove old items when switching pages
        for (int yOffset = 0; yOffset < this.height; yOffset++) {
            for (int xOffset = 0; xOffset < this.width; xOffset++) {
                int compX = this.x() + xOffset;
                int compY = this.y() + 1 + yOffset; // Account for the 1 + offset in localY
                slots.add((compY - 1) * 9 + compX);
            }
        }

        return slots;
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
    private ObjectList<Component> currentPageComponents() {
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
     * page and no disabled item is configured, returns null.
     *
     * @return a Button for going to the previous page, or null if not applicable
     */
    @Nullable
    public Button backButton() {
        if (this.page <= 0 && this.offBackItem == null)
            return null;

        return Button.create()
                .item(context -> {
                    return this.page > 0
                            ? this.backItem.apply(context)
                            : this.offBackItem.apply(context);
                })
                .onClick(event -> {
                    if (this.page <= 0)
                        return;

                    this.page--;
                    this.invalidateCache();
                    this.render(event.context());
                })
                .build();
    }

    /**
     * Creates a next navigation button for this paginator.
     * <p>
     * The button navigates to the next page when clicked. If already on the last
     * page and no disabled item is configured, returns null.
     *
     * @return a Button for going to the next page, or null if not applicable
     */
    @Nullable
    public Button nextButton() {
        int maxPage = this.maxPage();
        if (this.page >= maxPage && this.offNextItem == null)
            return null;

        return Button.create()
                .item(context -> {
                    return this.page < maxPage
                            ? this.nextItem.apply(context)
                            : this.offNextItem.apply(context);
                })
                .onClick(event -> {
                    if (this.page >= maxPage)
                        return;

                    this.page++;
                    this.invalidateCache();
                    this.render(event.context());
                })
                .build();
    }

    /**
     * Creates a first page navigation button for this paginator.
     * <p>
     * The button navigates to the first page (index 0) when clicked. If already on
     * the first page and no disabled item is configured, returns null.
     *
     * @return a Button for going to the first page, or null if not applicable
     */
    @Nullable
    public Button firstPageButton() {
        if (this.page <= 0 && this.offFirstPageItem == null)
            return null;

        return Button.create()
                .item(context -> {
                    return this.page > 0
                            ? this.firstPageItem.apply(context)
                            : this.offFirstPageItem.apply(context);
                })
                .onClick(event -> {
                    if (this.page <= 0)
                        return;

                    this.page = 0;
                    this.invalidateCache();
                    this.render(event.context());
                })
                .build();
    }

    /**
     * Creates a last page navigation button for this paginator.
     * <p>
     * The button navigates to the last page when clicked. If already on the last
     * page and no disabled item is configured, returns null.
     *
     * @return a Button for going to the last page, or null if not applicable
     */
    @Nullable
    public Button lastPageButton() {
        int maxPage = this.maxPage();
        if (this.page >= maxPage && this.offLastPageItem == null)
            return null;

        return Button.create()
                .item(context -> {
                    return this.page < maxPage
                            ? this.lastPageItem.apply(context)
                            : this.offLastPageItem.apply(context);
                })
                .onClick(event -> {
                    if (this.page >= maxPage)
                        return;

                    this.page = maxPage;
                    this.invalidateCache();
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
    public static class Builder {
        private final ObjectList<Component> components = new ObjectArrayList<>();

        private Function<MenuContext, ItemStack> backItem = context -> new ItemStack(Material.ARROW);
        private Function<MenuContext, ItemStack> nextItem = context -> new ItemStack(Material.ARROW);
        private Function<MenuContext, ItemStack> offBackItem, offNextItem;

        private Function<MenuContext, ItemStack> firstPageItem, lastPageItem;
        private Function<MenuContext, ItemStack> offFirstPageItem, offLastPageItem;

        private int page;

        private int width, height;

        /**
         * Adds a component to the paginator.
         *
         * @param component the component to add
         * @return this builder for method chaining
         * @throws NullPointerException if component is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder add(@NotNull Component component) {
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
        public Builder addAll(@NotNull ObjectList<Component> components) {
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