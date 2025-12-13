package toutouchien.niveriaapi.menu.component.interactive;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;
import toutouchien.niveriaapi.utils.Task;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An interactive button component that responds to double-drop actions.
 * <p>
 * This specialized button component displays one item normally and switches to
 * a different "drop item" when a drop action is detected. If another drop action
 * occurs within 3 seconds (60 ticks), it triggers a double-drop callback.
 * The button supports various click handlers for different interaction types.
 */
public class DoubleDropButton extends Component {
    private Function<MenuContext, ItemStack> item;
    private Function<MenuContext, ItemStack> dropItem;

    private Consumer<NiveriaInventoryClickEvent> onClick, onLeftClick, onRightClick, onShiftLeftClick, onShiftRightClick, onDoubleDrop;

    private Sound sound;

    private final int width, height;

    private BukkitTask dropTask;

    /**
     * Constructs a new DoubleDropButton with the specified parameters.
     *
     * @param item              function that provides the normal ItemStack
     * @param dropItem          function that provides the drop state ItemStack
     * @param onClick           general click handler for mouse clicks
     * @param onLeftClick       handler for left clicks
     * @param onRightClick      handler for right clicks
     * @param onShiftLeftClick  handler for shift+left clicks
     * @param onShiftRightClick handler for shift+right clicks
     * @param onDoubleDrop      handler for double-drop actions
     * @param sound             sound to play when clicked (may be null)
     * @param width             width of the button in slots
     * @param height            height of the button in rows
     */
    private DoubleDropButton(
            Function<MenuContext, ItemStack> item,
            Function<MenuContext, ItemStack> dropItem,
            Consumer<NiveriaInventoryClickEvent> onClick,
            Consumer<NiveriaInventoryClickEvent> onLeftClick, Consumer<NiveriaInventoryClickEvent> onRightClick,
            Consumer<NiveriaInventoryClickEvent> onShiftLeftClick, Consumer<NiveriaInventoryClickEvent> onShiftRightClick,
            Consumer<NiveriaInventoryClickEvent> onDoubleDrop,
            Sound sound,
            int width, int height
    ) {
        this.item = item;
        this.dropItem = dropItem;

        this.onClick = onClick;
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
        this.onShiftLeftClick = onShiftLeftClick;
        this.onShiftRightClick = onShiftRightClick;
        this.onDoubleDrop = onDoubleDrop;

        this.sound = sound;

        this.width = width;
        this.height = height;
    }

    /**
     * Called when this button is removed from a menu.
     * <p>
     * Cancels any pending drop task to prevent memory leaks and
     * ensure proper cleanup.
     *
     * @param context the menu context
     */
    @Override
    public void onRemove(@NotNull MenuContext context) {
        if (this.dropTask != null)
            this.dropTask.cancel();
    }

    /**
     * Handles click events on this button.
     * <p>
     * The button supports several interaction modes:
     * - Drop clicks: First drop enters "drop state", second drop within 3 seconds triggers double-drop
     * - Specific click handlers: Left, right, shift+left, shift+right clicks
     * - General click handler: Fallback for other mouse clicks
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    @Override
    public void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context) {
        if (!this.interactable())
            return;

        if (this.sound != null)
            context.player().playSound(this.sound, Sound.Emitter.self());

        ClickType click = event.getClick();
        if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) {
            if (this.dropTask != null) {
                this.dropTask.cancel();
                this.dropTask = null;

                if (this.onDoubleDrop != null)
                    this.onDoubleDrop.accept(event);
            } else {
                this.dropTask = Task.syncLater(() -> {
                    this.dropTask = null;
                    render(context);
                }, NiveriaAPI.instance(), 60L);
            }

            return;
        }

        Consumer<NiveriaInventoryClickEvent> handler = switch (event.getClick()) {
            case LEFT, DOUBLE_CLICK -> this.onLeftClick;
            case RIGHT -> this.onRightClick;
            case SHIFT_LEFT -> this.onShiftLeftClick;
            case SHIFT_RIGHT -> this.onShiftRightClick;
            default -> null;
        };

        if (handler != null) {
            handler.accept(event);
            return;
        }

        if (this.onClick != null && click.isMouseClick())
            this.onClick.accept(event);
    }

    /**
     * Returns the items to be displayed by this button.
     * <p>
     * The button fills all slots within its widthxheight area with the
     * current item (normal or drop state). Returns an empty map if not visible.
     *
     * @param context the menu context
     * @return a map from slot indices to ItemStacks
     */
    @NotNull
    @Override
    public Int2ObjectMap<ItemStack> items(@NotNull MenuContext context) {
        Int2ObjectMap<ItemStack> items = new Int2ObjectOpenHashMap<>();
        if (!this.visible())
            return items;

        ItemStack baseItem = this.currentItem(context);
        int baseSlot = this.slot();
        int rowLength = 9;

        for (int row = 0; row < this.height; row++) {
            for (int col = 0; col < this.width; col++) {
                int slot = baseSlot + col + (row * rowLength);
                items.put(slot, baseItem);
            }
        }

        return items;
    }

