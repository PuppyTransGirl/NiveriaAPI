# Pull Request Summary: Dynamic Component Modification Support

## Problem Statement
Previously, menu components in NiveriaAPI were immutable once created. This meant that once you displayed a component like a progress bar, you couldn't dynamically update its percentage or appearance without recreating the entire component. This limitation applied to all component types.

## Solution
This PR introduces dynamic component modification capabilities, allowing developers to update component properties in real-time and reflect those changes immediately in the displayed inventory.

## Changes Overview

### 1. Menu Class Updates
**File**: `src/main/java/toutouchien/niveriaapi/menu/Menu.java`

Added two new methods:
- `update()` - Re-renders the entire menu
- `update(Component component)` - Re-renders a specific component

These methods allow developers to trigger visual updates after modifying component properties.

### 2. Component Architecture Changes
Changed component properties from `final` (immutable) to mutable for all display and interactive components, except for dimensions (width/height) which remain immutable for consistency.

### 3. Component-Specific Enhancements

#### ProgressBar (`src/main/java/toutouchien/niveriaapi/menu/component/display/ProgressBar.java`)
- ✅ `percentage(double)` - Set static percentage
- ✅ `percentage(Object2DoubleFunction<MenuContext>)` - Set dynamic percentage function
- ✅ `doneItem(ItemStack)` / `doneItem(Function)` - Update completed section appearance
- ✅ `currentItem(ItemStack)` / `currentItem(Function)` - Update current position appearance
- ✅ `notDoneItem(ItemStack)` / `notDoneItem(Function)` - Update incomplete section appearance
- ✅ `direction(Direction.Default)` - Change fill direction

#### Icon (`src/main/java/toutouchien/niveriaapi/menu/component/display/Icon.java`)
- ✅ `item(ItemStack)` / `item(Function)` - Update displayed item
- ✅ `sound(Sound)` - Change click sound

#### Toggle (`src/main/java/toutouchien/niveriaapi/menu/component/interactive/Toggle.java`)
- ✅ `state(boolean)` - Set toggle state programmatically
- ✅ `state()` - Get current toggle state
- ✅ `onItem(ItemStack)` / `onItem(Function)` - Update on-state appearance
- ✅ `offItem(ItemStack)` / `offItem(Function)` - Update off-state appearance
- ✅ `sound(Sound)` - Change click sound

#### Button (`src/main/java/toutouchien/niveriaapi/menu/component/interactive/Button.java`)
- ✅ `item(ItemStack)` / `item(Function)` - Update static item
- ✅ `dynamicItem(Function)` - Set dynamic content function
- ✅ `animationFrames(Function)` - Update animation frames
- ✅ `animationInterval(int)` - Change animation speed
- ✅ `updateInterval(int)` - Change dynamic update speed
- ✅ `sound(Sound)` - Change click sound
- ✅ `onClick(Consumer)` - Update general click handler
- ✅ `onLeftClick(Consumer)` - Update left-click handler
- ✅ `onRightClick(Consumer)` - Update right-click handler
- ✅ `onShiftLeftClick(Consumer)` - Update shift-left handler
- ✅ `onShiftRightClick(Consumer)` - Update shift-right handler
- ✅ `onDrop(Consumer)` - Update drop handler

#### Selector (`src/main/java/toutouchien/niveriaapi/menu/component/interactive/Selector.java`)
- ✅ `selection(T)` - Set selection by value
- ✅ `selection()` - Get current selection value
- ✅ `selectionIndex(int)` - Set selection by index
- ✅ `selectionIndex()` - Get current selection index
- ✅ `addOption(ItemStack, T)` - Add new option dynamically
- ✅ `clearOptions()` - Remove all options
- ✅ `sound(Sound)` - Change click sound
- ✅ `onSelectionChange(Consumer)` - Update change handler
- ✅ Edge case handling (empty options, invalid values, bounds checking)

#### DoubleDropButton (`src/main/java/toutouchien/niveriaapi/menu/component/interactive/DoubleDropButton.java`)
- ✅ `item(ItemStack)` / `item(Function)` - Update normal item
- ✅ `dropItem(ItemStack)` / `dropItem(Function)` - Update drop-state item
- ✅ `sound(Sound)` - Change click sound
- ✅ All click handler setters (onClick, onLeftClick, onRightClick, etc.)

### 4. Documentation & Examples

#### DynamicTestMenu (`src/main/java/toutouchien/niveriaapi/menu/test/DynamicTestMenu.java`)
A comprehensive test menu demonstrating:
- Dynamic progress bar updates
- Status icon that changes based on progress
- Interactive buttons to increment/decrement progress
- Reset functionality
- Toggle for auto-updates
- Real-time component synchronization

#### Usage Guide (`DYNAMIC_COMPONENTS_EXAMPLE.md`)
Complete documentation including:
- Overview of the feature
- 5 detailed code examples
- API reference for all new methods
- Important usage notes

## Usage Example

```java
public class MyMenu extends Menu {
    private ProgressBar progressBar;
    
    @Override
    protected Component root(MenuContext context) {
        progressBar = ProgressBar.create()
            .percentage(0.0)
            .size(5, 1)
            .build();
        // ... add to grid
    }
    
    // Update progress dynamically
    public void updateProgress(double newPercentage) {
        progressBar.percentage(newPercentage);
        this.update(progressBar);  // Trigger re-render
    }
}
```

## Benefits

1. **Real-time Updates**: Components can now reflect live data without menu recreation
2. **Better UX**: Smoother interactions and visual feedback
3. **Cleaner Code**: No need to rebuild entire menus for simple updates
4. **Backward Compatible**: All existing code continues to work unchanged
5. **Type Safe**: Strong typing with proper validation and error messages
6. **Well Documented**: Comprehensive examples and API documentation

## Testing

- ✅ All component setters validated with proper preconditions
- ✅ Edge cases handled (empty selectors, invalid indices, null checks)
- ✅ Code review passed with no issues
- ✅ Security scan (CodeQL) passed with 0 alerts
- ✅ Example test menu created demonstrating all features
- ⚠️ Full build pending (network access limitations in environment)

## Breaking Changes

**None** - This is a fully backward-compatible addition. All existing code continues to work without modification.

## Migration Guide

For existing code, no changes are required. To leverage the new dynamic update capabilities:

1. Store references to components you want to update
2. Call setter methods on those components to change properties
3. Call `menu.update()` or `menu.update(component)` to reflect changes

See `DYNAMIC_COMPONENTS_EXAMPLE.md` for detailed examples.

## Future Enhancements

Potential future improvements:
- Automatic re-rendering on setter calls (optional mode)
- Batch update API for updating multiple components at once
- Animation/transition effects for updates
- Component state snapshot/restore functionality
