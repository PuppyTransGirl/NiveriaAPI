package toutouchien.niveriaapi.menu.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class Icon extends MenuItem {
    public Icon(int slot, @NotNull ItemStack itemStack) {
        super(slot, itemStack, null);
    }

    public Icon(int slot, @NotNull Material material) {
        super(slot, material, null);
    }
}
