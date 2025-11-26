package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SerializeUtils {
    private SerializeUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static byte @NotNull [] serializeItemStack(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack cannot be null");

        return itemStack.serializeAsBytes();
    }

    @Nullable
    public static ItemStack deserializeItemStack(byte @NotNull [] serializedItemStack) {
        Preconditions.checkNotNull(serializedItemStack, "serializedItemStack cannot be null");

        return ItemStack.deserializeBytes(serializedItemStack);
    }

    public static byte @NotNull [] serializeItemStacks(@NotNull ItemStack[] itemStacks) {
        Preconditions.checkNotNull(itemStacks, "itemStacks cannot be null");

        return ItemStack.serializeItemsAsBytes(itemStacks);
    }

    @NotNull
    public static ItemStack[] deserializeItemStacks(byte @NotNull [] serializedItemStacks) {
        Preconditions.checkNotNull(serializedItemStacks, "serializedItemStacks cannot be null");

        return ItemStack.deserializeItemsFromBytes(serializedItemStacks);
    }
}
