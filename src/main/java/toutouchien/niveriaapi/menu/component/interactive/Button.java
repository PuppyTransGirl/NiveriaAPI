package toutouchien.niveriaapi.menu.component.interactive;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;
import java.util.function.Function;

public class Button extends Component {
    private final ItemStack item;

    private final Consumer<NiveriaInventoryClickEvent> onClick, onLeftClick, onRightClick, onDrop;

    private final Sound sound;

    private final List<ItemStack> animationFrames;
    private final int animationInterval;
    private BukkitTask animationTask;
    private int currentFrame;

    private final Function<MenuContext, ItemStack> dynamicItem;

    private Button(
            ItemStack item,
            Consumer<NiveriaInventoryClickEvent> onClick,
            Consumer<NiveriaInventoryClickEvent> onLeftClick, Consumer<NiveriaInventoryClickEvent> onRightClick,
            Consumer<NiveriaInventoryClickEvent> onDrop,
            Sound sound,
            List<ItemStack> animationFrames, int animationInterval,
            Function<MenuContext, ItemStack> dynamicItem
    ) {
        this.item = item;

        this.onClick = onClick;
        this.onLeftClick = onLeftClick;
        this.onRightClick = onRightClick;
        this.onDrop = onDrop;

        this.sound = sound;

        this.animationFrames = animationFrames;
        this.animationInterval = animationInterval;

        this.dynamicItem = dynamicItem;
    }

    @Override
    protected void onAdd(@NotNull MenuContext context) {
        if (this.animationFrames == null || this.animationFrames.isEmpty() || this.animationInterval <= 0)
            return;

        this.startAnimation(context);
    }

    @Override
    protected void onRemove(@NotNull MenuContext context) {
        this.stopAnimation();
    }

    @Override
    protected void onClick(@NotNull NiveriaInventoryClickEvent event, @NotNull MenuContext context) {
        if (!this.interactable())
            return;

        if (this.sound != null)
            context.player().playSound(this.sound, Sound.Emitter.self());

        if (this.onClick != null)
            this.onClick.accept(event);

        switch (event.getClick()) {
            case LEFT, SHIFT_LEFT -> {
                if (this.onLeftClick != null)
                    this.onLeftClick.accept(event);
            }

            case RIGHT, SHIFT_RIGHT -> {
                if (this.onRightClick != null)
                    this.onRightClick.accept(event);
            }

            case DROP, CONTROL_DROP -> {
                if (this.onDrop != null)
                    this.onDrop.accept(event);
            }

            default -> {
                // Do nothing for other click types
            }
        }
    }

    private void startAnimation(@NotNull MenuContext context) {
        this.animationTask = new BukkitRunnable() {
            @Override
            public void run() {
                Menu menu = context.menu();
                if (!interactable()) {
                    stopAnimation();
                    return;
                }

                currentFrame = (currentFrame + 1) % animationFrames.size();
                ItemStack frameItem = animationFrames.get(currentFrame);
                menu.getInventory().setItem(slot(), frameItem);
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

    public static class Builder {
        private ItemStack item = ItemStack.of(Material.STONE);
        private Consumer<NiveriaInventoryClickEvent> onClick, onLeftClick, onRightClick, onDrop;
        private Sound sound = Sound.sound(
                Key.key("minecraft", "ui.button.click"),
                Sound.Source.UI,
                1F,
                ThreadLocalRandom.current().nextFloat()
        );

        private List<ItemStack> animationFrames;
        private int animationInterval = 20;

        private Function<MenuContext, ItemStack> dynamicItem;

        public Builder item(ItemStack item) {
            this.item = item;
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

        public Builder animationFrames(List<ItemStack> animationFrames) {
            this.animationFrames = animationFrames;
            return this;
        }

        public Builder animationInterval(int animationInterval) {
            this.animationInterval = animationInterval;
            return this;
        }

        public Builder dynamicItem(Function<MenuContext, ItemStack> dynamicItem) {
            this.dynamicItem = dynamicItem;
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
                    this.dynamicItem
            );
        }
    }
}
