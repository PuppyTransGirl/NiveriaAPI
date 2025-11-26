package toutouchien.niveriaapi.menu.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;

public class Icon extends MenuItem {
    public Icon(@NonNegative int slot, @NotNull ItemStack itemStack) {
        super(slot, itemStack, null);
    }

    public Icon(@NonNegative int slot, @NotNull Material material) {
        super(slot, material, null);
    }
}
