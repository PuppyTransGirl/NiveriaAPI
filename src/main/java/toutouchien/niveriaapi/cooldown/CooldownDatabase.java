package toutouchien.niveriaapi.cooldown;

import com.google.common.base.Preconditions;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bson.Document;
import org.jspecify.annotations.NullMarked;
import org.slf4j.Logger;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.database.impl.NiveriaDatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages the storage and retrieval of player cooldowns in the database.
 */
@NullMarked
public class CooldownDatabase {
    private static final String PLAYERS = "players";
    private static final String EXPIRATION_TIME = "expirationTime";

    private final NiveriaDatabaseManager database;
    private final Logger logger;

    /**
     * Constructs a new CooldownDatabase.
     *
     * @param database The NiveriaDatabaseManager instance for database operations.
     * @param logger   The Logger instance for logging.
     */
    public CooldownDatabase(NiveriaDatabaseManager database, Logger logger) {
        Preconditions.checkNotNull(database, "database cannot be null");
        Preconditions.checkNotNull(logger, "logger cannot be null");

        this.database = database;
        this.logger = logger;
    }

    /**
     * Saves a cooldown to the database.
     *
     * @param cooldown The Cooldown to save.
     */
    public void saveCooldown(Cooldown cooldown) {
        Preconditions.checkNotNull(cooldown, "cooldown cannot be null");

        this.database.documentOrDefaultAsync(PLAYERS, cooldown.uuid().toString()).thenComposeAsync(document -> {
            Document cooldownDocument = new Document(EXPIRATION_TIME, cooldown.expirationTime());
            return this.database.setAsync(PLAYERS, cooldown.uuid().toString(), "cooldowns." + cooldown.key().asString(), cooldownDocument)
                    .thenApply(success -> null);
        }).exceptionally(throwable -> {
            this.logger.error(
                    "Failed to save cooldown for player {} with key {}",
                    cooldown.uuid(), cooldown.key(),
                    throwable
            );

            return false;
        });
    }

    /**
     * Deletes a cooldown from the database.
     *
     * @param uuid The UUID of the player.
     * @param key  The Key of the cooldown to delete.
     */
    public void deleteCooldown(UUID uuid, Key key) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        this.database.documentAsync(PLAYERS, uuid.toString()).thenComposeAsync(document -> {
            if (document == null)
                return null;

            return this.database.removeAsync(PLAYERS, uuid.toString(), "cooldowns." + key.asString())
                    .thenApply(success -> null);
        });
    }

    /**
     * Loads all active cooldowns from the database.
     *
     * @return A list of all active Cooldowns.
     */
    @SuppressWarnings("PatternValidation")
    public List<Cooldown> loadAllCooldowns() {
        if (NiveriaAPI.isUnitTest())
            return new ArrayList<>();

        List<Cooldown> activeCooldowns = new ArrayList<>();
        this.database.collection(PLAYERS).find().forEach(document -> {
            UUID uuid = UUID.fromString(document.getString("_id"));
            Document cooldownsDocument = document.get("cooldowns", Document.class);

            if (cooldownsDocument == null)
                return;

            for (String key : cooldownsDocument.keySet()) {
                Document cooldownDocument = cooldownsDocument.get(key, Document.class);

                Key adventureKey;
                try {
                    adventureKey = Key.key(key);
                } catch (InvalidKeyException e) {
                    continue;
                }

                long expirationTime = cooldownDocument.getLong(EXPIRATION_TIME);
                activeCooldowns.add(new Cooldown(uuid, adventureKey, expirationTime, true));
            }
        });

        return activeCooldowns;
    }

    /**
     * Deletes all expired cooldowns from the database.
     */
    @SuppressWarnings("PatternValidation")
    public void deleteExpiredCooldowns() {
        this.database.collection(PLAYERS).find().forEach(document -> {
            UUID uuid = UUID.fromString(document.getString("_id"));
            Document cooldownsDocument = document.get("cooldowns", Document.class);

            if (cooldownsDocument == null)
                return;

            for (String key : cooldownsDocument.keySet()) {
                Document cooldownDocument = cooldownsDocument.get(key, Document.class);
                long expirationTime = cooldownDocument.getLong(EXPIRATION_TIME);

                if (System.currentTimeMillis() <= expirationTime)
                    continue;

                Key adventureKey;
                try {
                    adventureKey = Key.key(key);
                } catch (InvalidKeyException e) {
                    continue;
                }

                deleteCooldown(uuid, adventureKey);
            }
        });
    }

    /**
     * Deletes all cooldowns for a player from the database.
     *
     * @param uuid The UUID of the player.
     */
    public void deleteAllCooldowns(UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid cannot be null");

        this.database.documentAsync(PLAYERS, uuid.toString()).thenComposeAsync(document -> {
            if (document == null)
                return null;

            return this.database.removeAsync(PLAYERS, uuid.toString(), "cooldowns")
                    .thenApply(success -> null);
        }).exceptionally(throwable -> {
            this.logger.error(
                    "Failed to remove all cooldown for player {}",
                    uuid,
                    throwable
            );
            return false;
        });
    }
}
