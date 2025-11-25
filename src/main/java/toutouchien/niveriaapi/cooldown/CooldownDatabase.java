package toutouchien.niveriaapi.cooldown;

import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.database.impl.NiveriaDatabaseManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CooldownDatabase {
    private static final String PLAYERS = "players";
    private static final String EXPIRATION_TIME = "expirationTime";

    private final NiveriaDatabaseManager database;
    private final Logger logger;

    public CooldownDatabase(NiveriaDatabaseManager database, Logger logger) {
        this.database = database;
        this.logger = logger;
    }

    public void saveCooldown(@NotNull Cooldown cooldown) {
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

    public void deleteCooldown(@NotNull UUID uuid, @NotNull Key key) {
        this.database.documentAsync(PLAYERS, uuid.toString()).thenComposeAsync(document -> {
            if (document == null)
                return null;

            return this.database.removeAsync(PLAYERS, uuid.toString(), "cooldowns." + key.asString())
                    .thenApply(success -> null);
        });
    }

    @NotNull
    public List<Cooldown> loadAllCooldowns() {
        if (NiveriaAPI.isUnitTestVersion())
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

    public void deleteAllCooldowns(@NotNull UUID uuid) {
        this.database.documentAsync(PLAYERS, uuid.toString()).thenComposeAsync(document -> {
            if (document == null)
                return null;

            return this.database.removeAsync(PLAYERS, uuid.toString(), "cooldowns")
                    .thenApply(success -> null);
        });
    }
}
