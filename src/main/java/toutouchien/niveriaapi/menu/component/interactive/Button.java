package toutouchien.niveriaapi.menu.component.interactive;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;
import toutouchien.niveriaapi.utils.BackwardUtils;
import toutouchien.niveriaapi.utils.Task;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A versatile interactive button component with support for animations and dynamic content.
 * <p>
 * The Button component provides a comprehensive set of features including:
 * - Multiple click handler types (left, right, shift variants, drop)
 * - Animation support with configurable frame sequences and intervals
 * - Dynamic content updates at regular intervals
 * - Customizable click sounds
 * - Multi-slot rendering with configurable dimensions
 */
@NullMarked
public class Button extends MenuComponent {
    private Function<MenuContext, ItemStack> item;

    @Nullable
    private Consumer<NiveriaInventoryClickEvent> onClick, onLeftClick, onRightClick, onShiftLeftClick, onShiftRightClick, onDrop;

    @Nullable
    private Sound sound;

    @Nullable
    private Function<MenuContext, ObjectList<ItemStack>> animationFrames;
    private int animationInterval;
    private boolean stopAnimationOnHide;
    @Nullable
    private BukkitTask animationTask;
    private int currentFrame;

    @Nullable
    private Function<MenuContext, ItemStack> dynamicItem;
    private int updateInterval;
    private boolean stopUpdatesOnHide;
    @Nullable
    private BukkitTask updateTask;

    private final int width, height;

    /**
     * Constructs a new Button with the specified properties.
     *
     * @param builder the builder containing the button configuration
     */
    private Button(Builder builder) {
        super(builder.id());
        this.item = builder.item;

        this.onClick = builder.onClick;
        this.onLeftClick = builder.onLeftClick;
        this.onRightClick = builder.onRightClick;
        this.onShiftLeftClick = builder.onShiftLeftClick;
        this.onShiftRightClick = builder.onShiftRightClick;
        this.onDrop = builder.onDrop;

        this.sound = builder.sound;

        this.animationFrames = builder.animationFrames;
        this.animationInterval = builder.animationInterval;
        this.stopAnimationOnHide = builder.stopAnimationOnHide;

        this.dynamicItem = builder.dynamicItem;
        this.updateInterval = builder.updateInterval;
        this.stopUpdatesOnHide = builder.stopUpdatesOnHide;

        this.width = builder.width;
        this.height = builder.height;
    }

    /**
     * Called when this button is added to a menu.
     * <p>
     * Starts animation and dynamic update tasks if configured.
     *
     * @param context the menu context
     */
    @Override
    public void onAdd(MenuContext context) {
        if (this.animationFrames != null && this.animationInterval > 0)
            this.startAnimation(context);

        if (this.dynamicItem != null && this.updateInterval > 0)
            this.startUpdates(context);
    }

    /**
     * Called when this button is removed from a menu.
     * <p>
     * Stops all running tasks to prevent memory leaks and ensure proper cleanup.
     *
     * @param context the menu context
     */
    @Override
    public void onRemove(MenuContext context) {
        this.stopAnimation();
        this.stopUpdates();
    }

