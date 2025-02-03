package toutouchien.niveriaapi.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class MenuListener implements Listener {
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		Inventory inventory = event.getClickedInventory();
		if (event.getCurrentItem() == null || inventory == null)
			return;

		Player player = (Player) event.getWhoClicked();

		InventoryHolder topHolder = player.getOpenInventory().getTopInventory().getHolder(false);
		if (topHolder instanceof Menu)
			event.setCancelled(true);

		InventoryHolder holder = inventory.getHolder(false);
		if (!(holder instanceof Menu menu))
			return;

		event.setCancelled(true);
		menu.handleClick(event);
	}

	@EventHandler
	public void onDrag(InventoryDragEvent event) {
		InventoryHolder holder = event.getInventory().getHolder(false);
		if (!(holder instanceof Menu menu))
			return;

		menu.handleDrag(event);
	}

	@EventHandler
	public void onClose(InventoryCloseEvent event) {
		InventoryHolder holder = event.getInventory().getHolder(false);
		if (!(holder instanceof Menu menu))
			return;

		menu.handleClose(event);
	}

	@EventHandler
	public void onLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		Menu menu = Menu.getMenu(player);
		if (menu == null)
			return;

		menu.close(true);
	}
}
