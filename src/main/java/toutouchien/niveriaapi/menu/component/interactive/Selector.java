package toutouchien.niveriaapi.menu.component.interactive;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.concurrent.ThreadLocalRandom;
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

        if (this.sound != null) {
            Sound finalSound;
            if (this.sound.pitch() == 0F)
                finalSound = Sound.sound(this.sound)
                        .pitch(ThreadLocalRandom.current().nextFloat())
                        .build();
            else
                finalSound = this.sound;

            context.player().playSound(finalSound, Sound.Emitter.self());
        }

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

    private void selection(T value) {
        for (int i = 0; i < this.options.size(); i++) {
            if (this.options.get(i).value().equals(value))
                this.currentIndex = i;
        }
    }

    private Option<T> currentOption() {
        return this.options.get(this.currentIndex);
    }

    private ItemStack currentItem(@NotNull MenuContext context) {
        return this.currentOption().item.apply(context);
    }

    public record SelectionChangeEvent<T>(MenuContext context, T oldValue, T newValue, int oldIndex, int newIndex) {

    }

    public record Option<T>(Function<MenuContext, ItemStack> item, T value) {

    }

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
                0F // Will be randomized later
        );

        private int width = 1;
        private int height = 1;

        public Builder<T> addOption(ItemStack item, T value) {
            options.add(new Option<>(context -> item, value));
            return this;
        }

        public Builder<T> addOption(Function<MenuContext, ItemStack> item, T value) {
            options.add(new Option<>(item, value));
            return this;
        }

        public Builder<T> defaultIndex(int index) {
            this.defaultIndex = index;
            return this;
        }

        public Builder<T> onSelectionChange(Consumer<SelectionChangeEvent<T>> consumer) {
            this.onSelectionChange = consumer;
            return this;
        }

        public Builder<T> defaultOption(Function<MenuContext, T> defaultOption) {
            this.defaultOption = defaultOption;
            return this;
        }

        public Builder<T> sound(Sound sound) {
            this.sound = sound;
            return this;
        }

        public Builder<T> width(int width) {
            this.width = width;
            return this;
        }

        public Builder<T> height(int height) {
            this.height = height;
            return this;
        }

        public Builder<T> size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

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
