package toutouchien.niveriaapi.delay;

import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class Delay {
	private final Player player;
	private final Component text;
	private final Consumer<Player> successConsumer;
	private final Consumer<Player> failConsumer;
	private final Location originalLocation;
	private final boolean cancelOnMove;
	private final boolean actionbar, chat, title;

	private final int delay;
	private int delayRemaining;

	Delay(@NotNull Player player, Component text, Consumer<Player> successConsumer, Consumer<Player> failConsumer, int delay, boolean cancelOnMove, boolean actionbar, boolean chat, boolean title) {
		this.player = player;
		this.text = text;
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

	public Player player() {
		return player;
	}

	public Component text() {
		return text;
	}

	public Consumer<Player> successConsumer() {
		return successConsumer;
	}

	public Consumer<Player> failConsumer() {
		return failConsumer;
	}

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

	public int delay() {
		return delay;
	}

	public int delayRemaining() {
		return delayRemaining;
	}
}
