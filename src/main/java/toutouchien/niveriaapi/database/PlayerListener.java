package toutouchien.niveriaapi.database;

import com.google.common.base.Preconditions;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.jspecify.annotations.NullMarked;
import toutouchien.niveriaapi.database.impl.NiveriaDatabaseManager;

/**
 * Listener for player pre-login events to manage player data in the database.
 */
@NullMarked
public class PlayerListener implements Listener {
    private final NiveriaDatabaseManager niveriaDatabase;

    /**
     * Constructs a PlayerListener with the specified NiveriaDatabaseManager.
     *
     * @param niveriaDatabase The NiveriaDatabaseManager instance.
     */
    public PlayerListener(NiveriaDatabaseManager niveriaDatabase) {
        Preconditions.checkNotNull(niveriaDatabase, "niveriaDatabase cannot be null");
        this.niveriaDatabase = niveriaDatabase;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED)
            return;

        String uuid = event.getUniqueId().toString();
        long currentTimeMillis = System.currentTimeMillis();

        this.niveriaDatabase.documentAsync("players", uuid).thenComposeAsync(document -> {
            if (document != null)
                return this.niveriaDatabase.setAsync("players", uuid, "lastJoin", currentTimeMillis);

            return this.niveriaDatabase.createDefaultDocumentAsync("players", uuid, newDocument -> {
                newDocument.put("ip", event.getAddress().getHostAddress());
                newDocument.put("firstJoin", currentTimeMillis);
                newDocument.put("lastJoin", currentTimeMillis);
            }).thenApply(createdDocument -> null);
        });
    }
}
