package toutouchien.niveriaapi.menu.component.interactive;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;

public class Button extends Component {
    private final Function<MenuContext, ItemStack> item;

    private final Consumer<NiveriaInventoryClickEvent> onClick, onLeftClick, onRightClick, onDrop;

    private final Sound sound;

    private final Function<MenuContext, ObjectList<ItemStack>> animationFrames;
    private final int animationInterval;
    private final boolean stopAnimationOnHide;
    private BukkitTask animationTask;
    private int currentFrame;

    private final Function<MenuContext, ItemStack> dynamicItem;
    private final int updateInterval;
    private final boolean stopUpdatesOnHide;
    private BukkitTask updateTask;

    private final int width, height;

    private Button(
            Function<MenuContext, ItemStack> item,
            Consumer<NiveriaInventoryClickEvent> onClick,
            Consumer<NiveriaInventoryClickEvent> onLeftClick, Consumer<NiveriaInventoryClickEvent> onRightClick,
            Consumer<NiveriaInventoryClickEvent> onDrop,
            Sound sound,
            Function<MenuContext, ObjectList<ItemStack>> animationFrames, int animationInterval, boolean stopAnimationOnHide,
            Function<MenuContext, ItemStack> dynamicItem, int updateInterval, boolean stopUpdatesOnHide,
            int width, int height
    ) {
        this.item = item;

        this.onClick = onClick;
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
        this.onDrop = onDrop;

        this.sound = sound;

        this.animationFrames = animationFrames;
        this.animationInterval = animationInterval;
        this.stopAnimationOnHide = stopAnimationOnHide;

        this.dynamicItem = dynamicItem;
        this.updateInterval = updateInterval;
        this.stopUpdatesOnHide = stopUpdatesOnHide;

        this.width = width;
        this.height = height;
    }

    @Override
    public void onAdd(@NotNull MenuContext context) {
        if (this.animationFrames != null && this.animationInterval > 0)
            this.startAnimation(context);

        if (this.dynamicItem != null && this.updateInterval > 0)
            this.startUpdates(context);
    }

    @Override
    public void onRemove(@NotNull MenuContext context) {
        this.stopAnimation();
    }

    @Override
    public void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context) {
        if (!this.interactable())
            return;

        if (this.sound != null)
            context.player().playSound(this.sound, Sound.Emitter.self());

        Consumer<NiveriaInventoryClickEvent> handler = switch (event.getClick()) {
            case LEFT, SHIFT_LEFT -> this.onLeftClick;
            case RIGHT, SHIFT_RIGHT -> this.onRightClick;
            case DROP, CONTROL_DROP -> this.onDrop;
            default -> null;
        };

        if (handler != null) {
            handler.accept(event);
            return;
        }

        if (this.onClick != null && event.getClick() != ClickType.DROP)
            this.onClick.accept(event);
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

    private void startAnimation(@NotNull MenuContext context) {
        this.animationTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled() || (stopAnimationOnHide && !visible())) {
                    stopAnimation();
                    return;
                }

                List<ItemStack> frames = animationFrames.apply(context);

                currentFrame = (currentFrame + 1) % frames.size();
                render(context);
            }
        }.runTaskTimer(NiveriaAPI.instance(), this.animationInterval, this.animationInterval);
    }

    private void stopAnimation() {
        this.currentFrame = 0;

        if (this.animationTask == null || this.animationTask.isCancelled())
            return;

        this.animationTask.cancel();
        this.animationTask = null;
    }

    private void startUpdates(@NotNull MenuContext context) {
        this.updateTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!enabled() || (stopUpdatesOnHide && !visible())) {
                    stopUpdates();
                    return;
                }

                render(context);
            }
        }.runTaskTimer(NiveriaAPI.instance(), this.updateInterval, this.updateInterval);
    }

    private void stopUpdates() {
        if (this.updateTask == null || this.updateTask.isCancelled())
            return;

        this.updateTask.cancel();
        this.updateTask = null;
    }

    private ItemStack currentItem(@NotNull MenuContext context) {
        if (this.dynamicItem != null)
            return this.dynamicItem.apply(context);

        if (this.animationFrames != null)
            return this.animationFrames.apply(context).get(this.currentFrame);

        return this.item.apply(context);
    }

    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private Function<MenuContext, ItemStack> item = context -> ItemStack.of(Material.STONE);

        private Consumer<NiveriaInventoryClickEvent> onClick, onLeftClick, onRightClick, onDrop;

        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                Sound.Source.UI,
                1F,
                ThreadLocalRandom.current().nextFloat()
        );

        private Function<MenuContext, ObjectList<ItemStack>> animationFrames;
        private int animationInterval = 20;
        private boolean stopAnimationOnHide = true;

        private Function<MenuContext, ItemStack> dynamicItem;
        private int updateInterval = 20;
        private boolean stopUpdatesOnHide = false;

        private int width = 1;
        private int height = 1;

        public Builder item(ItemStack item) {
            this.item = context -> item;
            return this;
        }

        public Builder item(Function<MenuContext, ItemStack> item) {
            this.dynamicItem = item;
            return this;
        }

        public Builder onClick(Consumer<NiveriaInventoryClickEvent> onClick) {
            this.onClick = onClick;
            return this;
        }

        public Builder onLeftClick(Consumer<NiveriaInventoryClickEvent> onLeftClick) {
            this.onLeftClick = onLeftClick;
            return this;
        }

        public Builder onRightClick(Consumer<NiveriaInventoryClickEvent> onRightClick) {
            this.onRightClick = onRightClick;
            return this;
        }

        public Builder onDrop(Consumer<NiveriaInventoryClickEvent> onDrop) {
            this.onDrop = onDrop;
            return this;
        }

        public Builder sound(Sound sound) {
            this.sound = sound;
            return this;
        }

        public Builder animationFrames(Function<MenuContext, ObjectList<ItemStack>> animationFrames) {
            this.animationFrames = animationFrames;
            return this;
        }

        public Builder animationInterval(int animationInterval) {
            this.animationInterval = animationInterval;
            return this;
        }

        public Builder stopAnimationOnHide(boolean stopAnimationOnHide) {
            this.stopAnimationOnHide = stopAnimationOnHide;
            return this;
        }

        public Builder dynamicItem(Function<MenuContext, ItemStack> dynamicItem) {
            this.dynamicItem = dynamicItem;
            return this;
        }

        public Builder updateInterval(int updateInterval) {
            this.updateInterval = updateInterval;
            return this;
        }

        public Builder stopUpdatesOnHide(boolean stopUpdatesOnHide) {
            this.stopUpdatesOnHide = stopUpdatesOnHide;
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

        public Button build() {
            return new Button(
                    this.item,
                    this.onClick,
                    this.onLeftClick,
                    this.onRightClick,
                    this.onDrop,
                    this.sound,
                    this.animationFrames,
                    this.animationInterval,
                    this.stopAnimationOnHide,
                    this.dynamicItem,
                    this.updateInterval,
                    this.stopUpdatesOnHide,
                    this.width,
                    this.height
            );
        }
    }
}
