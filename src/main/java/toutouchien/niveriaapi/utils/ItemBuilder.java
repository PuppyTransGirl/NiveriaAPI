package toutouchien.niveriaapi.utils;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.material.MaterialData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ItemBuilder {

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
	public Component name() {
		return itemStack.getItemMeta().displayName();
	}

	@NotNull
	public ItemBuilder name(Component name) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.displayName(name.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	public int durability() {
		ItemMeta itemMeta = itemStack.getItemMeta();

		if (!(itemMeta instanceof Damageable damageable))
			throw new IllegalArgumentException(String.format("You can't get the durability of this item. Provided: %s", itemStack.getType().name()));

		return itemStack.getType().getMaxDurability() - damageable.getDamage();
	}

	public ItemBuilder durability(int durability) {
		ItemMeta itemMeta = itemStack.getItemMeta();

		if (!(itemMeta instanceof Damageable damageable))
			throw new IllegalArgumentException(String.format("You can't set the durability of this item. Provided: %s", itemStack.getType().name()));

		damageable.setDamage(itemStack.getType().getMaxDurability() - durability);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	public int damage() {
		ItemMeta itemMeta = itemStack.getItemMeta();

		if (!(itemMeta instanceof Damageable damageable))
			throw new IllegalArgumentException(String.format("You can't get the damage of this item. Provided: %s", itemStack.getType().name()));

		return damageable.getDamage();
	}

	@NotNull
	public ItemBuilder damage(int damage) {
		ItemMeta itemMeta = itemStack.getItemMeta();

		if (!(itemMeta instanceof Damageable damageable))
			throw new IllegalArgumentException(String.format("You can't set the damage of this item. Provided: %s", itemStack.getType().name()));

		damageable.setDamage(damage);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public ItemBuilder addEnchantment(@NotNull Enchantment enchantment, int level) {
		itemStack.addEnchantment(enchantment, level);
		return this;
	}

	@NotNull
	public ItemBuilder enchantment(@NotNull Enchantment enchantment, int level) {
		removeEnchantments();
		itemStack.addEnchantment(enchantment, level);
		return this;
	}

	@NotNull
	public Map<Enchantment, Integer> enchantments() {
		ItemMeta itemMeta = itemStack.getItemMeta();
		return itemMeta.getEnchants();
	}

	@NotNull
	public ItemBuilder addEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
		itemStack.addEnchantments(enchantments);
		return this;
	}

	@NotNull
	public ItemBuilder enchantments(@NotNull Map<Enchantment, Integer> enchantments) {
		removeEnchantments();
		itemStack.addEnchantments(enchantments);
		return this;
	}

	@NotNull
	public ItemBuilder addUnsafeEnchantment(@NotNull Enchantment enchantment, int level) {
		itemStack.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	@NotNull
	public ItemBuilder unsafeEnchantment(@NotNull Enchantment enchantment, int level) {
		removeEnchantments();
		itemStack.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	@NotNull
	public ItemBuilder addUnsafeEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
		itemStack.addUnsafeEnchantments(enchantments);
		return this;
	}

	@NotNull
	public ItemBuilder unsafeEnchantments(@NotNull Map<Enchantment, Integer> enchantments) {
		removeEnchantments();
		itemStack.addUnsafeEnchantments(enchantments);
		return this;
	}

	@NotNull
	public ItemBuilder removeEnchantment(@NotNull Enchantment enchantment) {
		itemStack.removeEnchantment(enchantment);
		return this;
	}

	@NotNull
	public ItemBuilder removeEnchantments(@NotNull Enchantment... enchantments) {
		ItemMeta itemMeta = itemStack.getItemMeta();

		for (int i = 0; i < enchantments.length; i++) {
			Enchantment enchantment = enchantments[i];
			itemMeta.removeEnchant(enchantment);
		}
		return this;
	}

	@NotNull
	public ItemBuilder removeEnchantments() {
		itemStack.removeEnchantments();
		return this;
	}

	@Nullable
	public String skullOwner() {
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (!(itemMeta instanceof SkullMeta skullMeta))
			throw new IllegalArgumentException(String.format("You can't get the skull owner of this item. Provided: %s", itemStack.getType().name()));

		PlayerProfile ownerProfile = skullMeta.getPlayerProfile();
		return ownerProfile == null ? null : ownerProfile.getName();
	}

	@NotNull
	public ItemBuilder skullOwner(String owner) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (!(itemMeta instanceof SkullMeta skullMeta))
			throw new IllegalArgumentException(String.format("You can't set the skull owner of this item. Provided: %s", itemStack.getType().name()));

		skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(owner));

		itemStack.setItemMeta(skullMeta);
		return this;
	}

	public boolean unbreakable() {
		ItemMeta itemMeta = itemStack.getItemMeta();
		return itemMeta.isUnbreakable();
	}

	@NotNull
	public ItemBuilder unbreakable(boolean unbreakable) {
		ItemMeta itemMeta = itemStack.getItemMeta();

		itemMeta.setUnbreakable(unbreakable);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@Nullable
	public List<Component> lore() {
		ItemMeta itemMeta = itemStack.getItemMeta();
		return itemMeta.lore();
	}

	@NotNull
	public ItemBuilder lore(@NotNull Component... lore) {
		List<Component> lores = new ArrayList<>();
		Arrays.stream(lore).forEach(l -> lores.add(l.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));

		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.lore(lores);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public ItemBuilder lore(@NotNull List<Component> lore) {
		List<Component> lores = new ArrayList<>();
		lore.forEach(l -> lores.add(l.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE)));

		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.lore(lores);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public ItemBuilder removeLore() {
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.lore(null);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@Nullable
	public Component loreLine(int index) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		List<Component> lore = itemMeta.lore();

		return lore == null ? null : lore.get(index);
	}

	@NotNull
	public ItemBuilder addLoreLine(@NotNull Component line) {
		ItemMeta itemMeta = itemStack.getItemMeta();

		List<Component> lore = new ArrayList<>();
		if (itemMeta.hasLore())
			lore.addAll(itemMeta.lore());

		lore.add(line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
		itemMeta.lore(lore);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public ItemBuilder addLoreLine(@NotNull Component line, int index) {
		ItemMeta itemMeta = itemStack.getItemMeta();

		List<Component> lore = new ArrayList<>();
		if (itemMeta.hasLore())
			lore.addAll(itemMeta.lore());

		int realIndex = Math.min(index, lore.size() - 1);
		lore.set(realIndex, line.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));

		itemMeta.lore(lore);
		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public ItemBuilder removeLoreLine(@NotNull Component line) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (!itemMeta.hasLore())
			throw new IllegalArgumentException(String.format("You can't remove a lore line from an item that doesn't contain any. Provided: %s", itemStack.getType().name()));

		List<Component> lore = itemMeta.lore();
		if (!lore.contains(line))
			return this;

		lore.remove(line);
		itemMeta.lore(lore);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public ItemBuilder removeLoreLine(int index) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (!itemMeta.hasLore())
			throw new IllegalArgumentException(String.format("You can't remove a lore line from an item that doesn't contain any. Provided: %s", itemStack.getType().name()));

		List<Component> lore = new ArrayList<>(itemMeta.lore());
		if (index < 0 || index > lore.size())
			return this;

		lore.remove(index);
		itemMeta.lore(lore);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public DyeColor dyeColor() {
		return dyeColor(itemStack.getType());
	}

	@NotNull
	public ItemBuilder dyeColor(@Nullable DyeColor color) {
		MaterialData materialData = itemStack.getData();
		materialData.setData(color.getDyeData());

		itemStack.setData(materialData);
		return this;
	}

	@NotNull
	public ItemBuilder woolColor(@Nullable DyeColor color) {
		MaterialData materialData = itemStack.getData();
		materialData.setData(color.getWoolData());

		itemStack.setData(materialData);
		return this;
	}

	@NotNull
	public ItemBuilder leatherArmorColor(@Nullable Color color) {
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (!(itemMeta instanceof LeatherArmorMeta leatherArmorMeta))
			throw new IllegalArgumentException(String.format("You can't set the leather armor color to this item. Provided: %s", itemStack.getType().name()));

		leatherArmorMeta.setColor(color);

		itemStack.setItemMeta(itemMeta);
		return this;
	}

	@NotNull
	public Set<ItemFlag> itemFlags() {
		ItemMeta itemMeta = itemStack.getItemMeta();
		return itemMeta.getItemFlags();
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
	public ItemStack build() {
		return itemStack;
	}

	private DyeColor dyeColor(Material material) {
		return switch (material) {
			case ORANGE_WOOL, ORANGE_DYE -> DyeColor.ORANGE;
			case MAGENTA_WOOL, MAGENTA_DYE -> DyeColor.MAGENTA;
			case LIGHT_BLUE_WOOL, LIGHT_BLUE_DYE -> DyeColor.LIGHT_BLUE;
			case YELLOW_WOOL, YELLOW_DYE -> DyeColor.YELLOW;
			case LIME_WOOL, LIME_DYE -> DyeColor.LIME;
			case PINK_WOOL, PINK_DYE -> DyeColor.PINK;
			case GRAY_WOOL, GRAY_DYE -> DyeColor.GRAY;
			case LIGHT_GRAY_WOOL, LIGHT_GRAY_DYE -> DyeColor.LIGHT_GRAY;
			case CYAN_WOOL, CYAN_DYE -> DyeColor.CYAN;
			case PURPLE_WOOL, PURPLE_DYE -> DyeColor.PURPLE;
			case BLUE_WOOL, BLUE_DYE -> DyeColor.BLUE;
			case BROWN_WOOL, BROWN_DYE -> DyeColor.BROWN;
			case GREEN_WOOL, GREEN_DYE -> DyeColor.GREEN;
			case RED_WOOL, RED_DYE -> DyeColor.RED;
			case BLACK_WOOL, BLACK_DYE -> DyeColor.BLACK;
			default -> DyeColor.WHITE;
		};
	}
}