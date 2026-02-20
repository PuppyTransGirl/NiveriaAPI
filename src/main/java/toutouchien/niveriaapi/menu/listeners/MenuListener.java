package toutouchien.niveriaapi.menu.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

import java.util.EnumSet;

public class MenuListener implements Listener {
    // These actions could lead to duping so that's a no no
    private static final EnumSet<InventoryAction> DISALLOWED_ACTIONS = EnumSet.of(
            InventoryAction.COLLECT_TO_CURSOR,
            InventoryAction.MOVE_TO_OTHER_INVENTORY
    );

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null)
            return;

        // Remove the ability for players to shift-click items in the menus
        // While being able to move their items in their inventory
        // getInventory will always be the top inventory
        InventoryHolder topHolder = event.getInventory().getHolder(false);
        if (topHolder instanceof Menu && DISALLOWED_ACTIONS.contains(event.getAction()))
            event.setCancelled(true);

        // Check if a player click on a menu
        InventoryHolder holder = inventory.getHolder(false);
        if (!(holder instanceof Menu menu))
            return;

        event.setCancelled(true);

        // Check if the player click on an item in the menu
        // If the player click on an item that means it's a component
        if (event.getCurrentItem() == null)
            return;

        NiveriaInventoryClickEvent clickEvent = new NiveriaInventoryClickEvent(event, menu.context());
        menu.handleClick(clickEvent);
    }

    // That disables all the way of dragging items when you have a menu open
    // I haven't found a simple & efficient way to block only dragging inside the menu
    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder(false);
        if (!(holder instanceof Menu))
            return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryCloseEvent.Reason reason = event.getReason();
        if (reason == InventoryCloseEvent.Reason.PLUGIN)
            return;

        Inventory inventory = event.getInventory();
        InventoryHolder holder = inventory.getHolder(false);
        if (!(holder instanceof Menu menu))
            return;

        menu.close(reason);
    }
}
