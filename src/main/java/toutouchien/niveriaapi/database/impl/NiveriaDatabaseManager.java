package toutouchien.niveriaapi.database.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.database.AbstractDatabaseManager;

@ApiStatus.Internal
public class NiveriaDatabaseManager extends AbstractDatabaseManager {
    @ApiStatus.Internal
    public NiveriaDatabaseManager(@NotNull NiveriaAPI plugin) {
        super(plugin, plugin.mongoManager().database("Niveria"));
    }
}
