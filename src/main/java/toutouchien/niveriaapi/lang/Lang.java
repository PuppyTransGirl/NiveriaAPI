package toutouchien.niveriaapi.lang;

import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.NiveriaAPI;

import java.io.File;
import java.util.*;


/**
 * Central language and localization utility for NiveriaAPI.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Load and cache per-locale message files from {@code /lang/*.yml}.</li>
 *     <li>Resolve messages by key to raw strings or {@link Component}s.</li>
 *     <li>Support multi-line messages for lore (split by newlines into lists).</li>
 *     <li>Optionally use per-player locales (via {@link Player#locale()}) or a
 *         server-wide default locale configured in {@code config.yml}.</li>
 *     <li>Support MiniMessage-based formatting with custom per-locale tags
 *         (e.g. prefixes, named colors, separators).</li>
 *     <li>Provide convenience methods to send localized messages (optionally
 *         with sounds) directly to an {@link Audience}.</li>
 * </ul>
 * <p>
 * Usage:
 * <ol>
 *     <li>Call {@link #load(JavaPlugin)} once during plugin startup.</li>
 *     <li>Use {@link #get(String)} / {@link #get(Audience, String)} for
 *         components or {@link #getString(String)} for raw strings.</li>
 *     <li>Use {@link #getList(String)} for multi-line messages (lore).</li>
 *     <li>Use {@link #sendMessage(Audience, String, Object...)} to send
 *         localized messages directly.</li>
 * </ol>
 * <p>
 * This is a static utility class and cannot be instantiated.
 */
