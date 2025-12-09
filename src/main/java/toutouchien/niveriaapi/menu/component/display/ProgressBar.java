package toutouchien.niveriaapi.menu.component.display;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2DoubleFunction;
import it.unimi.dsi.fastutil.objects.Object2ObjectFunction;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.utils.Direction;

import java.util.function.BiConsumer;

public class ProgressBar extends Component {
    private final Object2ObjectFunction<MenuContext, ItemStack> doneItem, currentItem, notDoneItem;

    private final Direction.Default direction;

    private final Object2DoubleFunction<MenuContext> percentage;

    private final int width;
    private final int height;

    private ProgressBar(
            Object2ObjectFunction<MenuContext, ItemStack> doneItem, Object2ObjectFunction<MenuContext, ItemStack> currentItem, Object2ObjectFunction<MenuContext, ItemStack> notDoneItem,
            Direction.Default direction,
            Object2DoubleFunction<MenuContext> percentage,
            int width, int height
    ) {
        this.doneItem = doneItem;
        this.currentItem = currentItem;
        this.notDoneItem = notDoneItem;
        this.direction = direction;
        this.percentage = percentage;
        this.width = width;
        this.height = height;
    }

    @NotNull
    @Override
    public Int2ObjectMap<ItemStack> items(@NotNull MenuContext context) {
        Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>(this.width * this.height);
        if (!this.visible())
            return items;


        double pct = Math.clamp(this.percentage.applyAsDouble(context), 0, 1);

        int total = this.width * this.height;
        int done = (int) Math.floor(pct * total);
        boolean full = pct >= 1d || done >= total;

        Int2ObjectFunction<ItemStack> pick = index -> {
            if (index < done)
                return this.doneItem.apply(context);

            if (!full && index == done)
                return this.currentItem.apply(context);

            return this.notDoneItem.apply(context);
        };

        this.forEachSlot((idx, slot) -> items.put(slot.intValue(), pick.apply(idx)));
        return items;
    }

    private void forEachSlot(BiConsumer<Integer, Integer> consumer) {
        Traversal t = this.traversal();
        int baseSlot = this.slot();
        int rowLength = 9;
        int idx = 0;

        Range outer = t.rowMajor() ? t.rows() : t.cols();
        Range inner = t.rowMajor() ? t.cols() : t.rows();
        boolean outerIsRow = t.rowMajor();

        for (int o = outer.start; o != outer.endExclusive; o += outer.step) {
            for (int i = inner.start; i != inner.endExclusive; i += inner.step) {
                int row = outerIsRow ? o : i;
                int col = outerIsRow ? i : o;
                consumer.accept(idx++, baseSlot + col + (row * rowLength));
            }
        }
    }

    private Traversal traversal() {
        Range rowsRange = new Range(0, this.height, 1);
        Range colsRange = new Range(0, this.width, 1);

        return switch (this.direction) {
            case RIGHT -> new Traversal(rowsRange, colsRange, true);
            case LEFT -> new Traversal(rowsRange, new Range(this.width - 1, -1, -1), true);
            case DOWN -> new Traversal(rowsRange, colsRange, false);
            case UP -> new Traversal(new Range(this.height - 1, -1, -1), colsRange, false);
        };
    }

    private record Range(@NonNegative int start, int endExclusive, int step) {

    }

    private record Traversal(@NotNull Range rows, @NotNull Range cols, boolean rowMajor) {

    }

    @NotNull
    @Override
    public IntSet slots(@NotNull MenuContext context) {
        IntSet slots = new IntOpenHashSet(this.width * this.height);
        if (!this.visible())
            return slots;

        int baseSlot = this.slot();
        int rowLength = 9;

        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                int slot = baseSlot + col + (row * rowLength);
                slots.add(slot);
            }
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
        private Object2ObjectFunction<MenuContext, ItemStack> doneItem = context -> ItemStack.of(Material.LIME_CONCRETE);
        private Object2ObjectFunction<MenuContext, ItemStack> currentItem = context -> ItemStack.of(Material.ORANGE_CONCRETE);
        private Object2ObjectFunction<MenuContext, ItemStack> notDoneItem = context -> ItemStack.of(Material.RED_CONCRETE);

        private Direction.Default direction = Direction.Default.RIGHT;

        private Object2DoubleFunction<MenuContext> percentage = context -> 0D;

        private int width = 1;
        private int height = 1;

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder doneItem(@NotNull ItemStack doneItem) {
            Preconditions.checkNotNull(doneItem, "doneItem cannot be null");

            this.doneItem = context -> doneItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder doneItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> doneItem) {
            Preconditions.checkNotNull(doneItem, "doneItem cannot be null");

            this.doneItem = doneItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder currentItem(@NotNull ItemStack currentItem) {
            Preconditions.checkNotNull(currentItem, "currentItem cannot be null");

            this.currentItem = context -> currentItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder currentItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> currentItem) {
            Preconditions.checkNotNull(currentItem, "currentItem cannot be null");

            this.currentItem = currentItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder notDoneItem(@NotNull ItemStack notDoneItem) {
            Preconditions.checkNotNull(notDoneItem, "notDoneItem cannot be null");

            this.notDoneItem = context -> notDoneItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder notDoneItem(@NotNull Object2ObjectFunction<MenuContext, ItemStack> notDoneItem) {
            Preconditions.checkNotNull(notDoneItem, "notDoneItem cannot be null");

            this.notDoneItem = notDoneItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder direction(@NotNull Direction.Default direction) {
            Preconditions.checkNotNull(direction, "direction cannot be null");

            this.direction = direction;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder percentage(@NonNegative double percentage) {
            Preconditions.checkArgument(percentage >= 0, "percentage cannot be negative: %s", percentage);

            this.percentage = context -> percentage;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder percentage(@NotNull Object2DoubleFunction<MenuContext> percentage) {
            Preconditions.checkNotNull(percentage, "percentage cannot be null");

            this.percentage = percentage;
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
        public ProgressBar build() {
            return new ProgressBar(
                    this.doneItem,
                    this.currentItem,
                    this.notDoneItem,
                    this.direction,
                    this.percentage,
                    this.width,
                    this.height
            );
        }
    }
}
