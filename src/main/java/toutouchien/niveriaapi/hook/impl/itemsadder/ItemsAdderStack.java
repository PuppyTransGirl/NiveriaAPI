package toutouchien.niveriaapi.hook.impl.itemsadder;

import com.google.common.base.Preconditions;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ItemsAdderStack {
    private final CustomStack stack;

    public ItemsAdderStack(@NotNull CustomStack stack) {
        Preconditions.checkNotNull(stack, "stack cannot be null");

        this.stack = stack;
    }

    @NotNull
    public String displayName() {
        return stack.getDisplayName();
    }

    @NotNull
    public ItemStack itemStack() {
        return stack.getItemStack();
    }
}
