package toutouchien.niveriaapi.menu;

import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import toutouchien.niveriaapi.menu.event.ClickEvent;
import toutouchien.niveriaapi.menu.event.CustomInventoryClickEvent;
import toutouchien.niveriaapi.menu.items.MenuItem;
import toutouchien.niveriaapi.utils.game.NMSUtils;

import java.util.concurrent.ThreadLocalRandom;

public class MenuListener implements Listener {
	@EventHandler(ignoreCancelled = true)
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

			ServerPlayer serverPlayer = NMSUtils.getNMSPlayer(player);
			ClientboundSoundEntityPacket soundPacket = new ClientboundSoundEntityPacket(SoundEvents.UI_BUTTON_CLICK, SoundSource.MASTER, serverPlayer, 1F, 1F, ThreadLocalRandom.current().nextLong());
			serverPlayer.connection.send(soundPacket);

			ClickEvent clickEvent = menuItem.clickEvent();
			if (clickEvent == null)
				return;

			CustomInventoryClickEvent customEvent = new CustomInventoryClickEvent(event);
			clickEvent.onClick(customEvent);
		}
	}
}
