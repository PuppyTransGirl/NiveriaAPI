package toutouchien.niveriaapi.hook.impl.itemsadder;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderStack {
    private final CustomStack stack;

    public ItemsAdderStack(@NotNull CustomStack stack) {
        this.stack = stack;
    }

    public String displayName() {
        return stack.getDisplayName();
    }

    public ItemStack itemStack() {
        return stack.getItemStack();
    }
}
