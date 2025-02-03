package toutouchien.niveriaapi.menu.component.interactive;

import net.kyori.adventure.sound.Sound;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class Button extends Component {
	private final ItemStack item;
	private final Consumer<InventoryClickEvent> onClick, onLeftClick, onRightClick, onDrop;
	private final Sound sound;

	private final List<ItemStack> animationFrames;
	private final int animationInterval;
	private BukkitTask animationTask;
	private int currentFrame;

	private final Function<MenuContext, ItemStack> dynamicItem;

	private Button(Builder builder) {
		this.item = builder.item;
		this.sound = builder.sound;
		this.animationFrames = builder.animationFrames;
		this.animationInterval = builder.animationInterval;
		this.dynamicItem = builder.dynamicItem;

		this.onClick = builder.onClick;
		this.onLeftClick = builder.onLeftClick;
		this.onRightClick = builder.onRightClick;
		this.onDrop = builder.onDrop;

		size(1, 1);
	}

	@Override
	public void mount(@NotNull MenuContext context) {
		super.mount(context);
		if (animationFrames == null || animationFrames.isEmpty() || animationInterval < 0)
			return;

		startAnimation(context);
	}

	@Override
	public void render(@NotNull MenuContext context) {
		updateDynamicContent(context);
	}

	@Override
	public void onClick(@NotNull InventoryClickEvent event, @NotNull MenuContext context) {
		if (!enabled())
			return;

		if (sound != null)
			context.viewer().playSound(sound, Sound.Emitter.self());

		if (onClick != null)
			onClick.accept(event);

		if (onLeftClick != null)
			onLeftClick.accept(event);

		if (onRightClick != null)
			onRightClick.accept(event);

		if (onDrop != null)
			onDrop.accept(event);
	}

	@Override
	public List<Integer> slots() {
		if (!visible())
			return Collections.emptyList();

		return Collections.singletonList(toSlot(x(), y()));
	}

	@Override
	public Map<Integer, ItemStack> items(@NotNull MenuContext context) {
		Map<Integer, ItemStack> items = new HashMap<>();
		if (!visible())
			return items;

		ItemStack item = currentItem(context);
		if (item == null)
			return items;

		items.put(toSlot(x(), y()), item);

		return items;
	}

	@Override
	public void unmount(@NotNull MenuContext context) {
		super.unmount(context);
		stopAnimation();
	}

	private ItemStack currentItem(MenuContext context) {
		if (dynamicItem != null)
			return dynamicItem.apply(context);

		if (animationFrames != null && !animationFrames.isEmpty())
			return animationFrames.get(currentFrame);

		return item;
	}

	private void startAnimation(MenuContext context) {
		animationTask = new BukkitRunnable() {
			@Override
			public void run() {
				Menu menu = context.menu();
				if (!visible() || menu == null) {
					stopAnimation();
					return;
				}

				currentFrame = (currentFrame + 1) % animationFrames.size();
				menu.getInventory().setItem(toSlot(x(), y()), animationFrames.get(currentFrame));
			}
		}.runTaskTimer(NiveriaAPI.instance(), animationInterval, animationInterval);
	}

	private void stopAnimation() {
		if (animationTask != null && !animationTask.isCancelled()) {
			animationTask.cancel();
			animationTask = null;
		}

		currentFrame = 0;
	}

	private void updateDynamicContent(MenuContext context) {
		if (dynamicItem == null)
			return;

		context.menu().update();
	}

	public static Builder create() {
		return new Builder();
	}

	public static class Builder {
		private ItemStack item = new ItemStack(Material.STONE);
		private Consumer<InventoryClickEvent> onClick, onLeftClick, onRightClick, onDrop;
		private Sound sound = Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.MASTER, 1F, 1F);

		private List<ItemStack> animationFrames;
		private int animationInterval = 20;

		private Function<MenuContext, ItemStack> dynamicItem;

		public Builder item(ItemStack item) {
			this.item = item;
			return this;
		}

		public Builder onClick(Consumer<InventoryClickEvent> onClick) {
			this.onClick = onClick;
			return this;
		}

		public Builder onLeftClick(Consumer<InventoryClickEvent> onLeftClick) {
			this.onLeftClick = onLeftClick;
			return this;
		}

		public Builder onRightClick(Consumer<InventoryClickEvent> onRightClick) {
			this.onRightClick = onRightClick;
			return this;
		}

		public Builder onDrop(Consumer<InventoryClickEvent> onDrop) {
			this.onDrop = onDrop;
			return this;
		}

		public Builder sound(Sound sound) {
			this.sound = sound;
			return this;
		}

		public Builder animate(List<ItemStack> frames, int interval) {
			this.animationFrames = frames;
			this.animationInterval = interval;
			return this;
		}

		public Builder dynamicItem(Function<MenuContext, ItemStack> supplier) {
			this.dynamicItem = supplier;
			return this;
		}

		public Button build() {
			return new Button(this);
		}
	}
}
