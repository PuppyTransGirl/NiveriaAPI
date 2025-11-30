package toutouchien.niveriaapi.menu.items;

import com.google.common.base.Preconditions;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.menu.event.ClickEvent;

/**
 * Abstract class representing an item in a menu.
 */
public abstract class MenuItem {
    private final int slot;
    private final ItemStack itemStack;
    private final ClickEvent clickEvent;

    MenuItem(@NonNegative int slot, @NotNull ItemStack itemStack, @Nullable ClickEvent clickEvent) {
        Preconditions.checkArgument(slot >= 0, "slot cannot be less than 0: %d", slot);
        Preconditions.checkNotNull(itemStack, "itemStack cannot be null");

        this.slot = slot;
        this.itemStack = itemStack;
        this.clickEvent = clickEvent;
    }

    MenuItem(int slot, @NotNull Material material, @Nullable ClickEvent clickEvent) {
        this(slot, ItemStack.of(material), clickEvent);
    }

    /**
     * Returns the slot index of the menu item.
     *
     * @return The slot index.
     */
    @NonNegative
    public int slot() {
        return slot;
    }

    /**
     * Returns the ItemStack representing the menu item.
     *
     * @return The ItemStack.
     */
    @NotNull
    public ItemStack itemStack() {
        return itemStack;
    }

    /**
     * Returns the ClickEvent associated with the menu item, if any.
     *
     * @return The ClickEvent, or null if none is associated.
     */
    @Nullable
    public ClickEvent clickEvent() {
        return clickEvent;
    }
}
