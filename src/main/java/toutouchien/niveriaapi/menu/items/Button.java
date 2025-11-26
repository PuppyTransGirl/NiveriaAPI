package toutouchien.niveriaapi.menu.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.event.ClickEvent;

public class Button extends MenuItem {
    public Button(@NonNegative int slot, @NotNull ItemStack itemStack, @NotNull ClickEvent clickEvent) {
        super(slot, itemStack, clickEvent);
    }

    public Button(@NonNegative int slot, @NotNull Material material, @NotNull ClickEvent clickEvent) {
        super(slot, material, clickEvent);
    }
}