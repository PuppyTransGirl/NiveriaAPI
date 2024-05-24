package toutouchien.niveriaapi.menu.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.event.ClickEvent;

public class Button extends MenuItem {
	public Button(int slot, @NotNull ItemStack itemStack, @Nullable ClickEvent clickEvent) {
		super(slot, itemStack, clickEvent);
	}

	public Button(int slot, @NotNull Material material, @Nullable ClickEvent clickEvent) {
		super(slot, material, clickEvent);
	}
}