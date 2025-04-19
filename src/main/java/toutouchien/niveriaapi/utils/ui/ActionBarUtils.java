package toutouchien.niveriaapi.utils.ui;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.utils.game.NMSUtils;

import java.util.Collection;

public class ActionBarUtils {
	private ActionBarUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static void sendActionBar(@NotNull Player player, @NotNull Component message) {
		if (message.equals(Component.empty()))
			return;

		NMSUtils.sendPacket(player, new ClientboundSetActionBarTextPacket(PaperAdventure.asVanilla(message)));
	}

	public static void sendActionBar(@NotNull Collection<? extends Player> players, @NotNull Component message) {
		if (players.isEmpty())
			return;

		if (message.equals(Component.empty()))
			return;

		if (players.size() == 1) {
			sendActionBar(players.iterator().next(), message);
			return;
		}

		ClientboundSetActionBarTextPacket actionBarTextPacket = new ClientboundSetActionBarTextPacket(PaperAdventure.asVanilla(message));
		players.forEach(player -> NMSUtils.sendPacket(player, actionBarTextPacket));
	}

	public static void sendActionBarToAllOnlinePlayers(@NotNull Component message) {
		if (message.equals(Component.empty()))
			return;

		Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
		if (onlinePlayers.isEmpty())
			return;

		if (onlinePlayers.size() == 1) {
			sendActionBar(onlinePlayers.iterator().next(), message);
			return;
		}

		sendActionBar(onlinePlayers, message);
	}

	public static void clearActionBar(@NotNull Player player) {
		NMSUtils.sendPacket(player, new ClientboundSetActionBarTextPacket(net.minecraft.network.chat.Component.empty()));
	}

	public static void clearActionBar(@NotNull Collection<? extends Player> players) {
		if (players.isEmpty())
			return;

		if (players.size() == 1) {
			clearActionBar(players.iterator().next());
			return;
		}

		ClientboundSetActionBarTextPacket actionBarTextPacket = new ClientboundSetActionBarTextPacket(net.minecraft.network.chat.Component.empty());
		players.forEach(player -> NMSUtils.sendPacket(player, actionBarTextPacket));
	}

	public static void clearActionBarOfAllOnlinePlayers() {
		Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
		if (onlinePlayers.isEmpty())
			return;

		if (onlinePlayers.size() == 1) {
			clearActionBar(onlinePlayers.iterator().next());
			return;
		}

		ClientboundSetActionBarTextPacket actionBarTextPacket = new ClientboundSetActionBarTextPacket(net.minecraft.network.chat.Component.empty());
		onlinePlayers.forEach(player -> NMSUtils.sendPacket(player, actionBarTextPacket));
	}
}
