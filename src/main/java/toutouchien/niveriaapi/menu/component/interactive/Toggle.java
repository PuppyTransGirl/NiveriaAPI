package toutouchien.niveriaapi.menu.component.interactive;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;
import toutouchien.niveriaapi.utils.BackwardUtils;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An interactive toggle component that switches between two states (on/off).
 * <p>
 * The Toggle component displays different items based on its current state and
 * automatically switches between states when clicked. It supports customizable
 * on/off items, click sounds, and can span multiple slots with configurable
 * width and height.
 */
@NullMarked
public class Toggle extends MenuComponent {
    private Function<MenuContext, ItemStack> onItem, offItem;
    @Nullable
    private Consumer<ToggleEvent> onToggle;
    @Nullable
    private Sound sound;
    private final int width, height;

    private boolean currentState;

    /**
     * Constructs a new Toggle with the specified properties.
     *
     * @param builder the builder containing the toggle configuration
     */
    private Toggle(Builder builder) {
        super(builder.id);
        this.onItem = builder.onItem;
        this.offItem = builder.offItem;

        this.onToggle = builder.onToggle;

        this.sound = builder.sound;

        this.currentState = builder.currentState;

        this.width = builder.width;
        this.height = builder.height;
    }

    /**
     * Handles click events on this toggle.
     * <p>
     * When clicked, the toggle switches its state, plays a sound (if configured),
     * and triggers a re-render to update the displayed item.
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    @Override
    public void onClick(NiveriaInventoryClickEvent event, MenuContext context) {
        if (!this.interactable())
            return;

        if (this.sound != null)
            context.player().playSound(this.sound, Sound.Emitter.self());

        this.currentState = !this.currentState;
        this.render(context);

        if (this.onToggle != null) {
            ToggleEvent toggleEvent = new ToggleEvent(event, this.currentState);
            this.onToggle.accept(toggleEvent);
        }
    }

    /**
     * Returns the items to be displayed by this toggle.
     * <p>
     * The toggle fills all slots within its widthxheight area with the
     * current state item (on or off). Returns an empty map if not visible.
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
     * Returns the set of slots occupied by this toggle.
     * <p>
     * Includes all slots within the toggle's widthxheight area.
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
     * Gets the ItemStack to display based on the current toggle state.
     *
     * @param context the menu context
     * @return the appropriate ItemStack for the current state
     */
    private ItemStack currentItem(MenuContext context) {
        return currentState ? this.onItem.apply(context) : this.offItem.apply(context);
    }

    public record ToggleEvent(NiveriaInventoryClickEvent clickEvent, boolean newState) {

    }

    /**
     * Sets the ItemStack to display when the toggle is in the "on" state.
     *
     * @param onItem the ItemStack for the "on" state
     * @return this selector for method chaining
     * @throws NullPointerException if onItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Toggle onItem(ItemStack onItem) {
        Preconditions.checkNotNull(onItem, "onItem cannot be null");

        this.onItem = context -> onItem;
        return this;
    }

    /**
     * Sets the ItemStack to display when the toggle is in the "off" state.
     *
     * @param offItem the ItemStack for the "off" state
     * @return this selector for method chaining
     * @throws NullPointerException if offItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Toggle offItem(ItemStack offItem) {
        Preconditions.checkNotNull(offItem, "offItem cannot be null");

        this.offItem = context -> offItem;
        return this;
    }

    /**
     * Sets a function to provide the ItemStack for the "on" state.
     *
     * @param onItem function that returns the ItemStack for the "on" state
     * @return this selector for method chaining
     * @throws NullPointerException if onItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Toggle onItem(Function<MenuContext, ItemStack> onItem) {
        Preconditions.checkNotNull(onItem, "onItem cannot be null");

        this.onItem = onItem;
        return this;
    }

    /**
     * Sets a function to provide the ItemStack for the "off" state.
     *
     * @param offItem function that returns the ItemStack for the "off" state
     * @return this selector for method chaining
     * @throws NullPointerException if offItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Toggle offItem(Function<MenuContext, ItemStack> offItem) {
        Preconditions.checkNotNull(offItem, "offItem cannot be null");

        this.offItem = offItem;
        return this;
    }

    /**
     * Sets the toggle state change handler.
     *
     * @param onToggle the consumer to handle toggle state changes
     * @return this selector for method chaining
     * @throws NullPointerException if onToggle is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Toggle onToggle(Consumer<ToggleEvent> onToggle) {
        Preconditions.checkNotNull(onToggle, "onToggle cannot be null");

        this.onToggle = onToggle;
        return this;
    }

    /**
     * Sets the sound to play when the toggle is clicked.
     *
     * @param sound the sound to play, or null for no sound
     * @return this selector for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Toggle sound(@Nullable Sound sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Sets the initial state of the toggle.
     *
     * @param state true for "on" state, false for "off" state
     * @return this selector for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Toggle currentState(boolean state) {
        this.currentState = state;
        return this;
    }

    /**
     * Returns the width of this toggle in slots.
     *
     * @return the toggle width
     */
    @Positive
    @Override
    public int width() {
        return this.width;
    }

