package toutouchien.niveriaapi.delay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.scheduler.BukkitRunnable;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.utils.base.Task;
import toutouchien.niveriaapi.utils.common.TimeUtils;
import toutouchien.niveriaapi.utils.ui.MessageUtils;

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

        Task.asyncRepeat(() -> {
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

                MessageUtils.sendErrorMessage(player, Component.text("Ta demande de téléportation a été annulée car tu as bougé."));
                reset(delay, true);
            }
        }, plugin, TimeUtils.ticks(3L, TimeUnit.SECONDS), 20L);
    }

    public void start(Delay delay) {
        Player player = delay.player();

        if (inDelay(player)) {
            MessageUtils.sendErrorMessage(player, Component.text("Tu as déjà une demande de téléportation."));
            return;
        }

        teleportDelays.put(delay.player(), delay);
        updateDisplays(delay);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (delay.delayRemaining() == 0 || !teleportDelays.containsKey(player)) {
                    this.cancel();
                    return;
                }

                delay.delayRemaining(delay.delayRemaining() - 1);
                updateDisplays(delay);
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L);
    }

    private void updateDisplays(Delay delay) {
        if (delay.delayRemaining() == 0) {
            endDelay(delay);
            reset(delay, false);
            return;
        }

        Player player = delay.player();

        int delayRemaining = delay.delayRemaining();
        Component text = delay.text().replaceText(builder ->
                builder.matchLiteral("%s").replacement(String.valueOf(delayRemaining))
        );

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
            player.resetTitle();

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