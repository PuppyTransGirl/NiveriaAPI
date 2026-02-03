package toutouchien.niveriaapi.lang;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.*;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import toutouchien.niveriaapi.NiveriaAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Advanced, plugin-independent language and localization system with Caffeine caching and MiniMessage placeholders.
 * <p>
 * <b>Key Features:</b>
 * <ul>
 *     <li><b>Instance-based:</b> Each plugin has its own Lang instance</li>
 *     <li><b>Caffeine cache:</b> High-performance, auto-evicting component cache</li>
 *     <li><b>MiniMessage placeholders:</b> Named placeholders instead of positional arguments</li>
 *     <li><b>Lazy loading:</b> Locales loaded on-demand</li>
 *     <li><b>Centralized config:</b> Reads locale settings from NiveriaAPI config.yml</li>
 *     <li><b>Flexible configuration:</b> Builder pattern with sensible defaults</li>
 *     <li><b>Custom tag resolvers:</b> Extensible tag system per plugin</li>
 *     <li><b>Better error handling:</b> Graceful degradation with detailed logging</li>
 * </ul>
 * <p>
 * <b>NiveriaAPI config.yml settings:</b>
 * <pre>
 * lang: en_US              # Default server locale
 * use_player_locale: true  # Use player's client locale
 * </pre>
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * // In your plugin's onEnable():
 * this.lang = Lang.builder(this)
 *     .addDefaultLanguageFiles("en_US.yml", "fr_FR.yml")
 *     .build();
 *
 * // Language file (en_US.yml):
 * welcome:
 *   message: "<green>Welcome <niveriaapi_player_name>! There are <niveriaapi_player_count> players online."
 *   join: "<gray>[<green>+<gray>] <niveriaapi_player_name>"
 *
 * // Send messages with named placeholders:
 * lang.sendMessage(player, "welcome.message",
 *     Placeholder.parsed("niveriaapi_player_name", player.getName()),
 *     Placeholder.parsed("niveriaapi_player_count", String.valueOf(Bukkit.getOnlinePlayers().size()))
 * );
 *
 * // Or use helper methods:
 * lang.sendMessage(player, "welcome.join",
 *     Lang.placeholder("niveriaapi_player_name", player.getName())
 * );
 *
 * // Get a component:
 * Component msg = lang.get(player, "error.not_found",
 *     Lang.placeholder("niveriaapi_item", "Diamond Sword")
 * );
 * }</pre>
 */
@NullMarked
public class Lang {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final MiniMessage miniMessage;

    private Locale defaultLocale = Locale.US;
    private boolean usePlayerLocale = false;

    private final boolean cacheComponents;
    private final MissingKeyBehavior missingKeyBehavior;
    private final ObjectSet<String> defaultLanguageFiles;
    private final String langDirectory;

    private final Object2ObjectMap<Locale, Object2ObjectMap<String, String>> messages;
    private final Object2ObjectMap<Locale, Object2ObjectMap<String, Object2ObjectMap<String, String>>> specialTags;
    @Nullable
    private final Cache<LangCacheKey, Component> componentCache;
    private final ObjectSet<Locale> loadedLocales;
    private final Object2ObjectMap<String, TagResolver> customTagResolvers;

    /**
     * Private constructor - use {@link LangBuilder} instead.
     */
    Lang(LangBuilder builder) {
        this.plugin = builder.plugin;
        this.logger = builder.logger != null ? builder.logger : plugin.getSLF4JLogger();
        this.miniMessage = MiniMessage.miniMessage();

        this.cacheComponents = builder.cacheComponents;
        this.missingKeyBehavior = builder.missingKeyBehavior;
        this.defaultLanguageFiles = new ObjectOpenHashSet<>(builder.defaultLanguageFiles);
        this.langDirectory = builder.langDirectory;

        this.messages = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
        this.specialTags = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
        this.componentCache = this.cacheComponents ? buildCaffeineCache(builder) : null;
        this.loadedLocales = ObjectSets.synchronize(new ObjectOpenHashSet<>());
        this.customTagResolvers = new Object2ObjectOpenHashMap<>(builder.customTagResolvers);

        this.initialize();
    }

