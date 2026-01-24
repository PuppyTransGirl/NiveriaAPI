package toutouchien.niveriaapi.updatechecker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import toutouchien.niveriaapi.lang.Lang;

import java.util.Locale;

public class UpdateCheckerListener implements Listener {
    private final boolean noNewVersion;
    private final JavaPlugin plugin;
    private final String langKey;
    private final String currentVersion;
    private final String latestVersion;

    public UpdateCheckerListener(boolean noNewVersion, JavaPlugin plugin, String langKey, String currentVersion, String latestVersion) {
        this.noNewVersion = noNewVersion;
        this.plugin = plugin;
        this.langKey = langKey;
        this.currentVersion = currentVersion;
        this.latestVersion = latestVersion;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (this.noNewVersion)
            return;

        if (!this.plugin.getConfig().getBoolean("update-checker.on-join", true))
            return;

        Player player = event.getPlayer();
        String smallPluginName = this.plugin.getName().toLowerCase(Locale.ROOT);
        if (!player.hasPermission(smallPluginName + ".update-checker"))
            return;

        Lang.sendMessage(player, this.langKey, this.currentVersion, this.latestVersion);
    }
}
