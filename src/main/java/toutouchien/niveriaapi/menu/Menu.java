package toutouchien.niveriaapi.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.component.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Menu implements InventoryHolder {
	private static final Map<UUID, Menu> openMenus = new HashMap<>();

	private final net.kyori.adventure.text.Component title;
	private int rows;
	private final Component root;

	private final Inventory inventory;
	private final MenuContext context;

	private Player viewer;
	private boolean cancelClicks;
	private boolean isDirty = true;

	private Menu(Builder builder) {
		this.title = builder.title;
		this.rows = builder.rows;
		this.root = builder.root;
		this.cancelClicks = builder.cancelClicks;

		this.inventory = Bukkit.createInventory(this, rows * 9, title);
		this.context = new MenuContext(this);
	}

	public void open(Player player) {
		this.viewer = player;

		if (root != null)
			root.mount(context);

		render();

		player.openInventory(inventory);
		openMenus.put(player.getUniqueId(), this);
	}

	public void close(boolean calledFromEvent) {
		if (viewer == null)
			return;

		if (root != null)
			root.unmount(context);

		if (!calledFromEvent)
			viewer.closeInventory();

		openMenus.remove(viewer.getUniqueId());
		context.close();
	}

	public void update() {
		isDirty = true;
	}

	private void render() {
		if (!isDirty)
			return;

		inventory.clear();

		if (root != null) {
			root.render(context);

			Map<Integer, ItemStack> items = root.items(context);
			items.forEach((slot, item) -> {
				if (slot < 0 || slot > inventory.getSize())
					return;

				inventory.setItem(slot, item);
			});
		}

		isDirty = false;
	}

	public void handleClick(@NotNull InventoryClickEvent event) {
		event.setCancelled(cancelClicks);

		if (root != null)
			root.onClick(event, context);

		if (isDirty)
			render();
	}

	public void handleDrag(@NotNull InventoryDragEvent event) {
		event.setCancelled(cancelClicks);
	}

	public void handleClose(@NotNull InventoryCloseEvent event) {
		close(true);
	}

	public static Menu getMenu(Player player) {
		return openMenus.get(player.getUniqueId());
	}

	public static Builder create() {
		return new Builder();
	}

	public net.kyori.adventure.text.Component title() {
		return title;
	}

	public int rows() {
		return rows;
	}

	@Override
	public @NotNull Inventory getInventory() {
		return inventory;
	}

	public Component root() {
		return root;
	}

	public Player viewer() {
		return viewer;
	}

	public MenuContext context() {
		return context;
	}

	public static class Builder {
		private net.kyori.adventure.text.Component title = net.kyori.adventure.text.Component.text("Menu");
		private int rows = 6;
		private Component root;
		private boolean cancelClicks = true;

		public Builder title(net.kyori.adventure.text.Component title) {
			this.title = title;
			return this;
		}

		public Builder title(String title) {
			this.title = net.kyori.adventure.text.Component.text(title);
			return this;
		}

		public Builder rows(int rows) {
			this.rows = Math.min(Math.max(1, rows), 6);
			return this;
		}

		public Builder root(Component root) {
			this.root = root;
			return this;
		}

		public Builder cancelClicks(boolean cancelClicks) {
			this.cancelClicks = cancelClicks;
			return this;
		}

		public Menu build() {
			return new Menu(this);
		}
	}
}
