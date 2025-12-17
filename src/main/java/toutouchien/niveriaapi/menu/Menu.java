package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.annotations.Overexcited;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

/**
 * Abstract base class for creating custom GUI menus in Bukkit/Spigot.
 * <p>
 * This class provides a framework for building interactive inventory-based menus
 * with component-based rendering and event handling.
 */
public abstract class Menu implements InventoryHolder {
    private Inventory inventory;
    protected final MenuContext context;
    private final Player player;

    private MenuComponent root;

    /**
     * Constructs a new Menu for the specified player.
     *
     * @param player the player who will interact with this menu
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
     * renders the menu contents, and opens it for the player.
     */
    public void open() {
        Component title = this.title();
        this.root = this.root(this.context);
        this.inventory = Bukkit.createInventory(this, this.root.height() * 9, title);

        this.root.onAdd(this.context);
        this.root.render(this.context);

        this.player.openInventory(this.inventory);
    }

    /**
     * Closes the menu and performs cleanup.
     *
     * @param event whether this close was triggered by an inventory close event.
     *              If false, the player's inventory will be closed programmatically.
     */
    public void close(boolean event) {
        this.root.onRemove(this.context);

        if (!event)
            this.player.closeInventory();

        this.context.close();
    }

    /**
     * Handles click events within the menu.
     * <p>
     * Delegates the click event to the root component and triggers a re-render
     * of the menu contents.
     *
     * @param event the inventory click event to handle
     * @throws NullPointerException if event is null
     */
    @Overexcited(reason = "Calls render on each click")
    public void handleClick(@NotNull NiveriaInventoryClickEvent event) {
        Preconditions.checkNotNull(event, "event cannot be null");

        this.root.onClick(event, this.context);
        this.root.render(this.context);
    }

    /**
     * Returns the title component for this menu's inventory.
     * <p>
     * This method must be implemented by subclasses to define the menu's title.
     *
     * @return the title component displayed at the top of the inventory
     */
    @NotNull
    protected abstract Component title();

    /**
     * Creates and returns the root component for this menu.
     * <p>
     * This method must be implemented by subclasses to define the menu's layout
     * and components.
     *
     * @param context the menu context for component interaction
     * @return the root component that defines the menu's structure
     */
    @NotNull
    protected abstract MenuComponent root(@NotNull MenuContext context);

    /**
     * Returns the player associated with this menu.
     *
     * @return the player who owns this menu
     */
    @NotNull
    public Player player() {
        return player;
    }

    /**
     * Returns the menu context for this menu.
     *
     * @return the menu context used for component interaction
     */
    @NotNull
    public MenuContext context() {
        return context;
    }

    /**
     * Returns the Bukkit inventory associated with this menu.
     * <p>
     * Required implementation of the InventoryHolder interface.
     *
     * @return the inventory instance for this menu
     */
    @NotNull
    @Override
    public Inventory getInventory() {
        if (this.inventory == null)
            throw new IllegalStateException("Menu inventory has not been initialized. Did you forget to call open() ?");

        return inventory;
    }
}
