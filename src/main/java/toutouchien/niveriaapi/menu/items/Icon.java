package toutouchien.niveriaapi.menu.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;

/**
 * Represents an icon item in a menu, which is a non-interactive visual element.
 */
public class Icon extends MenuItem {
    /**
     * Constructs an Icon with the specified slot and item stack.
     *
     * @param slot      The slot index where the icon will be placed.
     * @param itemStack The ItemStack representing the icon.
     */
    public Icon(@NonNegative int slot, @NotNull ItemStack itemStack) {
        super(slot, itemStack, null);
    }

    /**
     * Constructs an Icon with the specified slot and material.
     *
     * @param slot     The slot index where the icon will be placed.
     * @param material The Material representing the icon.
     */
    public Icon(@NonNegative int slot, @NotNull Material material) {
        super(slot, material, null);
    }
}
