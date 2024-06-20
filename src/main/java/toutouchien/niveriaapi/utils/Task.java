package toutouchien.niveriaapi.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class Task {
	public static BukkitTask task(Runnable runnable, Plugin plugin) {
		return Bukkit.getScheduler().runTask(plugin, runnable);
	}

	public static BukkitTask taskLater(Runnable runnable, Plugin plugin, long delay) {
		return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
	}

	public static BukkitTask taskTimer(Runnable runnable, Plugin plugin, long delay, long interval) {
		return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, interval);
	}

	public static BukkitTask taskAsync(Runnable runnable, Plugin plugin) {
		return Bukkit.getScheduler().runTaskAsynchronously(plugin, runnable);
	}

	public static BukkitTask taskLaterAsync(Runnable runnable, Plugin plugin, long delay) {
		return Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, runnable, delay);
	}

	public static BukkitTask taskTimerAsync(Runnable runnable, Plugin plugin, long delay, long interval) {
		return Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, runnable, delay, interval);
	}
}
