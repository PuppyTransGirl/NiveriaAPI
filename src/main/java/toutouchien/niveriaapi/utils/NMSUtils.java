package toutouchien.niveriaapi.utils;

import com.google.common.base.Preconditions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public class NMSUtils {
    private NMSUtils() {
        throw new IllegalStateException("Utility class");
    }

    @NotNull
    public static ServerPlayer getNMSPlayer(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        return ((CraftPlayer) player).getHandle();
    }

    @NotNull
    public static ServerGamePacketListenerImpl getConnection(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        return getNMSPlayer(player).connection;
    }

    public static void sendPacket(@NotNull Player player, @NotNull Packet<?> packet) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packet, "packet cannot be null");

        getConnection(player).send(packet);
    }

    public static void sendPackets(@NotNull Player player, @NotNull Packet<?> @NotNull ... packets) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packets, "packets cannot be null");

        ServerGamePacketListenerImpl connection = getConnection(player);
        Arrays.stream(packets).forEach(connection::send);
    }

    public static void sendNonNullPackets(@NotNull Player player, @Nullable Packet<?>... packets) {
        Preconditions.checkNotNull(player, "player cannot be null");

        if (packets == null)
            return;

        ServerGamePacketListenerImpl connection = getConnection(player);
        Arrays.stream(packets).filter(Objects::nonNull).forEach(connection::send);
    }

    public static void sendPackets(@NotNull Player player, @NotNull Collection<Packet<?>> packets) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packets, "packets cannot be null");

        ServerGamePacketListenerImpl connection = getConnection(player);
        packets.forEach(connection::send);
    }

    public static void sendNonNullPackets(@NotNull Player player, @Nullable Collection<Packet<?>> packets) {
        Preconditions.checkNotNull(player, "player cannot be null");

        if (packets == null)
            return;

        ServerGamePacketListenerImpl connection = getConnection(player);
        packets.stream().filter(Objects::nonNull).forEach(connection::send);
    }
}
