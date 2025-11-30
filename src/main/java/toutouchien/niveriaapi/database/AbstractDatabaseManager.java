package toutouchien.niveriaapi.database;

import com.google.common.base.Preconditions;
import com.mongodb.MongoException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import toutouchien.niveriaapi.database.exception.DefaultDocumentGenerationException;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Abstract base class for managing MongoDB database operations.
 * <p>
 * Provides synchronous and asynchronous methods for CRUD operations
 * on documents within specified collections, with support for default
 * document creation and dot-notation key access.
 */
public class AbstractDatabaseManager {
    private final Plugin plugin;
    private final Logger logger;
    private final BukkitScheduler scheduler;
    private final MongoDatabase mongoDatabase;
    private final Map<String, Supplier<Document>> defaultDocuments;
    private final Map<String, MongoCollection<Document>> collectionCache;

    /**
     * Creates a new database manager bound to a specific plugin and MongoDB database.
     *
     * @param plugin        plugin used for scheduler and logging
     * @param mongoDatabase MongoDB database instance
     */
    public AbstractDatabaseManager(@NotNull Plugin plugin, @NotNull MongoDatabase mongoDatabase) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");
        Preconditions.checkNotNull(mongoDatabase, "mongoDatabase cannot be null");

        this.plugin = plugin;
        this.logger = plugin.getSLF4JLogger();
        this.scheduler = plugin.getServer().getScheduler();
        this.mongoDatabase = mongoDatabase;
        this.defaultDocuments = new ConcurrentHashMap<>();
        this.collectionCache = new ConcurrentHashMap<>();

