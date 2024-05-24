package toutouchien.niveriaapi.menu.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.event.ClickEvent;

public abstract class MenuItem {
	private final int slot;
	private final ItemStack itemStack;
	private final ClickEvent clickEvent;

	public MenuItem(int slot, @NotNull ItemStack itemStack, @Nullable ClickEvent clickEvent) {
		this.slot = slot;
		this.itemStack = itemStack;
		this.clickEvent = clickEvent;
	}

	public MenuItem(int slot, @NotNull Material material, @Nullable ClickEvent clickEvent) {
		this(slot, new ItemStack(material), clickEvent);
	}

	public int slot() {
		return slot;
	}

	public ItemStack itemStack() {
		return itemStack;
	}

	public ClickEvent clickEvent() {
		return clickEvent;
	}
}
