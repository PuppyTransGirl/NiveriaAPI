package toutouchien.niveriaapi.menu.test;

import it.unimi.dsi.fastutil.objects.ObjectList;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import toutouchien.niveriaapi.menu.Menu;
import toutouchien.niveriaapi.menu.component.display.Icon;
import toutouchien.niveriaapi.menu.component.interactive.Button;
import toutouchien.niveriaapi.menu.component.interactive.Selector;
import toutouchien.niveriaapi.menu.component.interactive.Toggle;
import toutouchien.niveriaapi.menu.component.layout.Grid;
import toutouchien.niveriaapi.utils.ColorUtils;
import toutouchien.niveriaapi.utils.ItemBuilder;

public class TestMenu extends Menu {
    public TestMenu(Player player) {
        super(player);
    }

    @NotNull
    @Override
    protected Component title() {
        return Component.text("Test Menu Hehe :3", ColorUtils.primaryColor());
    }

    @NotNull
    @Override
    protected toutouchien.niveriaapi.menu.component.Component root() {
        return Grid.create()
                .add(0, simpleButton())
                .add(2, animatedButton())
                .add(8, dynamicButton())
                .add(13, coordinatesDynamicButton())
                .add(15, toggle())
                .add(17, icon())
                .add(18, selector())
                .build();

    }

    private static Button simpleButton() {
        return Button.create()
                .item(ItemStack.of(Material.APPLE))
                .onClick(click -> {
                    click.player().sendRichMessage("<rainbow>You clicked the apple!");
                })
                .onDrop(click -> {
                    click.player().sendRichMessage("<red>Why?");
                    click.player().closeInventory();
                })
                .build();
    }

    private static Button animatedButton() {
        return Button.create()
                .size(2, 2)
                .animationFrames(context ->
                        ObjectList.of(ItemStack.of(Material.RED_WOOL),
                                ItemStack.of(Material.ORANGE_WOOL),
                                ItemStack.of(Material.YELLOW_WOOL),
                                ItemStack.of(Material.LIME_WOOL),
                                ItemStack.of(Material.BLUE_WOOL),
                                ItemStack.of(Material.PURPLE_WOOL))
                )
                .animationInterval(5)
                .onClick(click -> {
                    click.player().sendRichMessage("<rainbow>You clicked the animated button!");
                })
                .build();
    }

    private static Button dynamicButton() {
        return Button.create()
                .dynamicItem(context -> {
                    int seconds = (int) (System.currentTimeMillis() / 1000 % 60);
                    return ItemBuilder.of(Material.OAK_SIGN).name(
                            Component.text("Seconds: " + seconds)
                    ).build();
                })
                .updateInterval(20)
                .onClick(click -> {
                    click.player().sendRichMessage("<green>This button shows the current seconds!");
                })
                .build();
    }

    private static Button coordinatesDynamicButton() {
        return Button.create()
                .dynamicItem(context -> {
                    Player player = context.menu().player();
                    double x = player.getLocation().getX();
                    double y = player.getLocation().getY();
                    double z = player.getLocation().getZ();
                    return ItemBuilder.of(Material.COMPASS).name(
                            Component.text(String.format("Coordinates: (%.1f, %.1f, %.1f)", x, y, z))
                    ).build();
                })
                .updateInterval(1)
                .onClick(click -> {
                    click.player().sendRichMessage("<green>This button shows your current coordinates!");
                })
                .build();
    }

    private static Toggle toggle() {
        return Toggle.create()
                .onItem(ItemStack.of(Material.LIME_DYE))
                .offItem(ItemStack.of(Material.RED_DYE))
                .build();
    }

    private static Icon icon() {
        return Icon.create()
                .item(ItemBuilder.of(Material.BEDROCK)
                        .name(Component.text("Just a useless item"))
                        .build())
                .build();
    }

    private static Selector<GameMode> selector() {
        return Selector.<GameMode>create()
                .addOption(ItemBuilder.of(Material.WOODEN_SWORD).name(Component.text("Survival")).build(), GameMode.SURVIVAL)
                .addOption(ItemBuilder.of(Material.COMPASS).name(Component.text("Adventure")).build(), GameMode.ADVENTURE)
                .addOption(ItemBuilder.of(Material.DIAMOND_BLOCK).name(Component.text("Creative")).build(), GameMode.CREATIVE)
                .defaultOption(context -> context.player().getGameMode())
                .onSelectionChange(event -> event.context().player().setGameMode(event.newValue()))
                .build();
    }
}
