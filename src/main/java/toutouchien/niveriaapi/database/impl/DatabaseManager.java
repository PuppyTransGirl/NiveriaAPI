package toutouchien.niveriaapi.database.impl;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.database.AbstractDatabaseManager;

public class DatabaseManager extends AbstractDatabaseManager {
    public DatabaseManager(@NotNull Plugin plugin) {
        super(plugin, NiveriaAPI.instance().mongoManager().database(plugin.getName()));
    }
}