    /**
     * Returns the height of this toggle in rows.
     *
     * @return the toggle height
     */
    @Positive
    @Override
    public int height() {
        return this.height;
    }

    /**
     * Creates a new Toggle builder instance.
     *
     * @return a new Toggle.Builder for constructing toggles
     */
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * Builder class for constructing Toggle instances with a fluent interface.
     */
    public static class Builder extends MenuComponent.Builder<Builder> {
        private Function<MenuContext, ItemStack> onItem = context -> ItemStack.of(Material.STONE);
        private Function<MenuContext, ItemStack> offItem = context -> ItemStack.of(Material.STONE);

        @Nullable
        private Consumer<ToggleEvent> onToggle;

        @Nullable
        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                BackwardUtils.UI_SOUND_SOURCE,
                1F,
                1F
        );

        private boolean currentState;

        private int width = 1;
        private int height = 1;

        /**
         * Sets the ItemStack to display when the toggle is in the "on" state.
         *
         * @param onItem the ItemStack for the "on" state
         * @return this builder for method chaining
         * @throws NullPointerException if onItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onItem(ItemStack onItem) {
            Preconditions.checkNotNull(onItem, "onItem cannot be null");

            this.onItem = context -> onItem;
            return this;
        }

        /**
         * Sets the ItemStack to display when the toggle is in the "off" state.
         *
         * @param offItem the ItemStack for the "off" state
         * @return this builder for method chaining
         * @throws NullPointerException if offItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offItem(ItemStack offItem) {
            Preconditions.checkNotNull(offItem, "offItem cannot be null");

            this.offItem = context -> offItem;
            return this;
        }

        /**
         * Sets a function to provide the ItemStack for the "on" state.
         *
         * @param onItem function that returns the ItemStack for the "on" state
         * @return this builder for method chaining
         * @throws NullPointerException if onItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onItem(Function<MenuContext, ItemStack> onItem) {
            Preconditions.checkNotNull(onItem, "onItem cannot be null");

            this.onItem = onItem;
            return this;
        }

        /**
         * Sets a function to provide the ItemStack for the "off" state.
         *
         * @param offItem function that returns the ItemStack for the "off" state
         * @return this builder for method chaining
         * @throws NullPointerException if offItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offItem(Function<MenuContext, ItemStack> offItem) {
            Preconditions.checkNotNull(offItem, "offItem cannot be null");

            this.offItem = offItem;
            return this;
        }

        @Contract(value = "_ -> this", mutates = "this")
        public Builder onToggle(Consumer<ToggleEvent> onToggle) {
            Preconditions.checkNotNull(onToggle, "onToggle cannot be null");

            this.onToggle = onToggle;
            return this;
        }

        /**
         * Sets the sound to play when the toggle is clicked.
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
         * Sets the initial state of the toggle.
         *
         * @param state true for "on" state, false for "off" state
         * @return this builder for method chaining
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder currentState(boolean state) {
            this.currentState = state;
            return this;
        }

        /**
         * Sets the width of the toggle in slots.
         *
         * @param width the width in slots
         * @return this builder for method chaining
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder width(@Positive int width) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %s", width);

            this.width = width;
            return this;
        }

        /**
         * Sets the height of the toggle in rows.
         *
         * @param height the height in rows
         * @return this builder for method chaining
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder height(@Positive int height) {
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %s", height);

            this.height = height;
            return this;
        }

        /**
         * Sets both width and height of the toggle.
         *
         * @param width  the width in slots
         * @param height the height in rows
         * @return this builder for method chaining
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
         * Builds and returns the configured Toggle instance.
         *
         * @return a new Toggle with the specified configuration
         */
        public Toggle build() {
            return new Toggle(this);
        }
    }
}