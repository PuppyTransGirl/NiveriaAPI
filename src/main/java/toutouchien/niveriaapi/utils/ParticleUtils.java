package toutouchien.niveriaapi.utils;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import org.bukkit.*;
import org.bukkit.craftbukkit.CraftParticle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;
import toutouchien.niveriaapi.annotations.Shivery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleFunction;

/**
 * Utility methods for spawning and drawing particle effects.
 * <p>
 * This class provides:
 * <ul>
 *     <li>Low-level helpers to send particle packets to players.</li>
 *     <li>Shape helpers (lines, circles, spheres, polygons, etc.).</li>
 *     <li>Animated effects such as following entities or frame-based animations.</li>
 * </ul>
 * All methods are static; the class cannot be instantiated.
 */
@Shivery
public final class ParticleUtils {
    private ParticleUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Spawns particles visible only to a specific player using a raw NMS packet.
     *
     * @param player   target player
     * @param location particle origin
     * @param particle particle type
     * @param count    number of particles
     * @param offsetX  x-offset for spread
     * @param offsetY  y-offset for spread
     * @param offsetZ  z-offset for spread
     * @param speed    particle speed (or extra parameter depending on particle)
     * @param data     particle data (e.g. {@link Particle.DustOptions}), may be null
     * @param force    whether to ignore client particle settings
     * @param <T>      type of particle data
     */
    public static <T> void spawnParticleForPlayer(Player player, Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, T data, boolean force) {
        NMSUtils.sendPacket(player, new ClientboundLevelParticlesPacket(
                CraftParticle.createParticleParam(particle, data),
                force,
                false,
                location.x(),
                location.y(),
                location.z(),
                (float) offsetX,
                (float) offsetY,
                (float) offsetZ,
                (float) speed,
                count
        ));
    }

    /**
     * Spawns particles visible only to the given collection of players.
     *
     * @param players  target players
     * @param location particle origin
     * @param particle particle type
     * @param count    number of particles
     * @param offsetX  x-offset for spread
     * @param offsetY  y-offset for spread
     * @param offsetZ  z-offset for spread
     * @param speed    particle speed (or extra parameter depending on particle)
     * @param data     particle data (e.g. {@link Particle.DustOptions}), may be null
     * @param force    whether to ignore client particle settings
     * @param <T>      type of particle data
     */
    public static <T> void spawnParticleForPlayers(Collection<Player> players, Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, T data, boolean force) {
        ClientboundLevelParticlesPacket particlesPacket = new ClientboundLevelParticlesPacket(
                CraftParticle.createParticleParam(particle, data),
                force,
                false,
                location.x(),
                location.y(),
                location.z(),
                (float) offsetX,
                (float) offsetY,
                (float) offsetZ,
                (float) speed,
                count
        );

        players.forEach(player -> NMSUtils.sendPacket(player, particlesPacket));
    }

    /**
     * Spawns particles at a location for all players in the world's player list.
     *
     * @param location particle origin
     * @param particle particle type
     * @param count    number of particles
     * @param offsetX  x-offset for spread
     * @param offsetY  y-offset for spread
     * @param offsetZ  z-offset for spread
     * @param speed    particle speed (or extra parameter depending on particle)
     * @param data     particle data (e.g. {@link Particle.DustOptions}), may be null
     * @param force    whether to ignore client particle settings
     * @param <T>      type of particle data
     */
    public static <T> void spawnParticle(Location location, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, T data, boolean force) {
        spawnParticleForPlayers(location.getWorld().getPlayers(), location, particle, count, offsetX, offsetY, offsetZ, speed, data, force);
    }

