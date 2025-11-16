package toutouchien.niveriaapi.command.impl;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Entity;
import toutouchien.niveriaapi.NiveriaAPI;
import toutouchien.niveriaapi.utils.ui.ColorUtils;
import toutouchien.niveriaapi.utils.ui.MessageUtils;

import java.text.DecimalFormat;
import java.util.Map;

public class NiveriaAPICommand {
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    public static LiteralArgumentBuilder<CommandSourceStack> get() {
         return Commands.literal("niveriaapi")
                .then(Commands.literal("ping")
                        .executes(ctx -> {
                            Entity executor = ctx.getSource().getExecutor();
                            Map<String, Long> pings = NiveriaAPI.instance().mongoManager().ping();

                            MessageUtils.sendInfoMessage(executor, Component.text("Ping des bases de données:"));

                            for (Map.Entry<String, Long> pingEntry : pings.entrySet()) {
                                double pingInMilliseconds = pingEntry.getValue() / 1_000_000D;
                                String formattedPing = DECIMAL_FORMAT.format(pingInMilliseconds);

                                executor.sendMessage(
                                        Component.text(pingEntry.getKey(), ColorUtils.primaryColor())
                                                .append(Component.text(" - ", NamedTextColor.DARK_GRAY))
                                                .append(Component.text(formattedPing + " ms"))
                                );
                            }

                            return Command.SINGLE_SUCCESS;
                        }));
    }
}
