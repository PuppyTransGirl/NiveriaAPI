package toutouchien.niveriaapi.utils.data;

import com.destroystokyo.paper.profile.PlayerProfile;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.profile.PlayerTextures;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.common.value.qual.IntRange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ItemBuilder {
	private static final UUID uuid = UUID.fromString("14030105-4f95-4a6d-9572-7cc1d6314ab2");
	private final ItemStack itemStack;

	private ItemBuilder(ItemStack itemStack) {
		this.itemStack = itemStack;
	}

	@NotNull
	public static ItemBuilder of(@NotNull Material material) {
		if (material.isAir())
			throw new IllegalArgumentException("Material cannot be air.");

		return new ItemBuilder(new ItemStack(material));
	}

	@NotNull
	public static ItemBuilder of(@NotNull ItemStack itemStack) {
		if (itemStack.getType().isAir())
			throw new IllegalArgumentException("Material cannot be air.");

		return new ItemBuilder(itemStack);
	}

	@NotNull
	public static ItemBuilder of(@NotNull Material material, int amount) {
		if (material.isAir())
			throw new IllegalArgumentException("Material cannot be air.");

		return new ItemBuilder(new ItemStack(material, amount));
	}

	@NotNull
	public ItemBuilder name(Component name) {
		itemStack.setData(DataComponentTypes.ITEM_NAME, name);
		return this;
	}

	@Nullable
	public Component name() {
		return itemStack.getData(DataComponentTypes.ITEM_NAME);
	}

	@NotNull
	public ItemBuilder skullName(Component name) {
		itemStack.setData(DataComponentTypes.CUSTOM_NAME, name);
		return this;
	}

	@Nullable
	public Component skullName() {
		return itemStack.getData(DataComponentTypes.CUSTOM_NAME);
	}

	public ItemBuilder durability(int durability) {
		itemStack.setData(DataComponentTypes.DAMAGE, itemStack.getData(DataComponentTypes.MAX_DAMAGE) - durability);
		return this;
	}

	public int durability() {
		return itemStack.getData(DataComponentTypes.MAX_DAMAGE) - itemStack.getData(DataComponentTypes.DAMAGE);
	}

	@NotNull
	public ItemBuilder damage(int damage) {
		itemStack.setData(DataComponentTypes.DAMAGE, damage);
		return this;
	}

	@Nullable
	@NonNegative
	public Integer damage() {
		return itemStack.getData(DataComponentTypes.DAMAGE);
	}

	@NotNull
	public ItemBuilder addEnchantment(@NotNull Enchantment enchantment, int level, boolean showInTooltip) {
		ItemEnchantments data = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
		ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments()
				.addAll(data == null ? Collections.emptyMap() : data.enchantments())
				.add(enchantment, level)
				.showInTooltip(showInTooltip)
				.build();

		itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
		return this;
	}

	@NotNull
	public ItemBuilder enchantment(@NotNull Enchantment enchantment, int level, boolean showInTooltip) {
		ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments()
				.add(enchantment, level)
				.showInTooltip(showInTooltip)
				.build();

		itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
		return this;
	}

	@NotNull
	public ItemBuilder addEnchantments(@NotNull Map<Enchantment, Integer> enchantments, boolean showInTooltip) {
		ItemEnchantments data = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
		ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments()
				.addAll(data == null ? Collections.emptyMap() : data.enchantments())
				.addAll(enchantments)
				.showInTooltip(showInTooltip)
				.build();

		itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
		return this;
	}

	@NotNull
	public ItemBuilder enchantments(@NotNull Map<Enchantment, Integer> enchantments, boolean showInTooltip) {
		ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments()
				.addAll(enchantments)
				.showInTooltip(showInTooltip)
				.build();

		itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
		return this;
	}

	@NotNull
	public ItemBuilder removeEnchantment(@NotNull Enchantment enchantment) {
		ItemEnchantments data = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
		if (data == null)
			return this;

		Map<Enchantment, @IntRange(from = 1L, to = 255L) Integer> enchantments = data.enchantments();
		enchantments.remove(enchantment);

		ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments()
				.addAll(enchantments)
				.build();

		itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
		return this;
	}

	@NotNull
	public ItemBuilder removeEnchantments(@NotNull Enchantment... enchantments) {
		ItemEnchantments data = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
		if (data == null)
			return this;

		Map<Enchantment, @IntRange(from = 1L, to = 255L) Integer> enchants = data.enchantments();
		Arrays.stream(enchantments).forEach(enchants::remove);

		ItemEnchantments itemEnchantments = ItemEnchantments.itemEnchantments()
				.addAll(enchants)
				.build();

		itemStack.setData(DataComponentTypes.ENCHANTMENTS, itemEnchantments);
		return this;
	}

	@NotNull
	public ItemBuilder removeEnchantments() {
		itemStack.setData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments().build());
		return this;
	}

	@Nullable
	public ItemEnchantments enchantments() {
		return itemStack.getData(DataComponentTypes.ENCHANTMENTS);
	}

	@Nullable
	public Map<Enchantment, Integer> enchantmentsMap() {
		ItemEnchantments data = itemStack.getData(DataComponentTypes.ENCHANTMENTS);
		return data == null ? null : data.enchantments();
	}

	@NotNull
	public ItemBuilder forceGlowing(boolean glowing) {
		itemStack.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, glowing);
		return this;
	}

	@NotNull
	public ItemBuilder resetGlowing() {
		itemStack.unsetData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
		return this;
	}

	public boolean forcedGlowing() {
		return itemStack.hasData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE);
	}

	@NotNull
	public ItemBuilder skullOwner(String owner) {
		ResolvableProfile ownerProfile = ResolvableProfile.resolvableProfile().name(owner).build();
		itemStack.setData(DataComponentTypes.PROFILE, ownerProfile);
		return this;
	}

	@NotNull
	public ItemBuilder skullOwner(OfflinePlayer player) {
		ResolvableProfile ownerProfile = ResolvableProfile.resolvableProfile(player.getPlayerProfile());
		itemStack.setData(DataComponentTypes.PROFILE, ownerProfile);
		return this;
	}

	@NotNull
	public ItemBuilder skullOwner(URL url) {
		PlayerProfile profile = Bukkit.createProfile(uuid);
		PlayerTextures textures = profile.getTextures();
		textures.setSkin(url);
		profile.setTextures(textures);

		ResolvableProfile ownerProfile = ResolvableProfile.resolvableProfile(profile);
		itemStack.setData(DataComponentTypes.PROFILE, ownerProfile);
		return this;
	}

	@Nullable
	public String skullOwner() {
		ResolvableProfile ownerProfile = itemStack.getData(DataComponentTypes.PROFILE);
		return ownerProfile == null ? null : ownerProfile.name();
	}

	@NotNull
	public ItemBuilder unbreakable(boolean unbreakable, boolean showInTooltip) {
		if (unbreakable)
			itemStack.setData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable().showInTooltip(showInTooltip).build());
		else
			itemStack.unsetData(DataComponentTypes.UNBREAKABLE);

		return this;
	}

	public boolean unbreakable() {
		return itemStack.hasData(DataComponentTypes.UNBREAKABLE);
	}

	@NotNull
	public ItemBuilder lore(@NotNull Component... lore) {
		List<Component> lores = new ArrayList<>();
		Arrays.stream(lore).forEach(l -> lores.add(l.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));

		itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lores));
		return this;
	}

	@NotNull
	public ItemBuilder lore(@NotNull List<Component> lore) {
		List<Component> lores = new ArrayList<>();
		lore.forEach(l -> lores.add(l.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));

		itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lores));
		return this;
	}

	@NotNull
	public ItemBuilder removeLore() {
		itemStack.unsetData(DataComponentTypes.LORE);
		return this;
	}

	@Nullable
	public Component loreLine(int index) {
		ItemLore data = itemStack.getData(DataComponentTypes.LORE);
		if (data == null)
			return null;

		List<Component> components = data.styledLines();
		return components.size() > index ? components.get(index) : null;
	}

	@NotNull
	public ItemBuilder addLoreLine(@NotNull Component line) {
		ItemLore data = itemStack.getData(DataComponentTypes.LORE);
		if (data == null)
			return this;

		ItemLore itemLore = ItemLore.lore()
				.lines(data.styledLines())
				.addLine(line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE))
				.build();

		itemStack.setData(DataComponentTypes.LORE, itemLore);
		return this;
	}

	@NotNull
	public ItemBuilder setLoreLine(@NotNull Component line, int index) {
		ItemLore data = itemStack.getData(DataComponentTypes.LORE);
		if (data == null) {
			itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(List.of(line)));
			return this;
		}

		List<Component> lore = new ArrayList<>(data.styledLines());
		lore.set(index, line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

		itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
		return this;
	}

	@NotNull
	public ItemBuilder removeLoreLine(@NotNull Component line) {
		if (!itemStack.hasData(DataComponentTypes.LORE))
			throw new IllegalArgumentException(String.format("You can't remove a lore line from an item that doesn't contain any. Provided: %s", itemStack.getType().name()));

		List<Component> lore = new ArrayList<>(itemStack.getData(DataComponentTypes.LORE).styledLines());
		lore.remove(line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

		itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
		return this;
	}

	@NotNull
	public ItemBuilder removeLoreLine(int index) {
		if (!itemStack.hasData(DataComponentTypes.LORE))
			throw new IllegalArgumentException(String.format("You can't remove a lore line from an item that doesn't contain any. Provided: %s", itemStack.getType().name()));

		List<Component> lore = new ArrayList<>(itemStack.getData(DataComponentTypes.LORE).styledLines());
		lore.remove(index);

		itemStack.setData(DataComponentTypes.LORE, ItemLore.lore(lore));
		return this;
	}

	@Nullable
	public List<Component> lore() {
		ItemLore data = itemStack.getData(DataComponentTypes.LORE);
		return data == null ? null : data.styledLines();
	}

	@NotNull
	public ItemBuilder addAttributeModifier(@NotNull Attribute attribute, @NotNull AttributeModifier modifier) {
		ItemAttributeModifiers.Builder itemAttributeModifiers = ItemAttributeModifiers.itemAttributes()
				.addModifier(attribute, modifier);

		ItemAttributeModifiers data = itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
		if (data != null)
			data.modifiers().forEach(attModifier -> itemAttributeModifiers.addModifier(attModifier.attribute(), attModifier.modifier(), attModifier.getGroup()));

		itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers);
		return this;
	}

	@NotNull
	public ItemBuilder addAttributeModifiers(@NotNull Map<Attribute, AttributeModifier> attributeModifiers) {
		ItemAttributeModifiers.Builder itemAttributeModifiers = ItemAttributeModifiers.itemAttributes();
		if (itemStack.hasData(DataComponentTypes.ATTRIBUTE_MODIFIERS))
			itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS).modifiers().forEach(attModifier -> itemAttributeModifiers.addModifier(attModifier.attribute(), attModifier.modifier(), attModifier.getGroup()));

		attributeModifiers.forEach(itemAttributeModifiers::addModifier);

		itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers.build());
		return this;
	}

	@NotNull
	public ItemBuilder attributeModifiers(@NotNull Attribute attribute, @NotNull AttributeModifier modifier) {
		ItemAttributeModifiers itemAttributeModifiers = ItemAttributeModifiers.itemAttributes()
				.addModifier(attribute, modifier)
				.build();

		itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers);
		return this;
	}

	@NotNull
	public ItemBuilder attributeModifiers(@NotNull Map<Attribute, AttributeModifier> attributeModifiers) {
		ItemAttributeModifiers.Builder itemAttributeModifiers = ItemAttributeModifiers.itemAttributes();
		attributeModifiers.forEach(itemAttributeModifiers::addModifier);

		itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, itemAttributeModifiers.build());
		return this;
	}

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

	@NotNull
	public ItemBuilder removeAttributeModifiers() {
		itemStack.setData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build());
		return this;
	}

	@NotNull
	public ItemBuilder resetAttributesModifiers() {
		itemStack.unsetData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
		return this;
	}

	@Nullable
	public Map<Attribute, AttributeModifier> attributeModifiers() {
		ItemAttributeModifiers data = itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS);
		return data == null ? null : data.modifiers().stream()
				.collect(Collectors.toMap(ItemAttributeModifiers.Entry::attribute, ItemAttributeModifiers.Entry::modifier));
	}

	@NotNull
	public ItemBuilder customModelData(CustomModelData customModelData) {
		itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, customModelData);
		return this;
	}

	@NotNull
	public ItemBuilder customModelData(int customModelData) {
		itemStack.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().addFloat(customModelData).build());
		return this;
	}

	@NotNull
	public ItemBuilder resetCustomModelData() {
		itemStack.unsetData(DataComponentTypes.CUSTOM_MODEL_DATA);
		return this;
	}

	@Nullable
	public CustomModelData customModelData() {
		return itemStack.getData(DataComponentTypes.CUSTOM_MODEL_DATA);
	}

	@NotNull
	public ItemBuilder maxStackSize(@IntRange(from = 1, to = 99) int maxStackSize) {
		itemStack.setData(DataComponentTypes.MAX_STACK_SIZE, maxStackSize);
		return this;
	}

	@NotNull
	public Integer maxStackSize() {
		return itemStack.getData(DataComponentTypes.MAX_STACK_SIZE);
	}

	@NotNull
	public ItemBuilder dyeColor(@Nullable Color color, boolean showInTooltip) {
		itemStack.setData(DataComponentTypes.DYED_COLOR, DyedItemColor.dyedItemColor(color, showInTooltip));
		return this;
	}

	@Nullable
	public Color dyeColor() {
		DyedItemColor data = itemStack.getData(DataComponentTypes.DYED_COLOR);
		return data == null ? null : data.color();
	}

	@NotNull
	public DyeColor itemColor() {
		return dyeColor(itemStack.getType());
	}

	@NotNull
	public <P, C> ItemBuilder persistentData(@NotNull NamespacedKey key, @NotNull PersistentDataType<P, C> type, @NotNull C value) {
		itemStack.editPersistentDataContainer(pdc -> pdc.set(key, type, value));
		return this;
	}

	@NotNull
	public ItemBuilder addItemFlags(@NotNull ItemFlag... itemFlags) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.addItemFlags(itemFlags);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public ItemBuilder itemFlags(@NotNull ItemFlag... itemFlags) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.removeItemFlags(itemMeta.getItemFlags().toArray(new ItemFlag[0]));
		itemMeta.addItemFlags(itemFlags);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public ItemBuilder removeItemFlags(@NotNull ItemFlag... itemFlags) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.removeItemFlags(itemFlags);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public ItemBuilder removeItemFlags() {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.removeItemFlags(itemMeta.getItemFlags().toArray(new ItemFlag[0]));

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public Set<ItemFlag> itemFlags() {
		ItemMeta itemMeta = itemStack.getItemMeta();
		return itemMeta.getItemFlags();
	}

	@NotNull
	public ItemStack build() {
		return itemStack;
	}

	private DyeColor dyeColor(Material material) {
		return switch (material) {
			case ORANGE_BANNER, ORANGE_BED, ORANGE_BUNDLE, ORANGE_CANDLE, ORANGE_CANDLE_CAKE, ORANGE_CARPET,
				 ORANGE_CONCRETE, ORANGE_CONCRETE_POWDER, ORANGE_DYE, ORANGE_WOOL, ORANGE_GLAZED_TERRACOTTA,
				 ORANGE_TERRACOTTA, ORANGE_SHULKER_BOX, ORANGE_STAINED_GLASS, ORANGE_STAINED_GLASS_PANE,
				 ORANGE_WALL_BANNER, ORANGE_TULIP, TORCHFLOWER, OPEN_EYEBLOSSOM -> DyeColor.ORANGE;
			case MAGENTA_BANNER, MAGENTA_BED, MAGENTA_BUNDLE, MAGENTA_CANDLE, MAGENTA_CANDLE_CAKE, MAGENTA_CARPET,
				 MAGENTA_CONCRETE, MAGENTA_CONCRETE_POWDER, MAGENTA_DYE, MAGENTA_WOOL, MAGENTA_GLAZED_TERRACOTTA,
				 MAGENTA_TERRACOTTA, MAGENTA_SHULKER_BOX, MAGENTA_STAINED_GLASS, MAGENTA_STAINED_GLASS_PANE,
				 MAGENTA_WALL_BANNER, ALLIUM, LILAC -> DyeColor.MAGENTA;
			case LIGHT_BLUE_BANNER, LIGHT_BLUE_BED, LIGHT_BLUE_BUNDLE, LIGHT_BLUE_CANDLE, LIGHT_BLUE_CANDLE_CAKE,
				 LIGHT_BLUE_CARPET, LIGHT_BLUE_CONCRETE, LIGHT_BLUE_CONCRETE_POWDER, LIGHT_BLUE_DYE, LIGHT_BLUE_WOOL,
				 LIGHT_BLUE_GLAZED_TERRACOTTA, LIGHT_BLUE_TERRACOTTA, LIGHT_BLUE_SHULKER_BOX, LIGHT_BLUE_STAINED_GLASS,
				 LIGHT_BLUE_STAINED_GLASS_PANE, LIGHT_BLUE_WALL_BANNER, BLUE_ORCHID -> DyeColor.LIGHT_BLUE;
			case YELLOW_BANNER, YELLOW_BED, YELLOW_BUNDLE, YELLOW_CANDLE, YELLOW_CANDLE_CAKE, YELLOW_CARPET,
				 YELLOW_CONCRETE, YELLOW_CONCRETE_POWDER, YELLOW_DYE, YELLOW_WOOL, YELLOW_GLAZED_TERRACOTTA,
				 YELLOW_TERRACOTTA, YELLOW_SHULKER_BOX, YELLOW_STAINED_GLASS, YELLOW_STAINED_GLASS_PANE,
				 YELLOW_WALL_BANNER, DANDELION, SUNFLOWER/*, WILD_FLOWERS (1.21.5) */ -> DyeColor.YELLOW;
			case LIME_BANNER, LIME_BED, LIME_BUNDLE, LIME_CANDLE, LIME_CANDLE_CAKE, LIME_CARPET, LIME_CONCRETE,
				 LIME_CONCRETE_POWDER, LIME_DYE, LIME_WOOL, LIME_GLAZED_TERRACOTTA, LIME_TERRACOTTA, LIME_SHULKER_BOX,
				 LIME_STAINED_GLASS, LIME_STAINED_GLASS_PANE, LIME_WALL_BANNER, SEA_PICKLE -> DyeColor.LIME;
			case PINK_BANNER, PINK_BED, PINK_BUNDLE, PINK_CANDLE, PINK_CANDLE_CAKE, PINK_CARPET, PINK_CONCRETE,
				 PINK_CONCRETE_POWDER, PINK_DYE, PINK_WOOL, PINK_GLAZED_TERRACOTTA, PINK_TERRACOTTA, PINK_SHULKER_BOX,
				 PINK_STAINED_GLASS, PINK_STAINED_GLASS_PANE, PINK_WALL_BANNER, PINK_TULIP, PEONY, PINK_PETALS ->
					DyeColor.PINK;
			case GRAY_BANNER, GRAY_BED, GRAY_BUNDLE, GRAY_CANDLE, GRAY_CANDLE_CAKE, GRAY_CARPET, GRAY_CONCRETE,
				 GRAY_CONCRETE_POWDER, GRAY_DYE, GRAY_WOOL, GRAY_GLAZED_TERRACOTTA, GRAY_TERRACOTTA, GRAY_SHULKER_BOX,
				 GRAY_STAINED_GLASS, GRAY_STAINED_GLASS_PANE, GRAY_WALL_BANNER, CLOSED_EYEBLOSSOM -> DyeColor.GRAY;
			case LIGHT_GRAY_BANNER, LIGHT_GRAY_BED, LIGHT_GRAY_BUNDLE, LIGHT_GRAY_CANDLE, LIGHT_GRAY_CANDLE_CAKE,
				 LIGHT_GRAY_CARPET, LIGHT_GRAY_CONCRETE, LIGHT_GRAY_CONCRETE_POWDER, LIGHT_GRAY_DYE, LIGHT_GRAY_WOOL,
				 LIGHT_GRAY_GLAZED_TERRACOTTA, LIGHT_GRAY_TERRACOTTA, LIGHT_GRAY_SHULKER_BOX, LIGHT_GRAY_STAINED_GLASS,
				 LIGHT_GRAY_STAINED_GLASS_PANE, LIGHT_GRAY_WALL_BANNER, AZURE_BLUET, OXEYE_DAISY, WHITE_TULIP ->
					DyeColor.LIGHT_GRAY;
			case CYAN_BANNER, CYAN_BED, CYAN_BUNDLE, CYAN_CANDLE, CYAN_CANDLE_CAKE, CYAN_CARPET, CYAN_CONCRETE,
				 CYAN_CONCRETE_POWDER, CYAN_DYE, CYAN_WOOL, CYAN_GLAZED_TERRACOTTA, CYAN_TERRACOTTA, CYAN_SHULKER_BOX,
				 CYAN_STAINED_GLASS, CYAN_STAINED_GLASS_PANE, CYAN_WALL_BANNER, PITCHER_PLANT -> DyeColor.CYAN;
			case PURPLE_BANNER, PURPLE_BED, PURPLE_BUNDLE, PURPLE_CANDLE, PURPLE_CANDLE_CAKE, PURPLE_CARPET,
				 PURPLE_CONCRETE, PURPLE_CONCRETE_POWDER, PURPLE_DYE, PURPLE_WOOL, PURPLE_GLAZED_TERRACOTTA,
				 PURPLE_TERRACOTTA, PURPLE_SHULKER_BOX, PURPLE_STAINED_GLASS, PURPLE_STAINED_GLASS_PANE,
				 PURPLE_WALL_BANNER -> DyeColor.PURPLE;
			case BLUE_BANNER, BLUE_BED, BLUE_BUNDLE, BLUE_CANDLE, BLUE_CANDLE_CAKE, BLUE_CARPET, BLUE_CONCRETE,
				 BLUE_CONCRETE_POWDER, BLUE_DYE, BLUE_WOOL, BLUE_GLAZED_TERRACOTTA, BLUE_TERRACOTTA, BLUE_SHULKER_BOX,
				 BLUE_STAINED_GLASS, BLUE_STAINED_GLASS_PANE, BLUE_WALL_BANNER, CORNFLOWER -> DyeColor.BLUE;
			case BROWN_BANNER, BROWN_BED, BROWN_BUNDLE, BROWN_CANDLE, BROWN_CANDLE_CAKE, BROWN_CARPET, BROWN_CONCRETE,
				 BROWN_CONCRETE_POWDER, BROWN_DYE, BROWN_WOOL, BROWN_GLAZED_TERRACOTTA, BROWN_TERRACOTTA,
				 BROWN_SHULKER_BOX, BROWN_STAINED_GLASS, BROWN_STAINED_GLASS_PANE, BROWN_WALL_BANNER, COCOA_BEANS ->
					DyeColor.BROWN;
			case GREEN_BANNER, GREEN_BED, GREEN_BUNDLE, GREEN_CANDLE, GREEN_CANDLE_CAKE, GREEN_CARPET, GREEN_CONCRETE,
				 GREEN_CONCRETE_POWDER, GREEN_DYE, GREEN_WOOL, GREEN_GLAZED_TERRACOTTA, GREEN_TERRACOTTA,
				 GREEN_SHULKER_BOX, GREEN_STAINED_GLASS, GREEN_STAINED_GLASS_PANE, GREEN_WALL_BANNER, CACTUS ->
					DyeColor.GREEN;
			case RED_BANNER, RED_BED, RED_BUNDLE, RED_CANDLE, RED_CANDLE_CAKE, RED_CARPET, RED_CONCRETE,
				 RED_CONCRETE_POWDER, RED_DYE, RED_WOOL, RED_GLAZED_TERRACOTTA, RED_TERRACOTTA, RED_SHULKER_BOX,
				 RED_STAINED_GLASS, RED_STAINED_GLASS_PANE, RED_WALL_BANNER, POPPY, RED_TULIP, ROSE_BUSH, BEETROOT ->
					DyeColor.RED;
			case BLACK_BANNER, BLACK_BED, BLACK_BUNDLE, BLACK_CANDLE, BLACK_CANDLE_CAKE, BLACK_CARPET, BLACK_CONCRETE,
				 BLACK_CONCRETE_POWDER, BLACK_DYE, BLACK_WOOL, BLACK_GLAZED_TERRACOTTA, BLACK_TERRACOTTA,
				 BLACK_SHULKER_BOX, BLACK_STAINED_GLASS, BLACK_STAINED_GLASS_PANE, BLACK_WALL_BANNER, INK_SAC,
				 WITHER_ROSE -> DyeColor.BLACK;
			default -> DyeColor.WHITE;
		};
	}
}