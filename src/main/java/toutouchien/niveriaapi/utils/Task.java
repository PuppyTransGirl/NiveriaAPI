package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utility class for scheduling synchronous and asynchronous tasks in a server.
 */
@NullMarked
public final class Task {
    private Task() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Schedules a synchronous task to run immediately.
     *
     * @param consumer The task to run.
     * @param plugin   The plugin scheduling the task.
     * @return The scheduled ScheduledTask.
     */
    public static ScheduledTask sync(Consumer<ScheduledTask> consumer, Plugin plugin) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        return Bukkit.getGlobalRegionScheduler().run(plugin, consumer);
    }

    /**
     * Schedules a synchronous task to run after a specified delay.
     *
     * @param consumer The task to run.
     * @param plugin   The plugin scheduling the task.
     * @param delay    The delay before the task is executed.
     * @param timeUnit The time unit of the delay.
     * @return The scheduled ScheduledTask.
     */
    public static ScheduledTask syncLater(Consumer<ScheduledTask> consumer, Plugin plugin, @NonNegative long delay, TimeUnit timeUnit) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(delay >= 0, "delay cannot be less than 0: %s", delay);
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, consumer, timeUnit.toMillis(delay) / 50);
    }

    /**
     * Schedules a synchronous repeating task.
     *
     * @param consumer The task to run.
     * @param plugin   The plugin scheduling the task.
     * @param delay    The delay before the first execution.
     * @param interval The interval between subsequent executions.
     * @param timeUnit The time unit of the delay and interval.
     * @return The scheduled ScheduledTask.
     */
    public static ScheduledTask syncRepeat(Consumer<ScheduledTask> consumer, Plugin plugin, @NonNegative long delay, @NonNegative long interval, TimeUnit timeUnit) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(delay >= 0, "delay cannot be less than 0: %s", delay);
        Preconditions.checkArgument(interval >= 0, "interval cannot be less than 0: %s", interval);
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, consumer, timeUnit.toMillis(delay) / 50, timeUnit.toMillis(interval) / 50);
    }

    /**
     * Schedules an asynchronous task to run immediately.
     *
     * @param consumer The task to run.
     * @param plugin   The plugin scheduling the task.
     * @return The scheduled ScheduledTask.
     */
    public static ScheduledTask async(Consumer<ScheduledTask> consumer, Plugin plugin) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        return Bukkit.getAsyncScheduler().runNow(plugin, consumer);
    }

    /**
     * Schedules an asynchronous task to run after a specified delay.
     *
     * @param consumer The task to run.
     * @param plugin   The plugin scheduling the task.
     * @param delay    The delay before the task is executed.
     * @param timeUnit The time unit of the delay.
     * @return The scheduled ScheduledTask.
     */
    public static ScheduledTask asyncLater(Consumer<ScheduledTask> consumer, Plugin plugin, @NonNegative long delay, TimeUnit timeUnit) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(delay >= 0, "delay cannot be less than 0: %s", delay);
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        return Bukkit.getAsyncScheduler().runDelayed(plugin, consumer, delay, timeUnit);
    }

    /**
     * Schedules an asynchronous repeating task.
     *
     * @param consumer The task to run.
     * @param plugin   The plugin scheduling the task.
     * @param delay    The delay before the first execution.
     * @param interval The interval between subsequent executions.
     * @param timeUnit The time unit of the delay and interval.
     * @return The scheduled ScheduledTask.
     */
    public static ScheduledTask asyncRepeat(Consumer<ScheduledTask> consumer, Plugin plugin, @NonNegative long delay, @NonNegative long interval, TimeUnit timeUnit) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkArgument(delay >= 0, "delay cannot be less than 0: %s", delay);
        Preconditions.checkArgument(interval >= 0, "interval cannot be less than 0: %s", interval);
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, consumer, delay, interval, timeUnit);
    }
}
