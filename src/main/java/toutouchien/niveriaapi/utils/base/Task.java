package toutouchien.niveriaapi.utils.base;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

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

	public static BukkitTask async(Runnable runnable, Plugin plugin) {
		return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
	}

	public static BukkitTask asyncLater(Runnable runnable, Plugin plugin, long delay) {
		return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
	}

	public static BukkitTask asyncRepeat(Runnable runnable, Plugin plugin, long delay, long interval) {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, interval);
	}
}
