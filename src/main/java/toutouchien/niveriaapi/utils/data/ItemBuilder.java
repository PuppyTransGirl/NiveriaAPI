package toutouchien.niveriaapi.utils.data;

import com.destroystokyo.paper.profile.ProfileProperty;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility builder for creating and modifying ItemStack instances using Paper's
 * DataComponent APIs (item data components, attributes, enchantments, lore,
 * profiles, etc.). The builder wraps an ItemStack and provides fluent methods
 * to set/get many of the commonly used data component properties.
 *
 * <p>Usage example:
 * <pre>
 * ItemStack item = ItemBuilder.of(Material.DIAMOND_SWORD)
 *     .name(Component.text("Epic Blade"))
 *     .addEnchantment(Enchantment.DAMAGE_ALL, 5)
 *     .unbreakable(true)
 *     .build();
 * </pre>
 *
 * Notes:
 * - Many methods use Paper's {@link DataComponentTypes} and will only work on
 *   servers that support Paper's DataComponent API.
 * - Methods throw IllegalArgumentException for invalid inputs (e.g. air
 *   materials, invalid amounts). The check against air materials exists because
 *   air does not have an {@link ItemMeta} and many item meta/data operations
 *   would be invalid for air items.
 */
@SuppressWarnings({"UnstableApiUsage", "ClassCanBeRecord", "unused"})
public class ItemBuilder {
    private final ItemStack itemStack;

    private ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    /**
     * Create a new ItemBuilder for the given material.
     *
     * @param material the non-air Material for the item
     * @return a new ItemBuilder wrapping a newly created ItemStack
     * @throws IllegalArgumentException if the material is air (air doesn't
     *                                  have an {@link ItemMeta})
     */
    @NotNull
    public static ItemBuilder of(@NotNull Material material) {
        if (material.isAir())
            throw new IllegalArgumentException("Material cannot be air.");

        return new ItemBuilder(ItemStack.of(material));
    }

    /**
     * Create a new ItemBuilder wrapping an existing ItemStack.
     *
     * @param itemStack the ItemStack to wrap (type must not be air)
     * @return a new ItemBuilder
     * @throws IllegalArgumentException if the item's material is air (air
     *                                  doesn't have an {@link ItemMeta})
     */
    @NotNull
    public static ItemBuilder of(@NotNull ItemStack itemStack) {
        if (itemStack.getType().isAir())
            throw new IllegalArgumentException("Material cannot be air.");

        return new ItemBuilder(itemStack);
    }

    /**
     * Create a new ItemBuilder for the given material and amount.
     *
     * @param material the non-air Material for the item
     * @param amount   the amount to set on the created ItemStack
     * @return a new ItemBuilder
     * @throws IllegalArgumentException if the material is air (air doesn't
     *                                  have an {@link ItemMeta})
     */
    @NotNull
    public static ItemBuilder of(@NotNull Material material, int amount) {
        if (material.isAir())
            throw new IllegalArgumentException("Material cannot be air.");

        return new ItemBuilder(ItemStack.of(material, amount));
    }

    /**
     * Return the wrapped ItemStack.
     *
     * @return the ItemStack instance (not a defensive copy)
     */
    @NotNull
    public ItemStack build() {
        return itemStack;
    }

    /**
     * Return a clone of the wrapped ItemStack.
     *
     * @return a cloned ItemStack
     */
    @NotNull
    public ItemStack buildCopy() {
        return itemStack.clone();
    }

    /**
     * Return a new ItemBuilder wrapping a clone of the current ItemStack.
     *
     * @return a new ItemBuilder with a cloned ItemStack
     */
    @NotNull
    public ItemBuilder copy() {
        return new ItemBuilder(itemStack.clone());
    }

    /**
     * Set the amount (stack size) for this ItemStack.
     *
     * @param amount the desired amount (1..maxStackSize)
     * @return this builder
     * @throws IllegalArgumentException if amount is out of range
     */
    @NotNull
    public ItemBuilder amount(int amount) {
        if (amount < 1 || amount > itemStack.getMaxStackSize())
            throw new IllegalArgumentException("Amount must be between 1 and " + itemStack.getMaxStackSize() + ".");

        itemStack.setAmount(amount);
        return this;
    }

    /**
     * Get the current amount (stack size) of the item.
     *
     * @return the item amount
     */
    public int amount() {
        return itemStack.getAmount();
    }

    /**
     * Set the display name using Paper's {@link DataComponentTypes#ITEM_NAME}
     * data component.
     *
     * @param name the component to set as the name
     * @return this builder
     */
    @NotNull
    public ItemBuilder name(Component name) {
        itemStack.setData(DataComponentTypes.ITEM_NAME, name);
        return this;
    }

    /**
     * Get the display name set via {@link DataComponentTypes#ITEM_NAME}.
     *
     * @return the name component, or null if not set
     */
    @Nullable
    public Component name() {
        return itemStack.getData(DataComponentTypes.ITEM_NAME);
    }

