package toutouchien.niveriaapi.hook.impl;

import com.google.common.base.Preconditions;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;
import toutouchien.niveriaapi.hook.impl.itemsadder.ItemsAdderStack;

/**
 * Hook for integrating with the ItemsAdder plugin to manage custom items.
 */
@NullMarked
public class ItemsAdderHook extends Hook {
    /**
     * Constructs an ItemsAdderHook with the specified plugin instance.
     *
     * @param plugin The NiveriaAPI plugin instance.
     */
    public ItemsAdderHook(NiveriaAPI plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        this.plugin.getSLF4JLogger().info("Hooked into ItemsAdder");
    }

    @Override
    public void onDisable() {
        this.plugin.getSLF4JLogger().info("Unhooked from ItemsAdder");
    }

    /**
     * Retrieves an ItemsAdderStack by its namespace.
     *
     * @param namespace The namespace of the custom item.
     * @return The ItemsAdderStack, or null if not found.
     */
    @Nullable
    public static ItemsAdderStack byNamespace(String namespace) {
        Preconditions.checkNotNull(namespace, "namespace cannot be null");

        CustomStack stack = CustomStack.getInstance(namespace);
        if (stack == null)
            return null;

        return new ItemsAdderStack(stack);
    }

    /**
     * Retrieves an ItemsAdderStack by an ItemStack.
     *
     * @param itemStack The ItemStack to check.
     * @return The ItemsAdderStack, or null if not found.
     */
    @Nullable
    public static ItemsAdderStack byItemStack(ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack cannot be null");

        CustomStack stack = CustomStack.byItemStack(itemStack);
        if (stack == null)
            return null;

        return new ItemsAdderStack(stack);
    }
}