public class Lang {
    private static final Object2ObjectMap<Locale, Object2ObjectMap<String, String>> MESSAGES = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    // Locale → ( Category → ( Key → Pattern ) )
    private static final Object2ObjectMap<Locale, Object2ObjectMap<String, Object2ObjectMap<String, String>>> LOCALE_SPECIAL_TAGS = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>());
    private static final MiniMessage MM = MiniMessage.builder().strict(true).build();
    private static final String DEFAULT_LANG = "en_US";

    private static final String[] DEFAULT_MESSAGES_FILES = {
            "en_US.yml",
            "fr_FR.yml"
    };

    private static Locale defaultLocale = Locale.US;
    private static boolean usePlayerLocale;

    private Lang() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Initializes the language system by loading configuration and message files.
     *
     * @param plugin The main plugin instance.
     */
    public static void load(@NotNull JavaPlugin plugin) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        saveDefaultMessages(plugin);
        loadConfig();
        loadMessages(plugin);
    }

    /**
     * Loads language settings from NiveriaAPI's `config.yml`.
     * <p>
     * This method reads the `lang` key to set the server's default locale and
     * the `use_player_locale` key to determine if player-specific locales
     * should be used.
     */
    private static void loadConfig() {
        FileConfiguration config = NiveriaAPI.instance().getConfig();
        String langCode = config.getString("lang", DEFAULT_LANG);
        defaultLocale = Locale.forLanguageTag(langCode.replace('_', '-'));
        usePlayerLocale = config.getBoolean("use_player_locale", false);
    }

    /**
     * Loads the message files from the `lang` directory into the cache.
     * <p>
     * If `usePlayerLocale` is enabled in the config, this method will attempt to
     * load a file for every available system locale. Otherwise, it will only
     * load the file corresponding to the server's default locale.
     *
     * @param plugin The main plugin instance.
     */
    private static void loadMessages(@NotNull JavaPlugin plugin) {
        if (!usePlayerLocale)
            loadLocaleFile(plugin, defaultLocale);
        else
            for (Locale locale : Locale.getAvailableLocales())
                loadLocaleFile(plugin, locale);
    }

    /**
     * Loads a single locale file and stores its messages in the cache.
     * <p>
     * This method reads the specified locale's YAML file from the `lang`
     * directory, flattens any nested sections into dot-separated keys, and
     * stores the resulting key-value pairs in the `MESSAGES` map. It also
     * loads any special tag patterns defined in the file.
     * <p>
     * Multi-line strings (containing newlines) are preserved as-is.
     *
     * @param plugin The main plugin instance.
     * @param locale The locale to load messages for.
     */
    private static void loadLocaleFile(@NotNull JavaPlugin plugin, @NotNull Locale locale) {
        String fileName = "lang/%s.yml".formatted(locale.toLanguageTag().replace('-', '_'));
        File langFile = new File(plugin.getDataFolder(), fileName);
        if (!langFile.exists())
            return;

        Object2ObjectMap<String, String> messages = new Object2ObjectOpenHashMap<>();
        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Flatten nested sections into dot-separated keys (e.g. niveriaapi.fixcommands.single)
        for (String key : langConfig.getKeys(false)) {
            if (langConfig.isString(key)) {
                String value = langConfig.getString(key);
                if (value != null)
                    messages.put(key, value);

                continue;
            }

            ConfigurationSection section = langConfig.getConfigurationSection(key);
            if (section != null)
                flattenSection(section, key, messages);
        }

        if (!messages.isEmpty())
            MESSAGES.computeIfAbsent(locale, k -> new Object2ObjectOpenHashMap<>()).putAll(messages);

        loadSpecialTags(locale, langConfig);
    }

    /**
     * Recursively flattens a configuration section into dot-separated keys.
     * <p>
     * This helper method traverses the provided configuration section,
     * converting nested keys into a flat structure with dot notation. For
     * example, a section like:
     * <pre>
     * parent:
     *   child:
     *     key: value
     * </pre>
     * would result in a key-value pair of `parent.child.key` → `value`.
     * <p>
     * Multi-line strings are preserved with their newline characters.
     *
     * @param section  The configuration section to flatten.
     * @param prefix   The current key prefix for nested sections.
     * @param messages The map to store the flattened key-value pairs.
     */
    private static void flattenSection(@NotNull ConfigurationSection section, @NotNull String prefix, @NotNull Object2ObjectMap<String, String> messages) {
        for (String k : section.getKeys(false)) {
            String fullKey = prefix.isEmpty() ? k : prefix + "." + k;
            if (section.isString(k)) {
                String v = section.getString(k);
                if (v != null)
                    messages.put(fullKey, v);

                continue;
            }

            ConfigurationSection child = section.getConfigurationSection(k);
            if (child != null)
                flattenSection(child, fullKey, messages);
        }
    }

    /**
     * Loads special tag patterns for a given locale from the language configuration.
     * <p>
     * This method reads the `special-tags` section from the provided language configuration,
     * organizing tag patterns by category (e.g., prefix, ncolor, other). Each category contains
     * key-pattern pairs, which are stored in a nested map structure for fast lookup during
     * message formatting.
     *
     * @param locale     The locale for which to load special tags.
     * @param langConfig The language file configuration section.
     */
    private static void loadSpecialTags(@NotNull Locale locale, FileConfiguration langConfig) {
        ConfigurationSection special = langConfig.getConfigurationSection("special-tags");
        if (special == null)
            return;

        Object2ObjectMap<String, Object2ObjectMap<String, String>> byCategory = new Object2ObjectOpenHashMap<>();
        for (String category : special.getKeys(false)) {
            ConfigurationSection section = special.getConfigurationSection(category);
            if (section == null)
                continue;

            Object2ObjectMap<String, String> entries = new Object2ObjectOpenHashMap<>();
            for (String tagKey : section.getKeys(false)) {
                String pattern = section.getString(tagKey);
                if (pattern != null)
                    entries.put(tagKey, pattern);
            }

            byCategory.put(category, entries);
        }

        if (byCategory.isEmpty())
            return;

        if (!LOCALE_SPECIAL_TAGS.containsKey(locale)) {
            LOCALE_SPECIAL_TAGS.put(locale, byCategory);
            return;
        }

        Object2ObjectMap<String, Object2ObjectMap<String, String>> existing = LOCALE_SPECIAL_TAGS.get(locale);
        for (Map.Entry<String, Object2ObjectMap<String, String>> category : byCategory.entrySet()) {
            Object2ObjectMap<String, String> entries = category.getValue();
            existing.computeIfAbsent(category.getKey(), k -> new Object2ObjectOpenHashMap<>()).putAll(entries);
        }
    }

    /**
     * Saves the default language files bundled within the plugin's JAR to the
     * `plugins/PluginName/lang/` directory.
     * <p>
     * This operation only copies files that are not already present, preventing
     * user-modified translations from being overwritten on startup.
     *
     * @param plugin The main plugin instance.
     */
    private static void saveDefaultMessages(@NotNull JavaPlugin plugin) {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists() && !langFolder.mkdirs()) {
            NiveriaAPI.instance().getSLF4JLogger().warn("Could not create lang folder for plugin {}", plugin.getName());
            return;
        }

        for (String lang : DEFAULT_MESSAGES_FILES) {
            File langFile = new File(langFolder, lang);
            if (!langFile.exists())
                plugin.saveResource("lang/%s".formatted(lang), false);
        }
    }

    /**
     * The core internal method for retrieving and formatting a localized string.
     * <p>
     * It determines the appropriate locale based on the {@link Audience}
     * and the `usePlayerLocale` setting. It then fetches the message, falling
     * back to the default locale's message or the key itself if a translation is
     * not found. Finally, it formats the string with the provided arguments.
     *
     * @param audience The entity, used to determine the locale. Can be
     *                 {@code null} to force the default server locale.
     * @param key      The key of the message to retrieve.
     * @param args     Optional arguments to format into the message string.
     * @return The final, formatted message string.
     */
    @NotNull
    private static String getStringInternal(@Nullable Audience audience, @NotNull String key, @NotNull Object @NotNull ... args) {
        Locale locale = defaultLocale;
        if (usePlayerLocale && audience instanceof Player player)
            locale = player.locale();

        Object2ObjectMap<String, String> messagesOrEmptyMap = MESSAGES.getOrDefault(defaultLocale, Object2ObjectMaps.emptyMap());
        String message = MESSAGES.getOrDefault(locale, messagesOrEmptyMap).getOrDefault(key, key);
        return args.length > 0 ? message.formatted(args) : message;
    }

    /**
     * Gets a raw localized string for the given key using the server's default
     * locale.
     *
     * @param key The key of the message to retrieve.
     * @return The localized string, or the key itself if not found.
     */
    @NotNull
    public static String getString(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        return getStringInternal(null, key);
    }

    /**
     * Gets a formatted raw localized string for the given key using the server's
     * default locale.
     *
     * @param key  The key of the message to retrieve.
     * @param args The arguments to format into the message string.
     * @return The formatted localized string, or the key itself if not found.
     */
    @NotNull
    public static String getString(@NotNull String key, @NotNull Object @NotNull ... args) {
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(args, "args cannot be null");

        return getStringInternal(null, key, args);
    }

    /**
     * Gets a raw localized string for the given key, using the audience's locale
     * if enabled.
     *
     * @param audience The recipient of the message, used for locale detection.
     * @param key      The key of the message to retrieve.
     * @return The localized string, or the key itself if not found.
     */
    @NotNull
    public static String getString(@NotNull Audience audience, @NotNull String key) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        return getStringInternal(audience, key);
    }

    /**
     * Gets a formatted raw localized string for the given key, using the
     * audience's locale if enabled.
     *
     * @param audience The recipient of the message, used for locale detection.
     * @param key      The key of the message to retrieve.
     * @param args     The arguments to format into the message string.
     * @return The formatted localized string, or the key itself if not found.
     */
    @NotNull
    public static String getString(@NotNull Audience audience, @NotNull String key, @NotNull Object @NotNull ... args) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(args, "args cannot be null");

        return getStringInternal(audience, key, args);
    }

    /**
     * Parses a localized message string into a {@link Component} using MiniMessage,
     * applying custom tag resolvers for prefixes, named colors, and separators.
     * <p>
     * The method determines the locale to use (player's or default), retrieves
     * the special tag patterns for that locale, and sets up tag resolvers:
     * <ul>
     *     <li><b>prefix</b>: Inserts a component for a named prefix.</li>
     *     <li><b>ncolor</b>: Applies a color style from a named color or falls back to black.</li>
     *     <li><b>separator</b>: Inserts a separator component if defined.</li>
     * </ul>
     * The input string is then deserialized with these resolvers.
     *
     * @param audience The entity, used for locale detection (may be {@code null}).
     * @param input    The message string to parse.
     * @param key      The key of the message, used for logging on parse failure.
     * @return The parsed {@link Component} with all custom tags resolved.
     */
    @NotNull
    private static Component getComponentInternal(@Nullable Audience audience, @NotNull String input, @NotNull String key) {
        Locale loc = defaultLocale;
        if (usePlayerLocale && audience instanceof Player p)
            loc = p.locale();

        Object2ObjectMap<String, Object2ObjectMap<String, String>> tagsByCat = LOCALE_SPECIAL_TAGS.getOrDefault(loc, Object2ObjectMaps.emptyMap());

        Object2ObjectMap<String, String> prefixMap = tagsByCat.getOrDefault("prefix", Object2ObjectMaps.emptyMap());
        Object2ObjectMap<String, String> colorMap = tagsByCat.getOrDefault("ncolor", Object2ObjectMaps.emptyMap());
        Object2ObjectMap<String, String> otherMap = tagsByCat.getOrDefault("other", Object2ObjectMaps.emptyMap());

        TagResolver prefixResolver = TagResolver.resolver("prefix",
                (args, ctx) -> {
                    String id = args.popOr("prefix expected").value();
                    String pat = prefixMap.getOrDefault(id, "");

                    return Tag.inserting(MM.deserialize(pat));
                });

        TagResolver colorResolver = TagResolver.resolver("ncolor",
                (args, ctx) -> {
                    String id = args.popOr("color expected").value();
                    String hex = colorMap.get(id);
                    TextColor c = hex != null ? TextColor.fromHexString(hex) : NamedTextColor.BLACK;

                    return Tag.styling(b -> b.color(c));
                });

        TagResolver.Single separatorResolver = Placeholder.component("separator", MM.deserialize(otherMap.getOrDefault("separator", "")));

        Component deserializedText;

        try {
            deserializedText = MM.deserialize(
                    input,
                    prefixResolver,
                    colorResolver,
                    separatorResolver
            );
        } catch (ParsingException e) {
            NiveriaAPI.instance().getSLF4JLogger().error("Failed to parse MiniMessage string: {} (language key: {})", input, key, e);
            deserializedText = Component.text(key);
        }

        return deserializedText;
    }

    /**
     * Gets a localized {@link Component} for the given key using the server's
     * default locale. The string is parsed using MiniMessage.
     *
     * @param key The key of the message to retrieve.
     * @return The localized component.
     */
    @NotNull
    public static Component get(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        String raw = getStringInternal(null, key);
        return getComponentInternal(null, raw, key);
    }

    /**
     * Gets a formatted, localized {@link Component} for the given key using the
     * server's default locale. The string is parsed using MiniMessage.
     *
     * @param key  The key of the message to retrieve.
     * @param args The arguments to format into the message.
     * @return The formatted, localized component.
     */
    @NotNull
    public static Component get(@NotNull String key, @NotNull Object @NotNull ... args) {
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(args, "args cannot be null");

        String raw = getStringInternal(null, key, args);
        return getComponentInternal(null, raw, key);
    }

    /**
     * Gets a localized {@link Component} for the given key, using the audience's
     * locale if enabled. The string is parsed using MiniMessage.
     *
     * @param audience The recipient of the message, used for locale detection.
     * @param key      The key of the message to retrieve.
     * @return The localized component.
     */
    @NotNull
    public static Component get(@NotNull Audience audience, @NotNull String key) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        String raw = getStringInternal(audience, key);
        return getComponentInternal(audience, raw, key);
    }

    /**
     * Gets a formatted, localized {@link Component} for the given key, using the
     * audience's locale if enabled. The string is parsed using MiniMessage.
     *
     * @param audience The recipient of the message, used for locale detection.
     * @param key      The key of the message to retrieve.
     * @param args     The arguments to format into the message.
     * @return The formatted, localized component.
     */
    @NotNull
    public static Component get(@NotNull Audience audience, @NotNull String key, @NotNull Object @NotNull ... args) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(args, "args cannot be null");

        String raw = getStringInternal(audience, key, args);
        return getComponentInternal(audience, raw, key);
    }

    /**
     * Gets a list of localized {@link Component}s for the given key using the server's
     * default locale. Multi-line strings (separated by \n) are split into individual
     * components, making this method ideal for item lore.
     *
     * @param key The key of the message to retrieve.
     * @return A list of localized components, one per line.
     */
    @NotNull
    public static List<Component> getList(@NotNull String key) {
        Preconditions.checkNotNull(key, "key cannot be null");

        String raw = getStringInternal(null, key);
        return splitAndParseComponents(null, raw, key);
    }

    /**
     * Gets a list of formatted, localized {@link Component}s for the given key using
     * the server's default locale. Multi-line strings are split into individual
     * components.
     *
     * @param key  The key of the message to retrieve.
     * @param args The arguments to format into the message.
     * @return A list of formatted, localized components, one per line.
     */
    @NotNull
    public static List<Component> getList(@NotNull String key, @NotNull Object @NotNull ... args) {
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(args, "args cannot be null");

        String raw = getStringInternal(null, key, args);
        return splitAndParseComponents(null, raw, key);
    }

    /**
     * Gets a list of localized {@link Component}s for the given key, using the
     * audience's locale if enabled. Multi-line strings are split into individual
     * components.
     *
     * @param audience The recipient of the message, used for locale detection.
     * @param key      The key of the message to retrieve.
     * @return A list of localized components, one per line.
     */
    @NotNull
    public static List<Component> getList(@NotNull Audience audience, @NotNull String key) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        String raw = getStringInternal(audience, key);
        return splitAndParseComponents(audience, raw, key);
    }

    /**
     * Gets a list of formatted, localized {@link Component}s for the given key,
     * using the audience's locale if enabled. Multi-line strings are split into
     * individual components.
     *
     * @param audience The recipient of the message, used for locale detection.
     * @param key      The key of the message to retrieve.
     * @param args     The arguments to format into the message.
     * @return A list of formatted, localized components, one per line.
     */
    @NotNull
    public static List<Component> getList(@NotNull Audience audience, @NotNull String key, @NotNull Object @NotNull ... args) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(args, "args cannot be null");

        String raw = getStringInternal(audience, key, args);
        return splitAndParseComponents(audience, raw, key);
    }

    /**
     * Splits a multi-line string by newlines and parses each line into a {@link Component}.
     * <p>
     * This is primarily used for item lore, where each line needs to be a separate
     * component in a list. Empty lines are preserved as empty components.
     *
     * @param audience The entity, used for locale detection (may be {@code null}).
     * @param raw      The raw message string to split and parse.
     * @param key      The key of the message, used for logging on parse failure.
     * @return A list of components, one per line.
     */
    @NotNull
    private static List<Component> splitAndParseComponents(@Nullable Audience audience, @NotNull String raw, @NotNull String key) {
        if (raw.isEmpty() || raw.equals(key))
            return Collections.emptyList();

        return Arrays.stream(raw.split("\n"))
                .map(line -> getComponentInternal(audience, line, key))
                .toList();
    }

    /**
     * Gets a localized message and sends it directly to an
     * {@link Audience}.
     *
     * @param audience The recipient of the message.
     * @param key      The key of the message to send.
     */
    public static void sendMessage(@NotNull Audience audience, @NotNull String key) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        sendMessage(audience, null, key, (Object[]) null);
    }

    /**
     * Gets a formatted, localized message and sends it directly to an
     * {@link Audience}.
     *
     * @param audience The recipient of the message.
     * @param key      The key of the message to send.
     * @param args     The arguments to format into the message.
     */
    public static void sendMessage(@NotNull Audience audience, @NotNull String key, @NotNull Object @NotNull ... args) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");
        Preconditions.checkNotNull(args, "args cannot be null");

        sendMessage(audience, null, key, args);
    }

    /**
     * Gets a localized message, plays a sound, and sends it directly to an
     * {@link Audience}.
     *
     * @param audience The recipient of the message.
     * @param sound    The sound to play when sending the message.
     * @param key      The key of the message to send.
     */
    public static void sendMessage(@NotNull Audience audience, @NotNull Sound sound, @NotNull String key) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(sound, "sound cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        sendMessage(audience, sound, key, (Object[]) null);
    }

    /**
     * Gets a formatted, localized message, plays a sound, and sends it directly
     * to an {@link Audience}.
     * <p>
     * If the provided sound is {@code null}, the method will attempt to derive
     * a sound from the language file by appending "_sound" to the message key.
     * The sound string must follow the format: {@code <sound_key>;<source>;<volume>;<pitch>}
     * (e.g., {@code "minecraft:entity.ender_dragon.death;MASTER;1.0;1.0"}).
     * If the sound key is not found or the format is invalid, the sound will be
     * skipped silently (with error logging).
     *
     * @param audience The recipient of the message.
     * @param sound    The sound to play when sending the message, or {@code null} to derive from the language file using the "{@code key_sound}" convention.
     * @param key      The key of the message to send.
     * @param args     The arguments to format into the message.
     */
    @SuppressWarnings("PatternValidation")
    public static void sendMessage(@NotNull Audience audience, @Nullable Sound sound, @NotNull String key, @NotNull Object @Nullable ... args) {
        Preconditions.checkNotNull(audience, "audience cannot be null");
        Preconditions.checkNotNull(key, "key cannot be null");

        Component message = args == null ? get(audience, key) : get(audience, key, args);
        if (message.equals(Component.empty()))
            return;

        audience.sendMessage(message);
        if (sound != null) {
            audience.playSound(sound, Sound.Emitter.self());
            return;
        }

        String soundString = args == null ? getString(audience, key + "_sound") : getString(audience, key + "_sound", args);
        if (soundString.equals(key + "_sound"))
            return;

        try {
            String[] split = soundString.split(";");
            if (split.length != 4) {
                NiveriaAPI.instance().getSLF4JLogger().error("Invalid sound string format for key: {} (sound string: {})", key, soundString);
                NiveriaAPI.instance().getSLF4JLogger().error("Expected format: <sound_key>;<source>;<volume>;<pitch>");
                return;
            }

            Key soundKey = Key.key(split[0].trim());
            Sound.Source source = Sound.Source.valueOf(split[1].trim());
            float volume = Float.parseFloat(split[2].trim());
            float pitch = Float.parseFloat(split[3].trim());

            audience.playSound(Sound.sound(soundKey, source, volume, pitch), Sound.Emitter.self());
        } catch (Exception e) {
            NiveriaAPI.instance().getSLF4JLogger().error("Failed to play sound for key: {} (sound string: {})", key, soundString, e);
        }
    }

    /**
     * Reloads the language files and configuration from disk.
     *
     * @param plugin The plugin instance to reload languages for.
     */
    public static void reload(@NotNull JavaPlugin plugin) {
        Preconditions.checkNotNull(plugin, "plugin cannot be null");

        load(plugin);
    }
}
