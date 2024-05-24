package toutouchien.niveriaapi.delay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.utils.MessageUtils;
import toutouchien.niveriaapi.utils.TimeUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
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
	}

	public void start(Delay delay) {
		Player player = delay.player();

		teleportDelays.put(delay.player(), delay);
		updateDisplays(delay);

		new BukkitRunnable() {
			@Override
			public void run() {
				if (!teleportDelays.containsKey(player) || delay.delayRemaining() == 0) {
					this.cancel();
					return;
				}

				delay.delayRemaining(delay.delayRemaining() - 1);
				updateDisplays(delay);
			}
		}.runTaskTimerAsynchronously(plugin, 20L, TimeUtils.secondsToTicks(1));
	}

	private void updateDisplays(Delay delay) {
		if (delay.delayRemaining() == 0) {
			endDelay(delay);
			reset(delay, false);
			return;
		}

		Player player = delay.player();
		int delayRemaining = delay.delayRemaining();

		Component text = MessageUtils.infoMessage(
				Component.text("Téléportation dans %s secondes".formatted(delayRemaining))
		);

		if (delay.actionbar()) {
			player.sendActionBar(text);
		}

		if (delay.chat()) {
			player.sendMessage(text);
		}

		if (delay.title()) {
			Title.Times times = Title.Times.times(Duration.ofSeconds(0), Duration.ofMillis(1500), Duration.ofSeconds(0));
			Title title = Title.title(text, Component.empty(), times);
			player.showTitle(title);
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

		if (delay.title()) {
			player.clearTitle();
		}

		Consumer<Player> failConsumer = delay.failConsumer();
		if (failConsumer == null || !fail)
			return;

		failConsumer.accept(player);
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (!teleportDelays.containsKey(player))
			return;

		Location to = event.getTo();

		Delay delay = teleportDelays.get(player);
		if (!delay.cancelOnMove())
			return;

		Location originalLocation = delay.originalLocation();

		if (to.getWorld() == originalLocation.getWorld()) {
			double distance = to.distance(originalLocation);
			if (!Double.isNaN(distance) && distance <= 1)
				return;
		}

		Component message = MessageUtils.errorMessage(
				Component.text("Votre demande de téléportation a été annulée car vous avez bougé.")
		);

		player.sendMessage(message);
		reset(delay, true);
	}

	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		if (!teleportDelays.containsKey(player))
			return;

		reset(teleportDelays.get(player), true);
	}
}