package toutouchien.niveriaapi.hook.impl;

import dev.lone.itemsadder.api.CustomStack;
import org.bukkit.inventory.ItemStack;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.hook.Hook;
import toutouchien.niveriaapi.hook.impl.itemsadder.ItemsAdderStack;

public class ItemsAdderHook extends Hook {
    private boolean enabled;

    public ItemsAdderHook(NiveriaAPI plugin) {
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

    public static ItemsAdderStack byNamespace(String namespace) {
        CustomStack stack = CustomStack.getInstance(namespace);
        if (stack == null)
            return null;

        return new ItemsAdderStack(stack);
    }

    public static ItemsAdderStack byItemStack(ItemStack itemStack) {
        CustomStack stack = CustomStack.byItemStack(itemStack);
        if (stack == null)
            return null;

        return new ItemsAdderStack(stack);
    }
}