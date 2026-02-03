package toutouchien.homeplugin.managers;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import toutouchien.homeplugin.HomePlugin;
import toutouchien.homeplugin.models.Home;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.delay.Delay;
import toutouchien.niveriaapi.delay.DelayBuilder;
import toutouchien.niveriaapi.hook.HookType;
import toutouchien.niveriaapi.hook.impl.LuckPermsHook;
import toutouchien.niveriaapi.utils.StringUtils;

import java.io.File;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static toutouchien.homeplugin.HomePlugin.LANG;

public class HomeManager {
    private final File homesFolder;
    private final Object2ObjectMap<UUID, ObjectSet<Home>> homes;

    public HomeManager(File dataFolder) {
        this.homesFolder = new File(dataFolder, "homes");
        if (!this.homesFolder.exists())
            this.homesFolder.mkdirs();

        this.homes = new Object2ObjectOpenHashMap<>();
    }

    public void start() {
        this.loadHomes();
    }

    public void stop() {
        this.saveHomes();
    }

    public void createHome(Player player, String homeName) {
        Home newHome = new Home(
                homeName,
                player.getLocation(),
                Material.GRASS_BLOCK
        );

        this.homes.computeIfAbsent(player.getUniqueId(), k -> new ObjectOpenHashSet<>()).add(newHome);
    }

    public void deleteHome(UUID uuid, String homeName) {
        ObjectSet<Home> playerHomes = this.homes.get(uuid);
        if (playerHomes == null)
            return;

        for (Home home : playerHomes) {
            if (home.name().equalsIgnoreCase(homeName)) {
                this.deleteHome(uuid, home);
                return;
            }
        }
    }

    public void deleteHome(UUID uuid, Home home) {
        ObjectSet<Home> playerHomes = this.homes.get(uuid);
        if (playerHomes == null)
            return;

        playerHomes.remove(home);
    }

    public void teleportHome(Player player, String homeName) {
        Home home = this.home(player.getUniqueId(), homeName);
        if (home == null)
            return;

        this.teleportHome(player, home);
    }

    public void teleportHome(Player player, Home home) {
        Consumer<Player> teleportationConsumer = p -> {
            p.teleportAsync(home.location()).thenAcceptAsync(result -> {
                LANG.sendMessage(p, "homeplugin.home.teleported");
            });
        };

        LuckPermsHook luckPermsHook = NiveriaAPI.instance().hookManager().hook(HookType.LuckpermsHook);
        Delay delay = DelayBuilder.of(player)
                .delay(luckPermsHook == null ? 3 : luckPermsHook.metaCache().integerMeta(player, "home-teleport-delay", 3))
                .chat(true)
                .successConsumer(teleportationConsumer)
                .build();

        NiveriaAPI.instance().delayManager().start(delay);
    }

    public boolean homeExists(UUID uuid, String homeName) {
        return this.home(uuid, homeName) != null;
    }

    public Home home(UUID uuid, String homeName) {
        ObjectSet<Home> playerHomes = this.homes.get(uuid);
        if (playerHomes == null)
            return null;

        for (Home home : playerHomes) {
            if (home.name().equalsIgnoreCase(homeName))
                return home;
        }

        return null;
    }

    public ObjectSet<Home> homes(UUID uuid) {
        return this.homes.computeIfAbsent(uuid, k -> new ObjectOpenHashSet<>());
    }

    public void loadHomes() {
        File[] files = this.homesFolder.listFiles();
        if (files == null)
            return;

        Object2ObjectMap<UUID, ObjectSet<Home>> tempHomes = new Object2ObjectOpenHashMap<>();

        for (File homeFile : files) {
            String fileName = homeFile.getName();
            if (fileName.endsWith(".yml"))
                fileName = fileName.substring(0, fileName.length() - 4);

            UUID uuid;
            try {
                uuid = UUID.fromString(fileName);
            } catch (IllegalArgumentException e) {
                HomePlugin.instance().getSLF4JLogger().warn("Invalid home file name: {}", homeFile.getAbsolutePath());
                continue;
            }

            FileConfiguration homeConfig = YamlConfiguration.loadConfiguration(homeFile);
            ObjectSet<Home> playerHomes = new ObjectOpenHashSet<>();

            for (String homeName : homeConfig.getKeys(false)) {
                ConfigurationSection section = homeConfig.getConfigurationSection(homeName);
                if (section == null) {
                    HomePlugin.instance().getSLF4JLogger().warn("Invalid home section for {} in file {}", homeName, homeFile.getAbsolutePath());
                    continue;
                }

                Location location = section.getLocation("location");
                if (location == null) {
                    HomePlugin.instance().getSLF4JLogger().warn("Invalid location for home {} in file {}", homeName, homeFile.getAbsolutePath());
                    continue;
                }

                playerHomes.add(new Home(
                        homeName,
                        location,
                        StringUtils.match(section.getString("icon"), Material.class, Material.GRASS_BLOCK)
                ));
            }

            tempHomes.put(uuid, playerHomes);
        }

        this.homes.putAll(tempHomes);
    }

    public void saveHomes() {
        for (Map.Entry<UUID, ObjectSet<Home>> entry : homes.entrySet()) {
            UUID uuid = entry.getKey();
            ObjectSet<Home> playerHomes = entry.getValue();
            if (playerHomes == null || playerHomes.isEmpty())
                continue;

            File homeFile = new File(homesFolder, uuid.toString() + ".yml");
            YamlConfiguration config = new YamlConfiguration();

            for (Home home : playerHomes) {
                String homeName = home.name();
                config.set(homeName + ".location", home.location());
                config.set(homeName + ".icon", home.icon().name());
            }

            try {
                config.save(homeFile);
            } catch (Exception e) {
                HomePlugin.instance().getSLF4JLogger().error("Could not save homes to {}", homeFile.getAbsolutePath(), e);
            }
        }
    }
}
