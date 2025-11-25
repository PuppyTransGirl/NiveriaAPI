package toutouchien.niveriaapi.utils;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.mock.MockBukkitHelper;
import toutouchien.niveriaapi.mock.ServerMock;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ItemBuilderTest {
    private NiveriaAPI plugin;
    private ServerMock server;

    @BeforeEach
    void setUp() {
        this.server = MockBukkitHelper.safeMock();
        this.plugin = MockBukkit.load(NiveriaAPI.class);
    }

    @AfterEach
    void tearDown() {
        MockBukkitHelper.safeUnmock();
    }

    @Nested
    class Constructor {
        @Test
        @DisplayName("of(Material) should create builder with non-air material and default amount 1")
        void ofMaterial_shouldReturnBuilderWhenMaterialIsNotAir() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            ItemStack built = builder.build();

            assertEquals(Material.DIAMOND_SWORD, built.getType());
            assertEquals(1, built.getAmount());
        }

        @Test
        @DisplayName("of(Material) should throw IllegalArgumentException when material is air")
        void ofMaterial_shouldThrowWhenMaterialIsAir() {
            assertThrows(IllegalArgumentException.class, () -> ItemBuilder.of(Material.AIR));
        }

        @Test
        @DisplayName("of(ItemStack) should wrap existing non-air ItemStack without copying")
        void ofItemStack_shouldWrapExistingItemStackWhenMaterialNotAir() {
            ItemStack base = ItemStack.of(Material.EMERALD, 3);
            ItemBuilder builder = ItemBuilder.of(base);

            assertSame(base, builder.build());
        }

        @Test
        @DisplayName("of(ItemStack) should throw IllegalArgumentException when item type is air")
        void ofItemStack_shouldThrowWhenItemTypeIsAir() {
            ItemStack air = ItemStack.of(Material.AIR);
            assertThrows(IllegalArgumentException.class, () -> ItemBuilder.of(air));
        }

        @Test
        @DisplayName("of(Material,int) should create builder with given amount when material not air")
        void ofMaterialAndAmount_shouldReturnBuilderWhenMaterialNotAirAndAmountValid() {
            ItemBuilder builder = ItemBuilder.of(Material.APPLE, 5);
            ItemStack built = builder.build();

            assertEquals(Material.APPLE, built.getType());
            assertEquals(5, built.getAmount());
        }

        @Test
        @DisplayName("of(Material,int) should throw IllegalArgumentException when material is air")
        void ofMaterialAndAmount_shouldThrowWhenMaterialIsAir() {
            assertThrows(IllegalArgumentException.class, () -> ItemBuilder.of(Material.AIR, 5));
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -10})
        @DisplayName("of(Material,int) should IllegalArgumentException when amount less than one")
        void ofMaterialAndAmount_shouldThrowWhenAmountLessThanOne(int amount) {
            assertThrows(IllegalArgumentException.class, () -> ItemBuilder.of(Material.STONE, amount));
        }

        @Test
        @DisplayName("build should return underlying ItemStack instance")
        void build_shouldReturnUnderlyingItemStackWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.GOLD_INGOT);
            ItemStack built = builder.build();

            assertSame(built, builder.build());
        }

        @Test
        @DisplayName("buildCopy should return a cloned ItemStack independant of original")
        void buildCopy_shouldReturnCloneOfWrappedItemStack() {
            ItemBuilder builder = ItemBuilder.of(Material.IRON_INGOT, 4);
            ItemStack original = builder.build();
            ItemStack copy = builder.buildCopy();

            assertEquals(original, copy);
            assertNotSame(original, copy);
        }

        @Test
        @DisplayName("copy should return new ItemBuilder wrapping a clone of the ItemStack")
        void copy_shouldReturnNewBuilderWrappingCloneOfItemStack() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND, 2);
            ItemBuilder copied = builder.copy();

            assertNotSame(builder, copied);
            assertEquals(builder.build(), copied.build());
            assertNotSame(builder.build(), copied.build());
        }
    }

    @Nested
    class GetterSetter {
        @ParameterizedTest
        @ValueSource(ints = {1, 5, 64})
        @DisplayName("amount(int) should set stack size for valid amounts greater than zero")
        void amountSetter_shouldSetAmountWhenGreaterThanZero(int amount) {
            ItemBuilder builder = ItemBuilder.of(Material.APPLE);
            builder.amount(amount);

            assertEquals(amount, builder.build().getAmount());
            assertEquals(amount, builder.amount());
        }

        @ParameterizedTest
        @ValueSource(ints = {0, -1, -5})
        @DisplayName("amount(int) should throw IllegalArgumentException for invalid amounts less than one")
        void amountSetter_shouldThrowWhenAmountLessThanOne(int amount) {
            ItemBuilder builder = ItemBuilder.of(Material.APPLE);
            assertThrows(IllegalArgumentException.class, () -> builder.amount(amount));
        }

        @Test
        @DisplayName("amount() should return current stack size of the item")
        void amountGetter_shouldReturnCurrentStackSizeWhenCalled() {
            ItemBuilder builer = ItemBuilder.of(Material.APPLE, 7);
            assertEquals(7, builer.amount());
        }

        @Test
        @DisplayName("name(Component) should set and return custom item name through ITEM_NAME component")
        void name_shouldSetAndReturnCustomItemNameWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            Component name = Component.text("Epic sword");
            builder.name(name);

            assertEquals(name, builder.name());
            assertEquals(name, builder.build().getData(DataComponentTypes.ITEM_NAME));
        }

        @Test
        @DisplayName("renamableName(Component) should set and return custom name through CUSTOM_NAME component")
        void renamableName_shouldSetAndReturnCustomNameWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            Component name = Component.text("Rename Me");
            builder.renamableName(name);

            Component stored = builder.renamableName();
            assertEquals(name.decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE), stored);
        }

        @Test
        @DisplayName("renamableName(Component) should set italic decoration to false when not specified")
        void renamableName_shouldSetItalicFalseWhenExplicitlySpecified() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            Component original = Component.text("No italics specified");
            builder.renamableName(original);

            Component stored = builder.renamableName();
            assertEquals(TextDecoration.State.FALSE, stored.decoration(TextDecoration.ITALIC));
        }

        @Test
        @DisplayName("renamable(Component) should preserve existing italic decoration when already specified")
        void renamableName_shouldPreserveItalicDecorationWhenAlreadySpecified() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            Component original = Component.text("Already italic").decorate(TextDecoration.ITALIC);
            builder.renamableName(original);

            Component stored = builder.renamableName();
            assertEquals(TextDecoration.State.TRUE, stored.decoration(TextDecoration.ITALIC));
        }

        @Test
        @DisplayName("itemModel(Key) should set and get ITEM_MODEL data component")
        void itemModel_shouldSetAndReturnModelKeyWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            Key modelKey = Key.key("minecraft", "netherite_ingot");
            builder.itemModel(modelKey);

            assertEquals(modelKey, builder.itemModel());
            assertEquals(modelKey, builder.build().getData(DataComponentTypes.ITEM_MODEL));
        }

        @Test
        @DisplayName("damage(int) should set and return DAMAGE component value")
        void damage_shouldSetAndReturnDamageValueWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.IRON_SWORD);
            assertEquals(null, builder.damage());

            builder.damage(10);

            assertEquals(10, builder.damage());
            assertEquals(10, builder.build().getData(DataComponentTypes.DAMAGE));
        }

        @Test
        @DisplayName("durability(int) should set remaining durability based on MAX_DAMAGE and DAMAGE")
        void durability_shouldSetRemainingDurabilityWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.IRON_SWORD);
            builder.maxDamage(Material.IRON_SWORD.asItemType().getMaxDurability());

            int maxDamage = builder.maxDamage();
            int remaining = maxDamage - 5;
            builder.durability(remaining);

            assertEquals(remaining, builder.durability());
            assertEquals(maxDamage - remaining, builder.build().getData(DataComponentTypes.DAMAGE));
        }

        @Test
        @DisplayName("addEnchantment should add single enchantment and preserve existing ones")
        void addEnchantment_shouldAddSingleEnchantmentWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND);
            builder.addEnchantment(Enchantment.SHARPNESS, 5);
            builder.addEnchantment(Enchantment.SWEEPING_EDGE, 3);

            ItemEnchantments enchantments = builder.build().getData(DataComponentTypes.ENCHANTMENTS);
            assertNotNull(enchantments);

            Map<Enchantment, Integer> map = enchantments.enchantments();
            assertEquals(2, map.size());
            assertEquals(5, map.get(Enchantment.SHARPNESS));
            assertEquals(3, map.get(Enchantment.SWEEPING_EDGE));
        }

        @Test
        @DisplayName("enchantment should overwrite all enchantments with single entry")
        void enchantment_shouldOverwriteEnchantmentsWithSingleEntryWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addEnchantment(Enchantment.SHARPNESS, 5);

            builder.enchantment(Enchantment.SWEEPING_EDGE, 2);

            Map<Enchantment, Integer> map = builder.enchantmentsMap();
            assertEquals(1, map.size());
            assertEquals(2, map.get(Enchantment.SWEEPING_EDGE));
        }

        @Test
        @DisplayName("addEnchantments should add multiple enchantments and preserve existing ones")
        void addEnchantments_shouldAddMultipleEnchantmentsAndPreserveExistingWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addEnchantment(Enchantment.SHARPNESS, 5);

            Map<Enchantment, Integer> more = new HashMap<>();
            more.put(Enchantment.SWEEPING_EDGE, 3);
            more.put(Enchantment.UNBREAKING, 4);

            builder.addEnchantments(more);

            Map<Enchantment, Integer> map = builder.enchantmentsMap();
            assertEquals(3, map.size());
            assertEquals(5, map.get(Enchantment.SHARPNESS));
            assertEquals(3, map.get(Enchantment.SWEEPING_EDGE));
            assertEquals(4, map.get(Enchantment.UNBREAKING));
        }

        @Test
        @DisplayName("enchantments(Map) should overwrite all enchantments with provided map")
        void enchantmentsMapSetter_shouldOverwriteAllEnchantmentsWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addEnchantment(Enchantment.SHARPNESS, 5);

            Map<Enchantment, Integer> map = new HashMap<>();
            map.put(Enchantment.EFFICIENCY, 3);
            builder.enchantments(map);

            Map<Enchantment, Integer> result = builder.enchantmentsMap();
            assertEquals(1, result.size());
            assertEquals(3, result.get(Enchantment.EFFICIENCY));
        }

        @Test
        @DisplayName("removeEnchantment should remove single enchantment when present")
        void removeEnchantment_shouldRemoveSingleEnchantmentWhenPresent() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addEnchantment(Enchantment.SHARPNESS, 5);
            builder.addEnchantment(Enchantment.SWEEPING_EDGE, 2);

            builder.removeEnchantment(Enchantment.SHARPNESS);

            Map<Enchantment, Integer> map = builder.enchantmentsMap();
            assertEquals(1, map.size());
            assertFalse(map.containsKey(Enchantment.SHARPNESS));
            assertTrue(map.containsKey(Enchantment.SWEEPING_EDGE));
        }

        @Test
        @DisplayName("removeEnchantments(Enchantment...) should remove all specified enchantments when present")
        void removeEnchantmentsVarargs_shouldRemoveMultipleEnchantmentsWhenPresent() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addEnchantment(Enchantment.SHARPNESS, 5);
            builder.addEnchantment(Enchantment.SWEEPING_EDGE, 2);
            builder.addEnchantment(Enchantment.UNBREAKING, 3);

            builder.removeEnchantments(Enchantment.SHARPNESS, Enchantment.SWEEPING_EDGE);

            Map<Enchantment, Integer> map = builder.enchantmentsMap();
            assertEquals(1, map.size());
            assertTrue(map.containsKey(Enchantment.UNBREAKING));
        }

        @Test
        @DisplayName("removeEnchantments() should remove all enchantments leaving empty ItemEnchantments component")
        void removeEnchantmentsNoArgs_shouldRemoveAllEnchantmentsWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addEnchantment(Enchantment.SHARPNESS, 5);

            builder.removeEnchantments();

            ItemEnchantments enchantments = builder.build().getData(DataComponentTypes.ENCHANTMENTS);
            assertNotNull(enchantments);
            assertTrue(enchantments.enchantments().isEmpty());
        }

        @Test
        @DisplayName("enchantments() should return ItemEnchantments or null when none set")
        void enchantmentsGetter_shouldReturnItemEnchantmentsOrNullWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            assertNull(builder.enchantments());

            builder.addEnchantment(Enchantment.SHARPNESS, 5);
            assertNotNull(builder.enchantments());
        }

        @Test
        @DisplayName("enchantmentsMap() should return empty map when no enchantments set")
        void enchantmentsMap_shouldReturnEmptyMapWhenNoEnchantmentsSet() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            assertTrue(builder.enchantmentsMap().isEmpty());
        }

        @Test
        @DisplayName("forceGlowing(true) should set ENCHANTMENT_GLINT_OVERRIDE and forcedGlowing should return true")
        void forceGlowing_shouldSetGlintOverrideWhenTrue() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.forceGlowing(true);

            assertTrue(builder.forcedGlowing());
            assertTrue(builder.build().hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE));
        }

        @Test
        @DisplayName("forceGlowing(false) should set ENCHANTMENT_GLINT_OVERRIDE and forcedGlowing() should return false")
        void forceGlowing_shouldSetGlintOverrideWhenFalse() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.forceGlowing(true);

            builder.forceGlowing(false);

            assertFalse(builder.forcedGlowing());
            assertFalse(builder.build().getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE));
        }

        @Test
        @DisplayName("resetGlowing should unset ENCHANTMENT_GLINT_OVERRIDE and forcedGlowing() should return false")
        void resetGlowing_shouldRemoveGlintOverrideWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.forceGlowing(true);

            builder.resetGlowing();

            assertFalse(builder.forcedGlowing());
            assertFalse(builder.build().hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE));
        }

        @Disabled("Freeze the tests")
        @Test
        @DisplayName("headTexture(OfflinePlayer) should store profile with player's UUID")
        void headTextureOfflinePlayer_shouldStoreProfileWithPlayerUUIDWhenCalled() {
            PlayerMock player = server.addPlayer("HeadOwner");
            ItemBuilder builder = ItemBuilder.of(Material.PLAYER_HEAD);
            builder.headTexture(player);

            ResolvableProfile profile = builder.headTextureProfile();
            assertNotNull(profile);
            assertEquals(player.getUniqueId(), profile.uuid());
        }

        @Test
        @DisplayName("headTexture(String) should store base64 encoded textures property for given URL")
        void headTextureString_shouldStoreBase64TexturesPropertyWhenCalled() {
            String url = "https//textures.minecraft.net/texture/test_texture";
            ItemBuilder builder = ItemBuilder.of(Material.PLAYER_HEAD);
            builder.headTexture(url);

            String base64 = builder.headTextureBase64();
            assertNotNull(base64);

            String expectedJson = "{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}".formatted(url);
            String expectedBase64 = Base64.getEncoder().encodeToString(expectedJson.getBytes(StandardCharsets.UTF_8));

            assertEquals(expectedBase64, base64);
        }

        @Test
        @DisplayName("headTextureBase64 should return null when no profile or no textures property configured")
        void headTextureBase64_shouldReturnNullWhenNoProfileOrTexturesProperty() {
            ItemBuilder builder = ItemBuilder.of(Material.PLAYER_HEAD);
            assertNull(builder.headTextureBase64());
        }


        @Disabled("Freeze the tests")
        @Test
        @DisplayName("headTextureProfile should return null when profile not set and non-null when set")
        void headTextureProfile_shouldReturnNullWhenNotSetAndNonNullWhenSet() {
            ItemBuilder builder = ItemBuilder.of(Material.PLAYER_HEAD);
            assertNull(builder.headTextureProfile());

            PlayerMock player = server.addPlayer("HeadOwnerB");
            builder.headTexture(player);

            assertNotNull(builder.headTextureProfile());
        }

        @Test
        @DisplayName("unbreakable(true) should set UNBREAKABLE component and unbreakable() should return true")
        void unbreakableSetter_shouldSetUnbreakableWhenTrue() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.unbreakable(true);

            assertTrue(builder.unbreakable());
            assertTrue(builder.build().hasData(DataComponentTypes.UNBREAKABLE));
        }

        @Test
        @DisplayName("unbreakable(false) should unset UNBREAKABLE component and unbreakable() should return false")
        void unbreakableSetter_shouldUnsetUnbreakableWhenFalse() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.unbreakable(true);

            builder.unbreakable(false);

            assertFalse(builder.unbreakable());
            assertFalse(builder.build().hasData(DataComponentTypes.UNBREAKABLE));
        }

        @Test
        @DisplayName("lore(Component...) should set lore lines with italic decoration set to false by default")
        void loreVarargs_shouldSetLoreLinesWithItalicFalseByDefault() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            Component line1 = Component.text("First line");
            Component line2 = Component.text("Second line");

            builder.lore(line1, line2);

            ItemLore lore = builder.build().getData(DataComponentTypes.LORE);
            assertNotNull(lore);

            List<Component> lines = lore.lines();
            assertEquals(2, lines.size());
            assertEquals(TextDecoration.State.FALSE, lines.get(0).decoration(TextDecoration.ITALIC));
            assertEquals(TextDecoration.State.FALSE, lines.get(1).decoration(TextDecoration.ITALIC));
        }

        @Test
        @DisplayName("lore(List) should set lore from list and apply default italic decoration behavior")
        void loreList_shouldSetLoreLinesFromListWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            List<Component> lines = List.of(
                    Component.text("Line A"),
                    Component.text("Line B")
            );

            builder.lore(lines);

            ItemLore lore = builder.build().getData(DataComponentTypes.LORE);
            assertNotNull(lore);
            assertEquals(2, lore.lines().size());
        }

        @Test
        @DisplayName("removeLore should unset LORE component and lore() should return null")
        void removeLore_shouldUnsetLoreComponentWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            builder.lore(Component.text("To be removed"));

            builder.removeLore();

            assertFalse(builder.build().hasData(DataComponentTypes.LORE));
            assertNull(builder.lore());
        }

        @Test
        @DisplayName("loreLine should return specific lore line by index when present")
        void loreLine_shouldReturnLoreComponentWhenIndexWithinBounds() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            builder.lore(Component.text("Line 0"), Component.text("Line 1"));

            Component line = builder.loreLine(1);
            assertEquals(
                    Component.text("Line 1").decorationIfAbsent(
                            TextDecoration.ITALIC,
                            TextDecoration.State.FALSE
                    ),
                    line
            );
        }

        @Test
        @DisplayName("loreLine should return null when index is out of bounds or lore not present")
        void loreLine_shouldReturnNullWhenIndexOutOfBoundsOrLoreMissing() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            assertNull(builder.loreLine(0));

            builder.lore(Component.text("Only line"));
            assertNull(builder.loreLine(5));
        }

        @Test
        @DisplayName("addLoreLine should append new lore line when lore already exists")
        void addLoreLine_shouldAppendNewLoreLineWhenLoreExists() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            builder.lore(Component.text("Line 0"));

            builder.addLoreLine(Component.text("Line 1"));

            List<Component> lore = builder.lore();
            assertNotNull(lore);
            assertEquals(2, lore.size());
            assertEquals(
                    Component.text("Line 1").decorationIfAbsent(
                            TextDecoration.ITALIC,
                            TextDecoration.State.FALSE
                    ),
                    lore.get(1)
            );
        }

        @Test
        @DisplayName("setLoreLine should replace lore line at given index when present")
        void setLoreLine_shouldReplaceLoreLineWhenIndexValid() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            builder.lore(Component.text("Old line"));

            builder.setLoreLine(Component.text("New line"), 0);

            assertEquals(
                    Component.text("New line").decorationIfAbsent(
                            TextDecoration.ITALIC,
                            TextDecoration.State.FALSE
                    ),
                    builder.lore().get(0)
            );
        }

        @Test
        @DisplayName("removeLoreLine(Component) should remove first matching lore line when present")
        void removeLoreLineByComponent_shouldRemoveFirstMatchingLineWhenPresent() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            Component toRemove = Component.text("Remove me");
            builder.lore(toRemove, Component.text("Other line"));

            builder.removeLoreLine(toRemove);

            List<Component> lore = builder.lore();
            assertNotNull(lore);
            assertEquals(1, lore.size());
            assertEquals(
                    Component.text("Other line").decorationIfAbsent(
                            TextDecoration.ITALIC,
                            TextDecoration.State.FALSE
                    ),
                    lore.get(0)
            );
        }

        @Test
        @DisplayName("removeLoreLine(int) should remove lore line at given index when present")
        void removeLoreLineByIndex_shouldRemoveLoreLineWhenIndexValid() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            builder.lore(Component.text("Line 0"), Component.text("Line 1"));

            builder.removeLoreLine(0);

            List<Component> lore = builder.lore();
            assertNotNull(lore);
            assertEquals(1, lore.size());
            assertEquals(
                    Component.text("Line 1").decorationIfAbsent(
                            TextDecoration.ITALIC,
                            TextDecoration.State.FALSE
                    ),
                    lore.get(0)
            );
        }

        @Test
        @DisplayName("lore() should return full lore list or null when none set")
        void loreGetter_shouldReturnLoreListOrNullWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            assertNull(builder.lore());

            builder.lore(Component.text("Present"));
            assertNotNull(builder.lore());
            assertEquals(1, builder.lore().size());
        }

        @Test
        @DisplayName("tooltipStyle(Key) should set and get TOOLTIP_STYLE component value")
        void tooltipStyle_shouldSetAndReturnKeyWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            Key key = Key.key("niveria", "test_style");

            builder.tooltipStyle(key);

            assertEquals(key, builder.tooltipStyle());
            assertEquals(key, builder.build().getData(DataComponentTypes.TOOLTIP_STYLE));
        }

        @Test
        @DisplayName("tooltipStyle() should return null when no tooltip style set")
        void tooltipStyleGetter_shouldReturnNullWhenNotSet() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            assertNull(builder.tooltipStyle());
        }

        private AttributeModifier newTestModifier(double amount) {
            return new AttributeModifier(
                    new NamespacedKey("niveria", UUID.randomUUID().toString()),
                    amount,
                    AttributeModifier.Operation.ADD_NUMBER
            );
        }

        @Test
        @DisplayName("addAttributeModifier should add modifier and preserve existing modifiers")
        void addAttributeModifier_shouldAddModifierAndPreserveExistingWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            AttributeModifier mod1 = newTestModifier(5.0);
            AttributeModifier mod2 = newTestModifier(2.0);

            builder.addAttributeModifier(Attribute.ATTACK_DAMAGE, mod1);
            builder.addAttributeModifier(Attribute.ATTACK_SPEED, mod2);

            Map<Attribute, AttributeModifier> map = builder.attributeModifiers();
            assertEquals(2, map.size());
            assertEquals(mod1, map.get(Attribute.ATTACK_DAMAGE));
            assertEquals(mod2, map.get(Attribute.ATTACK_SPEED));
        }

        @Test
        @DisplayName("addAttributeModifiers should add multiple modifiers and preserve existing ones")
        void addAttributeModifiers_shouldAddMultipleModifiersAndPreserveExistingWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            AttributeModifier existing = newTestModifier(5.0);
            builder.addAttributeModifier(Attribute.ATTACK_DAMAGE, existing);

            Map<Attribute, AttributeModifier> more = new HashMap<>();
            more.put(Attribute.ATTACK_SPEED, newTestModifier(1.0));
            more.put(Attribute.MAX_HEALTH, newTestModifier(2.0));

            builder.addAttributeModifiers(more);

            Map<Attribute, AttributeModifier> result = builder.attributeModifiers();
            assertEquals(3, result.size());
        }

        @Test
        @DisplayName("attributeModifiers(Attribute,Modifier) should overwrite modifiers with single attribute entry")
        void attributeModifiersSingle_shouldOverwriteWithSingleEntryWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addAttributeModifier(Attribute.ATTACK_DAMAGE, newTestModifier(5.0));

            AttributeModifier replacement = newTestModifier(10.0);
            builder.attributeModifiers(Attribute.MAX_HEALTH, replacement);

            Map<Attribute, AttributeModifier> map = builder.attributeModifiers();
            assertEquals(1, map.size());
            assertEquals(replacement, map.get(Attribute.MAX_HEALTH));
        }

        @Test
        @DisplayName("attributeModifiers(Map) should overwrite all modifiers with provided map")
        void attributeModifiersMapSetter_shouldOverwriteAllModifiersWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addAttributeModifier(Attribute.ATTACK_DAMAGE, newTestModifier(5.0));

            Map<Attribute, AttributeModifier> map = new HashMap<>();
            AttributeModifier mod = newTestModifier(3.0);
            map.put(Attribute.ARMOR, mod);

            builder.attributeModifiers(map);

            Map<Attribute, AttributeModifier> result = builder.attributeModifiers();
            assertEquals(1, result.size());
            assertEquals(mod, result.get(Attribute.ARMOR));
        }

        @Test
        @DisplayName("removeAttributeModifier should remove all modifiers for specified attribute")
        void removeAttributeModifier_shouldRemoveAllModifiersForAttributeWhenPresent() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addAttributeModifier(Attribute.ATTACK_DAMAGE, newTestModifier(5.0));
            builder.addAttributeModifier(Attribute.ATTACK_SPEED, newTestModifier(1.0));

            builder.removeAttributeModifier(Attribute.ATTACK_DAMAGE);

            Map<Attribute, AttributeModifier> result = builder.attributeModifiers();
            assertFalse(result.containsKey(Attribute.ATTACK_DAMAGE));
            assertTrue(result.containsKey(Attribute.ATTACK_SPEED));
        }

        @Test
        @DisplayName("removeAttributeModifiers(Attribute...) should remove modifiers for all specified attributes")
        void removeAttributeModifiersVarargs_shouldRemoveSpecifiedAttributesWhenPresent() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addAttributeModifier(Attribute.ATTACK_DAMAGE, newTestModifier(5.0));
            builder.addAttributeModifier(Attribute.ATTACK_SPEED, newTestModifier(1.0));
            builder.addAttributeModifier(Attribute.ARMOR, newTestModifier(2.0));

            builder.removeAttributeModifiers(
                    Attribute.ATTACK_DAMAGE,
                    Attribute.ARMOR
            );

            Map<Attribute, AttributeModifier> result = builder.attributeModifiers();
            assertEquals(1, result.size());
            assertTrue(result.containsKey(Attribute.ATTACK_SPEED));
        }

        @Test
        @DisplayName("removeAttributeModifiers() should clear modifiers leaving empty ATTRIBUTE_MODIFIERS component")
        void removeAttributeModifiersNoArgs_shouldClearModifiersWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addAttributeModifier(Attribute.ATTACK_DAMAGE, newTestModifier(5.0));

            builder.removeAttributeModifiers();

            ItemAttributeModifiers data = builder.build().getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
            assertNotNull(data);
            assertTrue(data.modifiers().isEmpty());
        }

        @Test
        @DisplayName("resetAttributesModifiers should unset ATTRIBUTE_MODIFIERS component and clear map")
        void resetAttributesModifiers_shouldUnsetComponentWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            builder.addAttributeModifier(Attribute.ATTACK_DAMAGE, newTestModifier(5.0));

            builder.resetAttributesModifiers();

            assertFalse(builder.build().hasData(DataComponentTypes.ATTRIBUTE_MODIFIERS));
            assertTrue(builder.attributeModifiers().isEmpty());
        }

        @Test
        @DisplayName("attributeModifiers() should return map of attribute modifiers or empty when none set")
        void attributeModifiersGetter_shouldReturnMapOfModifiersOrEmptyWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            assertTrue(builder.attributeModifiers().isEmpty());

            AttributeModifier mod = newTestModifier(5.0);
            builder.addAttributeModifier(Attribute.ATTACK_DAMAGE, mod);

            Map<Attribute, AttributeModifier> map = builder.attributeModifiers();
            assertEquals(1, map.size());
            assertEquals(mod, map.get(Attribute.ATTACK_DAMAGE));
        }

        @Test
        @DisplayName("customModelData(CustomModelData) should set and return custom model data object")
        void customModelDataObjectSetter_shouldSetAndReturnWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            CustomModelData data = CustomModelData.customModelData()
                    .addFloat(1.5F)
                    .build();

            builder.customModelData(data);

            assertEquals(data, builder.customModelData());
            assertEquals(data, builder.build().getData(DataComponentTypes.CUSTOM_MODEL_DATA));
        }

        @Test
        @DisplayName("customModelData(float) should create and set CUSTOM_MODEL_DATA with single float value")
        void customModelDataFloatSetter_shouldCreateAndSetWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            builder.customModelData(2.5F);

            assertNotNull(builder.customModelData());
            assertTrue(builder.build().hasData(DataComponentTypes.CUSTOM_MODEL_DATA));
        }

        @Test
        @DisplayName("resetCustomModelData should unset CUSTOM_MODEL_DATA component and customModelData() should return null")
        void resetCustomModelData_shouldUnsetComponentWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            builder.customModelData(1.0F);

            builder.resetCustomModelData();

            assertNull(builder.customModelData());
            assertFalse(builder.build().hasData(DataComponentTypes.CUSTOM_MODEL_DATA));
        }

        @Test
        @DisplayName("customModelData() should return null when no custom model data set")
        void customModelDataGetter_shouldReturnNullWhenNotSet() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            assertNull(builder.customModelData());
        }

        @ParameterizedTest
        @ValueSource(ints = {1, 16, 99})
        @DisplayName("maxStackSize(int) should set MAX_STACK_SIZE component for valid values")
        void maxStackSizeSetter_shouldSetMaxStackSizeWhenCalled(int max) {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            builder.maxStackSize(max);

            assertEquals(max, builder.maxStackSize());
            assertEquals(max, builder.build().getData(DataComponentTypes.MAX_STACK_SIZE));
        }

        @Test
        @DisplayName("maxStackSize() should return null when custom max stack size not set")
        void maxStackSizeGetter_shouldReturnNullWhenNotSet() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            assertNull(builder.maxStackSize());
        }

        @Test
        @DisplayName("addBannerPattern should add single banner pattern and preserve existing patterns")
        void addBannerPattern_shouldAddPatternAndPreserveExistingWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.WHITE_BANNER);
            Pattern pattern1 = new Pattern(DyeColor.RED, PatternType.STRIPE_DOWNRIGHT);
            Pattern pattern2 = new Pattern(DyeColor.BLUE, PatternType.CIRCLE);

            builder.addBannerPattern(pattern1);
            builder.addBannerPattern(pattern2);

            List<Pattern> patterns = builder.bannerPatterns();
            assertEquals(2, patterns.size());
            assertEquals(pattern1, patterns.get(0));
            assertEquals(pattern2, patterns.get(1));
        }

        @Test
        @DisplayName("addBannerPatterns should add multiple patterns and preserve existing ones")
        void addBannerPatterns_shouldAddMultiplePatternsAndPreserveExistingWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.WHITE_BANNER);
            Pattern existing = new Pattern(DyeColor.RED, PatternType.STRIPE_DOWNRIGHT);
            builder.addBannerPattern(existing);

            Pattern p1 = new Pattern(DyeColor.BLUE, PatternType.CROSS);
            Pattern p2 = new Pattern(DyeColor.GREEN, PatternType.BRICKS);

            builder.addBannerPatterns(p1, p2);

            List<Pattern> patterns = builder.bannerPatterns();
            assertEquals(3, patterns.size());
            assertEquals(existing, patterns.get(0));
            assertEquals(p1, patterns.get(1));
            assertEquals(p2, patterns.get(2));
        }

        @Test
        @DisplayName("bannerPatterns(Pattern...) should overwrite patterns with provided array")
        void bannerPatternsVarargs_shouldOverwritePatternsWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.WHITE_BANNER);
            builder.addBannerPattern(new Pattern(DyeColor.RED, PatternType.STRIPE_DOWNRIGHT));

            Pattern p1 = new Pattern(DyeColor.BLUE, PatternType.CROSS);
            Pattern p2 = new Pattern(DyeColor.GREEN, PatternType.BRICKS);
            builder.bannerPatterns(p1, p2);

            List<Pattern> patterns = builder.bannerPatterns();
            assertEquals(2, patterns.size());
            assertEquals(p1, patterns.get(0));
            assertEquals(p2, patterns.get(1));
        }

        @Test
        @DisplayName("bannerPatterns(List) should overwrite patterns with provided list")
        void bannerPatternsList_shouldOverwritePatternsWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.WHITE_BANNER);

            List<Pattern> list = List.of(
                    new Pattern(DyeColor.BLACK, PatternType.BORDER),
                    new Pattern(DyeColor.WHITE, PatternType.CIRCLE)
            );

            builder.bannerPatterns(list);

            List<Pattern> patterns = builder.bannerPatterns();
            assertEquals(list, patterns);
        }

        @Test
        @DisplayName("resetBannerPatterns should unset BANNER_PATTERNS component and bannerPatterns() should return empty")
        void resetBannerPatterns_shouldUnsetComponentWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.WHITE_BANNER);
            builder.addBannerPattern(new Pattern(DyeColor.RED, PatternType.STRIPE_DOWNRIGHT));

            builder.resetBannerPatterns();

            assertTrue(builder.bannerPatterns().isEmpty());
            assertFalse(builder.build().hasData(DataComponentTypes.BANNER_PATTERNS));
        }

        @Test
        @DisplayName("bannerPatterns() should return empty list when no banner patterns set")
        void bannerPatternsGetter_shouldReturnEmptyListWhenNoPatternsSet() {
            ItemBuilder builder = ItemBuilder.of(Material.WHITE_BANNER);
            assertTrue(builder.bannerPatterns().isEmpty());
        }

        @Test
        @DisplayName("dyeColor(Color) should set DYED_COLOR and dyeColor() should return same color")
        void dyeColorSetter_shouldSetDyedColorWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.LEATHER_CHESTPLATE);
            Color color = Color.fromRGB(255, 0, 0);

            builder.dyeColor(color);

            assertEquals(color, builder.dyeColor());

            DyedItemColor component = builder.build().getData(DataComponentTypes.DYED_COLOR);
            assertNotNull(component);
            assertEquals(color, component.color());
        }

        @Test
        @DisplayName("resetDyeColor should unset DYED_COLOR and dyeColor() should return null")
        void resetDyeColor_shouldUnsetComponentWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.LEATHER_CHESTPLATE);
            builder.dyeColor(Color.BLUE);

            builder.resetDyeColor();

            assertNull(builder.dyeColor());
            assertFalse(builder.build().hasData(DataComponentTypes.DYED_COLOR));
        }

        @Test
        @DisplayName("dyeColor() should return null when no DYED_COLOR set")
        void dyeColorGetter_shouldReturnNullWhenNotSet() {
            ItemBuilder builder = ItemBuilder.of(Material.LEATHER_CHESTPLATE);
            assertNull(builder.dyeColor());
        }

        @Test
        @DisplayName("itemColor should map item material to its DyeColor or default to WHITE for non-colored items")
        void itemColor_shouldReturnDyeColorForMaterialWhenCalled() {
            ItemBuilder orangeBanner = ItemBuilder.of(Material.ORANGE_BANNER);
            ItemBuilder redBanner = ItemBuilder.of(Material.RED_BANNER);
            ItemBuilder unknown = ItemBuilder.of(Material.STONE);

            assertEquals(DyeColor.ORANGE, orangeBanner.itemColor());
            assertEquals(DyeColor.RED, redBanner.itemColor());
            assertEquals(DyeColor.WHITE, unknown.itemColor());
        }

        @Test
        @DisplayName("component(Valued,T) should set and get arbitrary valued data component")
        void componentValued_shouldSetAndGetArbitraryComponentWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);

            builder.component(DataComponentTypes.MAX_STACK_SIZE, 42);

            Integer value = builder.component(DataComponentTypes.MAX_STACK_SIZE);
            assertEquals(42, value);
            assertEquals(42, builder.build().getData(DataComponentTypes.MAX_STACK_SIZE));
        }

        @Test
        @DisplayName("component(NonValued) should set presence-only component and resetComponent should unset it")
        void componentNonValued_shouldSetPresenceOnlyComponentWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);

            builder.component(DataComponentTypes.UNBREAKABLE);
            assertTrue(builder.unbreakable());

            builder.resetComponent(DataComponentTypes.UNBREAKABLE);
            assertFalse(builder.unbreakable());
        }

        @Test
        @DisplayName("persistentData should store integer value in item PersistentDataContainer using Adventure Key")
        void persistentData_shouldStoreIntegerValueInPersistentDataContainerWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            Key key = Key.key("niveriatest", "int_flag");
            builder.persistentData(key, PersistentDataType.INTEGER, 123);

            ItemStack item = builder.build();
            Integer stored = item.getPersistentDataContainer().get(
                    new NamespacedKey("niveriatest", "int_flag"),
                    PersistentDataType.INTEGER
            );

            assertEquals(123, stored);
        }

        @Test
        @DisplayName("persistentData should store boolean value in item PersistentDataContainer using Adventure Key")
        void persistentData_shouldStoreBooleanValueInPersistentDataContainerWhenCalled() {
            ItemBuilder builder = ItemBuilder.of(Material.STONE);
            Key key = Key.key("niveriatest", "bool_flag");
            builder.persistentData(key, PersistentDataType.BOOLEAN, true);

            ItemStack item = builder.build();
            Boolean stored = item.getPersistentDataContainer().get(
                    new NamespacedKey("niveriatest", "bool_flag"),
                    PersistentDataType.BOOLEAN
            );

            assertTrue(stored);
        }

        @Test
        @DisplayName("hide should add hidden components to tooltip display when tooltip not already fully hidden")
        void hide_shouldAddHiddenComponentsWhenTooltipNotHidden() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);

            builder.hide(DataComponentTypes.LORE, DataComponentTypes.ENCHANTMENTS);

            Set<?> hidden = builder.hiddenComponents();
            assertTrue(hidden.contains(DataComponentTypes.LORE));
            assertTrue(hidden.contains(DataComponentTypes.ENCHANTMENTS));

            TooltipDisplay display = builder.build().getData(DataComponentTypes.TOOLTIP_DISPLAY);
            assertNotNull(display);
            assertFalse(display.hideTooltip());
        }

        @Test
        @DisplayName("hide should not modify hidden components when hideTooltip(true) already set")
        void hide_shouldNotModifyHiddenComponentsWhenTooltipAlreadyHidden() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);

            builder.hideTooltip(true);
            Set<?> before = builder.hiddenComponents();

            builder.hide(DataComponentTypes.LORE);

            Set<?> after = builder.hiddenComponents();
            assertEquals(before, after);
            assertTrue(builder.hideTooltip());
        }

        @Test
        @DisplayName("hiddenComponents should return empty set when TOOLTIP_DISPLAY is not present")
        void hiddenComponents_shouldReturnEmptySetWhenTooltipDisplayMissing() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            assertTrue(builder.hiddenComponents().isEmpty());
        }

        @ParameterizedTest
        @ValueSource(booleans = {true, false})
        @DisplayName("hideTooltip(boolean) should set hideTooltip flag and hideTooltip() should reflect it")
        void hideTooltipSetter_shouldSetHideTooltipFlagWhenCalled(boolean hide) {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);

            builder.hideTooltip(hide);

            assertEquals(hide, builder.hideTooltip());

            TooltipDisplay display = builder.build().getData(DataComponentTypes.TOOLTIP_DISPLAY);
            assertNotNull(display);
            assertEquals(hide, display.hideTooltip());
        }

        @Test
        @DisplayName("hideTooltip() should return false when TOOLTIP_DISPLAY is not present")
        void hideTooltipGetter_shouldReturnFalseWhenTooltipDisplayMissing() {
            ItemBuilder builder = ItemBuilder.of(Material.DIAMOND_SWORD);
            assertFalse(builder.hideTooltip());
        }
    }
}