    /**
     * Builds a Caffeine cache with the specified configuration.
     *
     * @param builder The builder containing cache configuration
     * @return Configured Caffeine cache
     */
    private static Cache<LangCacheKey, Component> buildCaffeineCache(LangBuilder builder) {
        Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder()
                .maximumSize(builder.maxCacheSize);

        if (builder.cacheExpireAfterAccess != null)
            cacheBuilder.expireAfterAccess(builder.cacheExpireAfterAccess);

        if (builder.cacheExpireAfterWrite != null)
            cacheBuilder.expireAfterWrite(builder.cacheExpireAfterWrite);

        if (builder.recordStats)
            cacheBuilder.recordStats();

        return cacheBuilder.build();
    }

    /**
     * Creates a new builder for configuring a Lang instance.
     *
     * @param plugin The plugin that owns this Lang instance
     * @return A new builder
     */
    public static LangBuilder builder(JavaPlugin plugin) {
        return new LangBuilder(plugin);
    }

    // ========== Placeholder Helper Methods ==========

    /**
     * Creates a parsed placeholder (value will be parsed for MiniMessage tags).
     *
     * @param key   The placeholder key (e.g., "niveriaapi_player_name")
     * @param value The value to replace with
     * @return TagResolver for this placeholder
     */
    @SuppressWarnings("PatternValidation")
    public static TagResolver placeholder(String key, String value) {
        return Placeholder.parsed(key, value);
    }

    /**
     * Creates an unparsed placeholder (value will NOT be parsed for MiniMessage tags).
     *
     * @param key   The placeholder key (e.g., "niveriaapi_player_name")
     * @param value The value to replace with
     * @return TagResolver for this placeholder
     */
    @SuppressWarnings("PatternValidation")
    public static TagResolver unparsedPlaceholder(String key, String value) {
        return Placeholder.unparsed(key, value);
    }

    /**
     * Creates a component placeholder.
     *
     * @param key       The placeholder key
     * @param component The component to replace with
     * @return TagResolver for this placeholder
     */
    @SuppressWarnings("PatternValidation")
    public static TagResolver componentPlaceholder(String key, Component component) {
        return Placeholder.component(key, component);
    }

    /**
     * Creates a number placeholder from a numeric value.
     *
     * @param key    The placeholder key
     * @param number The number value
     * @return TagResolver for this placeholder
     */
    @SuppressWarnings("PatternValidation")
    public static TagResolver numberPlaceholder(String key, Number number) {
        return Placeholder.unparsed(key, String.valueOf(number));
    }

    /**
     * Initializes the language system by extracting default files and loading the default locale.
     */
    @SuppressWarnings("java:S2629")
    private void initialize() {
        FileConfiguration config = NiveriaAPI.instance().getConfig();
        String langCode = config.getString("lang", LangUtils.DEFAULT_LANG_CODE);
        this.defaultLocale = Locale.forLanguageTag(langCode.replace('_', '-'));
        this.usePlayerLocale = config.getBoolean("use_player_locale", false);

        saveDefaultLanguageFiles();
        ensureLocaleLoaded(defaultLocale);

        logger.info("Initialized Lang system for {} with default locale: {} (use_player_locale: {}, cache: {})",
                plugin.getName(),
                defaultLocale.toLanguageTag(),
                usePlayerLocale,
                cacheComponents ? "enabled" : "disabled");
    }

    /**
     * Saves default language files from the plugin JAR to the lang directory.
     */
    private void saveDefaultLanguageFiles() {
        if (defaultLanguageFiles.isEmpty())
            return;

        File langFolder = new File(plugin.getDataFolder(), langDirectory);

        if (!langFolder.exists() && !langFolder.mkdirs()) {
            logger.warn("Could not create lang directory: {}", langFolder.getAbsolutePath());
            return;
        }

        for (String fileName : defaultLanguageFiles) {
            File langFile = new File(langFolder, fileName);
            if (langFile.exists())
                continue;

            try {
                String resourcePath = langDirectory + "/" + fileName;

                // Check if resource exists before attempting to save
                try (InputStream is = plugin.getResource(resourcePath)) {
                    if (is == null) {
                        logger.warn("Default language file not found in JAR: {}", resourcePath);
                        continue;
                    }

                    plugin.saveResource(resourcePath, false);
                    logger.debug("Extracted default language file: {}", fileName);
                }
            } catch (IOException e) {
                logger.error("Failed to check/extract language file: {}", fileName, e);
            }
        }
    }

