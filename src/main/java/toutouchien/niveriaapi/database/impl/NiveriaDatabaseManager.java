package toutouchien.niveriaapi.database.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NullMarked;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.database.AbstractDatabaseManager;

@ApiStatus.Internal
@NullMarked
public class NiveriaDatabaseManager extends AbstractDatabaseManager {
    @ApiStatus.Internal
    public NiveriaDatabaseManager(NiveriaAPI plugin) {
        super(plugin, plugin.mongoManager().database("Niveria"));
    }
}
