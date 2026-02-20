package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.index.qual.NonNegative;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Utility class for scheduling synchronous and asynchronous tasks in a server.
 * <p>
 * Delays provided as (value, TimeUnit) are converted to server ticks (1 tick = 50 ms)
 * using ceiling division and clamped to a minimum of 1 tick so tasks never schedule
 * with zero delay.
 */
@NullMarked
public final class Task {
    private Task() {
        throw new IllegalStateException("Utility class");
    }

    private static long millisToCeilTicks(long millis) {
        long ticks = (millis + 49) / 50; // ceil to next tick
        return Math.max(1L, ticks);
    }

    /**
     * Schedules a synchronous task to run immediately (next tick).
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
     * <p>
     * Delay is provided as (delay, timeUnit) and converted to ticks (1 tick = 50ms).
     * Conversion uses ceiling and enforces a minimum of 1 tick.
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
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        long millis = timeUnit.toMillis(Math.max(1L, delay));
        long ticks = millisToCeilTicks(millis);
        return Bukkit.getGlobalRegionScheduler().runDelayed(plugin, consumer, ticks);
    }

    /**
     * Schedules a synchronous repeating task.
     * <p>
     * Delays and intervals are provided as (value, timeUnit) and converted to ticks (1 tick = 50ms).
     * Conversion uses ceiling and enforces a minimum of 1 tick for both values.
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
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        long delayMillis = timeUnit.toMillis(Math.max(1L, delay));
        long intervalMillis = timeUnit.toMillis(Math.max(1L, interval));
        long delayTicks = millisToCeilTicks(delayMillis);
        long intervalTicks = millisToCeilTicks(intervalMillis);

        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(plugin, consumer, delayTicks, intervalTicks);
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
     * <p>
     * The provided delay is clamped to a minimum of 1 unit in the given timeUnit.
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
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        long d = Math.max(1L, delay);
        return Bukkit.getAsyncScheduler().runDelayed(plugin, consumer, d, timeUnit);
    }

    /**
     * Schedules an asynchronous repeating task.
     * <p>
     * The provided delay and interval are clamped to a minimum of 1 unit in the given timeUnit.
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
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        long d = Math.max(1L, delay);
        long i = Math.max(1L, interval);
        return Bukkit.getAsyncScheduler().runAtFixedRate(plugin, consumer, d, i, timeUnit);
    }

    /**
     * Schedules a synchronous task to be executed on the region which owns the given location on the next tick.
     *
     * @param consumer The task to run.
     * @param plugin   The plugin scheduling the task.
     * @param location The location whose region should run the task.
     * @return The scheduled ScheduledTask.
     */
    public static ScheduledTask run(Consumer<ScheduledTask> consumer, Plugin plugin, Location location) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");

