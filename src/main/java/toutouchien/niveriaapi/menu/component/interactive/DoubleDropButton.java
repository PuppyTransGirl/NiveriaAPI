package toutouchien.niveriaapi.menu.component.interactive;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;
import toutouchien.niveriaapi.utils.BackwardUtils;
import toutouchien.niveriaapi.utils.Task;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
@NullMarked
public class DoubleDropButton extends MenuComponent {
    private Function<MenuContext, ItemStack> item;
    private Function<MenuContext, ItemStack> dropItem;

    private final Object2ObjectMap<EnumSet<ClickType>, Consumer<NiveriaInventoryClickEvent>> onClickMap;
    @Nullable private Consumer<NiveriaInventoryClickEvent> onDoubleDrop;

    @Nullable private Sound sound;

    @Nullable private ScheduledTask dropTask;

    /**
     * Constructs a new DoubleDropButton with the specified configuration.
     *
     * @param builder the builder containing the double drop button configuration
     */
    private DoubleDropButton(Builder builder) {
        super(builder);
        this.item = builder.item;
        this.dropItem = builder.dropItem;

        this.onClickMap = new Object2ObjectLinkedOpenHashMap<>(builder.onClickMap);
        this.onDoubleDrop = builder.onDoubleDrop;

        this.sound = builder.sound;
    }

    /**
     * Creates a new DoubleDropButton builder instance.
     *
     * @return a new DoubleDropBuilder for constructing buttons
     */
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * Called when this double drop button is removed from a menu.
     * <p>
     * Cancels any pending drop task to prevent memory leaks and
     * ensure proper cleanup.
     *
     * @param context the menu context
     */
    @Override
    public void onRemove(MenuContext context) {
        if (this.dropTask != null)
            this.dropTask.cancel();
    }

    /**
     * Handles click events on this double drop button.
     * <p>
     * The double drop button supports several interaction modes with priority handling:
     * 1. Drop clicks: First drop enters "drop state", second drop within 3 seconds triggers double-drop
     * 2. Specific click handlers (left, right, shift variants, drop)
     * 3. General click handler for other mouse clicks
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    @Override
    public void onClick(NiveriaInventoryClickEvent event, MenuContext context) {
        if (!this.interactable())
            return;

        event.component(this);

        ClickType click = event.getClick();
        if (click == ClickType.DROP || click == ClickType.CONTROL_DROP) {
            handleDropClick(event, context);
            return;
        }

        Consumer<NiveriaInventoryClickEvent> handler = null;
        for (Map.Entry<EnumSet<ClickType>, Consumer<NiveriaInventoryClickEvent>> entry : this.onClickMap.entrySet()) {
            EnumSet<ClickType> clickTypes = entry.getKey();
            if (!clickTypes.contains(event.getClick()))
                continue;

            handler = entry.getValue();

            // Check for a onClick method usage
            // We want to prioritize other more specific method used
            // So we wait for another one to maybe overwrite the onClick
            if (clickTypes.size() != ClickType.values().length)
                break;
        }

        if (handler == null)
            return;

        handler.accept(event);

        if (this.sound != null)
            context.player().playSound(this.sound, Sound.Emitter.self());
    }

    /**
     * Handles drop click events for double-drop functionality.
     * <p>
     * On the first drop click, starts a 3-second timer to enter drop state.
     * If a second drop click occurs within this period, triggers the double-drop action.
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    private void handleDropClick(NiveriaInventoryClickEvent event, MenuContext context) {
        if (this.dropTask != null) {
            this.dropTask.cancel();
            this.dropTask = null;

            if (this.onDoubleDrop != null)
                this.onDoubleDrop.accept(event);
        } else {
            this.dropTask = Task.syncLater(ignored -> {
                this.dropTask = null;
                render(context);
            }, NiveriaAPI.instance(), 3L, TimeUnit.SECONDS);
        }

        if (this.sound != null)
            context.player().playSound(this.sound, Sound.Emitter.self());
    }

    /**
     * Returns the items to be displayed by this double drop button.
     * <p>
     * The double drop button fills all slots within its widthxheight area with the
     * current item (normal or drop state). Returns an empty map if not visible.
     *
     * @param context the menu context
     * @return a map from slot indices to ItemStacks
     */
    @Override
    public Int2ObjectMap<ItemStack> items(MenuContext context) {
        return this.items(context, this.getCurrentItem(context));
    }

