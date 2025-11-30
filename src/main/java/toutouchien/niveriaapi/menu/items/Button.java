package toutouchien.niveriaapi.menu.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.event.ClickEvent;

/**
 * Represents a clickable button in a menu.
 */
public class Button extends MenuItem {
    /**
     * Constructs a Button with the specified slot, item stack, and click event.
     *
     * @param slot       The slot index of the button.
     * @param itemStack  The item stack representing the button.
     * @param clickEvent The click event handler for the button.
     */
    public Button(@NonNegative int slot, @NotNull ItemStack itemStack, @NotNull ClickEvent clickEvent) {
        super(slot, itemStack, clickEvent);
    }

    /**
     * Constructs a Button with the specified slot, material, and click event.
     *
     * @param slot       The slot index of the button.
     * @param material   The material representing the button.
     * @param clickEvent The click event handler for the button.
     */
    public Button(@NonNegative int slot, @NotNull Material material, @NotNull ClickEvent clickEvent) {
        super(slot, material, clickEvent);
    }
}