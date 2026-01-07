package toutouchien.niveriaapi;

import org.bson.Document;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import toutouchien.niveriaapi.cooldown.CooldownDatabase;
import toutouchien.niveriaapi.cooldown.CooldownManager;
import toutouchien.niveriaapi.database.MongoManager;
import toutouchien.niveriaapi.database.PlayerListener;
import toutouchien.niveriaapi.database.impl.NiveriaDatabaseManager;
import toutouchien.niveriaapi.delay.DelayManager;
import toutouchien.niveriaapi.hook.HookListener;
import toutouchien.niveriaapi.hook.HookManager;
import toutouchien.niveriaapi.input.ChatInputManager;
import toutouchien.niveriaapi.lang.Lang;
import toutouchien.niveriaapi.menu.MenuListener;

import java.util.Arrays;

public class NiveriaAPI extends JavaPlugin {
    private static final String MONGODB_ENV_KEY = "NIVERIAAPI_MONGODB_CONNECTION_STRING";
    private static final int BSTATS_PLUGIN_ID = 28754;

    private static NiveriaAPI instance;

    private ChatInputManager chatInputManager;
    private CooldownManager cooldownManager;
    private DelayManager delayManager;
    private HookManager hookManager;
    private MongoManager mongoManager;
    private NiveriaDatabaseManager niveriaDatabaseManager;

    private Metrics bStats;

    private boolean databaseDisabled;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        preLoadUtilsClasses();

        this.bStats = new Metrics(this, BSTATS_PLUGIN_ID);

        Lang.load(this);

        if (!isUnitTestVersion()) {
            try {
                String mongoDBEnv = System.getenv(MONGODB_ENV_KEY);
                String mongoDBConnectionString;
                if (mongoDBEnv == null) {
                    mongoDBConnectionString = this.getConfig().getString("mongodb-connection-string");
                    this.getSLF4JLogger().info("Loading MongoDB Connection String from the config.");
                } else {
                    mongoDBConnectionString = mongoDBEnv;
                    this.getSLF4JLogger().info("Loading MongoDB Connection String from the environment variable.");
                }

                if (mongoDBConnectionString == null || mongoDBConnectionString.isEmpty()) {
                    this.getSLF4JLogger().warn("No MongoDB connection string provided ! Skipping MongoDB initialization.");
                    this.getSLF4JLogger().warn("Any database-related features will be disabled.");
                    this.databaseDisabled = true;
                } else {
                    this.mongoManager = new MongoManager(mongoDBConnectionString);
                    this.getSLF4JLogger().info("MongoManager initialized.");

                    this.niveriaDatabaseManager = new NiveriaDatabaseManager(this);
                    this.getSLF4JLogger().info("NiveriaDatabaseManager initialized for the shared 'Niveria' database.");

                    registerSharedDefaults();
                }
            } catch (Exception e) {
                this.getSLF4JLogger().error("Failed to initialize MongoDB connections ! Stopping the server.", e);
                this.getServer().shutdown();
                return;
            }
        }

        this.chatInputManager = new ChatInputManager();
        if (!isUnitTestVersion()) {
            CooldownDatabase database = this.databaseDisabled ? null : new CooldownDatabase(niveriaDatabaseManager, this.getSLF4JLogger());
            this.cooldownManager = new CooldownManager(this, database);
        }
        (this.delayManager = new DelayManager(this)).initialize();
        (this.hookManager = new HookManager(this)).onEnable();

        registerListeners();
    }

    private void preLoadUtilsClasses() {
        String[] classes = {
                "ColorUtils",
                "CommandUtils",
                "ComponentUtils",
                "FileUtils",
                "ItemBuilder",
                "MathUtils",
                "PlayerUtils",
                "SerializeUtils",
                "StringUtils",
                "Task",
                "TimeUtils"
        };

        this.getSLF4JLogger().info("Starting to preload utility classes");

        int loadedCount = 0;
        String prefix = "toutouchien.niveriaapi.utils.";
        for (int i = 0; i < classes.length; i++) {
            try {
                Class.forName(prefix + classes[i]);
                loadedCount++;
            } catch (ClassNotFoundException e) {
                this.getSLF4JLogger().error("Couldn't load {}", classes[i], e);
            }
        }

        this.getSLF4JLogger().info(
                "Finished preloading utility classes. Successfully loaded {}/{} classes.",
                loadedCount,
                classes.length
        );
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        Arrays.asList(
                this.chatInputManager,
                new HookListener(this.hookManager),
                new MenuListener()
        ).forEach(listener -> pluginManager.registerEvents(listener, this));

        if (!isUnitTestVersion() && this.niveriaDatabaseManager != null) {
            pluginManager.registerEvents(new PlayerListener(this.niveriaDatabaseManager), this);
        }
    }

    public void reload() {
        this.getSLF4JLogger().info("Reloading NiveriaAPI...");

        this.reloadConfig();
        Lang.reload(this);

        this.getSLF4JLogger().info("NiveriaAPI reloaded.");
    }

    @Override
    public void onDisable() {
        if (!isUnitTestVersion() && !this.databaseDisabled)
            this.cooldownManager.shutdown();
        this.hookManager.onDisable();

        if (!isUnitTestVersion() && !this.databaseDisabled)
            this.mongoManager.shutdown();

        Bukkit.getScheduler().cancelTasks(this);
        this.bStats.shutdown();
    }

    private void registerSharedDefaults() {
        this.niveriaDatabaseManager.registerDefault("players", () ->
                new Document("ip", "")
                        .append("firstJoin", 0L)
                        .append("lastJoin", 0L)
        );
    }

    public ChatInputManager chatInputManager() {
        return chatInputManager;
    }

    public CooldownManager cooldownManager() {
        return this.cooldownManager;
    }

    public DelayManager delayManager() {
        return this.delayManager;
    }

    public HookManager hookManager() {
        return this.hookManager;
    }

    @ApiStatus.Internal
    public MongoManager mongoManager() {
        return mongoManager;
    }

    public NiveriaDatabaseManager niveriaDatabaseManager() {
        return niveriaDatabaseManager;
    }

    public static NiveriaAPI instance() {
        return instance;
    }

    public static boolean isUnitTestVersion() {
        return Bukkit.getServer().getVersion().contains("MockBukkit");
    }
}