    /**
     * Sets the ItemStack to display in normal state.
     *
     * @param item the ItemStack for normal state
     * @return this double drop button for method chaining
     * @throws NullPointerException if item is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public DoubleDropButton item(ItemStack item) {
        Preconditions.checkNotNull(item, "item cannot be null");

        this.item = context -> item;
        return this;
    }

    /**
     * Sets the ItemStack to display in drop state.
     *
     * @param dropItem the ItemStack for drop state
     * @return this double drop button for method chaining
     * @throws NullPointerException if dropItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public DoubleDropButton dropItem(ItemStack dropItem) {
        Preconditions.checkNotNull(dropItem, "dropItem cannot be null");

        this.dropItem = context -> dropItem;
        return this;
    }

    /**
     * Sets a function to provide the ItemStack for normal state.
     *
     * @param item function that returns the ItemStack for normal state
     * @return this double drop button for method chaining
     * @throws NullPointerException if item is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public DoubleDropButton item(Function<MenuContext, ItemStack> item) {
        Preconditions.checkNotNull(item, "item cannot be null");

        this.item = item;
        return this;
    }

    /**
     * Sets a function to provide the ItemStack for drop state.
     *
     * @param dropItem function that returns the ItemStack for drop state
     * @return this double drop button for method chaining
     * @throws NullPointerException if dropItem is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public DoubleDropButton dropItem(Function<MenuContext, ItemStack> dropItem) {
        Preconditions.checkNotNull(dropItem, "dropItem cannot be null");

        this.dropItem = dropItem;
        return this;
    }

    /**
     * Sets the general click handler for mouse clicks.
     *
     * @param onClick the click handler
     * @return this double drop button for method chaining
     * @throws NullPointerException if onClick is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public DoubleDropButton onClick(Consumer<NiveriaInventoryClickEvent> onClick) {
        Preconditions.checkNotNull(onClick, "onClick cannot be null");

        this.onClickMap.put(EnumSet.allOf(ClickType.class), onClick);
        return this;
    }

    /**
     * Sets the left click handler.
     *
     * @param onLeftClick the left click handler
     * @return this double drop button for method chaining
     * @throws NullPointerException if onLeftClick is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public DoubleDropButton onLeftClick(Consumer<NiveriaInventoryClickEvent> onLeftClick) {
        Preconditions.checkNotNull(onLeftClick, "onLeftClick cannot be null");

        this.onClickMap.put(EnumSet.of(ClickType.LEFT), onLeftClick);
        return this;
    }

    /**
     * Sets the right click handler.
     *
     * @param onRightClick the right click handler
     * @return this double drop button for method chaining
     * @throws NullPointerException if onRightClick is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public DoubleDropButton onRightClick(Consumer<NiveriaInventoryClickEvent> onRightClick) {
        Preconditions.checkNotNull(onRightClick, "onRightClick cannot be null");

        this.onClickMap.put(EnumSet.of(ClickType.RIGHT), onRightClick);
        return this;
    }

    /**
     * Sets the double-drop action handler.
     *
     * @param onDoubleDrop the double-drop handler
     * @return this double drop button for method chaining
     * @throws NullPointerException if onDoubleDrop is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public DoubleDropButton onDoubleDrop(Consumer<NiveriaInventoryClickEvent> onDoubleDrop) {
        Preconditions.checkNotNull(onDoubleDrop, "onDoubleDrop cannot be null");

        this.onDoubleDrop = onDoubleDrop;
        return this;
    }

    /**
     * Sets the sound to play when the button is clicked.
     *
     * @param sound the sound to play, or null for no sound
     * @return this double drop button for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public DoubleDropButton sound(@Nullable Sound sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Gets the ItemStack to display based on the current button state.
     *
     * @param context the menu context
     * @return the normal item if no drop task is active, otherwise the drop item
     */
    private ItemStack getCurrentItem(MenuContext context) {
        return this.dropTask == null ? this.item.apply(context) : this.dropItem.apply(context);
    }

