package toutouchien.niveriaapi.menu.component.interactive;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * An interactive selector component that allows cycling through multiple options.
 * <p>
 * The Selector component displays a single item representing the currently selected
 * option and allows players to cycle through available options using left-click
 * (next) and right-click (previous). It supports customizable options, default
 * selection, change callbacks, and click sounds.
 *
 * @param <T> the type of values associated with selector options
 */
@NullMarked
public class Selector<T> extends MenuComponent {
    private final ObjectList<Option<T>> options;
    @Nullable
    private Function<MenuContext, T> defaultOption;
    @Nullable
    private Consumer<SelectionChangeEvent<T>> onSelectionChange;
    @Nullable
    private Sound sound;
    private int currentIndex;

    /**
     * Constructs a new Selector with the specified configuration.
     *
     * @param builder the builder containing the selector configuration
     */
    private Selector(Builder<T> builder) {
        super(builder);
        this.options = new ObjectArrayList<>(builder.options);
        this.defaultOption = builder.defaultOption;
        this.onSelectionChange = builder.onSelectionChange;
        this.currentIndex = builder.defaultIndex;

        this.sound = builder.sound;
    }

    /**
     * Creates a new Selector builder instance.
     *
     * @param <T> the type of values for selector options
     * @return a new Selector.Builder for constructing selectors
     */
    @Contract(value = "-> new", pure = true)
    public static <T> Builder<T> create() {
        return new Builder<>();
    }

    /**
     * Called when this selector is added to a menu.
     * <p>
     * If a default option function is configured, it applies the default
     * selection based on the menu context.
     *
     * @param context the menu context
     */
    @Override
    public void onAdd(MenuContext context) {
        if (this.defaultOption == null)
            return;

        T appliedDefaultOption = this.defaultOption.apply(context);
        this.setSelection(appliedDefaultOption);
    }

    /**
     * Handles click events on this selector.
     * <p>
     * Left-click advances to the next option, right-click goes to the previous option.
     * Other click types are ignored. When the selection changes, the configured
     * callback is invoked with details about the change.
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    @Override
    public void onClick(NiveriaInventoryClickEvent event, MenuContext context) {
        if (!this.interactable())
            return;

        int operation = switch (event.getClick()) {
            case LEFT, SHIFT_LEFT, DOUBLE_CLICK -> 1;
            case RIGHT, SHIFT_RIGHT -> -1;
            default -> 0;
        };

        if (operation == 0)
            return;

        if (this.sound != null)
            context.player().playSound(this.sound, Sound.Emitter.self());

        Option<T> oldOption = this.getCurrentOption();
        int oldIndex = this.currentIndex;
        this.currentIndex = Math.floorMod(this.currentIndex + operation, this.options.size());
        Option<T> newOption = this.getCurrentOption();

        if (this.onSelectionChange == null || oldIndex == this.currentIndex)
            return;

        SelectionChangeEvent<T> selectionChangeEvent = new SelectionChangeEvent<>(
                context,
                oldOption.value,
                newOption.value,
                oldIndex,
                this.currentIndex
        );

        this.onSelectionChange.accept(selectionChangeEvent);
    }

    /**
     * Returns the items to be displayed by this selector.
     * <p>
     * The selector fills all slots within its widthxheight area with the
     * current selection's item. Returns an empty map if not visible.
     *
     * @param context the menu context
     * @return a map from slot indices to ItemStacks
     */
    @Override
    public Int2ObjectMap<ItemStack> items(MenuContext context) {
        return this.items(context, this.getCurrentItem(context));
    }

    /**
     * Sets the current selection to the option with the specified value.
     *
     * @param value the value to select
     */
    private void setSelection(T value) {
        for (int i = 0; i < this.options.size(); i++) {
            if (Objects.equals(this.options.get(i).value, value)) {
                this.currentIndex = i;
                break;
            }
        }
    }

    /**
     * Gets the currently selected option.
     *
     * @return the current Option instance
     */
    private Option<T> getCurrentOption() {
        return this.options.get(this.currentIndex);
    }

    /**
     * Gets the ItemStack to display for the current selection.
     *
     * @param context the menu context
     * @return the ItemStack for the current option
     */
    private ItemStack getCurrentItem(MenuContext context) {
        return this.getCurrentOption().item.apply(context);
    }

    /**
     * Adds an option to the selector with a static ItemStack.
     *
     * @param item  the ItemStack to display for this option
     * @param value the value associated with this option (may be null)
     * @return this selector for method chaining
     * @throws NullPointerException if item is null
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    public Selector<T> addOption(ItemStack item, @Nullable T value) {
        Preconditions.checkNotNull(item, "item cannot be null");

        this.options.add(new Option<>(context -> item, value));
        return this;
    }

    /**
     * Adds an option to the selector with a dynamic ItemStack function.
     *
     * @param item  function that provides the ItemStack for this option
     * @param value the value associated with this option (may be null)
     * @return this selector for method chaining
     * @throws NullPointerException if item is null
     */
    @Contract(value = "_, _ -> this", mutates = "this")
    public Selector<T> addOption(Function<MenuContext, ItemStack> item, @Nullable T value) {
        Preconditions.checkNotNull(item, "item cannot be null");

        this.options.add(new Option<>(item, value));
        return this;
    }

