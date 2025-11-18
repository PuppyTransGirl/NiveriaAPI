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
	protected Inventory inventory;
	Set<MenuItem> itemsCache;

	protected Menu(@NotNull MenuInfos menuInfos) {
        if (menuInfos == null)
            throw new IllegalArgumentException("menuInfos cannot be null");

		this.menuInfos = menuInfos;
	}

	public void open() {
		this.inventory = Bukkit.createInventory(this, slots(), name());

        for (MenuItem menuItem : this.itemsCache = this.items())
            this.inventory.setItem(menuItem.slot(), menuItem.itemStack());

		this.menuInfos.player().openInventory(inventory);
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

    @NotNull
	public MenuInfos menuInfos() {
		return menuInfos;
	}
}
