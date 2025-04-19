package toutouchien.niveriaapi.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoManager {
    private final MongoClient mongoClient;
    private final List<MongoDatabase> databaseCache;

    public MongoManager(String connectionString) {
        this.mongoClient = MongoClients.create(connectionString);
        this.databaseCache = new ArrayList<>();
    }

    public MongoDatabase database(String databaseName) {
        MongoDatabase mongoDatabase = this.mongoClient.getDatabase(databaseName);
        this.databaseCache.add(mongoDatabase);
        return mongoDatabase;
    }

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

    public void shutdown() {
        this.mongoClient.close();
    }
}
