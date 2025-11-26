package toutouchien.niveriaapi.delay;

import com.google.common.base.Preconditions;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import toutouchien.niveriaapi.lang.Lang;

import java.util.function.Consumer;

public class DelayBuilder {
    private final Player player;

    private Component text;
    private Component movedText;
    private Component alreadyHasDelayText;

    private Consumer<Player> successConsumer;
    private Consumer<Player> failConsumer;
    private int delay;
    private boolean cancelOnMove;
    private boolean actionbar;
    private boolean chat;
    private boolean title;

    private DelayBuilder(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        this.player = player;
        this.text = Lang.get("niveriaapi_delay_default_text");
        this.movedText = Lang.get("niveriaapi_delay_default_moved_text");
        this.alreadyHasDelayText = Lang.get("niveriaapi_delay_default_already_has_delay_text");
    }

    @NotNull
    public static DelayBuilder of(@NotNull Player player) {
        Preconditions.checkNotNull(player, "player cannot be null");

        return new DelayBuilder(player);
    }

    @NotNull
    public DelayBuilder text(@NotNull Component text) {
        Preconditions.checkNotNull(text, "text cannot be null");

        this.text = text;
        return this;
    }

    @NotNull
    public DelayBuilder movedText(@NotNull Component movedText) {
        Preconditions.checkNotNull(movedText, "movedText cannot be null");

        this.movedText = movedText;
        return this;
    }

    @NotNull
    public DelayBuilder alreadyHasDelayText(@NotNull Component alreadyHasDelayText) {
        Preconditions.checkNotNull(alreadyHasDelayText, "alreadyHasDelayText cannot be null");

        this.alreadyHasDelayText = alreadyHasDelayText;
        return this;
    }

    @NotNull
    public DelayBuilder texts(@NotNull Component text, @NotNull Component movedText, @NotNull Component alreadyHasDelayText) {
        Preconditions.checkNotNull(text, "text cannot be null");
        Preconditions.checkNotNull(movedText, "movedText cannot be null");
        Preconditions.checkNotNull(alreadyHasDelayText, "alreadyHasDelayText cannot be null");

        this.text = text;
        this.movedText = movedText;
        this.alreadyHasDelayText = alreadyHasDelayText;
        return this;
    }

    @NotNull
    public DelayBuilder successConsumer(@Nullable Consumer<Player> successConsumer) {
        this.successConsumer = successConsumer;
        return this;
    }

    @NotNull
    public DelayBuilder failConsumer(@Nullable Consumer<Player> failConsumer) {
        this.failConsumer = failConsumer;
        return this;
    }

    @NotNull
    public DelayBuilder delay(@Positive int delay) {
        Preconditions.checkArgument(delay >= 1, "delay cannot be less than 1: %d", delay);

        this.delay = delay;
        return this;
    }

    @NotNull
    public DelayBuilder cancelOnMove(boolean cancelOnMove) {
        this.cancelOnMove = cancelOnMove;
        return this;
    }

    @NotNull
    public DelayBuilder actionbar(boolean actionbar) {
        this.actionbar = actionbar;
        return this;
    }

    @NotNull
    public DelayBuilder chat(boolean chat) {
        this.chat = chat;
        return this;
    }

    @NotNull
    public DelayBuilder title(boolean title) {
        this.title = title;
        return this;
    }

    @NotNull
    public DelayBuilder visuals(boolean actionbar, boolean chat, boolean title) {
        this.actionbar = actionbar;
        this.chat = chat;
        this.title = title;
        return this;
    }

    @NotNull
    public Delay build() {
        return new Delay(player, text, movedText, alreadyHasDelayText, successConsumer, failConsumer, delay, cancelOnMove, actionbar, chat, title);
    }
}