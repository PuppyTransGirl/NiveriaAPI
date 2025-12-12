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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.function.Function;

/**
 * A toggle component that switches between two states when clicked.
 * <p>
 * Each state displays a different item. Useful for on/off switches or
 * binary choices.
 * <p>
 * Use {@link #create()} to obtain a builder for constructing toggles.
 */
public class Toggle extends Component {
    private final Function<MenuContext, ItemStack> onItem, offItem;
    private final Sound sound;
    private final int width, height;

    private boolean currentState;

    private Toggle(
            Function<MenuContext, ItemStack> onItem, Function<MenuContext, ItemStack> offItem,
            boolean currentState,
            Sound sound,
            int width, int height
    ) {
        this.onItem = onItem;
        this.offItem = offItem;
        this.currentState = currentState;
        this.sound = sound;
        this.width = width;
        this.height = height;
    }

    @Override
    public void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context) {
        if (!this.interactable())
            return;

        if (this.sound != null)
            context.player().playSound(this.sound, Sound.Emitter.self());

        this.currentState = !this.currentState;
        this.render(context);
    }

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

    private ItemStack currentItem(@NotNull MenuContext context) {
        return currentState ? this.onItem.apply(context) : this.offItem.apply(context);
    }

    /**
     * Creates a new builder for constructing a Toggle.
     *
     * @return a new builder instance
     */
    @NotNull
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    /**
     * Builder for constructing Toggle instances with a fluent API.
     */
    public static class Builder {
        private Function<MenuContext, ItemStack> onItem = context -> ItemStack.of(Material.STONE);
        private Function<MenuContext, ItemStack> offItem = context -> ItemStack.of(Material.STONE);

        private boolean currentState;

        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                Sound.Source.UI,
                1F,
                1F
        );

        private int width = 1;
        private int height = 1;

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onItem(@NotNull ItemStack onItem) {
            Preconditions.checkNotNull(onItem, "onItem cannot be null");

            this.onItem = context -> onItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offItem(@NotNull ItemStack offItem) {
            Preconditions.checkNotNull(offItem, "offItem cannot be null");

            this.offItem = context -> offItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onItem(@NotNull Function<MenuContext, ItemStack> onItem) {
            Preconditions.checkNotNull(onItem, "onItem cannot be null");

            this.onItem = onItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder offItem(@NotNull Function<MenuContext, ItemStack> offItem) {
            Preconditions.checkNotNull(offItem, "offItem cannot be null");

            this.offItem = offItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder currentState(boolean state) {
            this.currentState = state;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder sound(@Nullable Sound sound) {
            this.sound = sound;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder width(int width) {
            this.width = width;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder height(int height) {
            this.height = height;
            return this;
        }

        @NotNull
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        @NotNull
        public Toggle build() {
            return new Toggle(
                    this.onItem,
                    this.offItem,
                    this.currentState,
                    this.sound,
                    this.width,
                    this.height
            );
        }
    }
}
