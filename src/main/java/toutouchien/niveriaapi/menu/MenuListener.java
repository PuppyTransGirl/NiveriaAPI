package toutouchien.niveriaapi.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;

public class MenuListener implements Listener {
    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getClickedInventory();
        if (inventory == null || event.getCurrentItem() == null)
            return;

        Player player = (Player) event.getWhoClicked();

        InventoryHolder topHolder = player.getOpenInventory().getTopInventory().getHolder(false);
        if (topHolder instanceof Menu)
            event.setCancelled(true);

        InventoryHolder holder = inventory.getHolder(false);
        if (!(holder instanceof Menu menu))
            return;

        event.setCancelled(true);

        NiveriaInventoryClickEvent clickEvent = new NiveriaInventoryClickEvent(event, menu.context());
        menu.handleClick(clickEvent);
    }
}
