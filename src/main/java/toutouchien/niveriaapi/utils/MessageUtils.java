package toutouchien.niveriaapi.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public class MessageUtils {
	public static TextColor errorColor() {
		return TextColor.fromHexString("#F52F2E");
	}

	public static TextColor infoColor() {
		return TextColor.fromHexString("#364CD2");
	}

	public static TextColor successColor() {
		return TextColor.fromHexString("#36C835");
	}

	public static TextColor warnColor() {
		return TextColor.fromHexString("#FF6501");
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
				.color(errorColor());
	}

	public static TextComponent infoMessage(Component content) {
		return infoPrefix()
				.append(content)
				.color(infoColor());
	}

	public static TextComponent successMessage(Component content) {
		return successPrefix()
				.append(content)
				.color(successColor());
	}

	public static TextComponent warnMessage(Component content) {
		return warnPrefix()
				.append(content)
				.color(warnColor());
	}
}
