package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.component.Component;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

/**
 * Base class for creating custom menus with a component-based architecture.
 * <p>
 * This class provides a flexible framework for building interactive inventory menus
 * using a component system. Subclasses must define the menu title and root component.
 */
public abstract class Menu implements InventoryHolder {
    private Inventory inventory;
    private final MenuContext context;
    private final Player player;

    private Component root;

    /**
     * Constructs a new menu for the specified player.
     *
     * @param player the player who will view this menu
     * @throws NullPointerException if player is null
     */
    protected Menu(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.player = player;
        this.context = new MenuContext(this);
    }

    /**
     * Opens the menu for the player.
     * <p>
     * This method creates the inventory, initializes the root component,
     * renders it, and opens the inventory for the player.
     */
    public void open() {
        net.kyori.adventure.text.Component title = this.title();
        this.root = this.root(this.context);
        this.inventory = Bukkit.createInventory(this, this.root.height() * 9, title);

        this.root.onAdd(this.context);
        this.root.render(this.context);

        this.player.openInventory(this.inventory);
    }

    /**
     * Closes the menu and cleans up resources.
     *
     * @param event true if this close was triggered by an inventory close event,
     *              false to programmatically close the player's inventory
     */
    public void close(boolean event) {
        this.root.onRemove(this.context);

        if (!event)
            this.player.closeInventory();

        this.context.close();
    }

    /**
     * Handles click events in the menu.
     * <p>
     * Delegates the click to the root component and re-renders the menu.
     *
     * @param event the click event to handle
     * @throws NullPointerException if event is null
     */
    public void handleClick(@NotNull NiveriaInventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        this.root.onClick(event, this.context);
        this.root.render(this.context);
    }

    /**
     * Returns the title of the menu.
     *
     * @return the menu title as an Adventure text component
     */
    @NotNull
    protected abstract net.kyori.adventure.text.Component title();

    /**
     * Returns the root component of the menu.
     * <p>
     * This component defines the layout and behavior of the entire menu.
     *
     * @param context the menu context for accessing menu state
     * @return the root component
     */
    @NotNull
    protected abstract Component root(@NotNull MenuContext context);

    /**
     * Returns the player viewing this menu.
     *
     * @return the player
     */
    @NotNull
    public Player player() {
        return player;
    }

    /**
     * Returns the menu context.
     *
     * @return the context for storing and accessing menu state
     */
    @NotNull
    public MenuContext context() {
        return context;
    }

    /**
     * Returns the Bukkit inventory for this menu.
     *
     * @return the inventory
     */
    @NotNull
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
