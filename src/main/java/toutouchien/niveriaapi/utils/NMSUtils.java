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

/**
 * Utility class for interacting with Minecraft's NMS (Net Minecraft Server) classes.
 */
public class NMSUtils {
    private NMSUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Retrieves the NMS ServerPlayer instance from a Bukkit Player.
     *
     * @param player The Bukkit Player.
     * @return The corresponding NMS ServerPlayer.
     */
    @NotNull
    public static ServerPlayer getNMSPlayer(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        return ((CraftPlayer) player).getHandle();
    }

    /**
     * Retrieves the ServerGamePacketListenerImpl (player connection) from a Bukkit Player.
     *
     * @param player The Bukkit Player.
     * @return The corresponding ServerGamePacketListenerImpl.
     */
    @NotNull
    public static ServerGamePacketListenerImpl getConnection(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        return getNMSPlayer(player).connection;
    }

    /**
     * Sends a single packet to the specified player.
     *
     * @param player The Bukkit Player.
     * @param packet The packet to send.
     */
    public static void sendPacket(@NotNull Player player, @NotNull Packet<?> packet) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packet, "packet cannot be null");

        getConnection(player).send(packet);
    }

    /**
     * Sends multiple packets to the specified player.
     *
     * @param player  The Bukkit Player.
     * @param packets The packets to send.
     */
    public static void sendPackets(@NotNull Player player, @NotNull Packet<?> @NotNull ... packets) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packets, "packets cannot be null");

        ServerGamePacketListenerImpl connection = getConnection(player);
        Arrays.stream(packets).forEach(connection::send);
    }

    /**
     * Sends multiple non-null packets to the specified player.
     *
     * @param player  The Bukkit Player.
     * @param packets The packets to send.
     */
    public static void sendNonNullPackets(@NotNull Player player, @Nullable Packet<?>... packets) {
        Preconditions.checkNotNull(player, "player cannot be null");

        if (packets == null)
            return;

        ServerGamePacketListenerImpl connection = getConnection(player);
        Arrays.stream(packets).filter(Objects::nonNull).forEach(connection::send);
    }

    /**
     * Sends a collection of packets to the specified player.
     *
     * @param player  The Bukkit Player.
     * @param packets The collection of packets to send.
     */
    public static void sendPackets(@NotNull Player player, @NotNull Collection<Packet<?>> packets) {
        Preconditions.checkNotNull(player, "player cannot be null");
        Preconditions.checkNotNull(packets, "packets cannot be null");

        ServerGamePacketListenerImpl connection = getConnection(player);
        packets.forEach(connection::send);
    }

    /**
     * Sends a collection of non-null packets to the specified player.
     *
     * @param player  The Bukkit Player.
     * @param packets The collection of packets to send.
     */
    public static void sendNonNullPackets(@NotNull Player player, @Nullable Collection<Packet<?>> packets) {
        Preconditions.checkNotNull(player, "player cannot be null");

        if (packets == null)
            return;

        ServerGamePacketListenerImpl connection = getConnection(player);
        packets.stream().filter(Objects::nonNull).forEach(connection::send);
    }
}
