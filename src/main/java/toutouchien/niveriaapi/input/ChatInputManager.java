package toutouchien.niveriaapi.input;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Manages player chat input requests and routes responses to the appropriate handlers.
 * When a player has an active input request, their next chat message will be
 * intercepted and processed by the registered consumer.
 */
public class ChatInputManager implements Listener {
    private final Map<UUID, Consumer<String>> inputRequests = new ConcurrentHashMap<>();

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        if (!inputRequests.containsKey(playerId)) {
            return;
        }

        event.setCancelled(true);

        String input = PlainTextComponentSerializer.plainText().serialize(event.message());

        Consumer<String> consumer = inputRequests.remove(playerId);
        consumer.accept(input);
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        cleanupRequest(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        cleanupRequest(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        cleanupRequest(event.getPlayer());
    }
    
    /**
     * Registers a new input request for a player. The provided consumer will be
     * called with the player's next chat message.
     *
     * @param player The player to request input from
     * @param action The consumer to process the player's input
     */
    public void requestInput(Player player, Consumer<String> action) {
        inputRequests.put(player.getUniqueId(), action);
    }
    
    /**
     * Cancels an existing input request for a player.
     *
     * @param player The player whose input request should be canceled
     * @return true if a request was canceled, false otherwise
     */
    public boolean cancelRequest(Player player) {
        return inputRequests.remove(player.getUniqueId()) != null;
    }
    
    /**
     * Checks if a player has an active input request.
     *
     * @param player The player to check
     * @return true if the player has an active input request, false otherwise
     */
    public boolean hasActiveRequest(Player player) {
        return inputRequests.containsKey(player.getUniqueId());
    }
    
    /**
     * Removes any active input request for the given player.
     *
     * @param player The player whose input request should be removed
     */
    private void cleanupRequest(Player player) {
        inputRequests.remove(player.getUniqueId());
    }
}