    /**
     * Ensures a locale is loaded. If not already loaded, loads it from disk.
     * Thread-safe and idempotent.
     *
     * @param locale The locale to ensure is loaded
     */
    private synchronized void ensureLocaleLoaded(Locale locale) {
        if (loadedLocales.contains(locale))
            return;

        loadLocaleFile(locale);
        loadedLocales.add(locale);
    }

    /**
     * Loads a single locale file from disk.
     *
     * @param locale The locale to load
     */
    @SuppressWarnings("java:S2629")
    private void loadLocaleFile(Locale locale) {
        String fileName = normalizeLocaleToFileName(locale);
        File langFile = new File(plugin.getDataFolder(), langDirectory + "/" + fileName);

        if (!langFile.exists()) {
            logger.debug("Language file not found for locale {}: {}", locale.toLanguageTag(), fileName);
            return;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(langFile);

            // Load messages
            Object2ObjectMap<String, String> localeMessages = new Object2ObjectOpenHashMap<>();
            flattenConfiguration(config, "", localeMessages);

            if (!localeMessages.isEmpty()) {
                messages.put(locale, localeMessages);
                logger.info("Loaded {} messages for locale {} from {}",
                        localeMessages.size(), locale.toLanguageTag(), fileName);
            }

            // Load special tags
            loadSpecialTags(locale, config);

        } catch (Exception e) {
            logger.error("Failed to load language file: {}", langFile.getName(), e);
        }
    }

    /**
     * Normalizes a Locale to a filename (e.g., en-US -> en_US.yml).
     * Handles region and script subtags.
     *
     * @param locale The locale
     * @return The filename
     */
    private String normalizeLocaleToFileName(Locale locale) {
        return locale.toLanguageTag().replace('-', '_') + ".yml";
    }

    /**
     * Recursively flattens a YAML configuration into dot-separated keys.
     *
     * @param section The configuration section to flatten
     * @param prefix  Current key prefix
     * @param output  Map to store flattened keys
     */
    private void flattenConfiguration(ConfigurationSection section,
                                      String prefix,
                                      Object2ObjectMap<String, String> output) {
        for (String key : section.getKeys(false)) {
            String fullKey = prefix.isEmpty() ? key : prefix + "." + key;

            if (section.isString(key)) {
                String value = section.getString(key);
                if (value != null)
                    output.put(fullKey, value);
            } else if (section.isConfigurationSection(key)) {
                ConfigurationSection child = section.getConfigurationSection(key);
                if (child != null)
                    flattenConfiguration(child, fullKey, output);
            }
        }
    }

    /**
     * Loads special tag definitions for a locale.
     *
     * @param locale The locale
     * @param config The configuration file
     */
    @SuppressWarnings("java:S2629")
    private void loadSpecialTags(Locale locale, FileConfiguration config) {
        ConfigurationSection specialSection = config.getConfigurationSection("special-tags");
        if (specialSection == null)
            return;

        Object2ObjectMap<String, Object2ObjectMap<String, String>> tagsByCategory = new Object2ObjectOpenHashMap<>();

        for (String category : specialSection.getKeys(false)) {
            ConfigurationSection categorySection = specialSection.getConfigurationSection(category);
            if (categorySection == null)
                continue;

            Object2ObjectMap<String, String> categoryTags = new Object2ObjectOpenHashMap<>();
            for (String tagKey : categorySection.getKeys(false)) {
                String pattern = categorySection.getString(tagKey);
                if (pattern != null)
                    categoryTags.put(tagKey, pattern);
            }

            if (!categoryTags.isEmpty())
                tagsByCategory.put(category, categoryTags);
        }

        if (!tagsByCategory.isEmpty()) {
            specialTags.put(locale, tagsByCategory);
            logger.debug("Loaded {} special tag categories for locale {}",
                    tagsByCategory.size(), locale.toLanguageTag());
        }
    }

