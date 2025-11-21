package toutouchien.niveriaapi.utils;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;
import toutouchien.niveriaapi.NiveriaAPI;

import java.io.*;

public class DatabaseUtils {
    private DatabaseUtils() {
        throw new IllegalStateException("Utility class");
    }

    @Nullable
    public static String toString(@NotNull ItemStack itemStack) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream);
            dataOutput.writeObject(itemStack);
            dataOutput.flush();
            dataOutput.close();
            return new String(Base64Coder.encode(outputStream.toByteArray()));
        } catch (IOException e) {
            NiveriaAPI.instance().getSLF4JLogger().warn("Failed to serialize ItemStack", e);
            return null;
        }
    }

    @Nullable
    public static ItemStack itemStackFromDocument(@NotNull Document document, @NotNull String fieldName) {
        String serializedItemStack = document.getString(fieldName);
        if (serializedItemStack == null)
            throw new IllegalArgumentException("Document does not contain an ItemStack at field: " + fieldName);

        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decode(serializedItemStack));
            ObjectInputStream dataInput = new ObjectInputStream(inputStream);
            ItemStack itemStack = (ItemStack) dataInput.readObject();
            dataInput.close();
            return itemStack;
        } catch (IOException | ClassNotFoundException e) {
            NiveriaAPI.instance().getSLF4JLogger().warn("Failed to deserialize ItemStack", e);
            return null;
        }
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
