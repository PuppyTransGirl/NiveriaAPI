package toutouchien.niveriaapi.menu.component.interactive;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class Selector<T> extends Component {
    private final ObjectList<Option<T>> options;
    private final Function<MenuContext, T> defaultOption;
    private final Consumer<SelectionChangeEvent<T>> onSelectionChange;

    private final Sound sound;

    private final int width, height;

    private int currentIndex;

    private Selector(
            ObjectList<Option<T>> options,
            Function<MenuContext, T> defaultOption,
            Consumer<SelectionChangeEvent<T>> onSelectionChange,
            int defaultIndex,
            Sound sound,
            int width, int height
    ) {
        this.options = options;
        this.defaultOption = defaultOption;
        this.onSelectionChange = onSelectionChange;
        this.currentIndex = defaultIndex;

        this.sound = sound;

        this.width = width;
        this.height = height;
    }

    @Override
    public void onAdd(@NotNull MenuContext context) {
        if (this.defaultOption == null)
            return;

        T appliedDefaultOption = this.defaultOption.apply(context);
        this.selection(appliedDefaultOption);
    }

    @Override
    public void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context) {
        if (!this.interactable())
            return;

        if (this.sound != null)
            context.player().playSound(this.sound, Sound.Emitter.self());

        int operation = switch (event.getClick()) {
            case LEFT, SHIFT_LEFT, DOUBLE_CLICK -> 1;
            case RIGHT, SHIFT_RIGHT -> -1;
            default -> 0;
        };

        if (operation == 0)
            return;

        Option<T> oldOption = this.currentOption();
        int oldIndex = this.currentIndex;
        this.currentIndex = Math.floorMod(this.currentIndex + operation, this.options.size());
        Option<T> newOption = this.currentOption();

        if (this.onSelectionChange == null)
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
    public IntSet slots() {
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

    private void selection(T value) {
        for (int i = 0; i < this.options.size(); i++) {
            if (Objects.equals(this.options.get(i).value, value))
                this.currentIndex = i;
        }
    }

    private Option<T> currentOption() {
        return this.options.get(this.currentIndex);
    }

    private ItemStack currentItem(@NotNull MenuContext context) {
        return this.currentOption().item.apply(context);
    }

    public record SelectionChangeEvent<T>(@NotNull MenuContext context, @Nullable T oldValue, @Nullable T newValue,
                                          @NonNegative int oldIndex, @NonNegative int newIndex) {

    }

    public record Option<T>(@NotNull Function<MenuContext, ItemStack> item, @Nullable T value) {

    }

    @NotNull
    @Contract(value = "-> new", pure = true)
    public static <T> Builder<T> create() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private final ObjectList<Option<T>> options = new ObjectArrayList<>();
        private Function<MenuContext, T> defaultOption;
        private Consumer<SelectionChangeEvent<T>> onSelectionChange;

        private int defaultIndex = 0;

        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                Sound.Source.UI,
                1F,
                1F
        );

        private int width = 1;
        private int height = 1;

        @NotNull
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder<T> addOption(@NotNull ItemStack item, @Nullable T value) {
            Preconditions.checkNotNull(item, "item cannot be null");

            options.add(new Option<>(context -> item, value));
            return this;
        }

        @NotNull
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder<T> addOption(@NotNull Function<MenuContext, ItemStack> item, @Nullable T value) {
            Preconditions.checkNotNull(item, "item cannot be null");

            options.add(new Option<>(item, value));
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder<T> defaultIndex(@NonNegative int index) {
            Preconditions.checkArgument(index >= 0, "index cannot be negative: %d", index);

            this.defaultIndex = index;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder<T> onSelectionChange(@NotNull Consumer<SelectionChangeEvent<T>> consumer) {
            Preconditions.checkNotNull(consumer, "consumer cannot be null");

            this.onSelectionChange = consumer;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder<T> defaultOption(@NotNull Function<MenuContext, T> defaultOption) {
            Preconditions.checkNotNull(defaultOption, "defaultOption cannot be null");

            this.defaultOption = defaultOption;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder<T> sound(@Nullable Sound sound) {
            this.sound = sound;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder<T> width(@Positive int width) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %d", width);

            this.width = width;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder<T> height(@Positive int height) {
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %d", height);
            this.height = height;
            return this;
        }

        @NotNull
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder<T> size(@Positive int width, @Positive int height) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %d", width);
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %d", height);

            this.width = width;
            this.height = height;
            return this;
        }

        @NotNull
        public Selector<T> build() {
            return new Selector<>(
                    this.options,
                    this.defaultOption,
                    this.onSelectionChange,
                    this.defaultIndex,
                    this.sound,
                    this.width,
                    this.height
            );
        }
    }
}
