package toutouchien.niveriaapi.menu;

import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import toutouchien.niveriaapi.menu.event.ClickEvent;
import toutouchien.niveriaapi.menu.event.CustomInventoryClickEvent;
import toutouchien.niveriaapi.menu.items.MenuItem;

import java.util.Optional;

public class MenuListener implements Listener {
	@EventHandler
	public void onInventoryClick(InventoryClickEvent event) {
		if (event.getCurrentItem() == null)
			return;

		InventoryHolder holder = event.getInventory().getHolder(false);
		if (!(holder instanceof Menu menu))
			return;

		if (!(event.getWhoClicked() instanceof Player player))
			return;

		event.setCancelled(true);

		// TODO: Remove the fact that it recalculate the menu items on each click
		Optional<MenuItem> optional = menu.items().stream()
				.filter(m -> m.slot() == event.getSlot())
				.findFirst();

		optional.ifPresent(menuItem -> {
			ClickEvent clickEvent = menuItem.clickEvent();
			if (clickEvent == null)
				return;

			CustomInventoryClickEvent customEvent = new CustomInventoryClickEvent(event);
			clickEvent.onClick(customEvent);

			Sound clickSound = Sound.sound(org.bukkit.Sound.UI_BUTTON_CLICK, Sound.Source.MASTER, 1F, 1F);
			player.playSound(clickSound, Sound.Emitter.self());
		});
	}
}