    /**
     * Builder class for constructing DoubleDropButton instances with a fluent interface.
     */
    public static class Builder extends MenuComponent.Builder<Builder> {
        private Function<MenuContext, ItemStack> item = context -> ItemStack.of(Material.STONE);
        private Function<MenuContext, ItemStack> dropItem = context -> ItemStack.of(Material.DIRT);

        private final Object2ObjectMap<EnumSet<ClickType>, Consumer<NiveriaInventoryClickEvent>> onClickMap = new Object2ObjectLinkedOpenHashMap<>();
        @Nullable private Consumer<NiveriaInventoryClickEvent> onDoubleDrop;

        @Nullable
        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                BackwardUtils.UI_SOUND_SOURCE,
                1F,
                1F
        );

        /**
         * Sets the ItemStack to display in normal state.
         *
         * @param item the ItemStack for normal state
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
         * Sets the ItemStack to display in drop state.
         *
         * @param dropItem the ItemStack for drop state
         * @return this builder for method chaining
         * @throws NullPointerException if dropItem is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder dropItem(ItemStack dropItem) {
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
        @Contract(value = "_ -> this", mutates = "this")
        public Builder item(Function<MenuContext, ItemStack> item) {
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
        @Contract(value = "_ -> this", mutates = "this")
        public Builder dropItem(Function<MenuContext, ItemStack> dropItem) {
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
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onClick(Consumer<NiveriaInventoryClickEvent> onClick) {
            Preconditions.checkNotNull(onClick, "onClick cannot be null");

            this.onClickMap.put(EnumSet.allOf(ClickType.class), onClick);
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

            this.onClickMap.put(EnumSet.of(ClickType.LEFT), onLeftClick);
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

            this.onClickMap.put(EnumSet.of(ClickType.RIGHT), onRightClick);
            return this;
        }

        /**
         * Sets a click handler for specific click types.
         *
         * @param clickType the click type to handle
         * @param onClick   the click handler
         * @return this builder for method chaining
         * @throws NullPointerException if clickType or onClick is null
         */
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder onClick(ClickType clickType, Consumer<NiveriaInventoryClickEvent> onClick) {
            Preconditions.checkNotNull(clickType, "clickType cannot be null");
            Preconditions.checkNotNull(onClick, "onClick cannot be null");

            this.onClickMap.put(EnumSet.of(clickType), onClick);
            return this;
        }

        /**
         * Sets a click handler for multiple click types.
         *
         * @param clickTypes the click types to handle
         * @param onClick    the click handler
         * @return this builder for method chaining
         * @throws NullPointerException if clickTypes or onClick is null
         */
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder onClick(EnumSet<ClickType> clickTypes, Consumer<NiveriaInventoryClickEvent> onClick) {
            Preconditions.checkNotNull(clickTypes, "clickTypes cannot be null");
            Preconditions.checkNotNull(onClick, "onClick cannot be null");

            this.onClickMap.put(EnumSet.copyOf(clickTypes), onClick);
            return this;
        }

        /**
         * Sets the double-drop action handler.
         *
         * @param onDoubleDrop the double-drop handler
         * @return this builder for method chaining
         * @throws NullPointerException if onDoubleDrop is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onDoubleDrop(Consumer<NiveriaInventoryClickEvent> onDoubleDrop) {
            Preconditions.checkNotNull(onDoubleDrop, "onDoubleDrop cannot be null");

            this.onDoubleDrop = onDoubleDrop;
            return this;
        }

        /**
         * Sets the sound to play when the double drop button is clicked.
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
         * Builds and returns the configured DoubleDropButton instance.
         *
         * @return a new DoubleDropButton with the specified configuration
         */
        public DoubleDropButton build() {
            return new DoubleDropButton(this);
        }
    }
}
