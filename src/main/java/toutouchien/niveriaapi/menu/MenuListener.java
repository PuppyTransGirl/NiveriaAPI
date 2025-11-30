package toutouchien.niveriaapi.menu;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import toutouchien.niveriaapi.menu.event.ClickEvent;
import toutouchien.niveriaapi.menu.event.NiveriaInventoryClickEvent;
import toutouchien.niveriaapi.menu.items.MenuItem;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Listener for menu-related inventory click events.
 */
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

            player.playSound(Sound.sound(
                    Key.key("minecraft", "ui.button.click"),
                    Sound.Source.UI,
                    1F,
                    ThreadLocalRandom.current().nextFloat()
            ));

            ClickEvent clickEvent = menuItem.clickEvent();
            if (clickEvent == null)
                return;

            NiveriaInventoryClickEvent customEvent = new NiveriaInventoryClickEvent(event);
            clickEvent.onClick(customEvent);
        }
    }
}
