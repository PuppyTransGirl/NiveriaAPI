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

	MenuItem(int slot, @NotNull ItemStack itemStack, @Nullable ClickEvent clickEvent) {
        if (slot < 0)
            throw new IllegalArgumentException("slot cannot be negative");

        if (itemStack == null)
            throw new IllegalArgumentException("itemStack cannot be null");

		this.slot = slot;
		this.itemStack = itemStack;
		this.clickEvent = clickEvent;
	}

	MenuItem(int slot, @NotNull Material material, @Nullable ClickEvent clickEvent) {
		this(slot, ItemStack.of(material), clickEvent);
	}

	public int slot() {
		return slot;
	}

    @NotNull
	public ItemStack itemStack() {
		return itemStack;
	}

    @Nullable
	public ClickEvent clickEvent() {
		return clickEvent;
	}
}