        logger.info("Successfully connected to database '{}'.", mongoDatabase.getName());
    }

    /**
     * Registers a default document supplier for a collection.
     * <p>
     * The supplier is used when creating default documents for that collection.
     *
     * @param collection              collection name
     * @param defaultDocumentSupplier supplier creating a new default document
     */
    public void registerDefault(@NotNull String collection, @NotNull Supplier<Document> defaultDocumentSupplier) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(defaultDocumentSupplier, "defaultDocumentSupplier cannot be null");

        this.defaultDocuments.put(collection, defaultDocumentSupplier);

        this.logger.info("Registered default document supplier for database '{}', collection '{}'.",
                mongoDatabase.getName(), collection);
    }

    // --- Synchronous Methods ---


    /**
     * Retrieves a value from a document in the given collection by id and key.
     * <p>
     * The key supports dot-notation for nested documents.
     *
     * @param collection collection name
     * @param id         document id ({@code _id})
     * @param key        dot-separated key path
     * @param <T>        expected type of the value
     * @return the value, or {@code null} if not found or type mismatch
     */
    @Nullable
    public <T> T get(@NotNull String collection, @NotNull String id, @NotNull String key) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return this.getOrDefault(collection, id, key, null);
    }

    /**
     * Retrieves a value from an already loaded document using a dot-separated key.
     *
     * @param document document to read
     * @param key      dot-separated key path
     * @param <T>      expected type of the value
     * @return the value, or {@code null} if not found or type mismatch
     */
    @Nullable
    public <T> T get(@NotNull Document document, @NotNull String key) {
        Preconditions.checkNotNull(document, "document cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return this.getOrDefault(document, key, null);
    }

    /**
     * Retrieves a value from a document with a default fallback.
     *
     * @param collection   collection name
     * @param id           document id
     * @param key          dot-separated key path
     * @param defaultValue value to return if missing or incompatible
     * @param <T>          expected type of the value
     * @return found value or {@code defaultValue}
     */
    @Nullable
    public <T> T getOrDefault(@NotNull String collection, @NotNull String id, @NotNull String key, @Nullable T defaultValue) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        Document document = this.document(collection, id);
        if (document == null)
            return defaultValue;

        return this.getOrDefault(document, key, defaultValue);
    }

    /**
     * Retrieves a value from a document using a dot-separated key, with default.
     *
     * @param document     document to read
     * @param key          dot-separated key path
     * @param defaultValue fallback value if missing or incompatible
     * @param <T>          expected type
     * @return found value or {@code defaultValue}
     */
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> T getOrDefault(@NotNull Document document, @NotNull String key, @Nullable T defaultValue) {
        Preconditions.checkNotNull(document, "document cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        if (key.isBlank())
            return defaultValue;

        String[] keyParts = key.split("\\.");
        Document current = document;

        String part;
        for (int i = 0; i < keyParts.length; i++) {
            part = keyParts[i];
            if (!current.containsKey(part))
                break;

            Object valueObj = current.get(part);
            if (part.equals(keyParts[keyParts.length - 1]))
                try {
                    return (T) valueObj;
                } catch (ClassCastException e) {
                    this.logger.warn("Value found for key '{}' but has unexpected type: Expected compatible type for T, got {}",
                            key, valueObj.getClass().getName(), e);
                    return defaultValue;
                }

            if (!(valueObj instanceof Document newCurrent))
                break;

            current = newCurrent;
        }

        return defaultValue;
    }

    /**
     * Sets a value on a document identified by collection and id.
     * <p>
     * Performs a read-modify-write cycle. If the document does not exist,
     * the call is ignored and a warning is logged.
     *
     * @param collection collection name
     * @param id         document id
     * @param key        dot-separated key path
     * @param value      value to set (may be {@code null})
     * @param <T>        value type
     */
    public <T> void set(@NotNull String collection, @NotNull String id, @NotNull String key, @Nullable T value) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        Document document = this.document(collection, id);
        if (document == null) {
            this.logger.warn("Attempted to set value for non-existent document: database='{}', collection='{}', id='{}'",
                    mongoDatabase.getName(), collection, id);
            return;
        }

        this.set(collection, id, document, key, value);
    }

    /**
     * Sets a value on a pre-loaded document and persists it.
     *
     * @param collection collection name
     * @param id         document id
     * @param document   existing document instance
     * @param key        dot-separated key path
     * @param value      value to set
     * @param <T>        value type
     */
    public <T> void set(@NotNull String collection, @NotNull String id, @NotNull Document document, @NotNull String key, @Nullable T value) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(document, "document cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        if (!setValueInDocument(document, key, value, collection, id))
            return;

        this.document(collection, id, document);
    }

    /**
     * Removes a value at a given key path from a document and persists the change.
     *
     * @param collection collection name
     * @param id         document id
     * @param key        dot-separated key path
     */
    public void remove(@NotNull String collection, @NotNull String id, @NotNull String key) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        Document document = this.document(collection, id);
        if (document == null) {
            this.logger.warn("Attempted to remove value for non-existent document: database='{}', collection='{}', id='{}'",
                    mongoDatabase.getName(), collection, id);
            return;
        }

        if (key.isBlank()) {
            this.logger.warn("Attempted to remove value with blank key: database='{}', collection='{}', id='{}'",
                    mongoDatabase.getName(), collection, id);
            return;
        }

        if (!removeValueInDocument(document, key, collection, id))
            return;

        this.document(collection, id, document);
    }


    /**
     * Deletes an entire document by id from the given collection.
     *
     * @param collection collection name
     * @param id         document id
     */
    public void remove(@NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        MongoCollection<Document> mongoCollection = this.collection(collection);
        Bson filter = Filters.eq("_id", id);
        mongoCollection.deleteOne(filter);
    }

    /**
     * Replaces the entire document in the collection with the provided one.
     * <p>
     * Ensures the {@code _id} field in the document matches the given {@code id}.
     * Logs warnings if the document is not found or the operation is not acknowledged.
     *
     * @param collection collection name
     * @param id         document id
     * @param document   new document to store
     */
    public void document(@NotNull String collection, @NotNull String id, @NotNull Document document) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(document, "document cannot be null");

        try {
            MongoCollection<Document> mongoCollection = this.collection(collection);
            Bson filter = Filters.eq("_id", id);
            if (!document.containsKey("_id") || !document.get("_id").equals(id)) {
                document.put("_id", id);
                logger.warn("Document for replace did not contain matching _id. Added '_id: {}' for C: {}, ID: {}", id, collection, id);
            }

            UpdateResult result = mongoCollection.replaceOne(filter, document);
            if (result.getMatchedCount() == 0) {
                this.logger.warn("ReplaceOne operation did not find document: database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id);
                return;
            }

            if (!result.wasAcknowledged()) {
                this.logger.warn("ReplaceOne operation was not acknowledged by the server for database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id);
            }
        } catch (MongoException e) {
            this.logger.error("Failed to replace document: database='{}', collection='{}', id='{}'",
                    mongoDatabase.getName(), collection, id, e);
        }
    }

    /**
     * Fetches a document by id from the given collection.
     *
     * @param collection collection name
     * @param id         document id
     * @return document or {@code null} if it does not exist
     */
    @Nullable
    public Document document(@NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        MongoCollection<Document> mongoCollection = this.collection(collection);
        Bson filter = Filters.eq("_id", id);
        return mongoCollection.find(filter).first();
    }

    /**
     * Returns an existing document or creates and inserts a default one if missing.
     *
     * @param collection collection name
     * @param id         document id
     * @return existing or newly created default document
     * @throws IllegalStateException if no default supplier is registered
     */
    @NotNull
    public Document documentOrDefault(@NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        Document document = this.document(collection, id);
        if (document == null)
            document = createDefaultDocument(collection, id, null);

        return document;
    }

    /**
     * Creates and inserts a default document using the registered supplier.
     *
     * @param collection collection name
     * @param id         document id to use
     * @param consumer   optional modifier for the document before insertion
     * @return inserted document
     * @throws IllegalStateException if no default supplier exists for the collection
     */
    @NotNull
    public Document createDefaultDocument(@NotNull String collection, @NotNull String id, @Nullable Consumer<Document> consumer) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        MongoCollection<Document> mongoCollection = this.collection(collection);
        Supplier<Document> supplier = this.defaultDocuments.get(collection);
        if (supplier == null) {
            this.logger.error("No default document supplier registered for database '{}', collection '{}'. Cannot create default document for id '{}'.",
                    mongoDatabase.getName(), collection, id);
            throw new IllegalStateException("No default document registered for " + collection);
        }

        Document defaultDocument = supplier.get();
        defaultDocument.append("_id", id);

        if (consumer != null)
            consumer.accept(defaultDocument);

        mongoCollection.insertOne(defaultDocument);
        return defaultDocument;
    }


    /**
     * Checks whether a document with the given id exists in the collection.
     *
     * @param collection collection name
     * @param id         document id
     * @return {@code true} if the document exists, {@code false} otherwise
     */
    public boolean documentExists(@NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        MongoCollection<Document> mongoCollection = this.collection(collection);
        return mongoCollection.find(Filters.eq("_id", id)).first() != null;
    }

    // --- Asynchronous Methods ---


    /**
     * Asynchronously retrieves a value by collection, id and key.
     *
     * @param collection collection name
     * @param id         document id
     * @param key        dot-separated key path
     * @param <T>        expected type
     * @return future completed with the value or {@code null}
     */
    @NotNull
    public <T> CompletableFuture<T> getAsync(@NotNull String collection, @NotNull String id, @NotNull String key) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return this.getOrDefaultAsync(collection, id, key, null);
    }


    /**
     * Asynchronously (immediately) returns a value from an already loaded document.
     *
     * @param document document to read from
     * @param key      dot-separated key path
     * @param <T>      expected type
     * @return completed future with the value or {@code null}
     */
    @NotNull
    public <T> CompletableFuture<T> getAsync(@NotNull Document document, @NotNull String key) {
        Preconditions.checkNotNull(document, "document cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return CompletableFuture.completedFuture(this.getOrDefault(document, key, null));
    }


    /**
     * Asynchronously retrieves a value with a default fallback.
     *
     * @param collection   collection name
     * @param id           document id
     * @param key          dot-separated key path
     * @param defaultValue fallback value
     * @param <T>          expected type
     * @return future with value or {@code defaultValue}
     */
    @NotNull
    public <T> CompletableFuture<T> getOrDefaultAsync(@NotNull String collection, @NotNull String id, @NotNull String key, @Nullable T defaultValue) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return documentAsync(collection, id).thenApply(document -> {
            if (document == null)
                return defaultValue;

            return this.getOrDefault(document, key, defaultValue);
        });
    }

    /**
     * Asynchronously sets a value on a document using a dot-separated key.
     *
     * @param collection collection name
     * @param id         document id
     * @param key        dot-separated key path
     * @param value      value to set
     * @param <T>        value type
     * @return future completed with {@code true} on success, {@code false}
     * if the document does not exist or key is invalid
     */
    @NotNull
    public <T> CompletableFuture<Boolean> setAsync(@NotNull String collection, @NotNull String id, @NotNull String key, @Nullable T value) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return documentAsync(collection, id).thenCompose(document -> {
            if (document == null) {
                this.logger.warn("Attempted to set value for non-existent document (async): database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id);
                return CompletableFuture.completedFuture(false);
            }

            if (!setValueInDocument(document, key, value, collection, id))
                return CompletableFuture.completedFuture(false);

            return documentAsync(collection, id, document);
        });
    }

    /**
     * Asynchronously removes a value at a given key path from a document.
     *
     * @param collection collection name
     * @param id         document id
     * @param key        dot-separated key path
     * @return future completed with {@code true} if successfully updated,
     * {@code false} on validation error or missing document
     */
    @NotNull
    public CompletableFuture<Boolean> removeAsync(@NotNull String collection, @NotNull String id, @NotNull String key) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return documentAsync(collection, id).thenCompose(document -> {
            if (document == null) {
                this.logger.warn("Attempted to remove value for non-existent document (async): database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id);
                return CompletableFuture.completedFuture(false);
            }

            if (key.isBlank()) {
                this.logger.warn("Attempted to remove value with blank key (async): database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id);
                return CompletableFuture.completedFuture(false);
            }

            if (!removeValueInDocument(document, key, collection, id))
                return CompletableFuture.completedFuture(false);

            return documentAsync(collection, id, document);
        });
    }

    /**
     * Asynchronously deletes an entire document by id.
     *
     * @param collection collection name
     * @param id         document id
     * @return future completed with {@code true} if the delete was attempted
     * successfully, or exceptionally on Mongo errors
     */
    @NotNull
    public CompletableFuture<Boolean> removeAsync(@NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                MongoCollection<Document> mongoCollection = this.collection(collection);
                Bson filter = Filters.eq("_id", id);
                mongoCollection.deleteOne(filter);
                future.complete(true);
            } catch (MongoException e) {
                this.logger.error("Failed to remove document asynchronously: database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id, e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Asynchronously replaces the entire document in the collection.
     *
     * @param collection collection name
     * @param id         document id
     * @param document   new document contents
     * @return future completed with {@code true} if the replace was acknowledged
     * and a document was matched, {@code false} otherwise
     */
    @NotNull
    public CompletableFuture<Boolean> documentAsync(@NotNull String collection, @NotNull String id, @NotNull Document document) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(document, "document cannot be null");

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                MongoCollection<Document> mongoCollection = this.collection(collection);
                Bson filter = Filters.eq("_id", id);
                if (!document.containsKey("_id") || !document.get("_id").equals(id)) {
                    document.put("_id", id);
                    logger.warn("Document for async replace did not contain matching _id. Added '_id: {}' for C: {}, ID: {}", id, collection, id);
                }

                UpdateResult result = mongoCollection.replaceOne(filter, document);
                boolean success = result.wasAcknowledged() && result.getMatchedCount() > 0;
                if (!success) {
                    if (result.getMatchedCount() == 0) {
                        this.logger.warn("Async ReplaceOne operation did not find document: database='{}', collection='{}', id='{}'",
                                mongoDatabase.getName(), collection, id);
                    } else if (!result.wasAcknowledged()) {
                        this.logger.warn("Async ReplaceOne operation was not acknowledged by the server for database='{}', collection='{}', id='{}'",
                                mongoDatabase.getName(), collection, id);
                    }
                }

                future.complete(success);
            } catch (MongoException e) {
                this.logger.error("Failed to replace document asynchronously: database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id, e);
                future.completeExceptionally(e);
            } catch (Exception e) {
                this.logger.error("Unexpected error during async replace document: database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id, e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Asynchronously fetches a document by id from the given collection.
     *
     * @param collection collection name
     * @param id         document id
     * @return future completed with the document or {@code null} on not found;
     * future may complete exceptionally on unexpected errors
     */
    @NotNull
    public CompletableFuture<Document> documentAsync(@NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        CompletableFuture<Document> future = new CompletableFuture<>();
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                MongoCollection<Document> mongoCollection = this.collection(collection);
                Bson filter = Filters.eq("_id", id);
                Document result = mongoCollection.find(filter).first();
                future.complete(result);
            } catch (MongoException e) {
                this.logger.error("Failed to get document asynchronously: database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id, e);

                future.complete(null);
            } catch (Exception e) {
                this.logger.error("Unexpected error during async get document: database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id, e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Asynchronously returns an existing document or creates a default one
     * if it does not exist.
     *
     * @param collection collection name
     * @param id         document id
     * @return future completed with existing or newly created document
     */
    @NotNull
    public CompletableFuture<Document> documentOrDefaultAsync(@NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        return documentAsync(collection, id).thenCompose(document ->
                document != null ? CompletableFuture.completedFuture(document)
                        : createDefaultDocumentAsync(collection, id, null));
    }


    /**
     * Asynchronously creates and inserts a default document using the registered
     * supplier, with an optional modifier.
     * <p>
     * Handles duplicate key errors by attempting to fetch the existing document.
     *
     * @param collection collection name
     * @param id         document id
     * @param modifier   optional modifier run before insertion
     * @return future completed with the inserted or existing document
     */
    @NotNull
    public CompletableFuture<Document> createDefaultDocumentAsync(@NotNull String collection, @NotNull String id, @Nullable Consumer<Document> modifier) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        CompletableFuture<Document> future = new CompletableFuture<>();
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                Document defaultDocument = generateDefaultDocumentStructure(collection, id, modifier);
                insertDocumentAndCompleteFuture(future, defaultDocument, collection, id);
            } catch (DefaultDocumentGenerationException e) {
                future.completeExceptionally(e.getCause() != null ? e.getCause() : e);
            } catch (Exception e) {
                this.logger.error("Unexpected error during async create document orchestration: database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id, e);
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    /**
     * Asynchronously checks whether a document with the given id exists.
     *
     * @param collection collection name
     * @param id         document id
     * @return future completed with {@code true} if the document exists,
     * {@code false} on not found or Mongo error; may complete exceptionally
     * on unexpected errors
     */
    @NotNull
    public CompletableFuture<Boolean> documentExistsAsync(@NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        CompletableFuture<Boolean> future = new CompletableFuture<>();
        scheduler.runTaskAsynchronously(plugin, () -> {
            try {
                MongoCollection<Document> mongoCollection = this.collection(collection);
                Bson filter = Filters.eq("_id", id);
                boolean exists = mongoCollection.find(filter).projection(new Document("_id", 1)).first() != null;
                future.complete(exists);
            } catch (MongoException e) {
                this.logger.error("Failed check document existence asynchronously: database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id, e);
                future.complete(false);
            } catch (Exception e) {
                this.logger.error("Unexpected error during async document exists check: database='{}', collection='{}', id='{}'",
                        mongoDatabase.getName(), collection, id, e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    /**
     * Returns (and caches) a {@link MongoCollection} for the given name.
     *
     * @param collection collection name
     * @return cached or newly resolved collection
     */
    @Nullable
    public MongoCollection<Document> collection(@NotNull String collection) {
        Preconditions.checkNotNull(collection, "collection cannot be null");

        return this.collectionCache.computeIfAbsent(collection, name -> {
            this.logger.info("Caching MongoCollection for: {} in database {}", name, mongoDatabase.getName());
            return this.mongoDatabase.getCollection(name);
        });
    }

    // --- Helper Methods ---

    /**
     * Sets a nested value in a document using a dot-separated key, creating
     * intermediate {@link Document}s as needed.
     *
     * @param document   target document
     * @param key        dot-separated key path
     * @param value      value to set
     * @param collection collection name (for logging)
     * @param id         document id (for logging)
     * @param <T>        value type
     * @return {@code true} if the value structure was updated, {@code false}
     * on validation or type errors
     */
    private <T> boolean setValueInDocument(@NotNull Document document, @NotNull String key, @Nullable T value, @NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(document, "document cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        if (key.isBlank()) {
            this.logger.warn("Attempted to set value with blank key: database='{}', collection='{}', id='{}'",
                    mongoDatabase.getName(), collection, id);
            return false;
        }

        String[] keyParts = key.split("\\.");
        Document current = document;

        try {
            for (int i = 0; i < keyParts.length - 1; i++) {
                String part = keyParts[i];
                Object next = current.computeIfAbsent(part, k -> new Document());

                if (!(next instanceof Document newCurrent)) {
                    this.logger.error("Cannot set nested value for key '{}'. Intermediate part '{}' exists but is not a Document (type: {}): database='{}', collection='{}', id='{}'",
                            key, part, next.getClass().getName(), mongoDatabase.getName(), collection, id);
                    return false;
                }

                current = newCurrent;
            }

            current.put(keyParts[keyParts.length - 1], value);
            return true;
        } catch (Exception e) {
            this.logger.error("Error while setting value for key '{}' in document structure: database='{}', collection='{}', id='{}'",
                    key, mongoDatabase.getName(), collection, id, e);
            return false;
        }
    }

    /**
     * Generates a default document structure using the registered supplier and
     * optional modifier.
     *
     * @param collection collection name
     * @param id         document id
     * @param modifier   optional modifier for the created document
     * @return generated document with {@code _id} set
     * @throws DefaultDocumentGenerationException if supplier is missing or fails
     */
    @NotNull
    private Document generateDefaultDocumentStructure(@NotNull String collection, @NotNull String id, @Nullable Consumer<Document> modifier) throws DefaultDocumentGenerationException {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        Supplier<Document> supplier = this.defaultDocuments.get(collection);
        if (supplier == null) {
            this.logger.error("No default document supplier registered for database '{}', collection '{}'. Cannot create default document for id '{}'.",
                    mongoDatabase.getName(), collection, id);
            throw new DefaultDocumentGenerationException("No default document registered for collection '" + collection + "'", new IllegalStateException());
        }

        try {
            Document defaultDocument = supplier.get();
            if (defaultDocument == null)
                throw new IllegalStateException("Default document supplier for collection '" + collection + "' returned null.");

            defaultDocument.put("_id", id);
            if (modifier != null) {
                modifier.accept(defaultDocument);
                defaultDocument.put("_id", id);
            }

            return defaultDocument;
        } catch (Exception e) {
            throw new DefaultDocumentGenerationException("Failed to generate default document structure for database='%s', collection='%s', id='%s'".formatted(mongoDatabase.getName(), collection, id), e);
        }
    }

    /**
     * Inserts a document into the collection and completes the given future.
     * <p>
     * Handles duplicate key errors by attempting to retrieve the existing document.
     *
     * @param future           future to complete
     * @param documentToInsert document to insert
     * @param collection       collection name
     * @param id               document id
     */
    private void insertDocumentAndCompleteFuture(@NotNull CompletableFuture<Document> future, @NotNull Document documentToInsert, @NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(future, "future cannot be null");
        Preconditions.checkNotNull(documentToInsert, "documentToInsert cannot be null");
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");

        try {
            MongoCollection<Document> mongoCollection = this.collection(collection);
            mongoCollection.insertOne(documentToInsert);
            this.logger.info("Created default document asynchronously: database='{}', collection='{}', id='{}'",
                    mongoDatabase.getName(), collection, id);
            future.complete(documentToInsert);

        } catch (MongoException e) {
            if (e.getCode() == 11000) {
                handleDuplicateKeyCompletion(future, collection, id, e);
                return;
            }

            this.logger.error("Failed to insert default document asynchronously (non-duplicate MongoException): database='{}', collection='{}', id='{}'",
                    mongoDatabase.getName(), collection, id, e);
            future.completeExceptionally(new RuntimeException("Failed to insert default document for collection '" + collection + "', id '" + id + "'", e));
        } catch (Exception e) {
            this.logger.error("Unexpected error during document insertion phase: database='{}', collection='{}', id='{}'",
                    mongoDatabase.getName(), collection, id, e);
            future.completeExceptionally(e);
        }
    }

    /**
     * Handles a duplicate key error by fetching and returning the existing document.
     *
     * @param future                future to complete
     * @param collection            collection name
     * @param id                    document id
     * @param duplicateKeyException the original Mongo duplicate key exception
     */
    private void handleDuplicateKeyCompletion(@NotNull CompletableFuture<Document> future, @NotNull String collection, @NotNull String id, @NotNull MongoException duplicateKeyException) {
        Preconditions.checkNotNull(future, "future cannot be null");
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(duplicateKeyException, "duplicateKeyException cannot be null");

        logger.warn("Document insertion failed due to duplicate key (likely race condition), attempting to fetch existing for C: {}, ID: {}", collection, id);

        documentAsync(collection, id)
                .thenAccept(existingDoc -> {
                    if (existingDoc != null) {
                        future.complete(existingDoc);
                        return;
                    }

                    logger.error("Duplicate key on insert but failed to fetch existing document for C: {}, ID: {}", collection, id);
                    future.completeExceptionally(new RuntimeException("Duplicate key on insert but failed to fetch existing doc", duplicateKeyException));
                })
                .exceptionally(fetchErr -> {
                    logger.error("Error fetching document after duplicate key error for C: {}, ID: {}", collection, id, fetchErr);
                    future.completeExceptionally(duplicateKeyException);
                    return null;
                });
    }

    /**
     * Removes a nested value from a document using a dot-separated key.
     *
     * @param document   document to modify
     * @param key        dot-separated key path
     * @param collection collection name (for logging)
     * @param id         document id (for logging)
     * @return {@code true} if the value was removed, {@code false} on errors
     */
    private boolean removeValueInDocument(@NotNull Document document, @NotNull String key, @NotNull String collection, @NotNull String id) {
        Preconditions.checkNotNull(collection, "collection cannot be null");
        Preconditions.checkNotNull(id, "id cannot be null");
        Preconditions.checkNotNull(document, "document cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        if (key.isBlank()) {
            this.logger.warn("Attempted to remove value with blank key: database='{}', collection='{}', id='{}'",
                    mongoDatabase.getName(), collection, id);
            return false;
        }

        String[] keyParts = key.split("\\.");
        Document current = document;

        try {
            for (int i = 0; i < keyParts.length - 1; i++) {
                String part = keyParts[i];
                Object next = current.computeIfAbsent(part, k -> new Document());

                if (!(next instanceof Document newCurrent)) {
                    this.logger.error("Cannot remove nested value for key '{}'. Intermediate part '{}' exists but is not a Document (type: {}): database='{}', collection='{}', id='{}'",
                            key, part, next.getClass().getName(), mongoDatabase.getName(), collection, id);
                    return false;
                }

                current = newCurrent;
            }

            current.remove(keyParts[keyParts.length - 1]);
            return true;
        } catch (Exception e) {
            this.logger.error("Error while removing value for key '{}' in document structure: database='{}', collection='{}', id='{}'",
                    key, mongoDatabase.getName(), collection, id, e);
            return false;
        }
    }
}
