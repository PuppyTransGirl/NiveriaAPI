package toutouchien.niveriaapi;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.ApiStatus;
import toutouchien.niveriaapi.command.CommandManager;
import toutouchien.niveriaapi.cooldown.CooldownDatabase;
import toutouchien.niveriaapi.cooldown.CooldownManager;
import toutouchien.niveriaapi.database.MongoManager;
import toutouchien.niveriaapi.database.PlayerListener;
import toutouchien.niveriaapi.database.impl.NiveriaDatabaseManager;
import toutouchien.niveriaapi.delay.DelayManager;
import toutouchien.niveriaapi.hook.HookListener;
import toutouchien.niveriaapi.hook.HookManager;
import toutouchien.niveriaapi.input.ChatInputManager;
import toutouchien.niveriaapi.menu.MenuListener;

import java.util.Arrays;

public final class NiveriaAPI extends JavaPlugin {
    private static NiveriaAPI instance;

    private ChatInputManager chatInputManager;
    private CommandManager commandManager;
    private CooldownManager cooldownManager;
    private DelayManager delayManager;
    private HookManager hookManager;
    private MongoManager mongoManager;
    private NiveriaDatabaseManager niveriaDatabaseManager;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();

        preLoadUtilsClasses();

        try {
            this.mongoManager = new MongoManager(this.getConfig().getString("mongodb-connection-string"));
            this.getSLF4JLogger().info("MongoManager initialized.");

            this.niveriaDatabaseManager = new NiveriaDatabaseManager(this);
            this.getSLF4JLogger().info("NiveriaDatabaseManager initialized for the shared 'Niveria' database.");

            registerSharedDefaults();
        } catch (Exception e) {
            this.getSLF4JLogger().error("Failed to initialize MongoDB connections ! Stopping the server.", e);
            this.getServer().shutdown();
            return;
        }

        this.chatInputManager = new ChatInputManager();
        this.commandManager = new CommandManager();
        this.cooldownManager = new CooldownManager(this, new CooldownDatabase(niveriaDatabaseManager));
        (this.delayManager = new DelayManager(this)).initialize();
        (this.hookManager = new HookManager(this)).onEnable();

        registerCommands();
        registerListeners();
    }

    private void preLoadUtilsClasses() {
        String[] classes = {
                "base.Task",
                "common.MathUtils",
                "common.StringUtils",
                "common.TimeUtils",
                "data.DatabaseUtils",
                "data.FileUtils",
                "game.PlayerUtils",
                "ui.ColorUtils",
                "ui.ComponentUtils"
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

    private void registerCommands() {
        this.commandManager.registerCommand(new toutouchien.niveriaapi.command.impl.niveriaapi.NiveriaAPI());
    }

    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();
        Arrays.asList(
                this.chatInputManager,
                new HookListener(this.hookManager),
                new MenuListener(),
                new PlayerListener(this.niveriaDatabaseManager)
        ).forEach(listener -> pluginManager.registerEvents(listener, this));
    }

    @Override
    public void onDisable() {
        this.cooldownManager.shutdown();
        this.hookManager.onDisable();
        this.mongoManager.shutdown();

        Bukkit.getScheduler().cancelTasks(this);
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

    public CommandManager commandManager() {
        return commandManager;
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
}