    /**
     * Resolves the appropriate locale for an audience.
     *
     * @param audience The audience (may be null)
     * @return The resolved locale
     */
    private Locale resolveLocale(@Nullable Audience audience) {
        if (usePlayerLocale && audience instanceof Player player) {
            Locale playerLocale = player.locale();
            ensureLocaleLoaded(playerLocale);
            return playerLocale;
        }

        return defaultLocale;
    }

    /**
     * Gets a raw message string for a key.
     *
     * @param locale The locale
     * @param key    The message key
     * @return The raw message or fallback based on missingKeyBehavior
     */
    @SuppressWarnings("ConstantValue")
    private String rawMessage(Locale locale, String key) {
        // Try requested locale
        Object2ObjectMap<String, String> localeMessages = messages.get(locale);
        if (localeMessages != null && localeMessages.containsKey(key))
            return localeMessages.get(key);

        // Fallback to default locale
        if (!locale.equals(defaultLocale)) {
            localeMessages = messages.get(defaultLocale);
            if (localeMessages != null && localeMessages.containsKey(key))
                return localeMessages.get(key);
        }

        // Key not found - apply missing key behavior
        return handleMissingKey(key, locale);
    }

    /**
     * Handles missing key based on configured behavior.
     *
     * @param key    The missing key
     * @param locale The locale
     * @return The fallback string
     */
    @SuppressWarnings("java:S2629")
    private String handleMissingKey(String key, Locale locale) {
        String result = switch (missingKeyBehavior) {
            case RETURN_KEY -> key;
            case RETURN_PLACEHOLDER -> "!" + key;
            case RETURN_EMPTY -> "";
            case LOG_WARNING -> {
                logger.warn("Missing translation key '{}' for locale {}", key, locale.toLanguageTag());
                yield key;
            }
        };

        logger.debug("Message key not found: {} (locale: {})", key, locale.toLanguageTag());
        return result;
    }

    /**
     * Creates tag resolvers for a locale, including special tags and custom placeholders.
     *
     * @param locale       The locale
     * @param placeholders Additional placeholders to include
     * @return Array of tag resolvers
     */
    private TagResolver[] createTagResolvers(Locale locale, TagResolver... placeholders) {
        Object2ObjectMap<String, Object2ObjectMap<String, String>> localeTags =
                specialTags.getOrDefault(locale, Object2ObjectMaps.emptyMap());

        ObjectList<TagResolver> resolvers = new ObjectArrayList<>();

        // Prefix resolver
        Object2ObjectMap<String, String> prefixMap = localeTags.getOrDefault("prefix", Object2ObjectMaps.emptyMap());
        if (!prefixMap.isEmpty()) {
            resolvers.add(TagResolver.resolver("prefix", (args, ctx) -> {
                String id = args.popOr("prefix id required").value();
                String pattern = prefixMap.getOrDefault(id, "");
                return Tag.inserting(miniMessage.deserialize(pattern));
            }));
        }

        // Color resolver
        Object2ObjectMap<String, String> colorMap = localeTags.getOrDefault("ncolor", Object2ObjectMaps.emptyMap());
        if (!colorMap.isEmpty()) {
            resolvers.add(TagResolver.resolver("ncolor", (args, ctx) -> {
                String id = args.popOr("color id required").value();
                String hex = colorMap.get(id);
                TextColor color = TextColor.fromHexString(hex);
                return Tag.styling(builder -> builder.color(color));
            }));
        }

        // Other tags (separator, etc.)
        Object2ObjectMap<String, String> otherMap = localeTags.getOrDefault("other", Object2ObjectMaps.emptyMap());
        if (otherMap.containsKey("separator")) {
            Component separator = miniMessage.deserialize(otherMap.get("separator"));
            resolvers.add(Placeholder.component("separator", separator));
        }

        // Custom tag resolvers from the builder
        resolvers.addAll(customTagResolvers.values());

        // User-provided placeholders
        if (placeholders.length > 0)
            resolvers.addAll(ObjectArrayList.of(placeholders));

        return resolvers.toArray(new TagResolver[0]);
    }

