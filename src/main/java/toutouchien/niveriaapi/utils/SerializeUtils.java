package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;

import java.io.*;
import java.util.UUID;

/**
 * Utility class for serializing and deserializing various Bukkit objects.
 */
public class SerializeUtils {
    private SerializeUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Serializes an ItemStack to a byte array.
     *
     * @param itemStack The ItemStack to serialize.
     * @return The serialized byte array.
     */
    public static byte @NotNull [] serializeItemStack(@NotNull ItemStack itemStack) {
        Preconditions.checkNotNull(itemStack, "itemStack cannot be null");

        return itemStack.serializeAsBytes();
    }

    /**
     * Deserializes an ItemStack from a byte array.
     *
     * @param serializedItemStack The byte array to deserialize.
     * @return The deserialized ItemStack, or null if deserialization fails.
     */
    @Nullable
    public static ItemStack deserializeItemStack(byte @NotNull [] serializedItemStack) {
        Preconditions.checkNotNull(serializedItemStack, "serializedItemStack cannot be null");

        return ItemStack.deserializeBytes(serializedItemStack);
    }

    /**
     * Serializes an array of ItemStacks to a byte array.
     *
     * @param itemStacks The array of ItemStacks to serialize.
     * @return The serialized byte array.
     */
    public static byte @NotNull [] serializeItemStacks(@NotNull ItemStack[] itemStacks) {
        Preconditions.checkNotNull(itemStacks, "itemStacks cannot be null");

        return ItemStack.serializeItemsAsBytes(itemStacks);
    }

    /**
     * Deserializes an array of ItemStacks from a byte array.
     *
     * @param serializedItemStacks The byte array to deserialize.
     * @return The deserialized array of ItemStacks.
     */
    @NotNull
    public static ItemStack[] deserializeItemStacks(byte @NotNull [] serializedItemStacks) {
        Preconditions.checkNotNull(serializedItemStacks, "serializedItemStacks cannot be null");

        return ItemStack.deserializeItemsFromBytes(serializedItemStacks);
    }

    /**
     * Serializes a Location to a byte array.
     *
     * @param location The Location to serialize.
     * @return The serialized byte array.
     */
    public static byte @NotNull [] serializeLocation(@NotNull Location location) {
        Preconditions.checkNotNull(location, "location cannot be null");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            World world = location.getWorld();
            if (world == null) {
                out.writeBoolean(false);
            } else {
                UUID worldUUID = world.getUID();

                out.writeBoolean(true);
                out.writeLong(worldUUID.getMostSignificantBits());
                out.writeLong(worldUUID.getLeastSignificantBits());
            }

            out.writeDouble(location.getX());
            out.writeDouble(location.getY());
            out.writeDouble(location.getZ());
            out.writeFloat(location.getYaw());
            out.writeFloat(location.getPitch());

            return baos.toByteArray();
        } catch (IOException e) {
            NiveriaAPI.instance().getSLF4JLogger().error("Failed to serialize Location", e);
            return new byte[0];
        }
    }

    /**
     * Deserializes a Location from a byte array.
     *
     * @param serializedLocation The byte array to deserialize.
     * @return The deserialized Location.
     */
    @NotNull
    public static Location deserializeLocation(byte @NotNull [] serializedLocation) {
        Preconditions.checkNotNull(serializedLocation, "serializedLocation cannot be null");

        ByteArrayInputStream bais = new ByteArrayInputStream(serializedLocation);
        DataInputStream in = new DataInputStream(bais);

        try {
            World world = null;
            boolean hasWorld = in.readBoolean();
            if (hasWorld) {
                long mostSigBits = in.readLong();
                long leastSigBits = in.readLong();
                UUID worldUUID = new UUID(mostSigBits, leastSigBits);
                world = Bukkit.getWorld(worldUUID);
            }

            double x = in.readDouble();
            double y = in.readDouble();
            double z = in.readDouble();
            float yaw = in.readFloat();
            float pitch = in.readFloat();

            return new Location(world, x, y, z, yaw, pitch);
        } catch (IOException e) {
            NiveriaAPI.instance().getSLF4JLogger().error("Failed to deserialize Location", e);
            return new Location(null, 0, 0, 0);
        }
    }

    /**
     * Serializes a Vector to a byte array.
     *
     * @param vector The Vector to serialize.
     * @return The serialized byte array.
     */
    public static byte @NotNull [] serializeVector(@NotNull Vector vector) {
        Preconditions.checkNotNull(vector, "vector cannot be null");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);

        try {
            out.writeDouble(vector.getX());
            out.writeDouble(vector.getY());
            out.writeDouble(vector.getZ());

            return baos.toByteArray();
        } catch (IOException e) {
            NiveriaAPI.instance().getSLF4JLogger().error("Failed to serialize Vector", e);
            return new byte[0];
        }
    }

    /**
     * Deserializes a Vector from a byte array.
     *
     * @param serializedVector The byte array to deserialize.
     * @return The deserialized Vector.
     */
    @NotNull
    public static Vector deserializeVector(byte @NotNull [] serializedVector) {
        Preconditions.checkNotNull(serializedVector, "serializedVector cannot be null");

        ByteArrayInputStream bais = new ByteArrayInputStream(serializedVector);
        DataInputStream in = new DataInputStream(bais);

        try {
            double x = in.readDouble();
            double y = in.readDouble();
            double z = in.readDouble();

            return new Vector(x, y, z);
        } catch (IOException e) {
            NiveriaAPI.instance().getSLF4JLogger().error("Failed to deserialize Vector", e);
            return new Vector(0, 0, 0);
        }
    }
}
