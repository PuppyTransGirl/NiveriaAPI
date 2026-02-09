package toutouchien.niveriaapi.menu.test;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.MenuComponent;
import toutouchien.niveriaapi.menu.component.display.Icon;
import toutouchien.niveriaapi.menu.component.display.ProgressBar;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.component.layout.Grid;
import toutouchien.niveriaapi.utils.ColorUtils;
import toutouchien.niveriaapi.utils.Direction;
import toutouchien.niveriaapi.utils.ItemBuilder;

/**
 * A test menu demonstrating dynamic component updates.
 * <p>
 * This menu showcases the new ability to modify component properties
 * after creation and update them in real-time.
 */
@NullMarked
public class DynamicTestMenu extends Menu {
    private static final String PROGRESS_ID = "progress";
    private static final String STATUS_ID = "status";
    private int currentProgress = 0;

    /**
     * Constructs a new DynamicTestMenu for the specified player.
     *
     * @param player the player who will view this menu
     */
    public DynamicTestMenu(Player player) {
        super(player);
    }

    /**
     * Returns the title component for this test menu.
     *
     * @return a colorized title component
     */
    @Override
    protected Component title() {
        return Component.text("Dynamic Component Test", ColorUtils.primaryColor());
    }

    /**
     * Builds the root component layout for this test menu.
     * <p>
     * The layout includes a progress bar, status icon, and buttons to
     * increment, decrement, and reset progress.
     *
     * @param context the menu context
     * @return the root component layout
     */
    @Override
    protected MenuComponent root(MenuContext context) {
        ProgressBar progressBar = ProgressBar.create()
                .id(PROGRESS_ID)
                .doneItem(ItemStack.of(Material.LIME_CONCRETE))
                .currentItem(ItemStack.of(Material.ORANGE_CONCRETE))
                .notDoneItem(ItemStack.of(Material.RED_CONCRETE))
                .direction(Direction.Default.RIGHT)
                .percentage(this.currentProgress / 100D)
                .size(5, 1)
                .build();

        Icon statusIcon = Icon.create()
                .id(STATUS_ID)
                .item(statusItem())
                .build();

        Button decrementButton = Button.create()
                .item(ctx -> {
                    if (this.currentProgress <= 0) {
                        return ItemBuilder.of(Material.GRAY_DYE)
                                .name(Component.text("Min Progress Reached"))
                                .build();
                    }

                    return ItemBuilder.of(Material.RED_DYE)
                            .name(Component.text("Decrement (-10%)"))
                            .build();
                })
                .onClick(click -> {
                    if (this.currentProgress <= 0)
                        return;

                    this.currentProgress -= 10;
                    updateComponents();
                    click.player().sendMessage("Progress: " + currentProgress + "%");
                })
                .build();

        Button incrementButton = Button.create()
                .item(ctx -> {
                    if (this.currentProgress >= 100) {
                        return ItemBuilder.of(Material.GRAY_DYE)
                                .name(Component.text("Max Progress Reached"))
                                .build();
                    }

                    return ItemBuilder.of(Material.LIME_DYE)
                            .name(Component.text("Increment (+10%)"))
                            .build();
                })
                .onClick(click -> {
                    if (this.currentProgress >= 100)
                        return;

                    this.currentProgress += 10;
                    updateComponents();
                    click.player().sendMessage("Progress: " + currentProgress + "%");
                })
                .build();

        Button resetButton = Button.create()
                .item(ItemBuilder.of(Material.BARRIER)
                        .name(Component.text("Reset"))
                        .build())
                .onClick(click -> {
                    this.currentProgress = 0;
                    updateComponents();
                    click.player().sendMessage("Progress reset to 0%");
                })
                .build();

        return Grid.create()
                .size(9, 6)
                .add(11, progressBar)
                .add(31, statusIcon)
                .add(39, decrementButton)
                .add(41, incrementButton)
                .add(49, resetButton)
                .build();
    }

    /**
     * Updates all dynamic components based on the current progress.
     * <p>
     * This demonstrates the new dynamic update capability:
     * - Updates the progress bar percentage
     * - Changes the status icon based on progress level
     */
    private void updateComponents() {
        ProgressBar progressBar = (ProgressBar) this.componentByID(PROGRESS_ID);
        Icon statusIcon = (Icon) this.componentByID(STATUS_ID);

        progressBar.percentage(this.currentProgress());

        statusIcon.item(this.statusItem());

        progressBar.render(this.context);
        statusIcon.render(this.context);
    }

    /**
     * Gets the appropriate status icon based on current progress.
     *
     * @return an ItemStack representing the current status
     */
    private ItemStack statusItem() {
        Material material;
        String status;

        if (this.currentProgress >= 100) {
            material = Material.DIAMOND;
            status = "Complete!";
        } else if (this.currentProgress >= 75) {
            material = Material.EMERALD;
            status = "Almost there!";
        } else if (this.currentProgress >= 50) {
            material = Material.GOLD_INGOT;
            status = "Halfway!";
        } else if (this.currentProgress >= 25) {
            material = Material.IRON_INGOT;
            status = "Making progress...";
        } else if (this.currentProgress > 0) {
            material = Material.COPPER_INGOT;
            status = "Just started";
        } else {
            material = Material.COAL;
            status = "Not started";
        }

        return ItemBuilder.of(material)
                .name(Component.text(status))
                .build();
    }

    private double currentProgress() {
        return (double) currentProgress / 100;
    }
}