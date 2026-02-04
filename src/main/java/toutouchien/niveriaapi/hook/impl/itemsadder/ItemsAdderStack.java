package toutouchien.niveriaapi.hook.impl.itemsadder;

import com.google.common.base.Preconditions;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

/**
 * Wrapper class for ItemsAdder custom stacks.
 */
@NullMarked
public class ItemsAdderStack {
    private final CustomStack stack;

    /**
     * Constructs an ItemsAdderStack with the specified CustomStack.
     *
     * @param stack The CustomStack instance.
     */
    public ItemsAdderStack(CustomStack stack) {
        Preconditions.checkNotNull(stack, "stack cannot be null");

        this.stack = stack;
    }

    /**
     * Retrieves the display name of the custom stack.
     *
     * @return The display name.
     */
    public String displayName() {
        return stack.getDisplayName();
    }

    /**
     * Retrieves the underlying ItemStack of the custom stack.
     *
     * @return The ItemStack.
     */
    public ItemStack itemStack() {
        return stack.getItemStack();
    }
}
