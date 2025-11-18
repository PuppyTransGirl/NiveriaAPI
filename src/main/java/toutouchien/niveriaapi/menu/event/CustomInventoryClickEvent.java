package toutouchien.niveriaapi.menu.event;

import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.utils.data.ItemBuilder;
import toutouchien.niveriaapi.utils.ui.ComponentUtils;

import java.util.function.Consumer;

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

	public void changeItem(@Nullable ItemStack newItem) {
		this.setCurrentItem(newItem);
	}

	public void changeItem(@NotNull Consumer<ItemBuilder> modifier) {
        if (modifier == null)
            throw new IllegalArgumentException("modifier cannot be null");

		ItemStack item = this.getCurrentItem();
		if (item == null)
			return;

		ItemBuilder builder = ItemBuilder.of(item);
		modifier.accept(builder);
		this.setCurrentItem(builder.build());
	}
}
