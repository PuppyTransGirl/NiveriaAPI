package toutouchien.niveriaapi.lang;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.time.Duration;
import java.util.Collections;

@NullMarked
public class LangBuilder {
    final JavaPlugin plugin;
    final ObjectList<String> defaultLanguageFiles = new ObjectArrayList<>();
    final Object2ObjectMap<String, TagResolver> customTagResolvers = new Object2ObjectOpenHashMap<>();
    @Nullable Logger logger;
    boolean cacheComponents = true;
    int maxCacheSize = LangUtils.DEFAULT_MAX_CACHE;
    @Nullable Duration cacheExpireAfterAccess = LangUtils.DEFAULT_CACHE_EXPIRE;
    @Nullable Duration cacheExpireAfterWrite = null;
    boolean recordStats = false;
    MissingKeyBehavior missingKeyBehavior = MissingKeyBehavior.RETURN_KEY;
    String langDirectory = "lang";

    LangBuilder(JavaPlugin plugin) {
        this.plugin = Preconditions.checkNotNull(plugin, "plugin cannot be null");
    }

    @Contract(value = "_ -> this", mutates = "this")
    public LangBuilder logger(Logger logger) {
        Preconditions.checkNotNull(logger, "logger cannot be null");

        this.logger = logger;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public LangBuilder cacheComponents(boolean cache) {
        this.cacheComponents = cache;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public LangBuilder maxCacheSize(int size) {
        Preconditions.checkArgument(size > 0, "size must be positive");

        this.maxCacheSize = size;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public LangBuilder cacheExpireAfterAccess(@Nullable Duration duration) {
        this.cacheExpireAfterAccess = duration;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public LangBuilder cacheExpireAfterWrite(@Nullable Duration duration) {
        this.cacheExpireAfterWrite = duration;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public LangBuilder recordStats(boolean recordStats) {
        this.recordStats = recordStats;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public LangBuilder missingKeyBehavior(MissingKeyBehavior behavior) {
        Preconditions.checkNotNull(behavior, "behavior cannot be null");

        this.missingKeyBehavior = behavior;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public LangBuilder langDirectory(String directory) {
        Preconditions.checkNotNull(directory, "directory cannot be null");

        this.langDirectory = directory;
        return this;
    }

    @Contract(value = "_ -> this", mutates = "this")
    public LangBuilder addDefaultLanguageFiles(String... files) {
        Preconditions.checkNotNull(files, "files cannot be null");

        Collections.addAll(defaultLanguageFiles, files);
        return this;
    }

    @Contract(value = "_, _ -> this", mutates = "this")
    public LangBuilder addTagResolver(String name, TagResolver resolver) {
        Preconditions.checkNotNull(name, "name cannot be null");
        Preconditions.checkNotNull(resolver, "resolver cannot be null");

        this.customTagResolvers.put(name, resolver);
        return this;
    }

    public Lang build() {
        return new Lang(this);
    }
}