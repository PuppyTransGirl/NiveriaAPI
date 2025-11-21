package toutouchien.niveriaapi.lang;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

import java.io.File;
import java.util.Locale;

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

    public static void load(@NotNull JavaPlugin plugin) {
        saveDefaultMessages(plugin);
        loadConfig(plugin);
        loadMessages(plugin);
    }

    /**
     * Loads language settings from the plugin's `config.yml`.
     * <p>
     * This method reads the `lang` key to set the server's default locale and
     * the `use_player_locale` key to determine if player-specific locales
     * should be used.
     *
     * @param plugin The main plugin instance.
     */
    private static void loadConfig(@NotNull JavaPlugin plugin) {
        FileConfiguration config = plugin.getConfig();
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
     * Loads a single language file for a specific locale from the disk.
     * <p>
     * It reads a `.yml` file from `plugins/PluginName/lang/`, parses its
     * key-value pairs, and stores them in the message cache. If the file does
     * not exist, this method does nothing.
     *
     * @param plugin The main plugin instance.
     * @param locale The locale for which to load the language file.
     */
    private static void loadLocaleFile(@NotNull JavaPlugin plugin, @NotNull Locale locale) {
        String fileName = "lang/%s.yml".formatted(locale.toLanguageTag().replace('-', '_'));
        File langFile = new File(plugin.getDataFolder(), fileName);
        if (!langFile.exists())
            return;

        Object2ObjectMap<String, String> messages = new Object2ObjectOpenHashMap<>();
        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);
        for (String key : langConfig.getKeys(false)) {
            if (!langConfig.isString(key))
                continue;

            String value = langConfig.getString(key);
            if (value != null)
                messages.put(key, value);
        }

        if (!messages.isEmpty())
            MESSAGES.put(locale, messages);

        loadSpecialTags(locale, langConfig);
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

        if (!byCategory.isEmpty())
            LOCALE_SPECIAL_TAGS.put(locale, byCategory);
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
            plugin.getSLF4JLogger().warn("Failed to create lang folder in plugin data directory.");
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
    private static String getStringInternal(@Nullable Audience audience, @NotNull String key, @Nullable Object @NotNull ... args) {
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
     * @return The parsed {@link Component} with all custom tags resolved.
     */
    @NotNull
    private static Component getComponentInternal(@Nullable Audience audience, @NotNull String input) {
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
                    TextColor c = hex != null
                            ? TextColor.fromHexString(hex)
                            : NamedTextColor.BLACK;
                    return Tag.styling(b -> b.color(c));
                });

        TagResolver.Single separatorResolver = Placeholder.component("separator", MM.deserialize(otherMap.getOrDefault("separator", "")));

        return MM.deserialize(
                input,
                prefixResolver,
                colorResolver,
                separatorResolver
        );
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
        String raw = getStringInternal(null, key);
        return getComponentInternal(null, raw);
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
    public static Component get(@NotNull String key, @Nullable Object @NotNull ... args) {
        String raw = getStringInternal(null, key, args);
        return getComponentInternal(null, raw);
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
        String raw = getStringInternal(audience, key);
        return getComponentInternal(audience, raw);
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
    public static Component get(@NotNull Audience audience, @NotNull String key, @Nullable Object @NotNull ... args) {
        String raw = getStringInternal(audience, key, args);
        return getComponentInternal(audience, raw);
    }

    /**
     * Gets a localized message and sends it directly to an
     * {@link Audience}.
     *
     * @param audience The recipient of the message.
     * @param key      The key of the message to send.
     */
    public static void sendMessage(@NotNull Audience audience, @NotNull String key) {
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
    public static void sendMessage(@NotNull Audience audience, @NotNull String key, @Nullable Object @NotNull ... args) {
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
        sendMessage(audience, sound, key, (Object[]) null);
    }

    /**
     * Gets a formatted, localized message, plays a sound, and sends it directly
     * to an {@link Audience}.
     *
     * @param audience The recipient of the message.
     * @param sound    The sound to play when sending the message.
     * @param key      The key of the message to send.
     * @param args     The arguments to format into the message.
     */
    public static void sendMessage(@NotNull Audience audience, @Nullable Sound sound, @NotNull String key, @Nullable Object @Nullable ... args) {
        Component message = args == null ? get(audience, key) : get(audience, key, args);
        if (message.equals(Component.empty()))
            return;

        audience.sendMessage(message);
        if (sound != null)
            audience.playSound(sound, Sound.Emitter.self());
    }

    /**
     * Reloads the language files and configuration from disk.
     *
     * @param plugin The plugin instance to reload languages for.
     */
    public static void reload(@NotNull JavaPlugin plugin) {
        load(plugin);
    }
}
