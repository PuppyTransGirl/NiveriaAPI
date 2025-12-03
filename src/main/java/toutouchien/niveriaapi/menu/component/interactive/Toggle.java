package toutouchien.niveriaapi.menu.component.interactive;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

public class Toggle extends Component {
    private final Function<MenuContext, ItemStack> onItem, offItem;
    private final Sound sound;
    private final int width, height;

    private boolean currentState;

    public Toggle(
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
    public void onAdd(@NotNull MenuContext context) {

    }

    @Override
    public void onRemove(@NotNull MenuContext context) {

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

        this.currentState = !this.currentState;
        this.render(context);
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

    private ItemStack currentItem(@NotNull MenuContext context) {
        return currentState ? this.onItem.apply(context) : this.offItem.apply(context);
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private Function<MenuContext, ItemStack> onItem = context -> ItemStack.of(Material.STONE);
        private Function<MenuContext, ItemStack> offItem = context -> ItemStack.of(Material.STONE);

        private boolean currentState;

        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                Sound.Source.UI,
                1F,
                0F // Will be randomized later
        );

        private int width = 1;
        private int height = 1;

        public Builder onItem(ItemStack onItem) {
            this.onItem = context -> onItem;
            return this;
        }

        public Builder offItem(ItemStack offItem) {
            this.offItem = context -> offItem;
            return this;
        }

        public Builder onItem(Function<MenuContext, ItemStack> onItem) {
            this.onItem = onItem;
            return this;
        }

        public Builder offItem(Function<MenuContext, ItemStack> offItem) {
            this.offItem = offItem;
            return this;
        }

        public Builder currentState(boolean state) {
            this.currentState = state;
            return this;
        }

        public Builder sound(Sound sound) {
            this.sound = sound;
            return this;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder height(int height) {
            this.height = height;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

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
