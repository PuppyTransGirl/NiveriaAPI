package toutouchien.niveriaapi.utils.ui;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.bukkit.entity.Player;
import toutouchien.niveriaapi.utils.game.NMSUtils;

public class MessageUtils {
	private MessageUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static TextComponent errorPrefix() {
		return Component.text(":error: ");
	}

	public static TextComponent infoPrefix() {
		return Component.text(":info: ");
	}

	public static TextComponent successPrefix() {
		return Component.text(":success: ");
	}

	public static TextComponent warnPrefix() {
		return Component.text(":warn: ");
	}

	public static TextComponent errorMessage(Component content) {
		return errorPrefix()
				.append(content)
				.color(ColorUtils.errorColor());
	}

	public static TextComponent infoMessage(Component content) {
		return infoPrefix()
				.append(content)
				.color(ColorUtils.infoColor());
	}

	public static TextComponent successMessage(Component content) {
		return successPrefix()
				.append(content)
				.color(ColorUtils.successColor());
	}

	public static TextComponent warnMessage(Component content) {
		return warnPrefix()
				.append(content)
				.color(ColorUtils.warnColor());
	}

	public static void sendErrorMessage(Player player, Component content) {
		net.minecraft.network.chat.Component nmsComponent = PaperAdventure.asVanilla(content);
		NMSUtils.sendPacket(player, new ClientboundSystemChatPacket(nmsErrorMessage(nmsComponent), false));
	}

	public static void sendInfoMessage(Player player, Component content) {
		net.minecraft.network.chat.Component nmsComponent = PaperAdventure.asVanilla(content);
		NMSUtils.sendPacket(player, new ClientboundSystemChatPacket(nmsInfoMessage(nmsComponent), false));
	}

	public static void sendSuccessMessage(Player player, Component content) {
		net.minecraft.network.chat.Component nmsComponent = PaperAdventure.asVanilla(content);
		NMSUtils.sendPacket(player, new ClientboundSystemChatPacket(nmsSuccessMessage(nmsComponent), false));
	}

	public static void sendWarnMessage(Player player, Component content) {
		net.minecraft.network.chat.Component nmsComponent = PaperAdventure.asVanilla(content);
		NMSUtils.sendPacket(player, new ClientboundSystemChatPacket(nmsWarnMessage(nmsComponent), false));
	}

	public static void sendMessage(Player player, Component content) {
		net.minecraft.network.chat.Component nmsComponent = PaperAdventure.asVanilla(content);
		NMSUtils.sendPacket(player, new ClientboundSystemChatPacket(nmsComponent, false));
	}

	public static net.minecraft.network.chat.MutableComponent nmsErrorPrefix() {
		return net.minecraft.network.chat.Component.literal(":error: ");
	}

	public static net.minecraft.network.chat.MutableComponent nmsInfoPrefix() {
		return net.minecraft.network.chat.Component.literal(":info: ");
	}

	public static net.minecraft.network.chat.MutableComponent nmsSuccessPrefix() {
		return net.minecraft.network.chat.Component.literal(":success: ");
	}

	public static net.minecraft.network.chat.MutableComponent nmsWarnPrefix() {
		return net.minecraft.network.chat.Component.literal(":warn: ");
	}

	public static net.minecraft.network.chat.MutableComponent nmsErrorMessage(net.minecraft.network.chat.Component content) {
		return nmsErrorPrefix()
				.append(content)
				.withColor(ColorUtils.errorColor().value());
	}

	public static net.minecraft.network.chat.MutableComponent nmsInfoMessage(net.minecraft.network.chat.Component content) {
		return nmsInfoPrefix()
				.append(content)
				.withColor(ColorUtils.infoColor().value());
	}

	public static net.minecraft.network.chat.MutableComponent nmsSuccessMessage(net.minecraft.network.chat.Component content) {
		return nmsSuccessPrefix()
				.append(content)
				.withColor(ColorUtils.successColor().value());
	}

	public static net.minecraft.network.chat.MutableComponent nmsWarnMessage(net.minecraft.network.chat.Component content) {
		return nmsWarnPrefix()
				.append(content)
				.withColor(ColorUtils.warnColor().value());
	}

	public static void sendNMSErrorMessage(Player player, net.minecraft.network.chat.Component content) {
		NMSUtils.sendPacket(player, new ClientboundSystemChatPacket(nmsErrorMessage(content), false));
	}

	public static void sendNMSInfoMessage(Player player, net.minecraft.network.chat.Component content) {
		NMSUtils.sendPacket(player, new ClientboundSystemChatPacket(nmsInfoMessage(content), false));
	}

	public static void sendNMSSuccessMessage(Player player, net.minecraft.network.chat.Component content) {
		NMSUtils.sendPacket(player, new ClientboundSystemChatPacket(nmsSuccessMessage(content), false));
	}

	public static void sendNMSWarnMessage(Player player, net.minecraft.network.chat.Component content) {
		NMSUtils.sendPacket(player, new ClientboundSystemChatPacket(nmsWarnMessage(content), false));
	}

	public static void sendNMSMessage(Player player, net.minecraft.network.chat.Component content) {
		NMSUtils.sendPacket(player, new ClientboundSystemChatPacket(content, false));
	}
}
