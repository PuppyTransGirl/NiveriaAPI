package toutouchien.niveriaapi.database.impl;

import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NullMarked;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.database.AbstractDatabaseManager;

/**
 * Manages database operations for a specific plugin using MongoDB.
 */
@NullMarked
public class DatabaseManager extends AbstractDatabaseManager {
    /**
     * Constructs a new DatabaseManager for the given plugin.
     *
     * @param plugin The plugin instance for which the database manager is created.
     */
    public DatabaseManager(Plugin plugin) {
        super(plugin, NiveriaAPI.instance().mongoManager().database(plugin.getName()));
    }
}
