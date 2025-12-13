package toutouchien.niveriaapi.menu.test;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.MenuContext;
import toutouchien.niveriaapi.menu.component.display.Icon;
import toutouchien.niveriaapi.menu.component.display.ProgressBar;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.component.interactive.Toggle;
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
public class DynamicTestMenu extends Menu {
    private ProgressBar progressBar;
    private Icon statusIcon;
    private Toggle autoUpdateToggle;
    private Button incrementButton;
    private Button decrementButton;
    private Button resetButton;
    
    private double currentProgress = 0.0;

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
    @NotNull
    @Override
    protected Component title() {
        return Component.text("Dynamic Component Test", ColorUtils.primaryColor());
    }

    /**
     * Creates and returns the root component for this test menu.
     * <p>
     * The menu layout includes:
     * - Progress bar that can be dynamically updated (slots 2-6, row 1)
     * - Status icon that changes based on progress (slot 4, row 3)
     * - Increment button to increase progress (slot 3, row 4)
     * - Decrement button to decrease progress (slot 5, row 4)
     * - Reset button to reset progress (slot 4, row 5)
     * - Toggle for auto-updates (slot 8, row 0)
     *
     * @param context the menu context
     * @return the root grid component containing all test components
     */
    @NotNull
    @Override
    protected toutouchien.niveriaapi.menu.component.Component root(@NotNull MenuContext context) {
        // Create progress bar with initial value
        progressBar = ProgressBar.create()
                .doneItem(ItemStack.of(Material.LIME_CONCRETE))
                .currentItem(ItemStack.of(Material.ORANGE_CONCRETE))
                .notDoneItem(ItemStack.of(Material.RED_CONCRETE))
                .direction(Direction.Default.RIGHT)
                .percentage(currentProgress)
                .size(5, 1)
                .build();
        progressBar.position(2, 1);

        // Create status icon that reflects progress
        statusIcon = Icon.create()
                .item(getStatusItem())
                .build();
        statusIcon.position(4, 3);

        // Create increment button
        incrementButton = Button.create()
                .item(ItemBuilder.of(Material.LIME_DYE)
                        .name(Component.text("Increment (+10%)"))
                        .build())
                .onClick(click -> {
                    currentProgress = Math.min(1.0, currentProgress + 0.1);
                    updateComponents();
                    click.player().sendMessage("Progress: " + (int)(currentProgress * 100) + "%");
                })
                .build();
        incrementButton.position(3, 4);

        // Create decrement button
        decrementButton = Button.create()
                .item(ItemBuilder.of(Material.RED_DYE)
                        .name(Component.text("Decrement (-10%)"))
                        .build())
                .onClick(click -> {
                    currentProgress = Math.max(0.0, currentProgress - 0.1);
                    updateComponents();
                    click.player().sendMessage("Progress: " + (int)(currentProgress * 100) + "%");
                })
                .build();
        decrementButton.position(5, 4);

        // Create reset button
        resetButton = Button.create()
                .item(ItemBuilder.of(Material.BARRIER)
                        .name(Component.text("Reset"))
                        .build())
                .onClick(click -> {
                    currentProgress = 0.0;
                    updateComponents();
                    click.player().sendMessage("Progress reset to 0%");
                })
                .build();
        resetButton.position(4, 5);

        // Create auto-update toggle
        autoUpdateToggle = Toggle.create()
                .onItem(ItemBuilder.of(Material.LIME_DYE)
                        .name(Component.text("Auto-update: ON"))
                        .build())
                .offItem(ItemBuilder.of(Material.RED_DYE)
                        .name(Component.text("Auto-update: OFF"))
                        .build())
                .build();
        autoUpdateToggle.position(8, 0);

        return Grid.create()
                .size(9, 6)
                .add(progressBar.slot(), progressBar)
                .add(statusIcon.slot(), statusIcon)
                .add(incrementButton.slot(), incrementButton)
                .add(decrementButton.slot(), decrementButton)
                .add(resetButton.slot(), resetButton)
                .add(autoUpdateToggle.slot(), autoUpdateToggle)
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
        // Update progress bar
        progressBar.percentage(currentProgress);
        
        // Update status icon based on progress
        statusIcon.item(getStatusItem());
        
        // Re-render all components
        this.update();
    }

    /**
     * Gets the appropriate status icon based on current progress.
     *
     * @return an ItemStack representing the current status
     */
    private ItemStack getStatusItem() {
        Material material;
        String status;
        
        if (currentProgress >= 1.0) {
            material = Material.DIAMOND;
            status = "Complete!";
        } else if (currentProgress >= 0.75) {
            material = Material.EMERALD;
            status = "Almost there!";
        } else if (currentProgress >= 0.5) {
            material = Material.GOLD_INGOT;
            status = "Halfway!";
        } else if (currentProgress >= 0.25) {
            material = Material.IRON_INGOT;
            status = "Making progress...";
        } else if (currentProgress > 0.0) {
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
    
    /**
     * Gets the current progress value.
     *
     * @return the current progress (0.0 to 1.0)
     */
    public double getProgress() {
        return currentProgress;
    }
    
    /**
     * Sets the progress to a specific value and updates the display.
     *
     * @param progress the new progress value (0.0 to 1.0)
     */
    public void setProgress(double progress) {
        this.currentProgress = Math.clamp(progress, 0.0, 1.0);
        updateComponents();
    }
}
