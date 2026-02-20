package toutouchien.niveriaapi.delay;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.jspecify.annotations.NullMarked;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.utils.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Manages delays for players, such as teleportation delays.
 */
@NullMarked
public class DelayManager implements Listener {
    private final NiveriaAPI plugin;
    private final Map<Player, Delay> teleportDelays;

    /**
     * Constructs a DelayManager with the specified plugin instance.
     *
     * @param plugin The NiveriaAPI plugin instance.
     */
    public DelayManager(NiveriaAPI plugin) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        this.plugin = plugin;
        this.teleportDelays = new HashMap<>();
    }

    /**
     * Initializes the DelayManager by registering event listeners and starting the movement check task.
     */
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

    /**
     * Starts a delay for the specified player.
     *
     * @param delay The Delay object containing delay information.
     */
    public void start(Delay delay) {
        Preconditions.checkNotNull(delay, "delay cannot be null");

        Player player = delay.player();

        Delay existing = teleportDelays.putIfAbsent(delay.player(), delay);
        if (existing != null) {
            player.sendMessage(delay.alreadyHasDelayText());
            return;
        }

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
        Preconditions.checkNotNull(delay, "delay cannot be null");

        if (delay.delayRemaining() == 0) {
            endDelay(delay);
            reset(delay, false);
            return;
        }

        Player player = delay.player();
        int delayRemaining = delay.delayRemaining();
        Component text = delay.text().replaceText(builder -> builder.matchLiteral("<delay_seconds>").replacement(String.valueOf(delayRemaining)));

        if (delay.actionbar())
            player.sendActionBar(text);

        if (delay.chat())
            player.sendMessage(text);

        if (delay.title()) {
            player.showTitle(Title.title(
                    text,
                    Component.empty(),
                    0, 30, 0
            ));
        }
    }

    private void endDelay(Delay delay) {
        Preconditions.checkNotNull(delay, "delay cannot be null");

        Player player = delay.player();

        Consumer<Player> successConsumer = delay.successConsumer();
        if (successConsumer == null)
            return;

        successConsumer.accept(player);
    }

    private void reset(Delay delay, boolean fail) {
        Preconditions.checkNotNull(delay, "delay cannot be null");

        Player player = delay.player();
        teleportDelays.remove(player);

        if (delay.title())
            player.clearTitle();

        Consumer<Player> failConsumer = delay.failConsumer();
        if (!fail || failConsumer == null)
            return;

        failConsumer.accept(player);
    }

    /**
     * Checks if the specified player is currently in a delay.
     *
     * @param player The player to check.
     * @return True if the player is in a delay, false otherwise.
     */
    public boolean inDelay(Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");
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
