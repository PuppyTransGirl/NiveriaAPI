package toutouchien.niveriaapi.utils;

import io.papermc.paper.adventure.PaperAdventure;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TitleUtils {
    private TitleUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static void sendTitle(@NotNull Player player, @NotNull Title title) {
        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();

        Title.Times times = title.times();
        if (times != null) {
            packets.add(new ClientboundSetTitlesAnimationPacket(
                    ticks(times.fadeIn()),
                    ticks(times.stay()),
                    ticks(times.fadeOut())
            ));
        }

        if (!title.subtitle().equals(Component.empty()))
            packets.add(new ClientboundSetSubtitleTextPacket(PaperAdventure.asVanilla(title.subtitle())));

        if (!title.title().equals(Component.empty()))
            packets.add(new ClientboundSetTitleTextPacket(PaperAdventure.asVanilla(title.title())));

        if (packets.isEmpty())
            return;

        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packets);
        NMSUtils.sendPacket(player, bundlePacket);
    }

    public static void sendTitle(@NotNull Collection<? extends Player> players, @NotNull Title title) {
        if (players.isEmpty())
            return;

        if (players.size() == 1) {
            sendTitle(players.iterator().next(), title);
            return;
        }

        List<Packet<? super ClientGamePacketListener>> packets = new ArrayList<>();

        Title.Times times = title.times();
        if (times != null)
            packets.add(new ClientboundSetTitlesAnimationPacket(
                    ticks(times.fadeIn()),
                    ticks(times.stay()),
                    ticks(times.fadeOut())
            ));

        if (!title.subtitle().equals(Component.empty()))
            packets.add(new ClientboundSetSubtitleTextPacket(PaperAdventure.asVanilla(title.subtitle())));

        if (!title.title().equals(Component.empty()))
            packets.add(new ClientboundSetTitleTextPacket(PaperAdventure.asVanilla(title.title())));

        if (packets.isEmpty())
            return;

        ClientboundBundlePacket bundlePacket = new ClientboundBundlePacket(packets);
        players.forEach(player -> NMSUtils.sendPacket(player, bundlePacket));
    }

    public static void sendTitleToAllOnlinePlayers(@NotNull Title title) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        if (onlinePlayers.isEmpty())
            return;

        if (onlinePlayers.size() == 1) {
            sendTitle(onlinePlayers.iterator().next(), title);
            return;
        }

        sendTitle(onlinePlayers, title);
    }

    public static void clearTitle(@NotNull Player player) {
        ClientboundClearTitlesPacket clearTitlesPacket = new ClientboundClearTitlesPacket(true);
        NMSUtils.sendPacket(player, clearTitlesPacket);
    }

    public static void clearTitle(@NotNull Collection<? extends Player> players) {
        if (players.isEmpty())
            return;

        if (players.size() == 1) {
            clearTitle(players.iterator().next());
            return;
        }

        ClientboundClearTitlesPacket clearTitlesPacket = new ClientboundClearTitlesPacket(true);
        players.forEach(player -> NMSUtils.sendPacket(player, clearTitlesPacket));
    }

    public static void clearTitleOfAllOnlinePlayers(@NotNull Title title) {
        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();

        if (onlinePlayers.isEmpty())
            return;

        if (onlinePlayers.size() == 1) {
            sendTitle(onlinePlayers.iterator().next(), title);
            return;
        }

        ClientboundClearTitlesPacket clearTitlesPacket = new ClientboundClearTitlesPacket(true);
        onlinePlayers.forEach(player -> NMSUtils.sendPacket(player, clearTitlesPacket));
    }

    private static int ticks(Duration duration) {
        if (duration == null)
            return -1;

        return (int) TimeUtils.ticks(duration);
    }

    private static boolean timesEquals(Title.Times times1, Title.Times times2) {
        if (times1 == times2) return true;
        if (times1 == null || times2 == null) return false;

        return times1.fadeIn().equals(times2.fadeIn()) &&
                times1.stay().equals(times2.stay()) &&
                times1.fadeOut().equals(times2.fadeOut());
    }

}
