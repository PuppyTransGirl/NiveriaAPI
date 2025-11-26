package toutouchien.niveriaapi.hook.impl;

import com.google.common.base.Preconditions;
import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;
import toutouchien.niveriaapi.hook.impl.itemsadder.ItemsAdderStack;

public class ItemsAdderHook extends Hook {
    private boolean enabled;

    public ItemsAdderHook(@NotNull NiveriaAPI plugin) {
        super(plugin);
    }

    @Override
    public void onEnable() {
        this.enabled = true;
        this.plugin.getSLF4JLogger().info("Hooked into ItemsAdder");
    }

    @Override
    public void onDisable() {
        this.enabled = false;
        this.plugin.getSLF4JLogger().info("Unhooked from ItemsAdder");
    }

    @Nullable
    public static ItemsAdderStack byNamespace(@NotNull String namespace) {
        Preconditions.checkNotNull(namespace, "namespace cannot be null");

        CustomStack stack = CustomStack.getInstance(namespace);
        if (stack == null)
            return null;

        return new ItemsAdderStack(stack);
    }

    @Nullable
    public static ItemsAdderStack byItemStack(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack cannot be null");

        CustomStack stack = CustomStack.byItemStack(itemStack);
        if (stack == null)
            return null;

        return new ItemsAdderStack(stack);
    }
}