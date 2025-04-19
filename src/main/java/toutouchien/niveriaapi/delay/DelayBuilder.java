package toutouchien.niveriaapi.delay;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import toutouchien.niveriaapi.utils.ui.MessageUtils;

import java.util.function.Consumer;

public class DelayBuilder {
	private final Player player;
	private Component text;
	private Consumer<Player> successConsumer;
	private Consumer<Player> failConsumer;
	private int delay;
	private boolean cancelOnMove;
	private boolean actionbar;
	private boolean chat;
	private boolean title;

	public static DelayBuilder of(Player player) {
		return new DelayBuilder(player);
	}

	private DelayBuilder(Player player) {
		this.player = player;
		this.text = MessageUtils.infoMessage(
				Component.text("Téléportation dans %s secondes")
		);
	}

	public DelayBuilder text(Component text) {
		this.text = text;
		return this;
	}

	public DelayBuilder successConsumer(Consumer<Player> successConsumer) {
		this.successConsumer = successConsumer;
		return this;
	}

	public DelayBuilder failConsumer(Consumer<Player> failConsumer) {
		this.failConsumer = failConsumer;
		return this;
	}

	public DelayBuilder delay(int delay) {
		this.delay = delay;
		return this;
	}

	public DelayBuilder cancelOnMove(boolean cancelOnMove) {
		this.cancelOnMove = cancelOnMove;
		return this;
	}

	public DelayBuilder actionbar(boolean actionbar) {
		this.actionbar = actionbar;
		return this;
	}

	public DelayBuilder chat(boolean chat) {
		this.chat = chat;
		return this;
	}

	public DelayBuilder title(boolean title) {
		this.title = title;
		return this;
	}

	public DelayBuilder visuals(boolean actionbar, boolean chat, boolean title) {
		this.actionbar = actionbar;
		this.chat = chat;
		this.title = title;
		return this;
	}

	public Delay build() {
		if (player == null)
			throw new IllegalArgumentException("Player cannot be null.");

		if (delay < 1)
			throw new IllegalArgumentException("Delay must be more than 0. Delay: " + delay);

		return new Delay(player, text, successConsumer, failConsumer, delay, cancelOnMove, actionbar, chat, title);
	}
}