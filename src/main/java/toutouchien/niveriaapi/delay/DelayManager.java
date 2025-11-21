package toutouchien.niveriaapi.delay;

import io.papermc.paper.adventure.PaperAdventure;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.utils.NMSUtils;
import toutouchien.niveriaapi.utils.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class DelayManager implements Listener {
	private final NiveriaAPI plugin;
	private final Map<Player, Delay> teleportDelays;

	public DelayManager(NiveriaAPI plugin) {
		this.plugin = plugin;
		this.teleportDelays = new HashMap<>();
	}

	public void initialize() {
		PluginManager pluginManager = Bukkit.getPluginManager();
		pluginManager.registerEvents(this, plugin);

		Task.asyncRepeat(ignored -> {
			if (teleportDelays.isEmpty())
				return;

			for (Map.Entry<Player, Delay> entry : teleportDelays.entrySet()) {
				Delay delay = entry.getValue();
				if (!delay.cancelOnMove())
					continue;

				Player player = entry.getKey();

				Location originalLocation = delay.originalLocation();
				Location to = player.getLocation();
				if (originalLocation.getWorld() == to.getWorld()) {
					double distance = to.distance(originalLocation);
					if (!Double.isNaN(distance) && distance <= 1)
						continue;
				}

				player.sendMessage(delay.movedText());
				reset(delay, true);
			}
		}, plugin, 3L, 1L, TimeUnit.SECONDS);
	}

	public void start(Delay delay) {
		Player player = delay.player();

		if (inDelay(player)) {
			player.sendMessage(delay.alreadyHasDelayText());
			return;
		}

		teleportDelays.put(delay.player(), delay);
		updateDisplays(delay);

        Task.asyncRepeat(task -> {
            if (delay.delayRemaining() == 0 || !teleportDelays.containsKey(player)) {
                task.cancel();
                return;
            }

            delay.delayRemaining(delay.delayRemaining() - 1);
            updateDisplays(delay);
        }, plugin, 1L, 1L, TimeUnit.SECONDS);
	}

	private void updateDisplays(Delay delay) {
		if (delay.delayRemaining() == 0) {
			endDelay(delay);
			reset(delay, false);
			return;
		}

		ServerGamePacketListenerImpl connection = NMSUtils.getConnection(delay.player());

		int delayRemaining = delay.delayRemaining();
		Component text = PaperAdventure.asVanilla(
				delay.text().replaceText(builder -> builder.matchLiteral("%s").replacement(String.valueOf(delayRemaining)))
		);

		if (delay.actionbar())
			connection.send(new ClientboundSetActionBarTextPacket(text));

		if (delay.chat())
			connection.send(new ClientboundSystemChatPacket(text, false));

		if (delay.title()) {
			ClientboundSetTitlesAnimationPacket titlesAnimationPacket = new ClientboundSetTitlesAnimationPacket(0, 30, 0);
			ClientboundSetSubtitleTextPacket subtitleTextPacket = new ClientboundSetSubtitleTextPacket(Component.empty());
			ClientboundSetTitleTextPacket titleTextPacket = new ClientboundSetTitleTextPacket(text);

			connection.send(titlesAnimationPacket);
			connection.send(subtitleTextPacket);
			connection.send(titleTextPacket);
		}
	}

	private void endDelay(Delay delay) {
		Player player = delay.player();

		Consumer<Player> successConsumer = delay.successConsumer();
		if (successConsumer == null)
			return;

		successConsumer.accept(player);
	}

	private void reset(Delay delay, boolean fail) {
		Player player = delay.player();
		teleportDelays.remove(player);

		if (delay.title())
			NMSUtils.sendPacket(player, new ClientboundClearTitlesPacket(true));

		Consumer<Player> failConsumer = delay.failConsumer();
		if (!fail || failConsumer == null)
			return;

		failConsumer.accept(player);
	}

	public boolean inDelay(Player player) {
		return teleportDelays.containsKey(player);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (!teleportDelays.containsKey(player))
			return;

		reset(teleportDelays.get(player), true);
	}
}