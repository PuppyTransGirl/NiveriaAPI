package toutouchien.homeplugin;

import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import toutouchien.homeplugin.commands.DeleteHomeCommand;
import toutouchien.homeplugin.commands.SetHomeCommand;

import java.util.Arrays;

@SuppressWarnings("UnstableApiUsage")
public class HomePluginBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext ctx) {
        ctx.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            Commands registrar = commands.registrar();
            Arrays.asList(
                    DeleteHomeCommand.get(),
                    SetHomeCommand.get()
            ).forEach(registrar::register);
        });
    }
}