    /**
     * Returns the set of slots occupied by this button.
     * <p>
     * Includes all slots within the button's widthxheight area.
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
     * Returns the width of this button in slots.
     *
     * @return the button width
     */
    @Positive
    @Override
    public int width() {
        return this.width;
    }

    /**
     * Returns the height of this button in rows.
     *
     * @return the button height
     */
    @Positive
    @Override
    public int height() {
        return this.height;
    }

    /**
     * Gets the ItemStack to display based on the current button state.
     *
     * @param context the menu context
     * @return the normal item if no drop task is active, otherwise the drop item
     */
    private ItemStack currentItem(@NotNull MenuContext context) {
        return this.dropTask == null ? this.item.apply(context) : this.dropItem.apply(context);
    }

    /**
     * Sets the normal ItemStack to display for this button.
     *
     * @param item the ItemStack to display normally
     * @throws NullPointerException if item is null
     */
    public void item(@NotNull ItemStack item) {
        Preconditions.checkNotNull(item, "item cannot be null");

        this.item = context -> item;
    }

    /**
     * Sets a function to provide the normal ItemStack for this button.
     *
     * @param item function that returns the normal ItemStack
     * @throws NullPointerException if item is null
     */
    public void item(@NotNull Function<MenuContext, ItemStack> item) {
        Preconditions.checkNotNull(item, "item cannot be null");

        this.item = item;
    }

    /**
     * Sets the drop state ItemStack to display for this button.
     *
     * @param dropItem the ItemStack to display in drop state
     * @throws NullPointerException if dropItem is null
     */
    public void dropItem(@NotNull ItemStack dropItem) {
        Preconditions.checkNotNull(dropItem, "dropItem cannot be null");

        this.dropItem = context -> dropItem;
    }

    /**
     * Sets a function to provide the drop state ItemStack for this button.
     *
     * @param dropItem function that returns the drop state ItemStack
     * @throws NullPointerException if dropItem is null
     */
    public void dropItem(@NotNull Function<MenuContext, ItemStack> dropItem) {
        Preconditions.checkNotNull(dropItem, "dropItem cannot be null");

        this.dropItem = dropItem;
    }

    /**
     * Sets the sound to play when the button is clicked.
     *
     * @param sound the sound to play, or null for no sound
     */
    public void sound(@Nullable Sound sound) {
        this.sound = sound;
    }

    /**
     * Sets the general click handler for this button.
     *
     * @param onClick the click handler
     * @throws NullPointerException if onClick is null
     */
    public void onClick(@NotNull Consumer<NiveriaInventoryClickEvent> onClick) {
        Preconditions.checkNotNull(onClick, "onClick cannot be null");

        this.onClick = onClick;
    }

