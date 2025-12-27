package toutouchien.homeplugin;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import toutouchien.homeplugin.commands.SetHomeCommand;

@SuppressWarnings("UnstableApiUsage")
public class HomePluginBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext ctx) {
        ctx.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(SetHomeCommand.get());
        });
    }
}
