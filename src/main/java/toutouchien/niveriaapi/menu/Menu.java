package toutouchien.niveriaapi.menu;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.annotations.Overexcited;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

/**
 * Abstract base class for creating custom GUI menus.
 * <p>
 * This class provides a framework for building interactive inventory-based menus
 * with component-based rendering and event handling.
 */
public abstract class Menu implements InventoryHolder {
    private Inventory inventory;
    private final Player player;
    protected final MenuContext context;

    private final Object2ObjectOpenHashMap<String, MenuComponent> componentIDs;

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
        this.componentIDs = new Object2ObjectOpenHashMap<>();
    }

	/**
	 * Constructs a new Menu with the specified player and context.
	 *
	 * @param player  the player who will interact with this menu
	 * @param context the menu context for component interaction
	 * @throws NullPointerException if player or context is null
	 */
	protected Menu(@NotNull Player player, @NotNull MenuContext context) {
		Preconditions.checkNotNull(player, "player cannot be null");
		Preconditions.checkNotNull(context, "context cannot be null");

		this.player = player;
		this.context = context;
        this.componentIDs = new Object2ObjectOpenHashMap<>();
	}

    /**
     * Opens the menu for the player.
     * <p>
     * This method creates the inventory, initializes the root component,
     * renders the menu contents, and opens it for the player.
     */
    public void open() {
		context.menu(this);

        Component title = this.title();
        this.root = this.root(this.context);
        this.inventory = Bukkit.createInventory(this, this.root.height() * 9, title);

        this.root.onAdd(this.context);
        this.root.render(this.context);

        this.player.openInventory(this.inventory);
        this.onOpen();
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
        this.onClose();
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
     * Registers a component with a unique identifier for later retrieval.
     *
     * @param id        the unique identifier for the component
     * @param component the menu component to register
     * @throws NullPointerException if id or component is null
     */
    public void registerComponentID(@NotNull String id, @NotNull MenuComponent component) {
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkArgument(!id.isEmpty(), "id cannot be empty");
        Preconditions.checkNotNull(component, "component cannot be null");

        if (this.componentIDs.containsKey(id))
            throw new IllegalArgumentException("A component with id '" + id + "' is already registered.");

        this.componentIDs.put(id, component);
    }

    /**
     * Unregisters a component by its unique identifier.
     *
     * @param id the unique identifier of the component to unregister
     * @throws NullPointerException if id is null
     */
    public void unregisterComponentID(@NotNull String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        this.componentIDs.remove(id);
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
     * Indicates whether the menu can be returned to using the previous menu system.
     * <p>
     * Subclasses can override this method to disable returning to this menu
     * from another menu.
     *
     * @return true if the menu can be returned to, false otherwise
     */
    protected boolean canGoBackToThisMenu() {
        return true;
    }

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
     * Called when the menu is opened.
     * <p>
     * Subclasses can override this method to perform actions when the menu
     * is opened.
     */
    protected void onOpen() {

    }

    /**
     * Called when the menu is closed.
     * <p>
     * Subclasses can override this method to perform actions when the menu
     * is closed.
     */
    protected void onClose() {

    }

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
     * Retrieves a registered component by its unique identifier.
     *
     * @param id the unique identifier of the component
     * @return the menu component associated with the given id, or null if not found
     * @throws NullPointerException if id is null
     */
    @Nullable
    public MenuComponent componentByID(@NotNull String id) {
        Preconditions.checkNotNull(id, "id cannot be null");

        return this.componentIDs.get(id);
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
