package toutouchien.niveriaapi.database;

import com.google.common.base.Preconditions;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manager class for handling MongoDB connections and operations.
 */
public class MongoManager {
    private final MongoClient mongoClient;
    private final List<MongoDatabase> databaseCache;

    /**
     * Constructs a MongoManager with the specified connection string.
     *
     * @param connectionString The MongoDB connection string.
     */
    public MongoManager(@NotNull String connectionString) {
        Preconditions.checkNotNull(connectionString, "connectionString cannot be null");

        this.mongoClient = MongoClients.create(connectionString);
        this.databaseCache = new ArrayList<>();
    }

    /**
     * Retrieves a MongoDatabase instance for the specified database name.
     *
     * @param databaseName The name of the database.
     * @return The MongoDatabase instance.
     */
    @NotNull
    public MongoDatabase database(@NotNull String databaseName) {
        Preconditions.checkNotNull(databaseName, "databaseName cannot be null");

        MongoDatabase mongoDatabase = this.mongoClient.getDatabase(databaseName);
        this.databaseCache.add(mongoDatabase);
        return mongoDatabase;
    }

    /**
     * Pings all cached databases and returns their response times.
     *
     * @return A map of database names to their ping times in nanoseconds.
     */
    @NotNull
    public Map<String, Long> ping() {
        Map<String, Long> pings = new HashMap<>();
        for (MongoDatabase mongoDatabase : this.databaseCache) {
            long startPing = System.nanoTime();
            mongoDatabase.runCommand(new Document("ping", 1));
            long ping = System.nanoTime() - startPing;

            pings.put(mongoDatabase.getName(), ping);
        }

        return pings;
    }

    /**
     * Shuts down the MongoDB client and releases resources.
     */
    public void shutdown() {
        this.mongoClient.close();
    }
}
