package toutouchien.niveriaapi.menu;

import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import toutouchien.niveriaapi.menu.event.ClickEvent;
import toutouchien.niveriaapi.menu.event.CustomInventoryClickEvent;
import toutouchien.niveriaapi.menu.items.MenuItem;

public class MenuListener implements Listener {
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
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

		int slot = event.getSlot();
		for (MenuItem menuItem : menu.itemsCache) {
			if (menuItem.slot() != slot)
				continue;

			Sound clickSound = Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.MASTER, 1F, 1F);
			player.playSound(clickSound, Sound.Emitter.self());

			ClickEvent clickEvent = menuItem.clickEvent();
			if (clickEvent == null)
				return;

			CustomInventoryClickEvent customEvent = new CustomInventoryClickEvent(event);
			clickEvent.onClick(customEvent);
		}
	}
}
