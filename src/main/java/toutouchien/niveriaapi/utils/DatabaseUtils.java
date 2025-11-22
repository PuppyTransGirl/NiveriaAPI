package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DatabaseUtils {
    private DatabaseUtils() {
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

    public static byte @NotNull [] serializeItemStackArray(@NotNull ItemStack[] itemStacks) {
        Preconditions.checkNotNull(itemStacks, "itemStacks cannot be null");
        return ItemStack.serializeItemsAsBytes(itemStacks);
    }

    @NotNull
    public static ItemStack[] deserializeItemStackArray(byte @NotNull [] serializedItemStacks) {
        Preconditions.checkNotNull(serializedItemStacks, "serializedItemStacks cannot be null");
        return ItemStack.deserializeItemsFromBytes(serializedItemStacks);
    }

    @NotNull
    public static Document locationToDocument(@NotNull Location location) {
        return new Document("world", location.getWorld().getName())
                .append("x", location.getX())
                .append("y", location.getY())
                .append("z", location.getZ())
                .append("yaw", location.getYaw())
                .append("pitch", location.getPitch());
    }

    @NotNull
    public static Location locationFromDocument(@NotNull Document document, @NotNull String fieldName) {
        Document locationDoc = (Document) document.get(fieldName);
        if (locationDoc == null)
            throw new IllegalArgumentException("Document does not contain a location at field: " + fieldName);

        return new Location(
                Bukkit.getWorld(locationDoc.getString("world")),
                locationDoc.getDouble("x"),
                locationDoc.getDouble("y"),
                locationDoc.getDouble("z"),
                locationDoc.getDouble("yaw").floatValue(),
                locationDoc.getDouble("pitch").floatValue()
        );
    }

    @NotNull
    public static Document vectorToDocument(@NotNull Vector vector) {
        return new Document("x", vector.getX())
                .append("y", vector.getY())
                .append("z", vector.getZ());
    }

    @NotNull
    public static Vector vectorFromDocument(@NotNull Document document, @NotNull String fieldName) {
        Document vectorDoc = (Document) document.get(fieldName);
        if (vectorDoc == null)
            throw new IllegalArgumentException("Document does not contain a vector at field: " + fieldName);

        return new Vector(
                vectorDoc.getDouble("x"),
                vectorDoc.getDouble("y"),
                vectorDoc.getDouble("z")
        );
    }
}