    /**
     * Set a renamable custom name using {@link DataComponentTypes#CUSTOM_NAME}.
     *
     * <p>This method sets a CUSTOM_NAME that players can remove (rename back
     * to default) in an anvil — i.e. it creates a renamable name. It is also
     * used for player heads; note that some head rendering logic may not be
     * affected by changing the method name on certain head types, so this
     * method is appropriate both when you want a renamable name and when
     * working with player head items.
     *
     * <p>The provided component will have its italic decoration set to false
     * only when the developer did not explicitly specify an italic value.
     * This is done via
     * {@link Component#decorationIfAbsent(TextDecoration, TextDecoration.State)}
     * — the method does not force italic off if the developer already set it.
     *
     * @param name the custom name component
     * @return this builder
     */
    @NotNull
    public ItemBuilder renamableName(Component name) {
        itemStack.setData(DataComponentTypes.CUSTOM_NAME, name.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
        return this;
    }

    /**
     * Get the {@link DataComponentTypes#CUSTOM_NAME} value for the item.
     *
     * @return the custom name component, or null if not present
     */
    @Nullable
    public Component renamableName() {
        return itemStack.getData(DataComponentTypes.CUSTOM_NAME);
    }

    /**
     * Set the item model key using {@link DataComponentTypes#ITEM_MODEL}.
     *
     * @param itemModelKey the model key to assign
     * @return this builder
     */
    @NotNull
    public ItemBuilder itemModel(Key itemModelKey) {
        itemStack.setData(DataComponentTypes.ITEM_MODEL, itemModelKey);
        return this;
    }

    /**
     * Get the item model key assigned to this item ({@link DataComponentTypes#ITEM_MODEL}).
     *
     * @return the model key
     */
    @NotNull
    public Key itemModel() {
        return itemStack.getData(DataComponentTypes.ITEM_MODEL);
    }

    /**
     * Set the current durability (remaining durability) of the item.
     * Uses {@link DataComponentTypes#DAMAGE} and {@link DataComponentTypes#MAX_DAMAGE}.
     *
     * @param durability durability value (remaining), will be converted to DAMAGE
     * @return this builder
     */
    public ItemBuilder durability(int durability) {
        itemStack.setData(DataComponentTypes.DAMAGE, itemStack.getData(DataComponentTypes.MAX_DAMAGE) - durability);
        return this;
    }

    /**
     * Get the current durability (remaining) of the item.
     *
     * @return remaining durability
     */
    public int durability() {
        return itemStack.getData(DataComponentTypes.MAX_DAMAGE) - itemStack.getData(DataComponentTypes.DAMAGE);
    }

    /**
     * Directly set the raw {@link DataComponentTypes#DAMAGE} data component
     * (damage taken).
     *
     * @param damage the damage value
     * @return this builder
     */
    @NotNull
    public ItemBuilder damage(int damage) {
        itemStack.setData(DataComponentTypes.DAMAGE, damage);
        return this;
    }

    /**
     * Get the raw {@link DataComponentTypes#DAMAGE} data component.
     *
     * @return the damage value or null if not set
     */
    @Nullable
    @NonNegative
    public Integer damage() {
        return itemStack.getData(DataComponentTypes.DAMAGE);
    }

    /**
     * Add a single enchantment to the item while preserving existing enchantments
     * using {@link DataComponentTypes#ENCHANTMENTS}.
     *
     * @param enchantment the enchantment to add
     * @param level       the level for that enchantment
     * @return this builder
     */
    @NotNull
    public ItemBuilder addEnchantment(@NotNull Enchantment enchantment, int level) {
        ItemEnchantments data = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
        ItemEnchantments.Builder itemEnchantments = ItemEnchantments.itemEnchantments()
                .add(enchantment, level);

        if (data != null)
            itemEnchantments.addAll(data.enchantments());

        itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments.build());
        return this;
    }

    /**
     * Overwrite enchantments with a single enchantment entry ({@link DataComponentTypes#ENCHANTMENTS}).
     *
     * @param enchantment the enchantment
     * @param level       the level
     * @return this builder
     */
    @NotNull
    public ItemBuilder enchantment(@NotNull Enchantment enchantment, int level) {
        ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments()
                .add(enchantment, level)
                .build();

        itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
        return this;
    }

    /**
     * Add multiple enchantments while preserving existing enchantments.
     *
     * @param enchantments a map of enchantments to levels
     * @return this builder
     */
    @NotNull
    public ItemBuilder addEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
        ItemEnchantments data = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
        ItemEnchantments.Builder itemEnchantments = ItemEnchantments.itemEnchantments()
                .addAll(enchantments);

        if (data != null)
            itemEnchantments.addAll(data.enchantments());

        itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments.build());
        return this;
    }

    /**
     * Overwrite all enchantments with the provided map.
     *
     * @param enchantments map of enchantments to levels
     * @return this builder
     */
    @NotNull
    public ItemBuilder enchantments(@NotNull Map<Enchantment, Integer> enchantments) {
        ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments()
                .addAll(enchantments)
                .build();

        itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
        return this;
    }

    /**
     * Remove a specific enchantment from the item.
     *
     * @param enchantment the enchantment to remove
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeEnchantment(@NotNull Enchantment enchantment) {
        ItemEnchantments data = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
        if (data == null)
            return this;

        Map<Enchantment, @IntRange(from = 1L, to = 255L) Integer> enchantments = new HashMap<>(data.enchantments());
        enchantments.remove(enchantment);

        ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments()
                .addAll(enchantments)
                .build();

        itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
        return this;
    }

    /**
     * Remove multiple enchantments from the item.
     *
     * @param enchantments the enchantments to remove
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeEnchantments(@NotNull Enchantment... enchantments) {
        ItemEnchantments data = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
        if (data == null)
            return this;

        Map<Enchantment, @IntRange(from = 1L, to = 255L) Integer> enchants = new HashMap<>(data.enchantments());
        Arrays.stream(enchantments).forEach(enchants::remove);

        ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments()
                .addAll(enchants)
                .build();

        itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
        return this;
    }

    /**
     * Remove all enchantments from the item.
     *
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeEnchantments() {
        itemStack.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments().build());
        return this;
    }

    /**
     * Get the {@link DataComponentTypes#ENCHANTMENTS} data component.
     *
     * @return the ItemEnchantments or null if none set
     */
    @Nullable
    public ItemEnchantments enchantments() {
        return itemStack.getData(DataComponentTypes.ENCHANTMENTS);
    }

    /**
     * Get a map of enchantments currently applied to the item.
     *
     * @return a map of Enchantment -> level (empty map if none)
     */
    @NotNull
    public Map<Enchantment, Integer> enchantmentsMap() {
        ItemEnchantments data = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
        return data == null ? Collections.emptyMap() : data.enchantments();
    }

    /**
     * Force or disable the enchantment glint (visual) using
     * {@link DataComponentTypes#ENCHANTMENT_GLINT_OVERRIDE}.
     *
     * @param glowing true to force glint, false to unset
     * @return this builder
     */
    @NotNull
    public ItemBuilder forceGlowing(boolean glowing) {
        itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glowing);
        return this;
    }

    /**
     * Reset the forced glowing state (remove {@link DataComponentTypes#ENCHANTMENT_GLINT_OVERRIDE}).
     *
     * @return this builder
     */
    @NotNull
    public ItemBuilder resetGlowing() {
        itemStack.unsetData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
        return this;
    }

    /**
     * Check whether the enchantment glint was forced.
     *
     * @return true if glint override is present
     */
    public boolean forcedGlowing() {
        return itemStack.hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
    }

    /**
     * Deprecated skull owner helper. Use headTexture variants instead.
     *
     * @param owner player name
     * @return this builder
     * @deprecated since 2.1.0
     */
    @NotNull
    @Deprecated(since = "2.1.0", forRemoval = true)
    public ItemBuilder skullOwner(String owner) {
        return this.headTexture(Bukkit.getOfflinePlayer(owner));
    }

    /**
     * Deprecated skull owner helper. Use headTexture variants instead.
     *
     * @param player offline player instance
     * @return this builder
     * @deprecated since 2.1.0
     */
    @NotNull
    @Deprecated(since = "2.1.0", forRemoval = true)
    public ItemBuilder skullOwner(OfflinePlayer player) {
        return this.headTexture(player);
    }

    /**
     * Deprecated skull owner helper. Use headTexture variants instead.
     *
     * @param url texture URL
     * @return this builder
     * @deprecated since 2.1.0
     */
    @NotNull
    @Deprecated(since = "2.1.0", forRemoval = true)
    public ItemBuilder skullOwner(URL url) {
        return this.headTexture(url);
    }

    /**
     * Deprecated getter for skull owner name.
     *
     * @return owner name or null
     * @deprecated since 2.1.0
     */
    @Nullable
    @Deprecated(since = "2.1.0", forRemoval = true)
    public String skullOwner() {
        ResolvableProfile ownerProfile = itemStack.getData(DataComponentTypes.PROFILE);
        return ownerProfile == null ? null : ownerProfile.name();
    }

    /**
     * Set the head/profile data to reference an OfflinePlayer (by UUID).
     * Useful for player head items; stores a {@link DataComponentTypes#PROFILE}.
     *
     * @param player the offline player whose UUID will be used
     * @return this builder
     */
    @NotNull
    public ItemBuilder headTexture(OfflinePlayer player) {
        ResolvableProfile resolvableProfile = ResolvableProfile.resolvableProfile()
                .uuid(player.getUniqueId())
                .build();

        itemStack.setData(DataComponentTypes.PROFILE, resolvableProfile);
        return this;
    }

    /**
     * Set a head texture using a raw texture URL. The method encodes the
     * required base64 property and creates a {@link DataComponentTypes#PROFILE}
     * with a "textures" property.
     *
     * @param textureURL the full URL to the skin texture
     * @return this builder
     */
    @NotNull
    public ItemBuilder headTexture(String textureURL) {
        byte[] texturesPropertyBytes = Base64.getEncoder().encode("{\"textures\":{\"SKIN\":{\"url\":\"%s\"}}}".formatted(textureURL).getBytes());
        String texturesProperty = new String(texturesPropertyBytes);

        ResolvableProfile resolvableProfile = ResolvableProfile.resolvableProfile()
                .addProperty(new ProfileProperty("textures", texturesProperty))
                .build();

        itemStack.setData(DataComponentTypes.PROFILE, resolvableProfile);
        return this;
    }

    /**
     * Set a head texture using a URL object.
     *
     * @param textureURL the texture URL
     * @return this builder
     */
    @NotNull
    public ItemBuilder headTexture(URL textureURL) {
        return this.headTexture(textureURL.toString());
    }

    /**
     * Get the base64-encoded texture property used for head textures, if any.
     *
     * @return base64 texture string or null if not present
     */
    @Nullable
    public String headTextureBase64() {
        ResolvableProfile resolvableProfile = itemStack.getData(DataComponentTypes.PROFILE);
        if (resolvableProfile == null)
            return null;

        if (resolvableProfile.properties().isEmpty())
            return null;

        Optional<ProfileProperty> property = resolvableProfile.properties().stream()
                .filter(prop -> prop.getName().equals("textures"))
                .findFirst();

        return property.map(ProfileProperty::getValue).orElse(null);
    }

    /**
     * Get the {@link DataComponentTypes#PROFILE} stored on the item (profile used for player
     * heads).
     *
     * @return the ResolvableProfile or null if none set
     */
    @Nullable
    public ResolvableProfile headTextureProfile() {
        return itemStack.getData(DataComponentTypes.PROFILE);
    }

    /**
     * Set or unset the {@link DataComponentTypes#UNBREAKABLE} flag on the item.
     *
     * @param unbreakable true to set unbreakable, false to unset
     * @return this builder
     */
    @NotNull
    public ItemBuilder unbreakable(boolean unbreakable) {
        if (unbreakable)
            itemStack.setData(DataComponentTypes.UNBREAKABLE);
        else
            itemStack.unsetData(DataComponentTypes.UNBREAKABLE);

        return this;
    }

    /**
     * Check whether the item is marked {@link DataComponentTypes#UNBREAKABLE}.
     *
     * @return true if unbreakable
     */
    public boolean unbreakable() {
        return itemStack.hasData(DataComponentTypes.UNBREAKABLE);
    }

    /**
     * Set lore lines for the item using {@link DataComponentTypes#LORE}.
     *
     * <p>Lines will have their italic decoration set only if the developer has
     * not explicitly specified italic (see
     * {@link Component#decorationIfAbsent(TextDecoration, TextDecoration.State)}).
     *
     * @param lore the lore components
     * @return this builder
     */
    @NotNull
    public ItemBuilder lore(@NotNull Component... lore) {
        List<Component> lores = new ArrayList<>();
        Arrays.stream(lore).forEach(l -> lores.add(l.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));

        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lores));
        return this;
    }

    /**
     * Set lore from a list of components ({@link DataComponentTypes#LORE}).
     *
     * @param lore the list of lore components
     * @return this builder
     */
    @NotNull
    public ItemBuilder lore(@NotNull List<Component> lore) {
        List<Component> lores = new ArrayList<>();
        lore.forEach(l -> lores.add(l.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));

        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lores));
        return this;
    }

    /**
     * Remove the {@link DataComponentTypes#LORE} data component from the item.
     *
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeLore() {
        itemStack.unsetData(DataComponentTypes.LORE);
        return this;
    }

    /**
     * Get a specific lore line by index.
     *
     * @param index the line index (0-based)
     * @return the component at index or null if not present
     */
    @Nullable
    public Component loreLine(int index) {
        ItemLore data = itemStack.getData(DataComponentTypes.LORE);
        if (data == null)
            return null;

        List<Component> components = data.styledLines();
        return components.size() > index ? components.get(index) : null;
    }

    /**
     * Add an additional lore line to the existing lore.
     *
     * @param line the lore line to add
     * @return this builder
     */
    @NotNull
    public ItemBuilder addLoreLine(@NotNull Component line) {
        ItemLore data = itemStack.getData(DataComponentTypes.LORE);
        ItemLore itemLore = ItemLore.lore()
                .lines(data.styledLines())
                .addLine(line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
                .build();

        itemStack.setData(DataComponentTypes.LORE, itemLore);
        return this;
    }

    /**
     * Replace a specific lore line at the given index.
     *
     * @param line  the new line component
     * @param index the index to set
     * @return this builder
     */
    @NotNull
    public ItemBuilder setLoreLine(@NotNull Component line, int index) {
        ItemLore data = itemStack.getData(DataComponentTypes.LORE);
        List<Component> lore = new ArrayList<>(data.styledLines());
        lore.set(index, line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
        return this;
    }

    /**
     * Remove the first matching lore line (by equality).
     *
     * @param line the component to remove
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeLoreLine(@NotNull Component line) {
        List<Component> lore = new ArrayList<>(itemStack.getData(DataComponentTypes.LORE).styledLines());
        lore.remove(line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
        return this;
    }

    /**
     * Remove a lore line by index.
     *
     * @param index the line index to remove
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeLoreLine(int index) {
        List<Component> lore = new ArrayList<>(itemStack.getData(DataComponentTypes.LORE).styledLines());
        lore.remove(index);

        itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
        return this;
    }

    /**
     * Get the full lore as a list of components.
     *
     * @return list of lore components or null if not present
     */
    @Nullable
    public List<Component> lore() {
        return itemStack.getData(DataComponentTypes.LORE).styledLines();
    }

    /**
     * Set the tooltip style Key for this item ({@link DataComponentTypes#TOOLTIP_STYLE}).
     *
     * @param key the tooltip style key
     * @return this builder
     */
    @NotNull
    public ItemBuilder tooltipStyle(Key key) {
        itemStack.setData(DataComponentTypes.TOOLTIP_STYLE, key);
        return this;
    }

    /**
     * Get the tooltip style key applied to this item ({@link DataComponentTypes#TOOLTIP_STYLE}).
     *
     * @return the Key or null if none set
     */
    @Nullable
    public Key tooltipStyle() {
        return itemStack.getData(DataComponentTypes.TOOLTIP_STYLE);
    }

    /**
     * Add an attribute modifier for the specified attribute while preserving
     * existing modifiers ({@link DataComponentTypes#ATTRIBUTE_MODIFIERS}).
     *
     * @param attribute the attribute to modify
     * @param modifier  the modifier to add
     * @return this builder
     */
    @NotNull
    public ItemBuilder addAttributeModifier(@NotNull Attribute attribute, @NotNull AttributeModifier modifier) {
        ItemAttributeModifiers.Builder itemAttributeModifiers = ItemAttributeModifiers.itemAttributes()
                .addModifier(attribute, modifier);

        ItemAttributeModifiers data = itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (data != null)
            data.modifiers().forEach(attModifier -> itemAttributeModifiers.addModifier(attModifier.attribute(), attModifier.modifier(), attModifier.getGroup()));

        itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers.build());
        return this;
    }

    /**
     * Add multiple attribute modifiers while preserving existing ones.
     *
     * @param attributeModifiers map of Attribute -> AttributeModifier
     * @return this builder
     */
    @NotNull
    public ItemBuilder addAttributeModifiers(@NotNull Map<Attribute, AttributeModifier> attributeModifiers) {
        ItemAttributeModifiers.Builder itemAttributeModifiers = ItemAttributeModifiers.itemAttributes();
        if (itemStack.hasData(DataComponentTypes.ATTRIBUTE_MODIFIERS))
            itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().forEach(attModifier -> itemAttributeModifiers.addModifier(attModifier.attribute(), attModifier.modifier(), attModifier.getGroup()));

        attributeModifiers.forEach(itemAttributeModifiers::addModifier);

        itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers.build());
        return this;
    }

    /**
     * Overwrite attribute modifiers with a single attribute entry ({@link DataComponentTypes#ATTRIBUTE_MODIFIERS}).
     *
     * @param attribute the attribute
     * @param modifier  the modifier
     * @return this builder
     */
    @NotNull
    public ItemBuilder attributeModifiers(@NotNull Attribute attribute, @NotNull AttributeModifier modifier) {
        ItemAttributeModifiers itemAttributeModifiers = ItemAttributeModifiers.itemAttributes()
                .addModifier(attribute, modifier)
                .build();

        itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers);
        return this;
    }

    /**
     * Overwrite attribute modifiers with the provided map.
     *
     * @param attributeModifiers map of Attribute -> AttributeModifier
     * @return this builder
     */
    @NotNull
    public ItemBuilder attributeModifiers(@NotNull Map<Attribute, AttributeModifier> attributeModifiers) {
        ItemAttributeModifiers.Builder itemAttributeModifiers = ItemAttributeModifiers.itemAttributes();
        attributeModifiers.forEach(itemAttributeModifiers::addModifier);

        itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers.build());
        return this;
    }

    /**
     * Remove all modifiers for a single attribute.
     *
     * @param attribute the attribute to remove modifiers for
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeAttributeModifier(@NotNull Attribute attribute) {
        ItemAttributeModifiers.Builder itemAttributeModifiers = ItemAttributeModifiers.itemAttributes();
        if (!itemStack.hasData(DataComponentTypes.ATTRIBUTE_MODIFIERS))
            return this;

        itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().stream()
                .filter(attModifier -> !attModifier.attribute().equals(attribute))
                .forEach(attModifier -> itemAttributeModifiers.addModifier(attModifier.attribute(), attModifier.modifier(), attModifier.getGroup()));

        itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers.build());
        return this;
    }

    /**
     * Remove modifiers for the provided attributes.
     *
     * @param attributes the attributes to keep removed
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeAttributeModifiers(@NotNull Attribute... attributes) {
        List<Attribute> list = Arrays.asList(attributes);
        ItemAttributeModifiers.Builder itemAttributeModifiers = ItemAttributeModifiers.itemAttributes();
        if (!itemStack.hasData(DataComponentTypes.ATTRIBUTE_MODIFIERS))
            return this;

        itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().stream()
                .filter(attModifier -> !list.contains(attModifier.attribute()))
                .forEach(attModifier -> itemAttributeModifiers.addModifier(attModifier.attribute(), attModifier.modifier(), attModifier.getGroup()));

        itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers.build());
        return this;
    }

    /**
     * Remove all attribute modifiers (set empty).
     *
     * @return this builder
     */
    @NotNull
    public ItemBuilder removeAttributeModifiers() {
        itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build());
        return this;
    }

    /**
     * Unset the attribute modifiers component entirely.
     *
     * @return this builder
     */
    @NotNull
    public ItemBuilder resetAttributesModifiers() {
        itemStack.unsetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        return this;
    }

    /**
     * Get a map of attribute modifiers currently set on the item.
     *
     * @return a map of Attribute -> AttributeModifier (empty if none)
     */
    @NotNull
    public Map<Attribute, AttributeModifier> attributeModifiers() {
        ItemAttributeModifiers data = itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        return data == null
                ? Collections.emptyMap()
                : data.modifiers().stream()
                .collect(Collectors.toMap(
                        ItemAttributeModifiers.Entry::attribute,
                        ItemAttributeModifiers.Entry::modifier
                ));
    }

    /**
     * Set a custom model data component instance ({@link DataComponentTypes#CUSTOM_MODEL_DATA}).
     *
     * @param customModelData the CustomModelData object
     * @return this builder
     */
    @NotNull
    public ItemBuilder customModelData(CustomModelData customModelData) {
        itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
        return this;
    }

    /**
     * Set a single float value for custom model data.
     *
     * @param customModelData float value to add
     * @return this builder
     */
    @NotNull
    public ItemBuilder customModelData(float customModelData) {
        itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(customModelData).build());
        return this;
    }

    /**
     * Remove the {@link DataComponentTypes#CUSTOM_MODEL_DATA} component.
     *
     * @return this builder
     */
    @NotNull
    public ItemBuilder resetCustomModelData() {
        itemStack.unsetData(DataComponentTypes.CUSTOM_MODEL_DATA);
        return this;
    }

    /**
     * Get the CustomModelData component if present.
     *
     * @return the CustomModelData or null
     */
    @Nullable
    public CustomModelData customModelData() {
        return itemStack.getData(DataComponentTypes.CUSTOM_MODEL_DATA);
    }

    /**
     * Set the maximum stack size for this item ({@link DataComponentTypes#MAX_STACK_SIZE}).
     *
     * @param maxStackSize value in range [1,99]
     * @return this builder
     */
    @NotNull
    public ItemBuilder maxStackSize(@IntRange(from = 1, to = 99) int maxStackSize) {
        itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, maxStackSize);
        return this;
    }

    /**
     * Get the {@link DataComponentTypes#MAX_STACK_SIZE} value if present.
     *
     * @return the integer max stack size or null
     */
    @NotNull
    public Integer maxStackSize() {
        return itemStack.getData(DataComponentTypes.MAX_STACK_SIZE);
    }

    /**
     * Add a banner pattern to this item, preserving existing patterns
     * ({@link DataComponentTypes#BANNER_PATTERNS}).
     *
     * @param pattern the pattern to add
     * @return this builder
     */
    @NotNull
    public ItemBuilder addBannerPattern(@NotNull Pattern pattern) {
        BannerPatternLayers data = itemStack.getData(DataComponentTypes.BANNER_PATTERNS);

        BannerPatternLayers.Builder builder = BannerPatternLayers.bannerPatternLayers();
        if (data != null)
            builder.addAll(data.patterns());

        builder.add(pattern);
        itemStack.setData(DataComponentTypes.BANNER_PATTERNS, builder.build());
        return this;
    }

    /**
     * Add multiple banner patterns while preserving existing ones.
     *
     * @param patterns patterns to add
     * @return this builder
     */
    @NotNull
    public ItemBuilder addBannerPatterns(@NotNull Pattern... patterns) {
        BannerPatternLayers data = itemStack.getData(DataComponentTypes.BANNER_PATTERNS);

        BannerPatternLayers.Builder builder = BannerPatternLayers.bannerPatternLayers();
        if (data != null)
            builder.addAll(data.patterns());

        builder.addAll(Arrays.asList(patterns));
        itemStack.setData(DataComponentTypes.BANNER_PATTERNS, builder.build());
        return this;
    }

    /**
     * Overwrite banner patterns with the provided patterns.
     *
     * @param patterns banner patterns
     * @return this builder
     */
    @NotNull
    public ItemBuilder bannerPatterns(@NotNull Pattern... patterns) {
        BannerPatternLayers bannerPatternLayers = BannerPatternLayers.bannerPatternLayers()
                .addAll(Arrays.asList(patterns))
                .build();

        itemStack.setData(DataComponentTypes.BANNER_PATTERNS, bannerPatternLayers);
        return this;
    }

    /**
     * Overwrite banner patterns with the provided list.
     *
     * @param patterns list of patterns
     * @return this builder
     */
    @NotNull
    public ItemBuilder bannerPatterns(@NotNull List<Pattern> patterns) {
        BannerPatternLayers bannerPatternLayers = BannerPatternLayers.bannerPatternLayers()
                .addAll(patterns)
                .build();

        itemStack.setData(DataComponentTypes.BANNER_PATTERNS, bannerPatternLayers);
        return this;
    }

    /**
     * Remove {@link DataComponentTypes#BANNER_PATTERNS} component.
     *
     * @return this builder
     */
    @NotNull
    public ItemBuilder resetBannerPatterns() {
        itemStack.unsetData(DataComponentTypes.BANNER_PATTERNS);
        return this;
    }

    /**
     * Get banner patterns applied to this item.
     *
     * @return list of patterns (empty if none)
     */
    @NotNull
    public List<Pattern> bannerPatterns() {
        BannerPatternLayers data = itemStack.getData(DataComponentTypes.BANNER_PATTERNS);
        return data == null ? Collections.emptyList() : data.patterns();
    }

    /**
     * Set the dyed color for dyed items using {@link DataComponentTypes#DYED_COLOR}.
     *
     * @param color the java.awt-like Color (Bukkit Color)
     * @return this builder
     */
    @NotNull
    public ItemBuilder dyeColor(@NotNull Color color) {
        itemStack.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(color));
        return this;
    }

    /**
     * Get the dyed color applied to the item ({@link DataComponentTypes#DYED_COLOR}).
     *
     * @return the Color or null if not dyed
     */
    @Nullable
    public Color dyeColor() {
        DyedItemColor data = itemStack.getData(DataComponentTypes.DYED_COLOR);
        return data == null ? null : data.color();
    }

    /**
     * Remove the {@link DataComponentTypes#DYED_COLOR} component from the item.
     *
     * @return this builder
     */
    @NotNull
    public ItemBuilder resetDyeColor() {
        itemStack.unsetData(DataComponentTypes.DYED_COLOR);
        return this;
    }

    /**
     * Get the material's corresponding DyeColor (for dyed variants). This
     * resolves a large switch of materials to DyeColor and returns WHITE by
     * default.
     *
     * @return the DyeColor for the current item type
     */
    @NotNull
    public DyeColor itemColor() {
        return dyeColor(itemStack.getType());
    }

    /**
     * Set an arbitrary valued data component on the item.
     *
     * @param type  the valued DataComponentType
     * @param value the value to set
     * @param <T>   type parameter for the component value
     * @return this builder
     */
    @NotNull
    public <T> ItemBuilder component(@NotNull DataComponentType.Valued<T> type, T value) {
        itemStack.setData(type, value);
        return this;
    }

    /**
     * Set an arbitrary non-valued data component on the item (presence flag).
     *
     * @param type the non-valued DataComponentType
     * @return this builder
     */
    @NotNull
    public ItemBuilder component(@NotNull DataComponentType.NonValued type) {
        itemStack.setData(type);
        return this;
    }

    /**
     * Get a valued data component from the item.
     *
     * @param type the valued DataComponentType
     * @param <T>  value type
     * @return the component's value or null if not present
     */
    @Nullable
    public <T> T component(@NotNull DataComponentType.Valued<T> type) {
        return itemStack.getData(type);
    }

    /**
     * Remove/unset a data component from the item.
     *
     * @param type the component type to unset
     * @return this builder
     */
    @NotNull
    public ItemBuilder resetComponent(@NotNull DataComponentType type) {
        itemStack.unsetData(type);
        return this;
    }

    /**
     * Store a value in the item's PersistentDataContainer using an Adventure Key
     * mapped to a Bukkit NamespacedKey.
     *
     * @param key   the Adventure key used to create a NamespacedKey
     * @param type  the PersistentDataType for the value
     * @param value the value to store
     * @param <P>   primitive type parameter
     * @param <C>   complex type parameter
     * @return this builder
     */
    @NotNull
    public <P, C> ItemBuilder persistentData(@NotNull Key key, @NotNull PersistentDataType<P, C> type, @NotNull C value) {
        itemStack.editPersistentDataContainer(pdc -> pdc.set(new NamespacedKey(key.namespace(), key.value()), type, value));
        return this;
    }

    /**
     * Deprecated helpers for Bukkit ItemFlags using ItemMeta. Prefer using
     * DataComponent-based tooltip/hide APIs where possible.
     *
     * @param itemFlags item flags to add
     * @return this builder
     * @deprecated since 2.1.0
     */
    @NotNull
    @Deprecated(since = "2.1.0", forRemoval = true)
    public ItemBuilder addItemFlags(@NotNull ItemFlag... itemFlags) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addItemFlags(itemFlags);

        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Replace the item flags with the provided set.
     *
     * @param itemFlags item flags to set
     * @return this builder
     * @deprecated since 2.1.0
     */
    @NotNull
    @Deprecated(since = "2.1.0", forRemoval = true)
    public ItemBuilder itemFlags(@NotNull ItemFlag... itemFlags) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.removeItemFlags(itemMeta.getItemFlags().toArray(new ItemFlag[0]));
        itemMeta.addItemFlags(itemFlags);

        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Remove specified item flags.
     *
     * @param itemFlags item flags to remove
     * @return this builder
     * @deprecated since 2.1.0
     */
    @NotNull
    @Deprecated(since = "2.1.0", forRemoval = true)
    public ItemBuilder removeItemFlags(@NotNull ItemFlag... itemFlags) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.removeItemFlags(itemFlags);

        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Remove all item flags.
     *
     * @return this builder
     * @deprecated since 2.1.0
     */
    @NotNull
    @Deprecated(since = "2.1.0", forRemoval = true)
    public ItemBuilder removeItemFlags() {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.removeItemFlags(itemMeta.getItemFlags().toArray(new ItemFlag[0]));

        itemStack.setItemMeta(itemMeta);
        return this;
    }

    /**
     * Get the ItemFlags currently set on the item's ItemMeta.
     *
     * @return set of ItemFlag
     * @deprecated since 2.1.0
     */
    @NotNull
    @Deprecated(since = "2.1.0", forRemoval = true)
    public Set<ItemFlag> itemFlags() {
        ItemMeta itemMeta = itemStack.getItemMeta();
        return itemMeta.getItemFlags();
    }

    /**
     * Hide specific data component types from the tooltip using {@link DataComponentTypes#TOOLTIP_DISPLAY}.
     * Will not overwrite hideTooltip if it was already set to hide everything.
     *
     * @param typesToHide data component types to hide from tooltip
     * @return this builder
     */
    @NotNull
    public ItemBuilder hide(@NotNull DataComponentType... typesToHide) {
        if (itemStack.hasData(DataComponentTypes.TOOLTIP_DISPLAY) && itemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY).hideTooltip())
            return this;

        TooltipDisplay tooltipDisplay = TooltipDisplay.tooltipDisplay()
                .addHiddenComponents(typesToHide)
                .build();

        itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltipDisplay);
        return this;
    }

    /**
     * Get the set of hidden data component types for the tooltip ({@link DataComponentTypes#TOOLTIP_DISPLAY}).
     *
     * @return set of hidden DataComponentType (empty if none)
     */
    @NotNull
    public Set<DataComponentType> hiddenComponents() {
        if (!itemStack.hasData(DataComponentTypes.TOOLTIP_DISPLAY))
            return Collections.emptySet();

        return itemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY).hiddenComponents();
    }

    /**
     * Set whether the tooltip should be hidden entirely (uses {@link DataComponentTypes#TOOLTIP_DISPLAY}).
     *
     * @param hideTooltip true to hide tooltip, false to show
     * @return this builder
     */
    @NotNull
    public ItemBuilder hideTooltip(boolean hideTooltip) {
        TooltipDisplay tooltipDisplay = TooltipDisplay.tooltipDisplay()
                .hideTooltip(hideTooltip)
                .build();

        itemStack.setData(DataComponentTypes.TOOLTIP_DISPLAY, tooltipDisplay);
        return this;
    }

    /**
     * Check whether the tooltip is hidden entirely.
     *
     * @return true if tooltip is hidden
     */
    public boolean hideTooltip() {
        if (!itemStack.hasData(DataComponentTypes.TOOLTIP_DISPLAY))
            return false;

        return itemStack.getData(DataComponentTypes.TOOLTIP_DISPLAY).hideTooltip();
    }

    /*
     * Internal helper: map a Material to its DyeColor. This covers many
     * material constants and returns WHITE by default.
     */
    private DyeColor dyeColor(Material material) {
        return switch (material) {
            case ORANGE_BANNER, ORANGE_BED, ORANGE_BUNDLE, ORANGE_CANDLE, ORANGE_CANDLE_CAKE, ORANGE_CARPET,
                 ORANGE_CONCRETE, ORANGE_CONCRETE_POWDER, ORANGE_DYE, ORANGE_WOOL, ORANGE_GLAZED_TERRACOTTA,
                 ORANGE_TERRACOTTA, ORANGE_SHULKER_BOX, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS_PANE,
                 ORANGE_WALL_BANNER, ORANGE_HARNESS, ORANGE_TULIP, TORCHFLOWER, OPEN_EYEBLOSSOM ->
                    DyeColor.ORANGE;

            case MAGENTA_BANNER, MAGENTA_BED, MAGENTA_BUNDLE, MAGENTA_CANDLE, MAGENTA_CANDLE_CAKE, MAGENTA_CARPET,
                 MAGENTA_CONCRETE, MAGENTA_CONCRETE_POWDER, MAGENTA_DYE, MAGENTA_WOOL, MAGENTA_GLAZED_TERRACOTTA,
                 MAGENTA_TERRACOTTA, MAGENTA_SHULKER_BOX, MAGENTA_STAINED_GLASS, MAGENTA_STAINED_GLASS_PANE,
                 MAGENTA_WALL_BANNER, MAGENTA_HARNESS, ALLIUM, LILAC ->
                    DyeColor.MAGENTA;

            case LIGHT_BLUE_BANNER, LIGHT_BLUE_BED, LIGHT_BLUE_BUNDLE, LIGHT_BLUE_CANDLE, LIGHT_BLUE_CANDLE_CAKE,
                 LIGHT_BLUE_CARPET, LIGHT_BLUE_CONCRETE, LIGHT_BLUE_CONCRETE_POWDER, LIGHT_BLUE_DYE, LIGHT_BLUE_WOOL,
                 LIGHT_BLUE_GLAZED_TERRACOTTA, LIGHT_BLUE_TERRACOTTA, LIGHT_BLUE_SHULKER_BOX, LIGHT_BLUE_STAINED_GLASS,
                 LIGHT_BLUE_STAINED_GLASS_PANE, LIGHT_BLUE_WALL_BANNER, LIGHT_BLUE_HARNESS, BLUE_ORCHID ->
                    DyeColor.LIGHT_BLUE;

            case YELLOW_BANNER, YELLOW_BED, YELLOW_BUNDLE, YELLOW_CANDLE, YELLOW_CANDLE_CAKE, YELLOW_CARPET,
                 YELLOW_CONCRETE, YELLOW_CONCRETE_POWDER, YELLOW_DYE, YELLOW_WOOL, YELLOW_GLAZED_TERRACOTTA,
                 YELLOW_TERRACOTTA, YELLOW_SHULKER_BOX, YELLOW_STAINED_GLASS, YELLOW_STAINED_GLASS_PANE,
                 YELLOW_WALL_BANNER, YELLOW_HARNESS, DANDELION, SUNFLOWER, WILDFLOWERS ->
                    DyeColor.YELLOW;

            case LIME_BANNER, LIME_BED, LIME_BUNDLE, LIME_CANDLE, LIME_CANDLE_CAKE, LIME_CARPET, LIME_CONCRETE,
                 LIME_CONCRETE_POWDER, LIME_DYE, LIME_WOOL, LIME_GLAZED_TERRACOTTA, LIME_TERRACOTTA, LIME_SHULKER_BOX,
                 LIME_STAINED_GLASS, LIME_STAINED_GLASS_PANE, LIME_WALL_BANNER, LIME_HARNESS, SEA_PICKLE ->
                    DyeColor.LIME;

            case PINK_BANNER, PINK_BED, PINK_BUNDLE, PINK_CANDLE, PINK_CANDLE_CAKE, PINK_CARPET, PINK_CONCRETE,
                 PINK_CONCRETE_POWDER, PINK_DYE, PINK_WOOL, PINK_GLAZED_TERRACOTTA, PINK_TERRACOTTA, PINK_SHULKER_BOX,
                 PINK_STAINED_GLASS, PINK_STAINED_GLASS_PANE, PINK_WALL_BANNER, PINK_HARNESS, PINK_TULIP, PEONY,
                 PINK_PETALS ->
                    DyeColor.PINK;

            case GRAY_BANNER, GRAY_BED, GRAY_BUNDLE, GRAY_CANDLE, GRAY_CANDLE_CAKE, GRAY_CARPET, GRAY_CONCRETE,
                 GRAY_CONCRETE_POWDER, GRAY_DYE, GRAY_WOOL, GRAY_GLAZED_TERRACOTTA, GRAY_TERRACOTTA, GRAY_SHULKER_BOX,
                 GRAY_STAINED_GLASS, GRAY_STAINED_GLASS_PANE, GRAY_WALL_BANNER, GRAY_HARNESS, CLOSED_EYEBLOSSOM ->
                    DyeColor.GRAY;

            case LIGHT_GRAY_BANNER, LIGHT_GRAY_BED, LIGHT_GRAY_BUNDLE, LIGHT_GRAY_CANDLE, LIGHT_GRAY_CANDLE_CAKE,
                 LIGHT_GRAY_CARPET, LIGHT_GRAY_CONCRETE, LIGHT_GRAY_CONCRETE_POWDER, LIGHT_GRAY_DYE, LIGHT_GRAY_WOOL,
                 LIGHT_GRAY_GLAZED_TERRACOTTA, LIGHT_GRAY_TERRACOTTA, LIGHT_GRAY_SHULKER_BOX, LIGHT_GRAY_STAINED_GLASS,
                 LIGHT_GRAY_STAINED_GLASS_PANE, LIGHT_GRAY_WALL_BANNER, LIGHT_GRAY_HARNESS, AZURE_BLUET, OXEYE_DAISY,
                 WHITE_TULIP ->
                    DyeColor.LIGHT_GRAY;

            case CYAN_BANNER, CYAN_BED, CYAN_BUNDLE, CYAN_CANDLE, CYAN_CANDLE_CAKE, CYAN_CARPET, CYAN_CONCRETE,
                 CYAN_CONCRETE_POWDER, CYAN_DYE, CYAN_WOOL, CYAN_GLAZED_TERRACOTTA, CYAN_TERRACOTTA, CYAN_SHULKER_BOX,
                 CYAN_STAINED_GLASS, CYAN_STAINED_GLASS_PANE, CYAN_WALL_BANNER, CYAN_HARNESS, PITCHER_PLANT ->
                    DyeColor.CYAN;

            case PURPLE_BANNER, PURPLE_BED, PURPLE_BUNDLE, PURPLE_CANDLE, PURPLE_CANDLE_CAKE, PURPLE_CARPET,
                 PURPLE_CONCRETE, PURPLE_CONCRETE_POWDER, PURPLE_DYE, PURPLE_WOOL, PURPLE_GLAZED_TERRACOTTA,
                 PURPLE_TERRACOTTA, PURPLE_SHULKER_BOX, PURPLE_STAINED_GLASS, PURPLE_STAINED_GLASS_PANE,
                 PURPLE_WALL_BANNER, PURPLE_HARNESS ->
                    DyeColor.PURPLE;

            case BLUE_BANNER, BLUE_BED, BLUE_BUNDLE, BLUE_CANDLE, BLUE_CANDLE_CAKE, BLUE_CARPET, BLUE_CONCRETE,
                 BLUE_CONCRETE_POWDER, BLUE_DYE, BLUE_WOOL, BLUE_GLAZED_TERRACOTTA, BLUE_TERRACOTTA, BLUE_SHULKER_BOX,
                 BLUE_STAINED_GLASS, BLUE_STAINED_GLASS_PANE, BLUE_WALL_BANNER, BLUE_HARNESS, CORNFLOWER ->
                    DyeColor.BLUE;

            case BROWN_BANNER, BROWN_BED, BROWN_BUNDLE, BROWN_CANDLE, BROWN_CANDLE_CAKE, BROWN_CARPET, BROWN_CONCRETE,
                 BROWN_CONCRETE_POWDER, BROWN_DYE, BROWN_WOOL, BROWN_GLAZED_TERRACOTTA, BROWN_TERRACOTTA,
                 BROWN_SHULKER_BOX, BROWN_STAINED_GLASS, BROWN_STAINED_GLASS_PANE, BROWN_WALL_BANNER, BROWN_HARNESS,
                 COCOA_BEANS ->
                    DyeColor.BROWN;

            case GREEN_BANNER, GREEN_BED, GREEN_BUNDLE, GREEN_CANDLE, GREEN_CANDLE_CAKE, GREEN_CARPET, GREEN_CONCRETE,
                 GREEN_CONCRETE_POWDER, GREEN_DYE, GREEN_WOOL, GREEN_GLAZED_TERRACOTTA, GREEN_TERRACOTTA,
                 GREEN_SHULKER_BOX, GREEN_STAINED_GLASS, GREEN_STAINED_GLASS_PANE, GREEN_WALL_BANNER, GREEN_HARNESS,
                 CACTUS ->
                    DyeColor.GREEN;

            case RED_BANNER, RED_BED, RED_BUNDLE, RED_CANDLE, RED_CANDLE_CAKE, RED_CARPET, RED_CONCRETE,
                 RED_CONCRETE_POWDER, RED_DYE, RED_WOOL, RED_GLAZED_TERRACOTTA, RED_TERRACOTTA, RED_SHULKER_BOX,
                 RED_STAINED_GLASS, RED_STAINED_GLASS_PANE, RED_WALL_BANNER, RED_HARNESS, POPPY, RED_TULIP, ROSE_BUSH,
                 BEETROOT ->
                    DyeColor.RED;

            case BLACK_BANNER, BLACK_BED, BLACK_BUNDLE, BLACK_CANDLE, BLACK_CANDLE_CAKE, BLACK_CARPET, BLACK_CONCRETE,
                 BLACK_CONCRETE_POWDER, BLACK_DYE, BLACK_WOOL, BLACK_GLAZED_TERRACOTTA, BLACK_TERRACOTTA,
                 BLACK_SHULKER_BOX, BLACK_STAINED_GLASS, BLACK_STAINED_GLASS_PANE, BLACK_WALL_BANNER, BLACK_HARNESS,
                 INK_SAC, WITHER_ROSE ->
                    DyeColor.BLACK;

            default -> DyeColor.WHITE;
        };
    }
}