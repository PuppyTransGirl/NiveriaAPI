package toutouchien.niveriaapi.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.infos.MenuInfos;
import toutouchien.niveriaapi.menu.items.MenuItem;

import java.util.Set;

public abstract class Menu implements InventoryHolder {
	protected final MenuInfos menuInfos;
	protected final Inventory inventory;

	public Menu(@NotNull MenuInfos menuInfos) {
		this.menuInfos = menuInfos;

		inventory = Bukkit.createInventory(this, slots(), name());

		items().forEach(menuItem -> inventory.setItem(menuItem.slot(), menuItem.itemStack()));
	}

	public void open() {
		menuInfos.player().openInventory(inventory);
	}

	@NotNull
	@Override
	public Inventory getInventory() {
		return inventory;
	}

	@NotNull
	public abstract Component name();

	public abstract int slots();

	@NotNull
	public abstract Set<MenuItem> items();

	public MenuInfos menuInfos() {
		return menuInfos;
	}
}
