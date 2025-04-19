package toutouchien.niveriaapi.menu.event;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.utils.ui.ComponentUtils;

public class CustomInventoryClickEvent extends InventoryClickEvent {
	public CustomInventoryClickEvent(@NotNull InventoryClickEvent event) {
		super(event.getView(), event.getSlotType(), event.getSlot(), event.getClick(), event.getAction());
	}

	@NotNull
	public Player player() {
		return (Player) getWhoClicked();
	}

	@NotNull
	public String itemName() {
		Component data = getCurrentItem().getData(DataComponentTypes.ITEM_NAME);
		if (data == null || data.equals(Component.empty()))
			return "";

		return ComponentUtils.serializePlainText(data);
	}
}