    /**
     * Parses a message string into a Component with placeholders.
     *
     * @param locale       The locale
     * @param message      The message string
     * @param key          The message key (for error logging)
     * @param placeholders Placeholder resolvers
     * @return The parsed component
     */
    private Component parseComponent(Locale locale, String message,
                                     String key, TagResolver... placeholders) {
        try {
            TagResolver[] resolvers = createTagResolvers(locale, placeholders);
            return miniMessage.deserialize(message, resolvers);
        } catch (ParsingException e) {
            logger.error("Failed to parse MiniMessage for key '{}' (locale: {}): {}",
                    key, locale.toLanguageTag(), message, e);
            // Return the raw message as text rather than the key to show actual content
            return Component.text(message);
        }
    }

    /**
     * Gets or creates a cached component using Caffeine cache.
     *
     * @param cacheKey The cache key
     * @param supplier The component supplier if not cached
     * @return The component
     */
    private Component getOrCacheComponent(LangCacheKey cacheKey, Supplier<Component> supplier) {
        if (componentCache == null)
            return supplier.get();

        return componentCache.get(cacheKey, k -> supplier.get());
    }

    // ========== Public API ==========

    /**
     * Gets a raw message string (without MiniMessage parsing).
     *
     * @param key The message key
     * @return The raw message string
     */
    public String getString(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");
        return rawMessage(defaultLocale, key);
    }