    /**
     * Sets the left-click handler for this button.
     *
     * @param onLeftClick the left-click handler
     * @throws NullPointerException if onLeftClick is null
     */
    public void onLeftClick(@NotNull Consumer<NiveriaInventoryClickEvent> onLeftClick) {
        Preconditions.checkNotNull(onLeftClick, "onLeftClick cannot be null");

        this.onLeftClick = onLeftClick;
    }

    /**
     * Sets the right-click handler for this button.
     *
     * @param onRightClick the right-click handler
     * @throws NullPointerException if onRightClick is null
     */
    public void onRightClick(@NotNull Consumer<NiveriaInventoryClickEvent> onRightClick) {
        Preconditions.checkNotNull(onRightClick, "onRightClick cannot be null");

        this.onRightClick = onRightClick;
    }

    /**
     * Sets the shift-left-click handler for this button.
     *
     * @param onShiftLeftClick the shift-left-click handler
     * @throws NullPointerException if onShiftLeftClick is null
     */
    public void onShiftLeftClick(@NotNull Consumer<NiveriaInventoryClickEvent> onShiftLeftClick) {
        Preconditions.checkNotNull(onShiftLeftClick, "onShiftLeftClick cannot be null");

        this.onShiftLeftClick = onShiftLeftClick;
    }

    /**
     * Sets the shift-right-click handler for this button.
     *
     * @param onShiftRightClick the shift-right-click handler
     * @throws NullPointerException if onShiftRightClick is null
     */
    public void onShiftRightClick(@NotNull Consumer<NiveriaInventoryClickEvent> onShiftRightClick) {
        Preconditions.checkNotNull(onShiftRightClick, "onShiftRightClick cannot be null");

        this.onShiftRightClick = onShiftRightClick;
    }

    /**
     * Sets the double-drop handler for this button.
     *
     * @param onDoubleDrop the double-drop handler
     * @throws NullPointerException if onDoubleDrop is null
     */
    public void onDoubleDrop(@NotNull Consumer<NiveriaInventoryClickEvent> onDoubleDrop) {
        Preconditions.checkNotNull(onDoubleDrop, "onDoubleDrop cannot be null");

        this.onDoubleDrop = onDoubleDrop;
    }

    /**
     * Creates a new DoubleDropButton builder instance.
     *
     * @return a new DoubleDropButton.Builder for constructing buttons
     */
    @NotNull
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * Builder class for constructing DoubleDropButton instances with a fluent interface.
     */
    public static class Builder {
        private Function<MenuContext, ItemStack> item = context -> ItemStack.of(Material.STONE);
        private Function<MenuContext, ItemStack> dropItem = context -> ItemStack.of(Material.DIRT);

        private Consumer<NiveriaInventoryClickEvent> onClick, onLeftClick, onRightClick, onShiftLeftClick, onShiftRightClick, onDoubleDrop;

        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                Sound.Source.UI,
                1F,
                1F
        );

        private int width = 1;
        private int height = 1;

