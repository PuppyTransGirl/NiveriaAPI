package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.infos.MenuInfos;
import toutouchien.niveriaapi.menu.items.MenuItem;

import java.util.Set;

/**
 * Abstract class representing a menu in the inventory system.
 */
public abstract class Menu implements InventoryHolder {
    protected final MenuInfos menuInfos;
    protected Inventory inventory;
    Set<MenuItem> itemsCache;

    /**
     * Constructs a Menu with the specified menu information.
     *
     * @param menuInfos The {@link MenuInfos} containing information about the menu.
     */
    protected Menu(@NotNull MenuInfos menuInfos) {
        Preconditions.checkNotNull(menuInfos, "menuInfos cannot be null");

        this.menuInfos = menuInfos;
    }

    /**
     * Opens the menu for the player.
     */
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

    /**
     * Returns the name of the menu.
     *
     * @return The name as a {@link Component}.
     */
    @NotNull
    public abstract Component name();

    /**
     * Returns the number of slots in the menu.
     *
     * @return The number of slots, must be a positive integer.
     */
    @Positive
    public abstract int slots();

    /**
     * Returns the set of menu items to be displayed in the menu.
     *
     * @return A set of {@link MenuItem} objects representing the items in the menu.
     */
    @NotNull
    public abstract Set<MenuItem> items();

    /**
     * Returns the menu information associated with this menu.
     *
     * @return The {@link MenuInfos} object.
     */
    @NotNull
    public MenuInfos menuInfos() {
        return menuInfos;
    }
}