    /**
     * Removes the option with the specified value from the selector.
     *
     * @param value the value of the option to remove
     * @return this selector for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Selector<T> removeOption(@Nullable T value) {
        int removedIndex = -1;
        for (int i = 0; i < this.options.size(); i++) {
            if (Objects.equals(this.options.get(i).value, value)) {
                removedIndex = i;
                break;
            }
        }

        if (removedIndex == -1)
            return this;

        if (this.options.size() == 1)
            throw new IllegalStateException("Cannot remove the last option from the selector");

        this.options.remove(removedIndex);

        if (removedIndex < this.currentIndex)
            this.currentIndex--;
        else if (this.currentIndex >= this.options.size())
            this.currentIndex = Math.max(0, this.options.size() - 1);

        return this;
    }

    /**
     * Sets the callback to invoke when the selection changes.
     *
     * @param consumer the selection change callback
     * @return this selector for method chaining
     * @throws NullPointerException if consumer is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Selector<T> onSelectionChange(Consumer<SelectionChangeEvent<T>> consumer) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");

        this.onSelectionChange = consumer;
        return this;
    }

    /**
     * Sets a function to determine the default option based on context.
     *
     * @param defaultOption function that returns the default value to select
     * @return this selector for method chaining
     * @throws NullPointerException if defaultOption is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Selector<T> defaultOption(Function<MenuContext, T> defaultOption) {
        Preconditions.checkNotNull(defaultOption, "defaultOption cannot be null");

        this.defaultOption = defaultOption;
        return this;
    }

    /**
     * Sets the sound to play when the selector is clicked.
     *
     * @param sound the sound to play, or null for no sound
     * @return this selector for method chaining
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Selector<T> sound(@Nullable Sound sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Event record containing information about a selection change.
     *
     * @param context  the menu context where the change occurred
     * @param oldValue the previously selected value
     * @param newValue the newly selected value
     * @param oldIndex the previous selection index
     * @param newIndex the new selection index
     * @param <T>      the type of values in the selector
     */
    public record SelectionChangeEvent<T>(MenuContext context, @Nullable T oldValue, @Nullable T newValue,
                                          @NonNegative int oldIndex, @NonNegative int newIndex) {

    }

    /**
     * Record representing a selectable option in the selector.
     *
     * @param item  function that provides the ItemStack to display for this option
     * @param value the value associated with this option (may be null)
     * @param <T>   the type of the option value
     */
    public record Option<T>(Function<MenuContext, ItemStack> item, @Nullable T value) {

    }

    /**
     * Builder class for constructing Selector instances with a fluent interface.
     *
     * @param <T> the type of values for selector options
     */
    public static class Builder<T> extends MenuComponent.Builder<Builder<T>> {
        private final ObjectList<Option<T>> options = new ObjectArrayList<>();
        @Nullable
        private Function<MenuContext, T> defaultOption;
        @Nullable
        private Consumer<SelectionChangeEvent<T>> onSelectionChange;

        private int defaultIndex = 0;

        @Nullable
        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                Sound.Source.UI,
                1F,
                1F
        );

        /**
         * Adds an option to the selector with a static ItemStack.
         *
         * @param item  the ItemStack to display for this option
         * @param value the value associated with this option (may be null)
         * @return this builder for method chaining
         * @throws NullPointerException if item is null
         */
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder<T> addOption(ItemStack item, @Nullable T value) {
            Preconditions.checkNotNull(item, "item cannot be null");

            this.options.add(new Option<>(context -> item, value));
            return this;
        }

        /**
         * Adds an option to the selector with a dynamic ItemStack function.
         *
         * @param item  function that provides the ItemStack for this option
         * @param value the value associated with this option (may be null)
         * @return this builder for method chaining
         * @throws NullPointerException if item is null
         */
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder<T> addOption(Function<MenuContext, ItemStack> item, @Nullable T value) {
            Preconditions.checkNotNull(item, "item cannot be null");

            this.options.add(new Option<>(item, value));
            return this;
        }

        /**
         * Sets the default selected index.
         *
         * @param index the index to select by default (0-based)
         * @return this builder for method chaining
         * @throws IllegalArgumentException if index is negative
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder<T> defaultIndex(@NonNegative int index) {
            Preconditions.checkArgument(index >= 0, "index cannot be negative: %s", index);

            this.defaultIndex = index;
            return this;
        }

        /**
         * Sets the callback to invoke when the selection changes.
         *
         * @param consumer the selection change callback
         * @return this builder for method chaining
         * @throws NullPointerException if consumer is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder<T> onSelectionChange(Consumer<SelectionChangeEvent<T>> consumer) {
            Preconditions.checkNotNull(consumer, "consumer cannot be null");

            this.onSelectionChange = consumer;
            return this;
        }

        /**
         * Sets a function to determine the default option based on context.
         *
         * @param defaultOption function that returns the default value to select
         * @return this builder for method chaining
         * @throws NullPointerException if defaultOption is null
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder<T> defaultOption(Function<MenuContext, T> defaultOption) {
            Preconditions.checkNotNull(defaultOption, "defaultOption cannot be null");

            this.defaultOption = defaultOption;
            return this;
        }

        /**
         * Sets the sound to play when the selector is clicked.
         *
         * @param sound the sound to play, or null for no sound
         * @return this builder for method chaining
         */
        @Contract(value = "_ -> this", mutates = "this")
        public Builder<T> sound(@Nullable Sound sound) {
            this.sound = sound;
            return this;
        }

        /**
         * Builds and returns the configured Selector instance.
         *
         * @return a new Selector with the specified configuration
         */
        public Selector<T> build() {
            Preconditions.checkArgument(
                    !this.options.isEmpty(),
                    "Selector must have at least one option"
            );

            Preconditions.checkArgument(
                    this.defaultIndex < this.options.size(),
                    "defaultIndex (%s) must be less than options size (%s)",
                    this.defaultIndex, this.options.size()
            );

            return new Selector<>(this);
        }
    }
}