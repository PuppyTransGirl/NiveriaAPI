package toutouchien.niveriaapi.menu.component.display;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.objects.Object2DoubleFunction;
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
import java.util.function.Function;

/**
 * A display component that visualizes progress as a bar with different item states.
 * <p>
 * The ProgressBar component displays progress using three types of items:
 * - Done items: represent completed progress
 * - Current item: represents the current progress position (when not fully complete)
 * - Not done items: represent remaining progress
 * <p>
 * The progress can be displayed in four directions: UP, DOWN, LEFT, or RIGHT.
 * The percentage value determines how much of the bar is filled.
 */
public class ProgressBar extends Component {
    private final Function<MenuContext, ItemStack> doneItem, currentItem, notDoneItem;

    private final Direction.Default direction;

    private final Object2DoubleFunction<MenuContext> percentage;

    private final int width;
    private final int height;

    /**
     * Constructs a new ProgressBar with the specified parameters.
     *
     * @param doneItem    function that provides the ItemStack for completed sections
     * @param currentItem function that provides the ItemStack for the current progress position
     * @param notDoneItem function that provides the ItemStack for incomplete sections
     * @param direction   the direction in which the progress bar fills
     * @param percentage  function that returns the progress percentage (0.0 to 1.0)
     * @param width       the width of the progress bar in slots
     * @param height      the height of the progress bar in rows
     */
    private ProgressBar(
            Function<MenuContext, ItemStack> doneItem, Function<MenuContext, ItemStack> currentItem, Function<MenuContext, ItemStack> notDoneItem,
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

    /**
     * Returns the items to be displayed by this progress bar.
     * <p>
     * The progress bar fills slots based on the current percentage value:
     * - Slots before the progress position show "done" items
     * - The slot at the current progress position shows the "current" item (unless 100% complete)
     * - Remaining slots show "not done" items
     *
     * @param context the menu context
     * @return a map from slot indices to ItemStacks
     */
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

    /**
     * Iterates through each slot in the progress bar according to the specified direction.
     * <p>
     * The traversal order depends on the direction:
     * - RIGHT: left-to-right, top-to-bottom
     * - LEFT: right-to-left, top-to-bottom
     * - DOWN: top-to-bottom, left-to-right
     * - UP: bottom-to-top, left-to-right
     *
     * @param consumer consumer that accepts (index, slot) pairs
     */
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

    /**
     * Creates a traversal configuration based on the progress bar's direction.
     *
     * @return a Traversal object defining how to iterate through the progress bar slots
     */
    private Traversal traversal() {
        Range rowsRange = new Range(0, this.height, 1);
        Range colsRange = new Range(0, this.width, 1);

        return switch (this.direction) {
            case UP -> new Traversal(new Range(this.height - 1, -1, -1), colsRange, false);
            case LEFT -> new Traversal(rowsRange, new Range(this.width - 1, -1, -1), true);
            case RIGHT -> new Traversal(rowsRange, colsRange, true);
            case DOWN -> new Traversal(rowsRange, colsRange, false);
        };
    }

    /**
     * Record representing a range for iteration with start, end, and step values.
     *
     * @param start        the starting index
     * @param endExclusive the ending index (exclusive)
     * @param step         the step size for iteration
     */
    private record Range(@NonNegative int start, int endExclusive, int step) {

    }

    /**
     * Record representing a traversal pattern for the progress bar.
     *
     * @param rows     the range for row iteration
     * @param cols     the range for column iteration
     * @param rowMajor whether to iterate rows first (true) or columns first (false)
     */
    private record Traversal(@NotNull Range rows, @NotNull Range cols, boolean rowMajor) {

    }

    /**
     * Returns the set of slots occupied by this progress bar.
     * <p>
     * Includes all slots within the progress bar's widthxheight area.
     * Returns an empty set if not visible.
     *
     * @param context the menu context
     * @return a set of slot indices
     */
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

    /**
     * Returns the width of this progress bar in slots.
     *
     * @return the progress bar width
     */
    @Positive
    @Override
    public int width() {
        return this.width;
    }

    /**
     * Returns the height of this progress bar in rows.
     *
     * @return the progress bar height
     */
    @Positive
    @Override
    public int height() {
        return this.height;
    }

    /**
     * Creates a new ProgressBar builder instance.
     *
     * @return a new ProgressBar.Builder for constructing progress bars
     */
    @NotNull
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * Builder class for constructing ProgressBar instances with a fluent interface.
     */
    public static class Builder {
        private Function<MenuContext, ItemStack> doneItem = context -> ItemStack.of(Material.LIME_CONCRETE);
        private Function<MenuContext, ItemStack> currentItem = context -> ItemStack.of(Material.ORANGE_CONCRETE);
        private Function<MenuContext, ItemStack> notDoneItem = context -> ItemStack.of(Material.RED_CONCRETE);

        private Direction.Default direction = Direction.Default.RIGHT;

        private Object2DoubleFunction<MenuContext> percentage = context -> 0D;

        private int width = 1;
        private int height = 1;

        /**
         * Sets the ItemStack to display for completed sections.
         *
         * @param doneItem the ItemStack for completed sections
         * @return this builder for method chaining
         * @throws NullPointerException if doneItem is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder doneItem(@NotNull ItemStack doneItem) {
            Preconditions.checkNotNull(doneItem, "doneItem cannot be null");

            this.doneItem = context -> doneItem;
            return this;
        }

        /**
         * Sets a function to provide the ItemStack for completed sections.
         *
         * @param doneItem function that returns the ItemStack for completed sections
         * @return this builder for method chaining
         * @throws NullPointerException if doneItem is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder doneItem(@NotNull Function<MenuContext, ItemStack> doneItem) {
            Preconditions.checkNotNull(doneItem, "doneItem cannot be null");

            this.doneItem = doneItem;
            return this;
        }

        /**
         * Sets the ItemStack to display for the current progress position.
         *
         * @param currentItem the ItemStack for the current progress position
         * @return this builder for method chaining
         * @throws NullPointerException if currentItem is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder currentItem(@NotNull ItemStack currentItem) {
            Preconditions.checkNotNull(currentItem, "currentItem cannot be null");

            this.currentItem = context -> currentItem;
            return this;
        }

        /**
         * Sets a function to provide the ItemStack for the current progress position.
         *
         * @param currentItem function that returns the ItemStack for the current progress position
         * @return this builder for method chaining
         * @throws NullPointerException if currentItem is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder currentItem(@NotNull Function<MenuContext, ItemStack> currentItem) {
            Preconditions.checkNotNull(currentItem, "currentItem cannot be null");

            this.currentItem = currentItem;
            return this;
        }

        /**
         * Sets the ItemStack to display for incomplete sections.
         *
         * @param notDoneItem the ItemStack for incomplete sections
         * @return this builder for method chaining
         * @throws NullPointerException if notDoneItem is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder notDoneItem(@NotNull ItemStack notDoneItem) {
            Preconditions.checkNotNull(notDoneItem, "notDoneItem cannot be null");

            this.notDoneItem = context -> notDoneItem;
            return this;
        }

        /**
         * Sets a function to provide the ItemStack for incomplete sections.
         *
         * @param notDoneItem function that returns the ItemStack for incomplete sections
         * @return this builder for method chaining
         * @throws NullPointerException if notDoneItem is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder notDoneItem(@NotNull Function<MenuContext, ItemStack> notDoneItem) {
            Preconditions.checkNotNull(notDoneItem, "notDoneItem cannot be null");

            this.notDoneItem = notDoneItem;
            return this;
        }

        /**
         * Sets the direction in which the progress bar fills.
         *
         * @param direction the fill direction (UP, DOWN, LEFT, or RIGHT)
         * @return this builder for method chaining
         * @throws NullPointerException if direction is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder direction(@NotNull Direction.Default direction) {
            Preconditions.checkNotNull(direction, "direction cannot be null");

            this.direction = direction;
            return this;
        }

        /**
         * Sets a static percentage value for the progress bar.
         *
         * @param percentage the progress percentage (0.0 to 1.0)
         * @return this builder for method chaining
         * @throws IllegalArgumentException if percentage is negative
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder percentage(@NonNegative double percentage) {
            Preconditions.checkArgument(percentage >= 0, "percentage cannot be negative: %s", percentage);

            this.percentage = context -> percentage;
            return this;
        }

        /**
         * Sets a function to provide the progress percentage.
         *
         * @param percentage function that returns the progress percentage (0.0 to 1.0)
         * @return this builder for method chaining
         * @throws NullPointerException if percentage is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder percentage(@NotNull Object2DoubleFunction<MenuContext> percentage) {
            Preconditions.checkNotNull(percentage, "percentage cannot be null");

            this.percentage = percentage;
            return this;
        }

        /**
         * Sets the width of the progress bar in slots.
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
         * Sets the height of the progress bar in rows.
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
         * Sets both width and height of the progress bar.
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
         * Builds and returns the configured ProgressBar instance.
         *
         * @return a new ProgressBar with the specified configuration
         */
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