    /**
     * Handles click events on this button.
     * <p>
     * The button supports several interaction modes with priority handling:
     * 1. Specific click handlers (left, right, shift variants, drop)
     * 2. General click handler for other mouse clicks
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    @Override
    public void onClick(NiveriaInventoryClickEvent event, MenuContext context) {
        if (!this.interactable())
            return;

        Consumer<NiveriaInventoryClickEvent> handler = switch (event.getClick()) {
            case LEFT, DOUBLE_CLICK -> this.onLeftClick;
            case RIGHT -> this.onRightClick;
            case SHIFT_LEFT -> this.onShiftLeftClick;
            case SHIFT_RIGHT -> this.onShiftRightClick;
            case DROP, CONTROL_DROP -> this.onDrop;
            default -> null;
        };

        if (handler != null) {
            handler.accept(event);

            if (this.sound != null)
                context.player().playSound(this.sound, Sound.Emitter.self());
            return;
        }

        if (this.onClick != null && event.getClick().isMouseClick()) {
            this.onClick.accept(event);

            if (this.sound != null)
                context.player().playSound(this.sound, Sound.Emitter.self());
        }
    }

    /**
     * Returns the items to be displayed by this button.
     * <p>
     * The button fills all slots within its widthxheight area with the
     * current item (static, animated, or dynamic). Returns an empty map if not visible.
     *
     * @param context the menu context
     * @return a map from slot indices to ItemStacks
     */
    @Override
    public Int2ObjectMap<ItemStack> items(MenuContext context) {
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
    @Override
    public IntSet slots(MenuContext context) {
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
     * Starts the animation task that cycles through animation frames.
     *
     * @param context the menu context
     */
    private void startAnimation(MenuContext context) {
        this.animationTask = Task.syncRepeat(() -> {
            if (!enabled() || (this.stopAnimationOnHide && !visible())) {
                stopAnimation();
                return;
            }

            List<ItemStack> frames = this.animationFrames.apply(context);
            if (frames.isEmpty())
                return;

            this.currentFrame = (this.currentFrame + 1) % frames.size();
            this.render(context);
        }, NiveriaAPI.instance(), this.animationInterval * 50L, this.animationInterval * 50L, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the animation task and resets the frame counter.
     */
    private void stopAnimation() {
        this.currentFrame = 0;

        if (this.animationTask == null || this.animationTask.isCancelled())
            return;

        this.animationTask.cancel();
        this.animationTask = null;
    }

    /**
     * Starts the dynamic update task that refreshes the button content.
     *
     * @param context the menu context
     */
    private void startUpdates(MenuContext context) {
        this.updateTask = Task.syncRepeat(() -> {
            if (!enabled() || (this.stopUpdatesOnHide && !visible())) {
                stopUpdates();
                return;
            }

            this.render(context);
        }, NiveriaAPI.instance(), this.updateInterval * 50L, this.updateInterval * 50L, TimeUnit.MILLISECONDS);
    }

    /**
     * Stops the dynamic update task.
     */
    private void stopUpdates() {
        if (this.updateTask == null || this.updateTask.isCancelled())
            return;

        this.updateTask.cancel();
        this.updateTask = null;
    }

    /**
     * Gets the ItemStack to display based on the current button configuration.
     * <p>
     * Priority order: dynamic item → animation frame → static item
     *
     * @param context the menu context
     * @return the appropriate ItemStack for the current state
     */
    private ItemStack currentItem(MenuContext context) {
        if (this.dynamicItem != null)
            return this.dynamicItem.apply(context);

        if (this.animationFrames != null) {
            ObjectList<ItemStack> frames = this.animationFrames.apply(context);
            if (frames.isEmpty())
                return this.item.apply(context);

            return frames.get(this.currentFrame % frames.size());
        }

        return this.item.apply(context);
    }

    /**
     * Sets the ItemStack to display for this button.
     *
     * @param item the ItemStack to display
     * @return this button for method chaining
     * @throws NullPointerException if item is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button item(ItemStack item) {
        Preconditions.checkNotNull(item, "item cannot be null");

        this.item = context -> item;
        return this;
    }

    /**
     * Sets a function to provide the ItemStack for this button.
     *
     * @param item function that returns the ItemStack to display
     * @return this button for method chaining
     * @throws NullPointerException if item is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button item(Function<MenuContext, ItemStack> item) {
        Preconditions.checkNotNull(item, "item cannot be null");

        this.item = item;
        return this;
    }

    /**
     * Sets the general click handler for mouse clicks.
     *
     * @param onClick the click handler
     * @return this button for method chaining
     * @throws NullPointerException if onClick is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button onClick(Consumer<NiveriaInventoryClickEvent> onClick) {
        Preconditions.checkNotNull(onClick, "onClick cannot be null");

        this.onClick = onClick;
        return this;
    }

    /**
     * Sets the left click handler.
     *
     * @param onLeftClick the left click handler
     * @return this button for method chaining
     * @throws NullPointerException if onLeftClick is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button onLeftClick(Consumer<NiveriaInventoryClickEvent> onLeftClick) {
        Preconditions.checkNotNull(onLeftClick, "onLeftClick cannot be null");

        this.onLeftClick = onLeftClick;
        return this;
    }

    /**
     * Sets the right click handler.
     *
     * @param onRightClick the right click handler
     * @return this button for method chaining
     * @throws NullPointerException if onRightClick is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button onRightClick(Consumer<NiveriaInventoryClickEvent> onRightClick) {
        Preconditions.checkNotNull(onRightClick, "onRightClick cannot be null");

        this.onRightClick = onRightClick;
        return this;
    }

    /**
     * Sets the shift+left click handler.
     *
     * @param onShiftLeftClick the shift+left click handler
     * @return this button for method chaining
     * @throws NullPointerException if onShiftLeftClick is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button onShiftLeftClick(Consumer<NiveriaInventoryClickEvent> onShiftLeftClick) {
        Preconditions.checkNotNull(onShiftLeftClick, "onShiftLeftClick cannot be null");

        this.onShiftLeftClick = onShiftLeftClick;
        return this;
    }

    /**
     * Sets the shift+right click handler.
     *
     * @param onShiftRightClick the shift+right click handler
     * @return this button for method chaining
     * @throws NullPointerException if onShiftRightClick is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button onShiftRightClick(Consumer<NiveriaInventoryClickEvent> onShiftRightClick) {
        Preconditions.checkNotNull(onShiftRightClick, "onShiftRightClick cannot be null");

        this.onShiftRightClick = onShiftRightClick;
        return this;
    }

    /**
     * Sets the drop action handler.
     *
     * @param onDrop the drop action handler
     * @return this button for method chaining
     * @throws NullPointerException if onDrop is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button onDrop(Consumer<NiveriaInventoryClickEvent> onDrop) {
        Preconditions.checkNotNull(onDrop, "onDrop cannot be null");

        this.onDrop = onDrop;
        return this;
    }

    /**
     * Sets the sound to play when the button is clicked.
     *
     * @param sound the sound to play, or null for no sound
     * @return this button for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button sound(@Nullable Sound sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Sets the function providing animation frames for this button.
     *
     * @param animationFrames function that returns a list of ItemStacks to cycle through
     * @return this button for method chaining
     * @throws NullPointerException if animationFrames is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button animationFrames(Function<MenuContext, ObjectList<ItemStack>> animationFrames) {
        Preconditions.checkNotNull(animationFrames, "animationFrames cannot be null");

        this.animationFrames = animationFrames;
        return this;
    }

    /**
     * Sets the interval between animation frames in ticks.
     *
     * @param animationInterval ticks between frames (must be positive)
     * @return this button for method chaining
     * @throws IllegalArgumentException if animationInterval is less than 1
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button animationInterval(@Positive int animationInterval) {
        Preconditions.checkArgument(animationInterval >= 1, "animationInterval cannot be less than 1: %s", animationInterval);

        this.animationInterval = animationInterval;
        return this;
    }

    /**
     * Sets whether animation should stop when the button is hidden.
     *
     * @param stopAnimationOnHide true to stop animation when hidden
     * @return this button for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button stopAnimationOnHide(boolean stopAnimationOnHide) {
        this.stopAnimationOnHide = stopAnimationOnHide;
        return this;
    }

    /**
     * Sets the function providing dynamic content for this button.
     *
     * @param dynamicItem function that returns dynamically updating ItemStack
     * @return this button for method chaining
     * @throws NullPointerException if dynamicItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button dynamicItem(Function<MenuContext, ItemStack> dynamicItem) {
        Preconditions.checkNotNull(dynamicItem, "dynamicItem cannot be null");

        this.dynamicItem = dynamicItem;
        return this;
    }

    /**
     * Sets the interval between dynamic content updates in ticks.
     *
     * @param updateInterval ticks between updates (must be positive)
     * @return this button for method chaining
     * @throws IllegalArgumentException if updateInterval is less than 1
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button updateInterval(@Positive int updateInterval) {
        Preconditions.checkArgument(updateInterval >= 1, "updateInterval cannot be less than 1: %s", updateInterval);

        this.updateInterval = updateInterval;
        return this;
    }

    /**
     * Sets whether dynamic updates should stop when the button is hidden.
     *
     * @param stopUpdatesOnHide true to stop updates when hidden
     * @return this button for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Button stopUpdatesOnHide(boolean stopUpdatesOnHide) {
        this.stopUpdatesOnHide = stopUpdatesOnHide;
        return this;
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
     * Creates a new Button builder instance.
     *
     * @return a new Button.Builder for constructing buttons
     */
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * Builder class for constructing Button instances with a fluent interface.
     */
    public static class Builder extends MenuComponent.Builder<Builder> {
        private Function<MenuContext, ItemStack> item = context -> ItemStack.of(Material.STONE);

        @Nullable
        private Consumer<NiveriaInventoryClickEvent> onClick, onLeftClick, onRightClick, onShiftLeftClick, onShiftRightClick, onDrop;

        @Nullable
        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                BackwardUtils.UI_SOUND_SOURCE,
                1F,
                1F
        );

        @Nullable
        private Function<MenuContext, ObjectList<ItemStack>> animationFrames;
        private int animationInterval = 20;
        private boolean stopAnimationOnHide = true;

        @Nullable
        private Function<MenuContext, ItemStack> dynamicItem;
        private int updateInterval = 20;
        private boolean stopUpdatesOnHide = false;

        private int width = 1;
        private int height = 1;

        /**
         * Sets the ItemStack to display for this button.
         *
         * @param item the ItemStack to display
         * @return this builder for method chaining
         * @throws NullPointerException if item is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder item(ItemStack item) {
            Preconditions.checkNotNull(item, "item cannot be null");

            this.item = context -> item;
            return this;
        }

        /**
         * Sets a function to provide the ItemStack for this button.
         *
         * @param item function that returns the ItemStack to display
         * @return this builder for method chaining
         * @throws NullPointerException if item is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder item(Function<MenuContext, ItemStack> item) {
            Preconditions.checkNotNull(item, "item cannot be null");

            this.item = item;
            return this;
        }

        /**
         * Sets the general click handler for mouse clicks.
         *
         * @param onClick the click handler
         * @return this builder for method chaining
         * @throws NullPointerException if onClick is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onClick(Consumer<NiveriaInventoryClickEvent> onClick) {
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
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onLeftClick(Consumer<NiveriaInventoryClickEvent> onLeftClick) {
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
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onRightClick(Consumer<NiveriaInventoryClickEvent> onRightClick) {
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
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onShiftLeftClick(Consumer<NiveriaInventoryClickEvent> onShiftLeftClick) {
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
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onShiftRightClick(Consumer<NiveriaInventoryClickEvent> onShiftRightClick) {
            Preconditions.checkNotNull(onShiftRightClick, "onShiftRightClick cannot be null");

            this.onShiftRightClick = onShiftRightClick;
            return this;
        }

        /**
         * Sets the drop action handler.
         *
         * @param onDrop the drop action handler
         * @return this builder for method chaining
         * @throws NullPointerException if onDrop is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onDrop(Consumer<NiveriaInventoryClickEvent> onDrop) {
            Preconditions.checkNotNull(onDrop, "onDrop cannot be null");

            this.onDrop = onDrop;
            return this;
        }

        /**
         * Sets the sound to play when the button is clicked.
         *
         * @param sound the sound to play, or null for no sound
         * @return this builder for method chaining
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder sound(@Nullable Sound sound) {
            this.sound = sound;
            return this;
        }

        /**
         * Sets the function providing animation frames for this button.
         *
         * @param animationFrames function that returns a list of ItemStacks to cycle through
         * @return this builder for method chaining
         * @throws NullPointerException if animationFrames is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder animationFrames(Function<MenuContext, ObjectList<ItemStack>> animationFrames) {
            Preconditions.checkNotNull(animationFrames, "animationFrames cannot be null");

            this.animationFrames = animationFrames;
            return this;
        }

        /**
         * Sets the interval between animation frames in ticks.
         *
         * @param animationInterval ticks between frames (must be positive)
         * @return this builder for method chaining
         * @throws IllegalArgumentException if animationInterval is less than 1
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder animationInterval(@Positive int animationInterval) {
            Preconditions.checkArgument(animationInterval >= 1, "animationInterval cannot be less than 1: %s", animationInterval);

            this.animationInterval = animationInterval;
            return this;
        }

        /**
         * Sets whether animation should stop when the button is hidden.
         *
         * @param stopAnimationOnHide true to stop animation when hidden
         * @return this builder for method chaining
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder stopAnimationOnHide(boolean stopAnimationOnHide) {
            this.stopAnimationOnHide = stopAnimationOnHide;
            return this;
        }

        /**
         * Sets the function providing dynamic content for this button.
         *
         * @param dynamicItem function that returns dynamically updating ItemStack
         * @return this builder for method chaining
         * @throws NullPointerException if dynamicItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder dynamicItem(Function<MenuContext, ItemStack> dynamicItem) {
            Preconditions.checkNotNull(dynamicItem, "dynamicItem cannot be null");

            this.dynamicItem = dynamicItem;
            return this;
        }

        /**
         * Sets the interval between dynamic content updates in ticks.
         *
         * @param updateInterval ticks between updates (must be positive)
         * @return this builder for method chaining
         * @throws IllegalArgumentException if updateInterval is less than 1
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder updateInterval(@Positive int updateInterval) {
            Preconditions.checkArgument(updateInterval >= 1, "updateInterval cannot be less than 1: %s", updateInterval);

            this.updateInterval = updateInterval;
            return this;
        }

        /**
         * Sets whether dynamic updates should stop when the button is hidden.
         *
         * @param stopUpdatesOnHide true to stop updates when hidden
         * @return this builder for method chaining
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder stopUpdatesOnHide(boolean stopUpdatesOnHide) {
            this.stopUpdatesOnHide = stopUpdatesOnHide;
            return this;
        }

        /**
         * Sets the width of the button in slots.
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
         * Sets the height of the button in rows.
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
         * Sets both width and height of the button.
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
         * Builds and returns the configured Button instance.
         *
         * @return a new Button with the specified configuration
         */
        public Button build() {
            return new Button(this);
        }
    }
}