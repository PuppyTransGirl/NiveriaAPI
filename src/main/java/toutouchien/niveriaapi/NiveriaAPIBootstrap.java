package toutouchien.niveriaapi;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import toutouchien.niveriaapi.command.impl.NiveriaAPICommand;

@SuppressWarnings("UnstableApiUsage")
public class NiveriaAPIBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext ctx) {
        ctx.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(NiveriaAPICommand.get().build());
        });
    }
}
