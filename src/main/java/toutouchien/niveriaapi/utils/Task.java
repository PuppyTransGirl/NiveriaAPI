package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Task {
    private Task() {
        throw new IllegalStateException("Utility class");
    }

    @NotNull
    public static BukkitTask sync(@NotNull Runnable runnable, @NotNull Plugin plugin) {
        Preconditions.checkNotNull(runnable, "runnable cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        return Bukkit.getScheduler().runTask(plugin, runnable);
    }

    @NotNull
    public static BukkitTask syncLater(@NotNull Runnable runnable, @NotNull Plugin plugin, @NonNegative long delay) {
        Preconditions.checkNotNull(runnable, "runnable cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(delay >= 0, "delay cannot be less than 0: %d", delay);

        return Bukkit.getScheduler().runTaskLater(plugin, runnable, delay);
    }

    @NotNull
    public static BukkitTask syncRepeat(@NotNull Runnable runnable, @NotNull Plugin plugin, @NonNegative long delay, @NonNegative long interval) {
        Preconditions.checkNotNull(runnable, "runnable cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(delay >= 0, "delay cannot be less than 0: %d", delay);
        Preconditions.checkArgument(interval >= 0, "interval cannot be less than 0: %d", delay);

        return Bukkit.getScheduler().runTaskTimer(plugin, runnable, delay, interval);
    }

    @NotNull
    public static ScheduledTask async(@NotNull Consumer<ScheduledTask> consumer, @NotNull Plugin plugin) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        return Bukkit.getAsyncScheduler().runNow(plugin, consumer);
    }

    @NotNull
    public static ScheduledTask asyncLater(@NotNull Consumer<ScheduledTask> consumer, @NotNull Plugin plugin, @NonNegative long delay, @NotNull TimeUnit timeUnit) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(delay >= 0, "delay cannot be less than 0: %d", delay);
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        return Bukkit.getAsyncScheduler().runDelayed(plugin, consumer, delay, timeUnit);
    }

    @NotNull
    public static ScheduledTask asyncRepeat(@NotNull Consumer<ScheduledTask> consumer, @NotNull Plugin plugin, @NonNegative long delay, @NonNegative long interval, @NotNull TimeUnit timeUnit) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(delay >= 0, "delay cannot be less than 0: %d", delay);
        Preconditions.checkArgument(interval >= 0, "interval cannot be less than 0: %d", delay);
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, consumer, delay, interval, timeUnit);
    }
}
