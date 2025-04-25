package toutouchien.niveriaapi.database;

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

public class AbstractDatabaseManager {
    private final Plugin plugin;
    private final Logger logger;
    private final BukkitScheduler scheduler;
    private final MongoDatabase mongoDatabase;
    private final Map<String, Supplier<Document>> defaultDocuments;
    private final Map<String, MongoCollection<Document>> collectionCache;

    public AbstractDatabaseManager(@NotNull Plugin plugin, @NotNull MongoDatabase mongoDatabase) {
        this.plugin = plugin;
        this.logger = plugin.getSLF4JLogger();
        this.scheduler = plugin.getServer().getScheduler();
        this.mongoDatabase = mongoDatabase;
        this.defaultDocuments = new ConcurrentHashMap<>();
        this.collectionCache = new ConcurrentHashMap<>();

        logger.info("Successfully connected to database '{}'.", mongoDatabase.getName());
    }

    public void registerDefault(@NotNull String collection, @NotNull Supplier<Document> defaultDocumentSupplier) {
        this.defaultDocuments.put(collection, defaultDocumentSupplier);

         this.logger.info("Registered default document supplier for database '{}', collection '{}'.",
                 mongoDatabase.getName(), collection);
    }

    // --- Synchronous Methods ---

    @Nullable
    public <T> T get(@NotNull String collection, @NotNull String id, @NotNull String key) {
        return this.getOrDefault(collection, id, key, null);
    }

    @Nullable
    public <T> T get(@NotNull Document document, @NotNull String key) {
        return this.getOrDefault(document, key, null);
    }

    @Nullable
    public <T> T getOrDefault(@NotNull String collection, @NotNull String id, @NotNull String key, @Nullable T defaultValue) {
        Document document = this.document(collection, id);
        if (document == null)
            return defaultValue;

        return this.getOrDefault(document, key, defaultValue);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getOrDefault(@NotNull Document document, @NotNull String key, @Nullable T defaultValue) {
        if(key.isBlank())
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

    public <T> void set(@NotNull String collection, @NotNull String id, @NotNull String key, @Nullable T value) {
        Document document = this.document(collection, id);
        if (document == null) {
            this.logger.warn("Attempted to set value for non-existent document: database='{}', collection='{}', id='{}'",
                    mongoDatabase.getName(), collection, id);
            return;
        }

        this.set(collection, id, document, key, value);
    }

    public <T> void set(@NotNull String collection, @NotNull String id, @NotNull Document document, @NotNull String key, @Nullable T value) {
        if (!setValueInDocument(document, key, value, collection, id))
            return;

        this.document(collection, id, document);
    }

    public void remove(@NotNull String collection, @NotNull String id, @NotNull String key) {
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

    public void remove(@NotNull String collection, @NotNull String id) {
        MongoCollection<Document> mongoCollection = this.collection(collection);
        Bson filter = Filters.eq("_id", id);
        mongoCollection.deleteOne(filter);
    }

    public void document(@NotNull String collection, @NotNull String id, @NotNull Document document) {
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

    @Nullable
    public Document document(@NotNull String collection, @NotNull String id) {
        MongoCollection<Document> mongoCollection = this.collection(collection);
        Bson filter = Filters.eq("_id", id);
        return mongoCollection.find(filter).first();
    }

    @NotNull
    public Document documentOrDefault(@NotNull String collection, @NotNull String id) {
        Document document = this.document(collection, id);
        if (document == null)
            document = createDefaultDocument(collection, id, null);

        return document;
    }

    @NotNull
    public Document createDefaultDocument(@NotNull String collection, @NotNull String id, @Nullable Consumer<Document> consumer) {
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

    public boolean documentExists(@NotNull String collection, @NotNull String id) {
        MongoCollection<Document> mongoCollection = this.collection(collection);
        return mongoCollection.find(Filters.eq("_id", id)).first() != null;
    }

    // --- Asynchronous Methods ---

    public <T> CompletableFuture<T> getAsync(@NotNull String collection, @NotNull String id, @NotNull String key) {
        return this.getOrDefaultAsync(collection, id, key, null);
    }

    public <T> CompletableFuture<T> getAsync(@NotNull Document document, @NotNull String key) {
        return CompletableFuture.completedFuture(this.getOrDefault(document, key, null));
    }

    public <T> CompletableFuture<T> getOrDefaultAsync(@NotNull String collection, @NotNull String id, @NotNull String key, @Nullable T defaultValue) {
        return documentAsync(collection, id).thenApply(document -> {
            if (document == null)
                return defaultValue;

            return this.getOrDefault(document, key, defaultValue);
        });
    }

    public <T> CompletableFuture<Boolean> setAsync(@NotNull String collection, @NotNull String id, @NotNull String key, @Nullable T value) {
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

    public CompletableFuture<Boolean> removeAsync(@NotNull String collection, @NotNull String id, @NotNull String key) {
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

    public CompletableFuture<Boolean> removeAsync(@NotNull String collection, @NotNull String id) {
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

    public CompletableFuture<Boolean> documentAsync(@NotNull String collection, @NotNull String id, @NotNull Document document) {
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

    public CompletableFuture<Document> documentAsync(@NotNull String collection, @NotNull String id) {
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

    @NotNull
    public CompletableFuture<Document> documentOrDefaultAsync(@NotNull String collection, @NotNull String id) {
        return documentAsync(collection, id).thenCompose(document ->
                document != null ? CompletableFuture.completedFuture(document)
                        : createDefaultDocumentAsync(collection, id, null));
    }

    @NotNull
    public CompletableFuture<Document> createDefaultDocumentAsync(@NotNull String collection, @NotNull String id, @Nullable Consumer<Document> modifier) {
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

    public CompletableFuture<Boolean> documentExistsAsync(@NotNull String collection, @NotNull String id) {
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

    public MongoCollection<Document> collection(@NotNull String collection) {
        return this.collectionCache.computeIfAbsent(collection, name -> {
            this.logger.info("Caching MongoCollection for: {} in database {}", name, mongoDatabase.getName());
            return this.mongoDatabase.getCollection(name);
        });
    }

    // --- Helper Methods ---

    private <T> boolean setValueInDocument(@NotNull Document document, @NotNull String key, @Nullable T value, @NotNull String collection, @NotNull String id) {
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

    private Document generateDefaultDocumentStructure(@NotNull String collection, @NotNull String id, @Nullable Consumer<Document> modifier) throws DefaultDocumentGenerationException {
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

    private void insertDocumentAndCompleteFuture(@NotNull CompletableFuture<Document> future, @NotNull Document documentToInsert, @NotNull String collection, @NotNull String id) {
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

    private void handleDuplicateKeyCompletion(@NotNull CompletableFuture<Document> future, @NotNull String collection, @NotNull String id, @NotNull MongoException duplicateKeyException) {
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

    private boolean removeValueInDocument(@NotNull Document document, @NotNull String key, @NotNull String collection, @NotNull String id) {
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
