package toutouchien.niveriaapi.menu.component.container;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;

public class Paginator extends Component {
    private final ObjectList<Component> components;

    private final Object2ObjectFunction<MenuContext, ItemStack> backItem, nextItem, offBackItem, offNextItem;
    private final Object2ObjectFunction<MenuContext, ItemStack> firstPageItem, lastPageItem, offFirstPageItem, offLastPageItem;

    private final int width, height;

    private int page;

    public Paginator(
            ObjectList<Component> components,
            Object2ObjectFunction<MenuContext, ItemStack> backItem, Object2ObjectFunction<MenuContext, ItemStack> nextItem,
            Object2ObjectFunction<MenuContext, ItemStack> offBackItem, Object2ObjectFunction<MenuContext, ItemStack> offNextItem,
            Object2ObjectFunction<MenuContext, ItemStack> firstPageItem, Object2ObjectFunction<MenuContext, ItemStack> lastPageItem,
            Object2ObjectFunction<MenuContext, ItemStack> offFirstPageItem, Object2ObjectFunction<MenuContext, ItemStack> offLastPageItem,
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
        return null;
    }

    @NotNull
    @Override
    public IntSet slots(@NotNull MenuContext context) {
        IntSet slots = new IntOpenHashSet();
        for (Component component : this.components) {
            IntSet compSlots = component.slots(context);
            slots.addAll(compSlots);
        }

        return slots;
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

        private Object2ObjectFunction<MenuContext, ItemStack> backItem = context -> new ItemStack(Material.ARROW);
        private Object2ObjectFunction<MenuContext, ItemStack> nextItem = context -> new ItemStack(Material.ARROW);
        private Object2ObjectFunction<MenuContext, ItemStack> offBackItem, offNextItem;

        private Object2ObjectFunction<MenuContext, ItemStack> firstPageItem, lastPageItem;
        private Object2ObjectFunction<MenuContext, ItemStack> offFirstPageItem, offLastPageItem;

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
        public Builder backItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> backItem) {
            Preconditions.checkNotNull(backItem, "backItem cannot be null");

            this.backItem = backItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder nextItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> nextItem) {
            Preconditions.checkNotNull(nextItem, "nextItem cannot be null");

            this.nextItem = nextItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offBackItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> offBackItem) {
            Preconditions.checkNotNull(offBackItem, "offBackItem cannot be null");

            this.offBackItem = offBackItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offNextItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> offNextItem) {
            Preconditions.checkNotNull(offNextItem, "offNextItem cannot be null");

            this.offNextItem = offNextItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder firstPageItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> firstPageItem) {
            Preconditions.checkNotNull(firstPageItem, "firstPageItem cannot be null");

            this.firstPageItem = firstPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder lastPageItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> lastPageItem) {
            Preconditions.checkNotNull(lastPageItem, "lastPageItem cannot be null");

            this.lastPageItem = lastPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offFirstPageItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> offFirstPageItem) {
            Preconditions.checkNotNull(offFirstPageItem, "offFirstPageItem cannot be null");

            this.offFirstPageItem = offFirstPageItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offLastPageItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> offLastPageItem) {
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