        /**
         * Sets the ItemStack to display in normal state.
         *
         * @param item the ItemStack for normal state
         * @return this builder for method chaining
         * @throws NullPointerException if item is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder item(@NotNull ItemStack item) {
            Preconditions.checkNotNull(item, "item cannot be null");

            this.item = context -> item;
            return this;
        }

        /**
         * Sets the ItemStack to display in drop state.
         *
         * @param dropItem the ItemStack for drop state
         * @return this builder for method chaining
         * @throws NullPointerException if dropItem is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder dropItem(@NotNull ItemStack dropItem) {
            Preconditions.checkNotNull(dropItem, "dropItem cannot be null");

            this.dropItem = context -> dropItem;
            return this;
        }

        /**
         * Sets a function to provide the ItemStack for normal state.
         *
         * @param item function that returns the ItemStack for normal state
         * @return this builder for method chaining
         * @throws NullPointerException if item is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder item(@NotNull Function<MenuContext, ItemStack> item) {
            Preconditions.checkNotNull(item, "item cannot be null");

            this.item = item;
            return this;
        }

        /**
         * Sets a function to provide the ItemStack for drop state.
         *
         * @param dropItem function that returns the ItemStack for drop state
         * @return this builder for method chaining
         * @throws NullPointerException if dropItem is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder dropItem(@NotNull Function<MenuContext, ItemStack> dropItem) {
            Preconditions.checkNotNull(dropItem, "dropItem cannot be null");

            this.dropItem = dropItem;
            return this;
        }

        /**
         * Sets the general click handler for mouse clicks.
         *
         * @param onClick the click handler
         * @return this builder for method chaining
         * @throws NullPointerException if onClick is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onClick(@NotNull Consumer<NiveriaInventoryClickEvent> onClick) {
            Preconditions.checkNotNull(onClick, "onClick cannot be null");

            this.onClick = onClick;
            return this;
        }

        /**
         * Sets the left click handler.
         *
         * @param onLeftClick the left click handler
         * @return this builder for method chaining
         * @throws NullPointerException if onLeftClick is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onLeftClick(@NotNull Consumer<NiveriaInventoryClickEvent> onLeftClick) {
            Preconditions.checkNotNull(onLeftClick, "onLeftClick cannot be null");

            this.onLeftClick = onLeftClick;
            return this;
        }

        /**
         * Sets the right click handler.
         *
         * @param onRightClick the right click handler
         * @return this builder for method chaining
         * @throws NullPointerException if onRightClick is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onRightClick(@NotNull Consumer<NiveriaInventoryClickEvent> onRightClick) {
            Preconditions.checkNotNull(onRightClick, "onRightClick cannot be null");

            this.onRightClick = onRightClick;
            return this;
        }

        /**
         * Sets the shift+left click handler.
         *
         * @param onShiftLeftClick the shift+left click handler
         * @return this builder for method chaining
         * @throws NullPointerException if onShiftLeftClick is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onShiftLeftClick(@NotNull Consumer<NiveriaInventoryClickEvent> onShiftLeftClick) {
            Preconditions.checkNotNull(onShiftLeftClick, "onShiftLeftClick cannot be null");

            this.onShiftLeftClick = onShiftLeftClick;
            return this;
        }

        /**
         * Sets the shift+right click handler.
         *
         * @param onShiftRightClick the shift+right click handler
         * @return this builder for method chaining
         * @throws NullPointerException if onShiftRightClick is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onShiftRightClick(@NotNull Consumer<NiveriaInventoryClickEvent> onShiftRightClick) {
            Preconditions.checkNotNull(onShiftRightClick, "onShiftRightClick cannot be null");

            this.onShiftRightClick = onShiftRightClick;
            return this;
        }

        /**
         * Sets the double-drop action handler.
         *
         * @param onDoubleDrop the double-drop handler
         * @return this builder for method chaining
         * @throws NullPointerException if onDoubleDrop is null
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onDoubleDrop(@NotNull Consumer<NiveriaInventoryClickEvent> onDoubleDrop) {
            Preconditions.checkNotNull(onDoubleDrop, "onDoubleDrop cannot be null");

            this.onDoubleDrop = onDoubleDrop;
            return this;
        }

        /**
         * Sets the sound to play when the button is clicked.
         *
         * @param sound the sound to play, or null for no sound
         * @return this builder for method chaining
         */
        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder sound(@Nullable Sound sound) {
            this.sound = sound;
            return this;
        }

        /**
         * Sets the width of the button in slots.
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
         * Sets the height of the button in rows.
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
         * Sets both width and height of the button.
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
         * Builds and returns the configured DoubleDropButton instance.
         *
         * @return a new DoubleDropButton with the specified configuration
         */
        @NotNull
        public DoubleDropButton build() {
            return new DoubleDropButton(
                    this.item,
                    this.dropItem,
                    this.onClick,
                    this.onLeftClick,
                    this.onRightClick,
                    this.onShiftLeftClick,
                    this.onShiftRightClick,
                    this.onDoubleDrop,
                    this.sound,
                    this.width,
                    this.height
            );
        }
    }
}