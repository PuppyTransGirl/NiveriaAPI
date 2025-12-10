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

import java.util.function.Function;

public class Paginator extends Component {
    private final ObjectList<Component> components;

    private final Function<MenuContext, ItemStack> backItem, nextItem, offBackItem, offNextItem;
    private final Function<MenuContext, ItemStack> firstPageItem, lastPageItem, offFirstPageItem, offLastPageItem;

    private final int width, height;

    private int page;

    public Paginator(
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

    @NotNull
    @Override
    public Int2ObjectMap<ItemStack> items(@NotNull MenuContext context) {
        Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();

        int maxItemsPerPage = this.width * this.height;
        int totalItems = this.components.size();
        int startIndex = this.page * maxItemsPerPage;
        int endIndex = Math.min(startIndex + maxItemsPerPage, totalItems);

        for (int i = startIndex; i < endIndex; i++) {
            int relativeIndex = i - startIndex;
            int localX = (relativeIndex % this.width);
            int localY = 1 + (relativeIndex / this.width);

            int compX = this.x() + localX;
            int compY = this.y() + localY;

            int baseSlot = (compY - 1) * 9 + compX;

            Component component = this.components.get(i);
            Int2ObjectMap<ItemStack> compItems = component.items(context);
            for (Int2ObjectMap.Entry<ItemStack> entry : compItems.int2ObjectEntrySet()) {
                int innerSlot = entry.getIntKey();
                ItemStack item = entry.getValue();
                items.put(baseSlot + innerSlot, item);
            }
        }

        return items;
    }

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
                    this.render(event.context());
                })
                .build();
    }

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
                    this.render(event.context());
                })
                .build();
    }

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
                    this.render(event.context());
                })
                .build();
    }

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
                    this.render(event.context());
                })
                .build();
    }

    public int maxPage() {
        int maxItemsPerPage = this.width * this.height;
        int totalItems = this.components.size();
        return (int) Math.ceil((double) totalItems / maxItemsPerPage) - 1;
    }

    @Positive
    @Override
    public int width() {
        return this.width;
    }

    @Positive
    @Override
    public int height() {
        return this.height;
    }

    @NotNull
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private final ObjectList<Component> components = new ObjectArrayList<>();

        private Function<MenuContext, ItemStack> backItem = context -> new ItemStack(Material.ARROW);
        private Function<MenuContext, ItemStack> nextItem = context -> new ItemStack(Material.ARROW);
        private Function<MenuContext, ItemStack> offBackItem, offNextItem;

        private Function<MenuContext, ItemStack> firstPageItem, lastPageItem;
        private Function<MenuContext, ItemStack> offFirstPageItem, offLastPageItem;

        private int page;

        private int width, height;

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder add(@NotNull Component component) {
            Preconditions.checkNotNull(component, "component cannot be null");

            this.components.add(component);
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder addAll(@NotNull ObjectList<Component> components) {
            Preconditions.checkNotNull(components, "components cannot be null");

            this.components.addAll(components);
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder backItem(@NotNull ItemStack backItem) {
            Preconditions.checkNotNull(backItem, "backItem cannot be null");

            this.backItem = context -> backItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder nextItem(@NotNull ItemStack nextItem) {
            Preconditions.checkNotNull(nextItem, "nextItem cannot be null");

            this.nextItem = context -> nextItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offBackItem(@NotNull ItemStack offBackItem) {
            Preconditions.checkNotNull(offBackItem, "offBackItem cannot be null");

            this.offBackItem = context -> offBackItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offNextItem(@NotNull ItemStack offNextItem) {
            Preconditions.checkNotNull(offNextItem, "offNextItem cannot be null");

            this.offNextItem = context -> offNextItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder firstPageItem(@NotNull ItemStack firstPageItem) {
            Preconditions.checkNotNull(firstPageItem, "firstPageItem cannot be null");

            this.firstPageItem = context -> firstPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder lastPageItem(@NotNull ItemStack lastPageItem) {
            Preconditions.checkNotNull(lastPageItem, "lastPageItem cannot be null");

            this.lastPageItem = context -> lastPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offFirstPageItem(@NotNull ItemStack offFirstPageItem) {
            Preconditions.checkNotNull(offFirstPageItem, "offFirstPageItem cannot be null");

            this.offFirstPageItem = context -> offFirstPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offLastPageItem(@NotNull ItemStack offLastPageItem) {
            Preconditions.checkNotNull(offLastPageItem, "offLastPageItem cannot be null");

            this.offLastPageItem = context -> offLastPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder backItem(@NotNull Function<MenuContext, ItemStack> backItem) {
            Preconditions.checkNotNull(backItem, "backItem cannot be null");

            this.backItem = backItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder nextItem(@NotNull Function<MenuContext, ItemStack> nextItem) {
            Preconditions.checkNotNull(nextItem, "nextItem cannot be null");

            this.nextItem = nextItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offBackItem(@NotNull Function<MenuContext, ItemStack> offBackItem) {
            Preconditions.checkNotNull(offBackItem, "offBackItem cannot be null");

            this.offBackItem = offBackItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offNextItem(@NotNull Function<MenuContext, ItemStack> offNextItem) {
            Preconditions.checkNotNull(offNextItem, "offNextItem cannot be null");

            this.offNextItem = offNextItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder firstPageItem(@NotNull Function<MenuContext, ItemStack> firstPageItem) {
            Preconditions.checkNotNull(firstPageItem, "firstPageItem cannot be null");

            this.firstPageItem = firstPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder lastPageItem(@NotNull Function<MenuContext, ItemStack> lastPageItem) {
            Preconditions.checkNotNull(lastPageItem, "lastPageItem cannot be null");

            this.lastPageItem = lastPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offFirstPageItem(@NotNull Function<MenuContext, ItemStack> offFirstPageItem) {
            Preconditions.checkNotNull(offFirstPageItem, "offFirstPageItem cannot be null");

            this.offFirstPageItem = offFirstPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offLastPageItem(@NotNull Function<MenuContext, ItemStack> offLastPageItem) {
            Preconditions.checkNotNull(offLastPageItem, "offLastPageItem cannot be null");

            this.offLastPageItem = offLastPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder page(@NonNegative int page) {
            Preconditions.checkArgument(page >= 0, "page cannot be less than 0: %s", page);

            this.page = page;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder width(@Positive int width) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %s", width);

            this.width = width;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder height(@Positive int height) {
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %s", height);

            this.height = height;
            return this;
        }

        @NotNull
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder size(@Positive int width, @Positive int height) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %s", width);
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %s", height);

            this.width = width;
            this.height = height;
            return this;
        }

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
