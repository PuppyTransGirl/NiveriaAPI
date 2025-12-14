package toutouchien.niveriaapi.menu.component.display;

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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.function.Function;

/**
 * A display component that shows an ItemStack without performing actions.
 * <p>
 * The Icon component is used for displaying decorative or informational items
 * in menus. Unlike interactive components, icons typically don't perform actions
 * when clicked, though they can optionally play a sound for audio feedback.
 * Icons can span multiple slots with configurable width and height.
 */
public class Icon extends Component {
    private Function<MenuContext, ItemStack> item;
    private Sound sound;

    private final int width, height;

    /**
     * Constructs a new Icon with the specified parameters.
     *
     * @param item   function that provides the ItemStack to display
     * @param sound  sound to play when clicked (may be null for no sound)
     * @param width  width of the icon in slots
     * @param height height of the icon in rows
     */
    private Icon(
            Function<MenuContext, ItemStack> item,
            Sound sound,
            int width, int height
    ) {
        this.item = item;

        this.sound = sound;

        this.width = width;
        this.height = height;
    }

    /**
     * Handles click events on this icon.
     * <p>
     * Icons only provide audio feedback when clicked - they don't perform
     * any functional actions. If a sound is configured and the icon is
     * interactable, the sound will be played to the player.
     *
     * @param event   the inventory click event
     * @param context the menu context
     */
    @Override
    public void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context) {
        if (!this.interactable())
            return;

        if (this.sound == null)
            return;

        context.player().playSound(this.sound, Sound.Emitter.self());
    }

    /**
     * Returns the items to be displayed by this icon.
     * <p>
     * The icon fills all slots within its widthxheight area with the
     * same ItemStack. Returns an empty map if not visible.
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

        ItemStack baseItem = this.item.apply(context);
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
     * Returns the set of slots occupied by this icon.
     * <p>
     * Includes all slots within the icon's widthxheight area.
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
     * Sets the ItemStack to display for this icon.
     *
     * @param item the ItemStack to display
     * @return this icon for method chaining
     * @throws NullPointerException if item is null
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Icon item(@NotNull ItemStack item) {
        Preconditions.checkNotNull(item, "item cannot be null");

        this.item = context -> item;
        return this;
    }

    /**
     * Sets a function to provide the ItemStack for this icon.
     *
     * @param item function that returns the ItemStack to display
     * @return this icon for method chaining
     * @throws NullPointerException if item is null
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Icon item(@NotNull Function<MenuContext, ItemStack> item) {
        Preconditions.checkNotNull(item, "item cannot be null");

        this.item = item;
        return this;
    }

    /**
     * Sets the sound to play when the icon is clicked.
     *
     * @param sound the sound to play, or null for no sound
     * @return this icon for method chaining
     */
    @NotNull
    @Contract(value = "_ -> this", mutates = "this")
    public Icon sound(@Nullable Sound sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Returns the width of this icon in slots.
     *
     * @return the icon width
     */
    @Positive
    @Override
    public int width() {
        return this.width;
    }

    /**
     * Returns the height of this icon in rows.
     *
     * @return the icon height
     */
    @Positive
    @Override
    public int height() {
        return this.height;
    }

    /**
     * Creates a new Icon builder instance.
     *
     * @return a new Icon.Builder for constructing icons
     */
    @NotNull
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * Builder class for constructing Icon instances with a fluent interface.
     */
    public static class Builder {
        private Function<MenuContext, ItemStack> item = context -> ItemStack.of(Material.STONE);

        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                Sound.Source.UI,
                1F,
                1F
        );

        private int width = 1;
        private int height = 1;

        /**
         * Sets the ItemStack to display for this icon.
         *
         * @param item the ItemStack to display
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
         * Sets a function to provide the ItemStack for this icon.
         *
         * @param item function that returns the ItemStack to display
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
         * Sets the sound to play when the icon is clicked.
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
         * Sets the width of the icon in slots.
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
         * Sets the height of the icon in rows.
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
         * Sets both width and height of the icon.
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
         * Builds and returns the configured Icon instance.
         *
         * @return a new Icon with the specified configuration
         */
        @NotNull
        public Icon build() {
            return new Icon(
                    this.item,
                    this.sound,
                    this.width,
                    this.height
            );
        }
    }
}