    /**
     * Gets a raw message string for an audience (without MiniMessage parsing).
     *
     * @param audience The audience
     * @param key      The message key
     * @return The raw message string
     */
    public String getString(Audience audience, String key) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        Locale locale = resolveLocale(audience);
        return rawMessage(locale, key);
    }

    /**
     * Gets a message as a Component.
     *
     * @param key The message key
     * @return The component
     */
    public Component get(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        LangCacheKey cacheKey = new LangCacheKey(defaultLocale, key, ObjectLists.emptyList());
        return getOrCacheComponent(cacheKey, () -> {
            String raw = rawMessage(defaultLocale, key);
            return parseComponent(defaultLocale, raw, key);
        });
    }

    /**
     * Gets a message as a Component with placeholders.
     * <p>
     * Example:
     * <pre>{@code
     * Component msg = lang.get("welcome.message",
     *     Lang.placeholder("niveriaapi_player_name", player.getName()),
     *     Lang.numberPlaceholder("niveriaapi_player_count", playerCount)
     * );
     * }</pre>
     *
     * @param key          The message key
     * @param placeholders TagResolvers for placeholders
     * @return The component
     */
    public Component get(String key, TagResolver... placeholders) {
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(placeholders, "placeholders cannot be null");

        // Don't cache with placeholders as they can vary
        String raw = rawMessage(defaultLocale, key);
        return parseComponent(defaultLocale, raw, key, placeholders);
    }

    /**
     * Gets a message as a Component for an audience.
     *
     * @param audience The audience
     * @param key      The message key
     * @return The component
     */
    public Component get(Audience audience, String key) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        Locale locale = resolveLocale(audience);
        LangCacheKey cacheKey = new LangCacheKey(locale, key, ObjectLists.emptyList());

        return getOrCacheComponent(cacheKey, () -> {
            String raw = rawMessage(locale, key);
            return parseComponent(locale, raw, key);
        });
    }

    /**
     * Gets a message as a Component for an audience with placeholders.
     * <p>
     * Example:
     * <pre>{@code
     * Component msg = lang.get(player, "welcome.message",
     *     Lang.placeholder("niveriaapi_player_name", player.getName()),
     *     Lang.numberPlaceholder("niveriaapi_player_count", playerCount)
     * );
     * }</pre>
     *
     * @param audience     The audience
     * @param key          The message key
     * @param placeholders TagResolvers for placeholders
     * @return The component
     */
    public Component get(Audience audience, String key, TagResolver... placeholders) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(placeholders, "placeholders cannot be null");

        Locale locale = resolveLocale(audience);
        String raw = rawMessage(locale, key);
        return parseComponent(locale, raw, key, placeholders);
    }

    /**
     * Gets a message as a list of Components (for multi-line messages like lore).
     *
     * @param key The message key
     * @return List of components
     */
    public ObjectList<Component> getList(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        String raw = rawMessage(defaultLocale, key);
        return splitAndParse(defaultLocale, raw, key);
    }

    /**
     * Gets a message as a list of Components with placeholders.
     *
     * @param key          The message key
     * @param placeholders TagResolvers for placeholders
     * @return List of components
     */
    public ObjectList<Component> getList(String key, TagResolver... placeholders) {
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(placeholders, "placeholders cannot be null");

        String raw = rawMessage(defaultLocale, key);
        return splitAndParse(defaultLocale, raw, key, placeholders);
    }

    /**
     * Gets a message as a list of Components for an audience.
     *
     * @param audience The audience
     * @param key      The message key
     * @return List of components
     */
    public ObjectList<Component> getList(Audience audience, String key) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        Locale locale = resolveLocale(audience);
        String raw = rawMessage(locale, key);
        return splitAndParse(locale, raw, key);
    }

    /**
     * Gets a message as a list of Components for an audience with placeholders.
     *
     * @param audience     The audience
     * @param key          The message key
     * @param placeholders TagResolvers for placeholders
     * @return List of components
     */
    public ObjectList<Component> getList(Audience audience, String key, TagResolver... placeholders) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(placeholders, "placeholders cannot be null");

        Locale locale = resolveLocale(audience);
        String raw = rawMessage(locale, key);
        return splitAndParse(locale, raw, key, placeholders);
    }

    /**
     * Splits a multi-line message and parses each line.
     *
     * @param locale       The locale
     * @param message      The message
     * @param key          The message key
     * @param placeholders Placeholder resolvers
     * @return List of components
     */
    private ObjectList<Component> splitAndParse(Locale locale, String message,
                                                String key, TagResolver... placeholders) {
        if (message.isEmpty())
            return ObjectLists.emptyList();

        String[] lines = LangUtils.NEWLINE_PATTERN.split(message, -1);
        ObjectList<Component> components = new ObjectArrayList<>(lines.length);

        for (String line : lines) {
            components.add(parseComponent(locale, line, key, placeholders));
        }

        return components;
    }

    /**
     * Sends a message to an audience.
     *
     * @param audience The audience
     * @param key      The message key
     */
    public void sendMessage(Audience audience, String key) {
        sendMessage(audience, null, key);
    }

    /**
     * Sends a message with placeholders to an audience.
     *
     * @param audience     The audience
     * @param key          The message key
     * @param placeholders TagResolvers for placeholders
     */
    public void sendMessage(Audience audience, String key, TagResolver... placeholders) {
        sendMessage(audience, null, key, placeholders);
    }

    /**
     * Sends a message with sound to an audience.
     *
     * @param audience The audience
     * @param sound    The sound (nullable)
     * @param key      The message key
     */
    public void sendMessage(Audience audience, @Nullable Sound sound, String key) {
        sendMessage(audience, sound, key, new TagResolver[0]);
    }

    /**
     * Sends a message with sound and placeholders to an audience.
     * <p>
     * Sound Format: {@code <sound_key>;<source>;<volume>;<pitch>}
     * <br>Example: {@code minecraft:entity.ender_dragon.death;MASTER;1.0;1.0}
     * <br>Lenient parsing: Accepts 1-4 parts with defaults (MASTER, 1.0, 1.0)
     * <p>
     * Example usage:
     * <pre>{@code
     * lang.sendMessage(player, "welcome.message",
     *     Lang.placeholder("niveriaapi_player_name", player.getName()),
     *     Lang.numberPlaceholder("niveriaapi_player_count", playerCount)
     * );
     * }</pre>
     *
     * @param audience     The audience
     * @param sound        The sound (nullable - will try to load from key_sound if null)
     * @param key          The message key
     * @param placeholders TagResolvers for placeholders
     */
    public void sendMessage(Audience audience, @Nullable Sound sound,
                            String key, TagResolver... placeholders) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        Component message = placeholders.length == 0 ? get(audience, key) : get(audience, key, placeholders);

        // Check if message is empty
        if (message == Component.empty())
            return;

        audience.sendMessage(message);

        // Handle sound
        if (sound != null) {
            audience.playSound(sound, Sound.Emitter.self());
            return;
        }

        // Try to load sound from language file
        String soundKey = key + LangUtils.SOUND_SUFFIX;

        if (!hasKey(soundKey))
            return; // No sound defined

        String soundString = getString(audience, soundKey);
        if (soundString.isEmpty())
            return;

        sendSound(audience, soundString, soundKey);
    }

    @SuppressWarnings("PatternValidation")
    private void sendSound(Audience audience, String soundString, String soundKey) {
        try {
            String[] parts = soundString.split(";");

            // Lenient parsing: accept 1-4 parts with defaults
            String soundKeyStr = parts[0].trim();
            Sound.Source source = Sound.Source.MASTER;
            float volume = 1.0f;
            float pitch = 1.0f;

            if (parts.length >= 2) {
                try {
                    source = Sound.Source.valueOf(parts[1].trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid sound source '{}' for key '{}', using MASTER", parts[1].trim(), soundKey);
                }
            }

            if (parts.length >= 3) {
                try {
                    volume = Float.parseFloat(parts[2].trim());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid volume '{}' for key '{}', using 1.0", parts[2].trim(), soundKey);
                }
            }

            if (parts.length >= 4) {
                try {
                    pitch = Float.parseFloat(parts[3].trim());
                } catch (NumberFormatException e) {
                    logger.warn("Invalid pitch '{}' for key '{}', using 1.0", parts[3].trim(), soundKey);
                }
            }

            Key soundKeyParsed = Key.key(soundKeyStr);
            Sound parsedSound = Sound.sound(soundKeyParsed, source, volume, pitch);
            audience.playSound(parsedSound, Sound.Emitter.self());

        } catch (Exception e) {
            logger.error("Failed to parse sound for key '{}': {}", soundKey, soundString, e);
        }
    }

    /**
     * Checks if a key exists in any loaded locale.
     *
     * @param key The message key
     * @return True if the key exists
     */
    public boolean hasKey(String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        synchronized (messages) {
            for (Object2ObjectMap<String, String> localeMessages : messages.values())
                if (localeMessages.containsKey(key))
                    return true;

            return false;
        }
    }

    /**
     * Reloads all language files and configuration from disk.
     * Clears all caches and re-reads NiveriaAPI config.
     */
    public void reload() {
        logger.info("Reloading language files for {}", plugin.getName());

        synchronized (this) {
            messages.clear();
            specialTags.clear();
            loadedLocales.clear();

            if (componentCache != null)
                componentCache.invalidateAll();

            initialize();
        }
    }

    /**
     * Gets cache statistics.
     *
     * @return Cache statistics string
     */
    public String cacheStats() {
        int totalMessages;
        synchronized (messages) {
            totalMessages = messages.values().stream()
                    .mapToInt(Object2ObjectMap::size)
                    .sum();
        }

        if (componentCache == null)
            return "Lang Stats [%s] - Locales: %d, Messages: %d, Component Cache: disabled".formatted(
                    plugin.getName(),
                    loadedLocales.size(),
                    totalMessages
            );

        CacheStats stats = componentCache.stats();

        return "Lang Stats [%s] - Locales: %d, Messages: %d, Cache: size=%d, hits=%d, misses=%d, hitRate=%.2f%%".formatted(
                plugin.getName(),
                loadedLocales.size(),
                totalMessages,
                componentCache.estimatedSize(),
                stats.hitCount(),
                stats.missCount(),
                stats.hitRate() * 100D
        );
    }

    /**
     * Gets the current default locale (from NiveriaAPI config).
     *
     * @return The default locale
     */
    public Locale defaultLocale() {
        return defaultLocale;
    }

    /**
     * Gets whether player locales are used (from NiveriaAPI config).
     *
     * @return True if player locales are used
     */
    public boolean usePlayerLocale() {
        return usePlayerLocale;
    }

    /**
     * Gets all currently loaded locales.
     *
     * @return Unmodifiable set of loaded locales
     */
    public ObjectSet<Locale> loadedLocales() {
        return ObjectSets.unmodifiable(loadedLocales);
    }
}