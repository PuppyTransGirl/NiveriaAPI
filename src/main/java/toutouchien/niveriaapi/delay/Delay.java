package toutouchien.niveriaapi.delay;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.index.qual.Positive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

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

    Delay(@NotNull Player player, @NotNull Component text, @NotNull Component movedText, @NotNull Component alreadyHasDelayText, @Nullable Consumer<Player> successConsumer, @Nullable Consumer<Player> failConsumer, @Positive int delay, boolean cancelOnMove, boolean actionbar, boolean chat, boolean title) {
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

    public void delayRemaining(int delayRemaining) {
        this.delayRemaining = delayRemaining;
    }

    @NotNull
    public Player player() {
        return player;
    }

    @NotNull
    public Component text() {
        return text;
    }

    @NotNull
    public Component movedText() {
        return movedText;
    }

    @NotNull
    public Component alreadyHasDelayText() {
        return alreadyHasDelayText;
    }

    @Nullable
    public Consumer<Player> successConsumer() {
        return successConsumer;
    }

    @Nullable
    public Consumer<Player> failConsumer() {
        return failConsumer;
    }

    @NotNull
    public Location originalLocation() {
        return originalLocation;
    }

    public boolean cancelOnMove() {
        return cancelOnMove;
    }

    public boolean actionbar() {
        return actionbar;
    }

    public boolean chat() {
        return chat;
    }

    public boolean title() {
        return title;
    }

    @Positive
    public int delay() {
        return delay;
    }

    @NonNegative
    public int delayRemaining() {
        return delayRemaining;
    }
}
