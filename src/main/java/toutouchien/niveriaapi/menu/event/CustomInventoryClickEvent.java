package toutouchien.niveriaapi.menu.event;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class CustomInventoryClickEvent extends InventoryClickEvent {
	public CustomInventoryClickEvent(@NotNull InventoryClickEvent event) {
		super(event.getView(), event.getSlotType(), event.getSlot(), event.getClick(), event.getAction());
	}

	@NotNull
	public Player player() {
		return (Player) getWhoClicked();
	}

	// TODO: Need to change the NotNull because getCurrentItem can return null
	@NotNull
	public String itemName() {
		return PlainTextComponentSerializer.plainText().serialize(getCurrentItem().displayName());
	}
}
