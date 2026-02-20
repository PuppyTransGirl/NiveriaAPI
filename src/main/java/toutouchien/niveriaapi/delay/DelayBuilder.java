package toutouchien.niveriaapi.delay;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.index.qual.Positive;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import toutouchien.niveriaapi.lang.Lang;

import java.util.function.Consumer;

import static toutouchien.niveriaapi.NiveriaAPI.LANG;

/**
 * Builder for creating {@link Delay} instances for a specific player.
 * <p>
 * Provides a fluent API to configure:
 * <ul>
 *     <li>Messages displayed during the delay and on cancellation.</li>
 *     <li>Callbacks for success and failure.</li>
 *     <li>Duration and movement-cancellation behavior.</li>
 *     <li>Output channels (actionbar, chat, title).</li>
 * </ul>
 * <p>
 * Default messages are loaded from {@link Lang}:
 * <ul>
 *     <li>{@code delay.start}</li>
 *     <li>{@code delay.moved}</li>
 *     <li>{@code delay.already}</li>
 * </ul>
 */
@NullMarked
public class DelayBuilder {
    private final Player player;

    private Component text;
    private Component movedText;
    private Component alreadyHasDelayText;

    @Nullable
    private Consumer<Player> successConsumer;
    @Nullable
    private Consumer<Player> failConsumer;
    private int delay;
    private boolean cancelOnMove;
    private boolean actionbar;
    private boolean chat;
    private boolean title;

    /**
     * Creates a new {@code DelayBuilder} for the given player, pre-populated
     * with default messages from {@link Lang}.
     *
     * @param player target player for the delay
     */
    private DelayBuilder(Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.player = player;
        this.text = LANG.get("delay.start");
        this.movedText = LANG.get("delay.moved");
        this.alreadyHasDelayText = LANG.get("delay.already");
    }

    /**
     * Creates a new {@link DelayBuilder} instance for the specified player.
     *
     * @param player target player
     * @return new builder instance
     */
    public static DelayBuilder of(Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        return new DelayBuilder(player);
    }

    /**
     * Sets the main delay message shown while the delay is active.
     *
     * @param text non-null main delay text
     * @return this builder for chaining
     */
    public DelayBuilder text(Component text) {
        Preconditions.checkNotNull(text, "text cannot be null");

        this.text = text;
        return this;
    }

    /**
     * Sets the message shown when the delay is cancelled due to movement.
     *
     * @param movedText non-null movement cancellation text
     * @return this builder for chaining
     */
    public DelayBuilder movedText(Component movedText) {
        Preconditions.checkNotNull(movedText, "movedText cannot be null");

        this.movedText = movedText;
        return this;
    }

    /**
     * Sets the message shown if the player already has an active delay.
     *
     * @param alreadyHasDelayText non-null "already has delay" text
     * @return this builder for chaining
     */
    public DelayBuilder alreadyHasDelayText(Component alreadyHasDelayText) {
        Preconditions.checkNotNull(alreadyHasDelayText, "alreadyHasDelayText cannot be null");

        this.alreadyHasDelayText = alreadyHasDelayText;
        return this;
    }

    /**
     * Sets all three delay-related texts at once.
     *
     * @param text                main delay text
     * @param movedText           movement cancellation text
     * @param alreadyHasDelayText "already has delay" text
     * @return this builder for chaining
     */
    public DelayBuilder texts(Component text, Component movedText, Component alreadyHasDelayText) {
        Preconditions.checkNotNull(text, "text cannot be null");
        Preconditions.checkNotNull(movedText, "movedText cannot be null");
        Preconditions.checkNotNull(alreadyHasDelayText, "alreadyHasDelayText cannot be null");

        this.text = text;
        this.movedText = movedText;
        this.alreadyHasDelayText = alreadyHasDelayText;
        return this;
    }

    /**
     * Sets the callback to be executed when the delay completes successfully.
     *
     * @param successConsumer callback or {@code null} to clear
     * @return this builder for chaining
     */
    public DelayBuilder successConsumer(@Nullable Consumer<Player> successConsumer) {
        this.successConsumer = successConsumer;
        return this;
    }

    /**
     * Sets the callback to be executed when the delay fails or is cancelled.
     *
     * @param failConsumer callback or {@code null} to clear
     * @return this builder for chaining
     */
    public DelayBuilder failConsumer(@Nullable Consumer<Player> failConsumer) {
        this.failConsumer = failConsumer;
        return this;
    }

    /**
     * Sets the total delay duration in ticks.
     *
     * @param delay delay duration (>= 1)
     * @return this builder for chaining
     * @throws IllegalArgumentException if {@code delay < 1}
     */
    public DelayBuilder delay(@Positive int delay) {
        Preconditions.checkArgument(delay >= 1, "delay cannot be less than 1: %s", delay);

        this.delay = delay;
        return this;
    }

    /**
     * Enables or disables cancelling the delay when the player moves
     * from the original location.
     *
     * @param cancelOnMove {@code true} to cancel on movement
     * @return this builder for chaining
     */
    public DelayBuilder cancelOnMove(boolean cancelOnMove) {
        this.cancelOnMove = cancelOnMove;
        return this;
    }

    /**
     * Enables or disables showing the delay message in the actionbar.
     *
     * @param actionbar {@code true} to use actionbar
     * @return this builder for chaining
     */
    public DelayBuilder actionbar(boolean actionbar) {
        this.actionbar = actionbar;
        return this;
    }

    /**
     * Enables or disables showing the delay message in chat.
     *
     * @param chat {@code true} to use chat
     * @return this builder for chaining
     */
    public DelayBuilder chat(boolean chat) {
        this.chat = chat;
        return this;
    }

    /**
     * Enables or disables showing the delay message as a title.
     *
     * @param title {@code true} to use title
     * @return this builder for chaining
     */
    public DelayBuilder title(boolean title) {
        this.title = title;
        return this;
    }

    /**
     * Sets all visual output flags at once.
     *
     * @param actionbar whether to use actionbar
     * @param chat      whether to use chat
     * @param title     whether to use title
     * @return this builder for chaining
     */
    public DelayBuilder visuals(boolean actionbar, boolean chat, boolean title) {
        this.actionbar = actionbar;
        this.chat = chat;
        this.title = title;
        return this;
    }

    /**
     * Builds a new {@link Delay} instance using the configured values.
     * <p>
     * The {@link Delay} is not automatically started; it must be registered
     * or scheduled by the caller.
     *
     * @return new {@link Delay} instance
     */
    public Delay build() {
        return new Delay(
                player,
                text, movedText, alreadyHasDelayText,
                successConsumer, failConsumer,
                delay,
                cancelOnMove,
                actionbar, chat, title
        );
    }
}
