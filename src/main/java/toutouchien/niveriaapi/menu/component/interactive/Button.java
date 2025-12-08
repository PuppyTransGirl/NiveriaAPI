package toutouchien.niveriaapi.menu.component.interactive;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class Button extends Component {
    private final Function<MenuContext, ItemStack> item;

    private final Consumer<NiveriaInventoryClickEvent> onClick, onLeftClick, onRightClick, onShiftLeftClick, onShiftRightClick, onDrop;

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
            Consumer<NiveriaInventoryClickEvent> onShiftLeftClick, Consumer<NiveriaInventoryClickEvent> onShiftRightClick,
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
        this.onShiftLeftClick = onShiftLeftClick;
        this.onShiftRightClick = onShiftRightClick;
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
            case LEFT, DOUBLE_CLICK -> this.onLeftClick;
            case RIGHT -> this.onRightClick;
            case SHIFT_LEFT -> this.onShiftLeftClick;
            case SHIFT_RIGHT -> this.onShiftRightClick;
            case DROP, CONTROL_DROP -> this.onDrop;
            default -> null;
        };

        if (handler != null) {
            handler.accept(event);
            return;
        }

        if (this.onClick != null && event.getClick().isMouseClick())
            this.onClick.accept(event);
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

    @NotNull
    @Contract(value = "-> new", pure = true)
    public static Builder create() {
        return new Builder();
    }

    public static class Builder {
        private Function<MenuContext, ItemStack> item = context -> ItemStack.of(Material.STONE);

        private Consumer<NiveriaInventoryClickEvent> onClick, onLeftClick, onRightClick, onShiftLeftClick, onShiftRightClick, onDrop;

        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                Sound.Source.UI,
                1F,
                1F
        );

        private Function<MenuContext, ObjectList<ItemStack>> animationFrames;
        private int animationInterval = 20;
        private boolean stopAnimationOnHide = true;

        private Function<MenuContext, ItemStack> dynamicItem;
        private int updateInterval = 20;
        private boolean stopUpdatesOnHide = false;

        private int width = 1;
        private int height = 1;

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder item(@NotNull ItemStack item) {
            Preconditions.checkNotNull(item, "item cannot be null");

            this.item = context -> item;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder item(@NotNull Function<MenuContext, ItemStack> item) {
            Preconditions.checkNotNull(item, "item cannot be null");

            this.item = item;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onClick(@NotNull Consumer<NiveriaInventoryClickEvent> onClick) {
            Preconditions.checkNotNull(onClick, "onClick cannot be null");

            this.onClick = onClick;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onLeftClick(@NotNull Consumer<NiveriaInventoryClickEvent> onLeftClick) {
            Preconditions.checkNotNull(onLeftClick, "onLeftClick cannot be null");

            this.onLeftClick = onLeftClick;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onRightClick(@NotNull Consumer<NiveriaInventoryClickEvent> onRightClick) {
            Preconditions.checkNotNull(onRightClick, "onRightClick cannot be null");

            this.onRightClick = onRightClick;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onShiftLeftClick(@NotNull Consumer<NiveriaInventoryClickEvent> onShiftLeftClick) {
            Preconditions.checkNotNull(onShiftLeftClick, "onShiftLeftClick cannot be null");

            this.onShiftLeftClick = onShiftLeftClick;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onShiftRightClick(@NotNull Consumer<NiveriaInventoryClickEvent> onShiftRightClick) {
            Preconditions.checkNotNull(onShiftRightClick, "onShiftRightClick cannot be null");

            this.onShiftRightClick = onShiftRightClick;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder onDrop(@NotNull Consumer<NiveriaInventoryClickEvent> onDrop) {
            Preconditions.checkNotNull(onDrop, "onDrop cannot be null");

            this.onDrop = onDrop;
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
        public Builder animationFrames(@NotNull Function<MenuContext, ObjectList<ItemStack>> animationFrames) {
            Preconditions.checkNotNull(animationFrames, "animationFrames cannot be null");

            this.animationFrames = animationFrames;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder animationInterval(@Positive int animationInterval) {
            Preconditions.checkArgument(animationInterval >= 1, "animationInterval cannot be less than 1: %d", animationInterval);

            this.animationInterval = animationInterval;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder stopAnimationOnHide(boolean stopAnimationOnHide) {
            this.stopAnimationOnHide = stopAnimationOnHide;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder dynamicItem(@NotNull Function<MenuContext, ItemStack> dynamicItem) {
            Preconditions.checkNotNull(dynamicItem, "dynamicItem cannot be null");

            this.dynamicItem = dynamicItem;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder updateInterval(@Positive int updateInterval) {
            Preconditions.checkArgument(updateInterval >= 1, "updateInterval cannot be less than 1: %d", updateInterval);

            this.updateInterval = updateInterval;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder stopUpdatesOnHide(boolean stopUpdatesOnHide) {
            this.stopUpdatesOnHide = stopUpdatesOnHide;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder width(@Positive int width) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %d", width);

            this.width = width;
            return this;
        }

        @NotNull
        @Contract(value = "_ -> this", mutates = "this")
        public Builder height(@Positive int height) {
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %d", height);

            this.height = height;
            return this;
        }

        @NotNull
        @Contract(value = "_, _ -> this", mutates = "this")
        public Builder size(@Positive int width, @Positive int height) {
            Preconditions.checkArgument(width >= 1, "width cannot be less than 1: %d", width);
            Preconditions.checkArgument(height >= 1, "height cannot be less than 1: %d", height);

            this.width = width;
            this.height = height;
            return this;
        }

        @NotNull
        public Button build() {
            return new Button(
                    this.item,
                    this.onClick,
                    this.onLeftClick,
                    this.onRightClick,
                    this.onShiftLeftClick,
                    this.onShiftRightClick,
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
