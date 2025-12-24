package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.mock.MockBukkitHelper;
import toutouchien.niveriaapi.utils.serialize.Serializable;

import java.io.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SerializerUtilsTest {
    @BeforeEach
    void setUp() {
        MockBukkitHelper.safeMock();
        MockBukkit.load(NiveriaAPI.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkitHelper.safeUnmock();
    }

    @Test
    @DisplayName("Serialize and deserialize a single ItemStack")
    void testSingleItemSerializeDeserialize() {
        ItemStack swordItem = ItemStack.of(Material.NETHERITE_SWORD);
        swordItem.editMeta(meta -> {
            meta.itemName(Component.text("Super mega sword", NamedTextColor.RED, TextDecoration.BOLD));
            meta.addEnchant(Enchantment.SHARPNESS, 10, true);
            meta.addEnchant(Enchantment.SWEEPING_EDGE, 10, true);
            meta.setRarity(ItemRarity.EPIC);
            meta.setUnbreakable(true);
            meta.getPersistentDataContainer().set(NamespacedKey.fromString("niveriatest:is_super_mega_sword"), PersistentDataType.BOOLEAN, true);
        });

        byte[] serializedSword = SerializeUtils.serializeItemStack(swordItem);
        ItemStack deserializedSword = SerializeUtils.deserializeItemStack(serializedSword);

        assertEquals(swordItem, deserializedSword);
    }

    @Test
    @DisplayName("Serialize and deserialize multiple ItemStack")
    void testMultipleItemSerializeDeserialize() {
        ItemStack[] items = {
                ItemStack.of(Material.APPLE, 5),
                ItemStack.of(Material.MILK_BUCKET),
                ItemStack.of(Material.DIAMOND_HELMET)
        };

        byte[] serializedItems = SerializeUtils.serializeItemStacks(items);
        ItemStack[] deserializedItems = SerializeUtils.deserializeItemStacks(serializedItems);

        assertArrayEquals(items, deserializedItems);
    }

    @Test
    @DisplayName("Serialize and deserialize a Location")
    void testLocationSerializeDeserialize() {
        Location location = new Location(null, 100.5D, 64D, -200.5D, 90F, 45F);

        byte[] serializedLocation = SerializeUtils.serializeLocation(location);
        Location deserializedLocation = SerializeUtils.deserializeLocation(serializedLocation);

        assertEquals(location, deserializedLocation);

        World world = MockBukkit.getMock().getWorld("world");
        Location locationWithWorld = new Location(world, 150D, 70D, 250D, 180F, 30F);

        byte[] serializedLocationWithWorld = SerializeUtils.serializeLocation(locationWithWorld);
        Location deserializedLocationWithWorld = SerializeUtils.deserializeLocation(serializedLocationWithWorld);

        assertEquals(locationWithWorld, deserializedLocationWithWorld);
    }

    @Test
    @DisplayName("Serialize and deserialize a Vector")
    void testVectorSerializeDeserialize() {
        Vector vector = new Vector(1.5D, -3D, 4.25D);

        byte[] serializedVector = SerializeUtils.serializeVector(vector);
        Vector deserializedVector = SerializeUtils.deserializeVector(serializedVector);

        assertEquals(vector, deserializedVector);
    }

    @Test
    @DisplayName("Serialize and deserialize a custom Serializable object")
    void testCustomSerializableSerializeDeserialize() {
        ItemStack[] items = {
                ItemStack.of(Material.BREAD, 10),
                ItemStack.of(Material.IRON_SWORD)
        };

        DummySerializable dummySerializable = new DummySerializable("TestObject", UUID.randomUUID(), items);

        byte[] serializedData = dummySerializable.serialize();
        DummySerializable deserialized = dummySerializable.deserialize(serializedData);

        assertEquals(dummySerializable.name, deserialized.name);
        assertEquals(dummySerializable.uuid, deserialized.uuid);
        assertArrayEquals(dummySerializable.itemStacks, deserialized.itemStacks);
    }

    record DummySerializable(String name, UUID uuid,
                             ItemStack[] itemStacks) implements Serializable<DummySerializable> {
        @Override
        public byte[] serialize() {
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream(); DataOutputStream out = new DataOutputStream(baos)) {
                byte[] nameBytes = this.name.getBytes();
                out.writeInt(nameBytes.length);
                out.write(nameBytes);

                out.writeLong(this.uuid.getMostSignificantBits());
                out.writeLong(this.uuid.getLeastSignificantBits());

                byte[] serializedInventory = SerializeUtils.serializeItemStacks(this.itemStacks);
                out.writeInt(serializedInventory.length);
                out.write(serializedInventory);

                out.flush();
                return baos.toByteArray();
            } catch (IOException e) {
                NiveriaAPI.instance().getSLF4JLogger().error("Failed to serialize DummySerializable", e);
                return new byte[0];
            }
        }

        @Override
        public DummySerializable deserialize(byte[] data) {
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data); DataInputStream in = new DataInputStream(bais)) {
                int nameLength = in.readInt();
                byte[] nameBytes = new byte[nameLength];
                in.readFully(nameBytes);
                String name = new String(nameBytes);

                long mostSigBits = in.readLong();
                long leastSigBits = in.readLong();
                UUID uuid = new UUID(mostSigBits, leastSigBits);

                int itemStacksLength = in.readInt();
                byte[] serializedItemStacks = new byte[itemStacksLength];
                in.readFully(serializedItemStacks);

                ItemStack[] itemStacks = SerializeUtils.deserializeItemStacks(serializedItemStacks);

                return new DummySerializable(name, uuid, itemStacks);
            } catch (IOException e) {
                NiveriaAPI.instance().getSLF4JLogger().error("Failed to deserialize DummySerializable", e);
                return new DummySerializable(null, null, null);
            }
        }
    }
}
