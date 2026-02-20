package toutouchien.niveriaapi.menu.component.display;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.function.Function;

/**
 * A display component that shows an ItemStack without performing actions.
 * <p>
 * The Icon component is used for displaying decorative or informational items
 * in menus. Unlike interactive components, icons typically don't perform actions
 * when clicked, though they can optionally play a sound for audio feedback.
 */
@NullMarked
public class Icon extends MenuComponent {
    private Function<MenuContext, ItemStack> item;
    @Nullable
    private Sound sound;

    /**
     * Constructs a new Icon with the specified configuration.
     *
     * @param builder the builder containing the icon configuration
     */
    private Icon(Builder builder) {
        super(builder);
        this.item = builder.item;

        this.sound = builder.sound;
    }

    /**
     * Creates a new Icon builder instance.
     *
     * @return a new Icon.Builder for constructing icons
     */
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
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
    public void onClick(NiveriaInventoryClickEvent event, MenuContext context) {
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
    @Override
    public Int2ObjectMap<ItemStack> items(MenuContext context) {
        return this.items(context, this.item.apply(context));
    }

    /**
     * Sets the ItemStack to display for this icon.
     *
     * @param item the ItemStack to display
     * @return this icon for method chaining
     * @throws NullPointerException if item is null
     */
    @Contract(value = "_ -> this", mutates = "this")
    public Icon item(ItemStack item) {
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
    @Contract(value = "_ -> this", mutates = "this")
    public Icon item(Function<MenuContext, ItemStack> item) {
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
    @Contract(value = "_ -> this", mutates = "this")
    public Icon sound(@Nullable Sound sound) {
        this.sound = sound;
        return this;
    }

    /**
     * Builder class for constructing Icon instances with a fluent interface.
     */
    public static class Builder extends MenuComponent.Builder<Builder> {
        private Function<MenuContext, ItemStack> item = context -> ItemStack.of(Material.STONE);

        @Nullable private Sound sound = null;

        /**
         * Sets the ItemStack to display for this icon.
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
         * Sets a function to provide the ItemStack for this icon.
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
         * Sets the sound to play when the icon is clicked.
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
         * Builds and returns the configured Icon instance.
         *
         * @return a new Icon with the specified configuration
         */
        public Icon build() {
            return new Icon(this);
        }
    }
}