package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.mock.MockBukkitHelper;

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
    @DisplayName("Serialize and deserialize a single item stack")
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
    @DisplayName("Serialize and deserialize multiple item stacks")
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
}
