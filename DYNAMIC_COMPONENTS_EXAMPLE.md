# Dynamic Component Updates - Usage Guide

This document demonstrates how to dynamically modify menu components in real-time.

## Overview

All menu components now support dynamic updates. You can modify component properties after creation and call `menu.update()` or `menu.update(component)` to reflect the changes in the displayed inventory.

## Examples

### Example 1: Dynamic ProgressBar

```java
public class MyMenu extends Menu {
    private ProgressBar progressBar;
    
    public MyMenu(Player player) {
        super(player);
    }
    
    @Override
    protected Component root(MenuContext context) {
        // Create a progress bar with initial percentage
        progressBar = ProgressBar.create()
            .percentage(0.0)
            .size(5, 1)
            .build();
        progressBar.position(2, 2);
        
        return Grid.create()
            .size(9, 3)
            .add(progressBar.slot(), progressBar)
            .build();
    }
    
    // Update the progress bar percentage dynamically
    public void updateProgress(double newPercentage) {
        progressBar.percentage(newPercentage);
        this.update(progressBar);  // Re-render the progress bar
    }
}
```

### Example 2: Dynamic Icon

```java
public class MyMenu extends Menu {
    private Icon statusIcon;
    
    @Override
    protected Component root(MenuContext context) {
        statusIcon = Icon.create()
            .item(ItemStack.of(Material.RED_CONCRETE))
            .build();
        statusIcon.position(4, 1);
        
        return Grid.create()
            .size(9, 3)
            .add(statusIcon.slot(), statusIcon)
            .build();
    }
    
    // Update the icon dynamically
    public void setStatus(boolean success) {
        Material material = success ? Material.LIME_CONCRETE : Material.RED_CONCRETE;
        statusIcon.item(ItemStack.of(material));
        this.update(statusIcon);
    }
}
```

### Example 3: Dynamic Toggle State

```java
public class MyMenu extends Menu {
    private Toggle toggle;
    
    @Override
    protected Component root(MenuContext context) {
        toggle = Toggle.create()
            .onItem(ItemStack.of(Material.LIME_DYE))
            .offItem(ItemStack.of(Material.RED_DYE))
            .build();
        toggle.position(4, 2);
        
        return Grid.create()
            .size(9, 3)
            .add(toggle.slot(), toggle)
            .build();
    }
    
    // Programmatically change toggle state
    public void setToggleState(boolean enabled) {
        toggle.state(enabled);
        this.update(toggle);
    }
    
    // Get current toggle state
    public boolean getToggleState() {
        return toggle.state();
    }
}
```

### Example 4: Dynamic Button Content

```java
public class MyMenu extends Menu {
    private Button button;
    
    @Override
    protected Component root(MenuContext context) {
        button = Button.create()
            .item(ItemStack.of(Material.STONE))
            .onClick(click -> {
                click.player().sendMessage("Button clicked!");
            })
            .build();
        button.position(4, 2);
        
        return Grid.create()
            .size(9, 3)
            .add(button.slot(), button)
            .build();
    }
    
    // Update button appearance
    public void updateButtonItem(Material material) {
        button.item(ItemStack.of(material));
        this.update(button);
    }
}
```

### Example 5: Dynamic Selector

```java
public class MyMenu extends Menu {
    private Selector<String> selector;
    
    @Override
    protected Component root(MenuContext context) {
        selector = Selector.<String>create()
            .addOption(ItemStack.of(Material.RED_WOOL), "red")
            .addOption(ItemStack.of(Material.BLUE_WOOL), "blue")
            .addOption(ItemStack.of(Material.GREEN_WOOL), "green")
            .build();
        selector.position(4, 2);
        
        return Grid.create()
            .size(9, 3)
            .add(selector.slot(), selector)
            .build();
    }
    
    // Programmatically change selection
    public void selectColor(String color) {
        selector.selection(color);
        this.update(selector);
    }
    
    // Get current selection
    public String getCurrentColor() {
        return selector.selection();
    }
    
    // Add a new option dynamically
    public void addNewColor(String name, Material material) {
        selector.addOption(ItemStack.of(material), name);
        this.update(selector);
    }
}
```

## API Methods

### Menu Class
- `update()` - Re-renders the entire menu
- `update(Component component)` - Re-renders a specific component

### ProgressBar
- `percentage(double percentage)` - Set percentage value (0.0 to 1.0)
- `percentage(Object2DoubleFunction<MenuContext> percentage)` - Set percentage function
- `doneItem(ItemStack item)` - Set completed section item
- `currentItem(ItemStack item)` - Set current position item
- `notDoneItem(ItemStack item)` - Set incomplete section item
- `direction(Direction.Default direction)` - Set fill direction

### Icon
- `item(ItemStack item)` - Set display item
- `item(Function<MenuContext, ItemStack> item)` - Set item function
- `sound(Sound sound)` - Set click sound

### Toggle
- `state(boolean state)` - Set toggle state
- `state()` - Get toggle state
- `onItem(ItemStack item)` - Set on-state item
- `offItem(ItemStack item)` - Set off-state item
- `sound(Sound sound)` - Set click sound

### Button
- `item(ItemStack item)` - Set button item
- `dynamicItem(Function<MenuContext, ItemStack> dynamicItem)` - Set dynamic content
- `animationFrames(Function<MenuContext, ObjectList<ItemStack>> frames)` - Set animation frames
- `animationInterval(int ticks)` - Set animation interval
- `updateInterval(int ticks)` - Set update interval
- `sound(Sound sound)` - Set click sound
- `onClick(Consumer<NiveriaInventoryClickEvent> handler)` - Set click handler
- `onLeftClick(Consumer<NiveriaInventoryClickEvent> handler)` - Set left-click handler
- `onRightClick(Consumer<NiveriaInventoryClickEvent> handler)` - Set right-click handler
- `onDrop(Consumer<NiveriaInventoryClickEvent> handler)` - Set drop handler

### Selector
- `selection(T value)` - Set current selection by value
- `selection()` - Get current selection value
- `selectionIndex(int index)` - Set selection by index
- `selectionIndex()` - Get current selection index
- `addOption(ItemStack item, T value)` - Add new option
- `clearOptions()` - Remove all options
- `sound(Sound sound)` - Set click sound
- `onSelectionChange(Consumer<SelectionChangeEvent<T>> handler)` - Set change handler

### DoubleDropButton
- `item(ItemStack item)` - Set normal item
- `dropItem(ItemStack item)` - Set drop-state item
- `sound(Sound sound)` - Set click sound
- `onClick(Consumer<NiveriaInventoryClickEvent> handler)` - Set click handler
- `onDoubleDrop(Consumer<NiveriaInventoryClickEvent> handler)` - Set double-drop handler

## Important Notes

1. Always call `menu.update()` or `menu.update(component)` after modifying component properties to see changes in the inventory.
2. Component dimensions (width, height) cannot be changed after creation.
3. Dynamic functions (like percentage functions in ProgressBar) can still be used for automatic updates.
4. The update methods are thread-safe and can be called from any thread.