    /**
     * Spawns colored dust particles at the given location.
     *
     * @param location particle origin
     * @param color    dust color
     * @param count    number of particles
     */
    public static void spawnColoredParticle(Location location, Color color, int count) {
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);
        spawnParticle(location, Particle.DUST, count, 0, 0, 0, 0, dustOptions, false);
    }

    /**
     * Spawns material/block particles at the given location.
     *
     * @param location particle origin
     * @param material block material to use for the particle
     * @param count    number of particles
     * @param offsetX  x-offset for spread
     * @param offsetY  y-offset for spread
     * @param offsetZ  z-offset for spread
     * @param speed    particle speed
     */
    public static void spawnMaterialParticle(Location location, Material material, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        spawnParticle(location, Particle.BLOCK, count, offsetX, offsetY, offsetZ, speed, material.createBlockData(), false);
    }

    /**
     * Draws a straight line of particles between two locations.
     *
     * @param start    start location
     * @param end      end location (must be in the same world as start)
     * @param particle particle type
     * @param spacing  distance between particle samples along the line
     * @param count    number of particles per sample
     * @param offsetX  x-offset for spread
     * @param offsetY  y-offset for spread
     * @param offsetZ  z-offset for spread
     * @param speed    particle speed
     * @throws IllegalArgumentException if locations are in different worlds
     */
    public static void drawLine(Location start, Location end, Particle particle, double spacing, int count, double offsetX, double offsetY, double offsetZ, double speed) {
        if (!start.getWorld().equals(end.getWorld()))
            throw new IllegalArgumentException("Locations must be in the same world");

        Vector direction = end.clone().subtract(start).toVector();
        double distance = direction.length();
        direction.normalize();

        for (double d = 0; d <= distance; d += spacing) {
            Location particleLocation = start.clone().add(direction.clone().multiply(d));
            spawnParticle(particleLocation, particle, count, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    /**
     * Draws a horizontal circle of particles around the given center.
     *
     * @param center  circle center
     * @param particle particle type
     * @param radius  circle radius
     * @param points  number of points on the circle
     * @param offsetX x-offset for spread
     * @param offsetY y-offset for spread
     * @param offsetZ z-offset for spread
     * @param speed   particle speed
     */
    public static void drawCircle(Location center, Particle particle, double radius, int points, double offsetX, double offsetY, double offsetZ, double speed) {
        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            double x = center.x() + radius * Math.cos(angle);
            double z = center.z() + radius * Math.sin(angle);
            Location particleLocation = new Location(center.getWorld(), x, center.y(), z);
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    /**
     * Draws a circle of particles in a plane defined by a center and normal vector.
     *
     * @param center  circle center
     * @param particle particle type
     * @param radius  circle radius
     * @param points  number of points on the circle
     * @param normal  normal vector of the circle plane
     * @param offsetX x-offset for spread
     * @param offsetY y-offset for spread
     * @param offsetZ z-offset for spread
     * @param speed   particle speed
     */
    public static void drawVerticalCircle(Location center, Particle particle, double radius, int points, Vector normal, double offsetX, double offsetY, double offsetZ, double speed) {
        Vector normalizedNormal = normal.clone().normalize();

        // Create perpendicular vectors
        Vector perpendicular1 = new Vector(0, 1, 0);
        if (Math.abs(normalizedNormal.dot(perpendicular1)) > 0.99)
            perpendicular1 = new Vector(1, 0, 0);

        Vector perpendicular2 = normalizedNormal.clone().crossProduct(perpendicular1).normalize();
        perpendicular1 = perpendicular2.clone().crossProduct(normalizedNormal).normalize();

        for (int i = 0; i < points; i++) {
            double angle = 2 * Math.PI * i / points;
            Vector direction = perpendicular1.clone().multiply(Math.cos(angle)).add(perpendicular2.clone().multiply(Math.sin(angle)));
            Location particleLocation = center.clone().add(direction.multiply(radius));
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    /**
     * Draws a sphere of particles centered at the given location.
     *
     * @param center        sphere center
     * @param particle      particle type
     * @param radius        sphere radius
     * @param rings         number of horizontal rings
     * @param pointsPerRing number of points per ring
     * @param offsetX       x-offset for spread
     * @param offsetY       y-offset for spread
     * @param offsetZ       z-offset for spread
     * @param speed         particle speed
     */
    public static void drawSphere(Location center, Particle particle, double radius, int rings, int pointsPerRing, double offsetX, double offsetY, double offsetZ, double speed) {
        for (int i = 0; i < rings; i++) {
            double phi = Math.PI * i / (rings - 1);
            double ringRadius = radius * Math.sin(phi);
            double y = center.y() + radius * Math.cos(phi);

            for (int j = 0; j < pointsPerRing; j++) {
                double theta = 2 * Math.PI * j / pointsPerRing;
                double x = center.x() + ringRadius * Math.cos(theta);
                double z = center.z() + ringRadius * Math.sin(theta);

                Location particleLocation = new Location(center.getWorld(), x, y, z);
                spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            }
        }
    }

    /**
     * Draws a helical particle pattern along the given direction vector.
     *
     * @param start         start location of the helix
     * @param direction     direction vector (axis) of the helix
     * @param particle      particle type
     * @param radius        helix radius
     * @param height        helix height along the direction
     * @param turns         number of turns
     * @param pointsPerTurn number of points per turn
     * @param offsetX       x-offset for spread
     * @param offsetY       y-offset for spread
     * @param offsetZ       z-offset for spread
     * @param speed         particle speed
     */
    public static void drawHelix(Location start, Vector direction, Particle particle, double radius, double height, int turns, int pointsPerTurn, double offsetX, double offsetY, double offsetZ, double speed) {
        Vector normalizedDirection = direction.clone().normalize();

        // Create perpendicular vectors
        Vector perpendicular1 = new Vector(0, 1, 0);
        if (Math.abs(normalizedDirection.dot(perpendicular1)) > 0.99)
            perpendicular1 = new Vector(1, 0, 0);

        Vector perpendicular2 = normalizedDirection.clone().crossProduct(perpendicular1).normalize();
        perpendicular1 = perpendicular2.clone().crossProduct(normalizedDirection).normalize();

        int totalPoints = turns * pointsPerTurn;
        for (int i = 0; i < totalPoints; i++) {
            double angle = 2 * Math.PI * i / pointsPerTurn;
            double heightOffset = height * i / totalPoints;

            Vector circleOffset = perpendicular1.clone().multiply(radius * Math.cos(angle))
                    .add(perpendicular2.clone().multiply(radius * Math.sin(angle)));

            Location particleLocation = start.clone().add(normalizedDirection.clone().multiply(heightOffset)).add(circleOffset);
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    /**
     * Draws a spiral particle pattern that changes radius from start to end over height.
     *
     * @param center        center/base location of the spiral
     * @param particle      particle type
     * @param startRadius   starting radius
     * @param endRadius     ending radius
     * @param height        total vertical height
     * @param turns         number of spiral turns
     * @param pointsPerTurn number of points per turn
     * @param offsetX       x-offset for spread
     * @param offsetY       y-offset for spread
     * @param offsetZ       z-offset for spread
     * @param speed         particle speed
     */
    public static void drawSpiral(Location center, Particle particle, double startRadius, double endRadius, double height, int turns, int pointsPerTurn, double offsetX, double offsetY, double offsetZ, double speed) {
        int totalPoints = turns * pointsPerTurn;

        for (int i = 0; i < totalPoints; i++) {
            double fraction = (double) i / totalPoints;
            double angle = 2 * Math.PI * turns * fraction;
            double radius = startRadius + (endRadius - startRadius) * fraction;

            double x = center.x() + radius * Math.cos(angle);
            double y = center.y() + height * fraction;
            double z = center.z() + radius * Math.sin(angle);

            Location particleLocation = new Location(center.getWorld(), x, y, z);
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    /**
     * Draws a vortex-like particle effect along a direction vector, with radius changing over length.
     *
     * @param center        base location of the vortex
     * @param direction     direction vector (axis) of the vortex
     * @param particle      particle type
     * @param startRadius   starting radius
     * @param endRadius     ending radius
     * @param length        vortex length along the direction
     * @param turns         number of twists/turns
     * @param pointsPerTurn number of points per turn
     * @param offsetX       x-offset for spread
     * @param offsetY       y-offset for spread
     * @param offsetZ       z-offset for spread
     * @param speed         particle speed
     */
    public static void drawVortex(Location center, Vector direction, Particle particle, double startRadius, double endRadius, double length, int turns, int pointsPerTurn, double offsetX, double offsetY, double offsetZ, double speed) {
        Vector normalizedDirection = direction.clone().normalize();

        // Create perpendicular vectors
        Vector perpendicular1 = new Vector(0, 1, 0);
        if (Math.abs(normalizedDirection.dot(perpendicular1)) > 0.99)
            perpendicular1 = new Vector(1, 0, 0);

        Vector perpendicular2 = normalizedDirection.clone().crossProduct(perpendicular1).normalize();
        perpendicular1 = perpendicular2.clone().crossProduct(normalizedDirection).normalize();

        int totalPoints = turns * pointsPerTurn;
        for (int i = 0; i < totalPoints; i++) {
            double fraction = (double) i / totalPoints;
            double angle = 2 * Math.PI * turns * fraction;
            double radius = startRadius + (endRadius - startRadius) * fraction;

            Vector circleOffset = perpendicular1.clone().multiply(radius * Math.cos(angle))
                    .add(perpendicular2.clone().multiply(radius * Math.sin(angle)));

            Location particleLocation = center.clone().add(normalizedDirection.clone().multiply(length * fraction)).add(circleOffset);
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    /**
     * Draws a wave-like particle effect along the X-axis with sinusoidal Y.
     *
     * @param center       center/base location
     * @param particle     particle type
     * @param length       total length along X
     * @param width        total width along Z
     * @param amplitude    wave amplitude along Y
     * @param waves        number of full waves
     * @param pointsPerWave number of samples per wave
     * @param offsetX      x-offset for spread
     * @param offsetY      y-offset for spread
     * @param offsetZ      z-offset for spread
     * @param speed        particle speed
     */
    public static void drawWave(Location center, Particle particle, double length, double width, double amplitude, int waves, int pointsPerWave, double offsetX, double offsetY, double offsetZ, double speed) {
        for (int i = 0; i < waves * pointsPerWave; i++) {
            double x = center.x() + length * i / (waves * pointsPerWave);
            double z = center.z();

            for (int j = 0; j < width / 0.5; j++) {
                double z2 = z - width / 2 + j * 0.5;
                double y = center.y() + amplitude * Math.sin(2 * Math.PI * i / pointsPerWave);

                Location particleLocation = new Location(center.getWorld(), x, y, z2);
                spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            }
        }
    }

    /**
     * Draws a regular polygon of particles around a center.
     *
     * @param center  polygon center
     * @param particle particle type
     * @param radius  distance from center to each vertex
     * @param sides   number of sides (vertices)
     * @param offsetX x-offset for spread
     * @param offsetY y-offset for spread
     * @param offsetZ z-offset for spread
     * @param speed   particle speed
     */
    public static void drawPolygon(Location center, Particle particle, double radius, int sides, double offsetX, double offsetY, double offsetZ, double speed) {
        List<Location> corners = new ArrayList<>();
        for (int i = 0; i < sides; i++) {
            double angle = 2 * Math.PI * i / sides;
            double x = center.x() + radius * Math.cos(angle);
            double z = center.z() + radius * Math.sin(angle);
            corners.add(new Location(center.getWorld(), x, center.y(), z));
        }

        for (int i = 0; i < sides; i++)
            drawLine(corners.get(i), corners.get((i + 1) % sides), particle, 0.2, 1, offsetX, offsetY, offsetZ, speed);
    }

    /**
     * Draws a star-shaped polygon of particles around a center.
     *
     * @param center      star center
     * @param particle    particle type
     * @param outerRadius radius of outer points
     * @param innerRadius radius of inner points
     * @param points      number of star points
     * @param offsetX     x-offset for spread
     * @param offsetY     y-offset for spread
     * @param offsetZ     z-offset for spread
     * @param speed       particle speed
     */
    public static void drawStar(Location center, Particle particle, double outerRadius, double innerRadius, int points, double offsetX, double offsetY, double offsetZ, double speed) {
        List<Location> corners = new ArrayList<>();
        for (int i = 0; i < 2 * points; i++) {
            double angle = Math.PI * i / points;
            double radius = (i % 2 == 0) ? outerRadius : innerRadius;
            double x = center.x() + radius * Math.cos(angle);
            double z = center.z() + radius * Math.sin(angle);
            corners.add(new Location(center.getWorld(), x, center.y(), z));
        }

        for (int i = 0; i < 2 * points; i++)
            drawLine(corners.get(i), corners.get((i + 1) % (2 * points)), particle, 0.2, 1, offsetX, offsetY, offsetZ, speed);
    }

    /**
     * Draws a wireframe cube of particles between two opposite corners.
     *
     * @param corner1 first corner
     * @param corner2 opposite corner (must be in the same world)
     * @param particle particle type
     * @param spacing  spacing between particles along edges
     * @param offsetX  x-offset for spread
     * @param offsetY  y-offset for spread
     * @param offsetZ  z-offset for spread
     * @param speed    particle speed
     * @throws IllegalArgumentException if locations are in different worlds
     */
    public static void drawCube(Location corner1, Location corner2, Particle particle, double spacing, double offsetX, double offsetY, double offsetZ, double speed) {
        World world = corner1.getWorld();
        if (!world.equals(corner2.getWorld()))
            throw new IllegalArgumentException("Locations must be in the same world");

        double minX = Math.min(corner1.x(), corner2.x());
        double minY = Math.min(corner1.y(), corner2.y());
        double minZ = Math.min(corner1.z(), corner2.z());
        double maxX = Math.max(corner1.x(), corner2.x());
        double maxY = Math.max(corner1.y(), corner2.y());
        double maxZ = Math.max(corner1.z(), corner2.z());

        // Draw edges
        for (double x = minX; x <= maxX; x += spacing) {
            spawnParticle(new Location(world, x, minY, minZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, x, minY, maxZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, x, maxY, minZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, x, maxY, maxZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }

        for (double y = minY; y <= maxY; y += spacing) {
            spawnParticle(new Location(world, minX, y, minZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, minX, y, maxZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, maxX, y, minZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, maxX, y, maxZ), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }

        for (double z = minZ; z <= maxZ; z += spacing) {
            spawnParticle(new Location(world, minX, minY, z), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, minX, maxY, z), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, maxX, minY, z), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
            spawnParticle(new Location(world, maxX, maxY, z), particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    /**
     * Starts a repeating task spawning particles that follow an entity.
     *
     * @param entity        entity to follow
     * @param plugin        owning plugin
     * @param particle      particle type
     * @param count         number of particles per spawn
     * @param offsetX       x-offset for spread
     * @param offsetY       y-offset for spread
     * @param offsetZ       z-offset for spread
     * @param speed         particle speed
     * @param durationTicks total duration in ticks, or -1 to run indefinitely
     * @param interval      interval in ticks between spawns
     * @return the scheduled {@link BukkitTask}
     */
    public static ScheduledTask followEntity(Entity entity, Plugin plugin, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, long durationTicks, long interval) {
        ScheduledTask task = Task.syncRepeat(ignored -> {
            if (!entity.isValid())
                return;

            spawnParticle(entity.getLocation().add(0, 1, 0), particle, count, offsetX, offsetY, offsetZ, speed, null, false);
        }, plugin, 0, interval * 50L, TimeUnit.MILLISECONDS);

        if (durationTicks == -1)
            return task;

        // Schedule cancellation after durationTicks
        Task.asyncLater(ignored -> task.cancel(), plugin, durationTicks * 50L, TimeUnit.MILLISECONDS);
        return task;
    }

    /**
     * Draws a parametric curve of particles relative to a center location.
     *
     * @param center            center/base location
     * @param particle          particle type
     * @param parametricFunction function mapping t to a relative position vector
     * @param tStart            starting t value (inclusive)
     * @param tEnd              ending t value (inclusive)
     * @param tStep             step size for t
     * @param offsetX           x-offset for spread
     * @param offsetY           y-offset for spread
     * @param offsetZ           z-offset for spread
     * @param speed             particle speed
     */
    public static void drawParametricCurve(Location center, Particle particle, DoubleFunction<Vector> parametricFunction, double tStart, double tEnd, double tStep, double offsetX, double offsetY, double offsetZ, double speed) {
        for (double t = tStart; t <= tEnd; t += tStep) {
            Vector position = parametricFunction.apply(t);
            Location particleLocation = center.clone().add(position);
            spawnParticle(particleLocation, particle, 1, offsetX, offsetY, offsetZ, speed, null, false);
        }
    }

    /**
     * Plays a frame-based particle animation using a list of locations as frames.
     *
     * @param plugin        owning plugin
     * @param frames        ordered list of locations to play as frames
     * @param particle      particle type
     * @param count         number of particles per frame
     * @param offsetX       x-offset for spread
     * @param offsetY       y-offset for spread
     * @param offsetZ       z-offset for spread
     * @param speed         particle speed
     * @param ticksPerFrame ticks between frames
     * @param durationTicks total animation duration in ticks, or -1 to run indefinitely
     * @param loop          whether to loop frames when the end is reached
     */
    public static void animateParticles(Plugin plugin, List<Location> frames, Particle particle, int count, double offsetX, double offsetY, double offsetZ, double speed, long ticksPerFrame, long durationTicks, boolean loop) {
        final int[] currentFrame = {0};

        ScheduledTask task = Task.syncRepeat(ignored -> {
            if (currentFrame[0] >= frames.size() && loop)
                currentFrame[0] = 0; // Loop animation (optional)

            Location location = frames.get(currentFrame[0]);
            spawnParticle(location, particle, count, offsetX, offsetY, offsetZ, speed, null, false);

            currentFrame[0]++;
        }, plugin, 0, ticksPerFrame * 50L, TimeUnit.MILLISECONDS);

        if (durationTicks == -1)
            return;

        // Schedule cancellation after durationTicks
        Task.asyncLater(ignored -> task.cancel(), plugin, durationTicks * 50L, TimeUnit.MILLISECONDS);
    }

    /**
     * Draws a gradient line of colored dust particles between two locations.
     *
     * @param start         start location
     * @param end           end location (must be in the same world)
     * @param steps         number of steps/samples along the line
     * @param colorFunction function mapping fraction (0..1) to a {@link Color}
     * @param offsetX       x-offset for spread
     * @param offsetY       y-offset for spread
     * @param offsetZ       z-offset for spread
     * @param speed         particle speed
     * @throws IllegalArgumentException if locations are in different worlds
     */
    public static void drawGradient(Location start, Location end, int steps, DoubleFunction<Color> colorFunction, double offsetX, double offsetY, double offsetZ, double speed) {
        if (!start.getWorld().equals(end.getWorld()))
            throw new IllegalArgumentException("Locations must be in the same world");

        Vector direction = end.clone().subtract(start).toVector();
        double distance = direction.length();
        direction.normalize();

        for (int i = 0; i <= steps; i++) {
            double fraction = (double) i / steps;
            Location particleLocation = start.clone().add(direction.clone().multiply(distance * fraction));

            Color color = colorFunction.apply(fraction);
            Particle.DustOptions dustOptions = new Particle.DustOptions(color, 1.0f);

            spawnParticle(particleLocation, Particle.DUST, 1, offsetX, offsetY, offsetZ, speed, dustOptions, false);
        }
    }
}
