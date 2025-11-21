package toutouchien.niveriaapi.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Task {
	private Task() {
		throw new IllegalStateException("Utility class");
	}

	public static BukkitTask sync(Runnable runnable, Plugin plugin) {
		return Bukkit.getScheduler().runTask(plugin, runnable);
	}

	public static BukkitTask syncLater(Runnable runnable, Plugin plugin, long delay) {
		return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
	}

	public static BukkitTask syncRepeat(Runnable runnable, Plugin plugin, long delay, long interval) {
		return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, interval);
	}

	public static ScheduledTask async(Consumer<ScheduledTask> consumer, Plugin plugin) {
		return Bukkit.getAsyncScheduler().runNow(plugin, consumer);
	}

	public static ScheduledTask asyncLater(Consumer<ScheduledTask> consumer, Plugin plugin, long delay, TimeUnit timeUnit) {
		return Bukkit.getAsyncScheduler().runDelayed(plugin, consumer, delay, timeUnit);
	}

	public static ScheduledTask asyncRepeat(Consumer<ScheduledTask> consumer, Plugin plugin, long delay, long interval, TimeUnit timeUnit) {
		return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, consumer, delay, interval, timeUnit);
	}
}