        return Bukkit.getRegionScheduler().run(
                plugin,
                location,
                consumer
        );
    }

    /**
     * Schedules a synchronous task to be executed on the region which owns the given location after the specified
     * delay.
     * <p>
     * The delay is provided with a TimeUnit and converted to server ticks (1 tick = 50 ms).
     * Conversion uses ceiling division and enforces a minimum of 1 tick.
     *
     * @param consumer The task to run.
     * @param plugin   The plugin scheduling the task.
     * @param location The location whose region should run the task.
     * @param delay    The delay before execution.
     * @param timeUnit The time unit of the delay.
     * @return The scheduled ScheduledTask.
     */
    public static ScheduledTask runDelayed(Consumer<ScheduledTask> consumer, Plugin plugin, Location location,
                                           @NonNegative long delay, TimeUnit timeUnit) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        long millis = timeUnit.toMillis(Math.max(1L, delay));
        long ticks = millisToCeilTicks(millis);
        return Bukkit.getRegionScheduler().runDelayed(
                plugin,
                location,
                consumer,
                ticks
        );
    }

    /**
     * Schedules a synchronous repeating task to be executed on the region which owns the given location after the
     * initial delay with the specified period.
     * <p>
     * The delays are provided with a TimeUnit and converted to server ticks (1 tick = 50 ms).
     * Conversion uses ceiling division and enforces a minimum of 1 tick.
     *
     * @param consumer     The task to run.
     * @param plugin       The plugin scheduling the task.
     * @param location     The location whose region should run the task.
     * @param initialDelay The initial delay.
     * @param period       The period between executions.
     * @param timeUnit     The time unit of the delays.
     * @return The scheduled ScheduledTask.
     */
    public static ScheduledTask runRepeat(Consumer<ScheduledTask> consumer, Plugin plugin, Location location,
                                          @NonNegative long initialDelay, @NonNegative long period, TimeUnit timeUnit) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkNotNull(location, "location cannot be null");
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        long initialMillis = timeUnit.toMillis(Math.max(1L, initialDelay));
        long periodMillis = timeUnit.toMillis(Math.max(1L, period));
        long initialTicks = millisToCeilTicks(initialMillis);
        long periodTicks = millisToCeilTicks(periodMillis);

        return Bukkit.getRegionScheduler().runAtFixedRate(
                plugin,
                location,
                consumer,
                initialTicks,
                periodTicks
        );
    }

    /*
     * Entity scheduler convenience methods (use Entity#getScheduler)
     */

    /**
     * Schedules a task to execute on the region which owns the given entity on the next tick.
     *
     * @param consumer The task to run.
     * @param plugin   The plugin scheduling the task.
     * @param entity   The entity whose scheduler should run the task.
     * @return The scheduled ScheduledTask, or null if the entity scheduler is retired/removed.
     */
    public static ScheduledTask run(Consumer<ScheduledTask> consumer, Plugin plugin, Entity entity) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");

        return entity.getScheduler().run(plugin, consumer, null);
    }

    /**
     * Schedules a task with the given delay on the region which owns the given entity.
     * <p>
     * The delay is supplied together with a {@link TimeUnit} and will be converted to server ticks (1 tick = 50 ms)
     * by this helper (ceiling conversion, minimum 1 tick).
     *
     * @param consumer The task to run.
     * @param plugin   The plugin scheduling the task.
     * @param entity   The entity whose scheduler should run the task.
     * @param delay    The delay before execution.
     * @param timeUnit The time unit of the delay.
     * @return The scheduled ScheduledTask, or null if the entity scheduler is retired/removed.
     */
    public static ScheduledTask runDelayed(Consumer<ScheduledTask> consumer, Plugin plugin, Entity entity,
                                           @NonNegative long delay, TimeUnit timeUnit) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        long millis = timeUnit.toMillis(Math.max(1L, delay));
        long ticks = millisToCeilTicks(millis);
        return entity.getScheduler().runDelayed(plugin, consumer, null, ticks);
    }

    /**
     * Schedules a repeating task with the given initial delay and period on the region which owns the given entity.
     * <p> <p>
     * The delays are supplied together with a {@link TimeUnit} and will be converted to server ticks (1 tick = 50 ms)
     * by this helper (ceiling conversion, minimum 1 tick).
     *
     * @param consumer     The task to run.
     * @param plugin       The plugin scheduling the task.
     * @param entity       The entity whose scheduler should run the task.
     * @param initialDelay The initial delay before the first execution.
     * @param period       The period between subsequent executions.
     * @param timeUnit     The time unit of the delays.
     * @return The scheduled ScheduledTask, or null if the entity scheduler is retired/removed.
     */
    public static ScheduledTask runRepeat(Consumer<ScheduledTask> consumer, Plugin plugin, Entity entity,
                                          @NonNegative long initialDelay, @NonNegative long period, TimeUnit timeUnit) {
        Preconditions.checkNotNull(consumer, "consumer cannot be null");
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkNotNull(entity, "entity cannot be null");
        Preconditions.checkNotNull(timeUnit, "timeUnit cannot be null");

        long initialMillis = timeUnit.toMillis(Math.max(1L, initialDelay));
        long periodMillis = timeUnit.toMillis(Math.max(1L, period));
        long initialTicks = millisToCeilTicks(initialMillis);
        long periodTicks = millisToCeilTicks(periodMillis);

        return entity.getScheduler().runAtFixedRate(plugin, consumer, null, initialTicks, periodTicks);
    }
}
