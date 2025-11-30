package toutouchien.niveriaapi.delay;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

/**
 * Represents a delayed action for a specific player.
 * <p>
 * A {@code Delay} encapsulates:
 * <ul>
 *     <li>How long the delay should last.</li>
 *     <li>Messages shown during the delay and on movement/collision with
 *         another delay.</li>
 *     <li>Whether movement should cancel the delay.</li>
 *     <li>Callbacks to execute on success or failure.</li>
 *     <li>Which channels (actionbar, chat, title) should be used to display
 *         the delay message.</li>
 * </ul>
 * Instances are typically created and managed by a dedicated delay manager.
 */
public class Delay {
    private final Player player;

    private final Component text;
    private final Component movedText;
    private final Component alreadyHasDelayText;

    private final Consumer<Player> successConsumer;
    private final Consumer<Player> failConsumer;
    private final Location originalLocation;
    private final boolean cancelOnMove;
    private final boolean actionbar, chat, title;

    private final int delay;
    private int delayRemaining;

    /**
     * Constructs a new {@code Delay} for the given player.
     *
     * @param player              player affected by this delay
     * @param text                main delay text to display
     * @param movedText           text shown if the delay is cancelled due to movement
     * @param alreadyHasDelayText text shown if the player already has a delay
     * @param successConsumer     optional callback invoked when the delay finishes successfully
     * @param failConsumer        optional callback invoked when the delay is cancelled or fails
     * @param delay               total delay duration in ticks (must be &gt; 0)
     * @param cancelOnMove        whether moving from the original location cancels the delay
     * @param actionbar           whether the main delay text should be sent via actionbar
     * @param chat                whether the main delay text should be sent in chat
     * @param title               whether the main delay text should be sent as a title
     */
    Delay(
            @NotNull Player player,
            @NotNull Component text,
            @NotNull Component movedText,
            @NotNull Component alreadyHasDelayText,
            @Nullable Consumer<Player> successConsumer,
            @Nullable Consumer<Player> failConsumer,
            @Positive int delay,
            boolean cancelOnMove,
            boolean actionbar,
            boolean chat,
            boolean title
    ) {
        this.player = player;

        this.text = text;
        this.movedText = movedText;
        this.alreadyHasDelayText = alreadyHasDelayText;

        this.successConsumer = successConsumer;
        this.failConsumer = failConsumer;
        this.originalLocation = player.getLocation();
        this.cancelOnMove = cancelOnMove;

        this.delay = delay;
        this.delayRemaining = delay;

        this.actionbar = actionbar;
        this.chat = chat;
        this.title = title;
    }

    /**
     * Updates the remaining delay time for this {@code Delay}.
     *
     * @param delayRemaining new remaining delay in ticks (must be &gt;= 0)
     */
    public void delayRemaining(int delayRemaining) {
        this.delayRemaining = delayRemaining;
    }

    /**
     * Returns the player associated with this delay.
     *
     * @return non-null player
     */
    @NotNull
    public Player player() {
        return player;
    }

    /**
     * Returns the main delay text that should be displayed while the delay
     * is active.
     *
     * @return non-null main delay message
     */
    @NotNull
    public Component text() {
        return text;
    }

    /**
     * Returns the text that should be displayed if the delay is cancelled
     * due to the player moving (when {@link #cancelOnMove()} is {@code true}).
     *
     * @return non-null movement cancellation message
     */
    @NotNull
    public Component movedText() {
        return movedText;
    }

    /**
     * Returns the text that should be displayed when the player already
     * has an active delay and a new one is attempted.
     *
     * @return non-null "already has delay" message
     */
    @NotNull
    public Component alreadyHasDelayText() {
        return alreadyHasDelayText;
    }

    /**
     * Returns the callback that will be executed when the delay completes
     * successfully, if any.
     *
     * @return success callback or {@code null} if none
     */
    @Nullable
    public Consumer<Player> successConsumer() {
        return successConsumer;
    }

    /**
     * Returns the callback that will be executed when the delay fails or
     * is cancelled, if any.
     *
     * @return failure callback or {@code null} if none
     */
    @Nullable
    public Consumer<Player> failConsumer() {
        return failConsumer;
    }

    /**
     * Returns the location where the delay was started.
     * <p>
     * This is typically used to determine whether the player has moved,
     * when {@link #cancelOnMove()} is enabled.
     *
     * @return non-null original location
     */
    @NotNull
    public Location originalLocation() {
        return originalLocation;
    }

    /**
     * Indicates whether this delay should be cancelled if the player
     * moves away from the original location.
     *
     * @return {@code true} if movement cancels the delay
     */
    public boolean cancelOnMove() {
        return cancelOnMove;
    }

    /**
     * Indicates whether the main delay text should be displayed via the
     * actionbar.
     *
     * @return {@code true} if actionbar display is enabled
     */
    public boolean actionbar() {
        return actionbar;
    }

    /**
     * Indicates whether the main delay text should be displayed in chat.
     *
     * @return {@code true} if chat display is enabled
     */
    public boolean chat() {
        return chat;
    }

    /**
     * Indicates whether the main delay text should be displayed as a title.
     *
     * @return {@code true} if title display is enabled
     */
    public boolean title() {
        return title;
    }

    /**
     * Returns the total delay duration in ticks configured for this delay.
     *
     * @return total delay duration (&gt; 0)
     */
    @Positive
    public int delay() {
        return delay;
    }

    /**
     * Returns the remaining delay time in ticks.
     *
     * @return remaining delay (never negative)
     */
    @NonNegative
    public int delayRemaining() {
        return delayRemaining;
    }
}