# NiveriaAPI

[![CodeFactor](https://www.codefactor.io/repository/github/puppytransgirl/niveriaapi/badge)](https://www.codefactor.io/repository/github/puppytransgirl/niveriaapi) [![GitHub Release](https://img.shields.io/github/v/release/PuppyTransGirl/NiveriaAPI?label=version)](https://github.com/PuppyTransGirl/NiveriaAPI/releases/latest)

NiveriaAPI is a lightweight helper library for Paper plugins that speeds up development with reusable menus, localization, cooldowns, MongoDB persistence, and utilities. Always maintained for 1.21.4-1.21.11 Minecraft releases.\
!!! This plugin only supports Paper servers or forks !!!

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Getting started (examples)](#getting-started-examples)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgements](#acknowledgements)

## Features

### Menu System
- Component-based menu system
- Includes built-in components (Grid, Button, Icon, ProgressBar, Toggle, etc.), exhaustive list can be found [here](/src/main/java/toutouchien/niveriaapi/menu/component)
- Easily customizable and extendable with your own components
- Supports pagination and dynamic content loading
- Examples can be found [here](/src/main/java/toutouchien/niveriaapi/menu/test)

### Language System
- Language system for easy localization
- Supports multiple languages
- Language files are stored in a simple YAML format for easy editing
- Can be based on the player locale or a defined one by you
- Features special tags to easily format messages (prefixes, colors)

### Cooldown System
- Easily manage cooldowns for players
- Set cooldowns for specific actions or commands
- Check if a player is on cooldown and get remaining time
- Automatically handle cooldown expiration
- Can be persistent across server restarts

### Database System
- Simple database abstraction layer
- Only supports MongoDB
- Make queries using a simple and intuitive API
- Supports asynchronous operations for better performance

### Utilities
- Various utility classes and methods to simplify common tasks
- ItemBuilder, Pair, Task, NMSUtils, etc.
- All the utilities classes can be found [here](/src/main/java/toutouchien/niveriaapi/utils)

## Installation
1. Download the latest release of NiveriaAPI from the [releases page](https://github.com/PuppyTransGirl/NiveriaAPI/releases).
2. Place the downloaded JAR file in your server's `plugins` directory.
3. Restart your server to load the plugin.

## Usage
**Maven**
```xml
<repository>
    <id>jitpack.io</id>
    <url>https://www.jitpack.io</url>
</repository>

<dependency>
    <groupId>com.github.PuppyTransGirl</groupId>
    <artifactId>NiveriaAPI</artifactId>
    <version>VERSION</version>
    <scope>provided</scope>
</dependency>
```

**Gradle**
```kotlin
repositories {
    maven("https://www.jitpack.io")
}

dependencies {
    compileOnly("com.github.PuppyTransGirl:NiveriaAPI:VERSION")
}
```
Replace `VERSION` with the latest release version.

### Plugin YML
**Bukkit plugin**
```yaml
depend: [NiveriaAPI]
```

**Paper plugin**
```yaml
dependencies:
  server:
    NiveriaAPI:
      load: BEFORE
      required: true
```

## Getting started (examples)
Minimal snippets to get you started with NiveriaAPI features.

<details>
<summary>Simple menu with button</summary>

```java
public class MenuTest extends Menu {
    public MenuTest(@NotNull Player player) {
        super(player);
    }

    @Override
    protected @NotNull Component title() {
        return Component.text("My menu :3");
    }

    @Override
    protected @NotNull MenuComponent root(@NotNull MenuContext context) {
        // Create a button that sends a message when clicked
        Button button = Button.create()
                .item(ItemBuilder.of(Material.DIAMOND_BLOCK)
                        .name(Component.text("Click me !", NamedTextColor.AQUA))
                        .build()
                )
                .onClick(event -> {
                    event.player().sendMessage(Component.text("Button clicked !", NamedTextColor.GREEN));
                })
                .build();
        
        // Create a grid layout and add the button to it
        return Grid.create()
                .size(9, 3)
                .add(13, button)
                .build();
    }
}
```
</details>

<details>
<summary>Language system example</summary>

```java
// Load language files (usually done in your plugin's onEnable method)
Lang.load(yourPluginInstance);

// Get a localized message as a Component or as a String
Component message = Lang.get("welcome_message");
String stringMessage = Lang.getString("welcome_message");

// Send a localized message to a player
Player player = ...; // Get the player instance
Lang.send(player, "welcome_message");
```
</details>

<details>
<summary>Cooldown system example</summary>

```java
CooldownManager cooldownManager = NiveriaAPI.instance().cooldownManager();

// Create cooldown keys
Key fireballKey = Key.key("plugin_name", "ability_fireball");
Key teleportKey = Key.key("plugin_name", "ability_teleport");
Key sprintKey = Key.key("plugin_name", "ability_sprint");

// Register a persistent cooldown (default behavior)
cooldownManager.setCooldown(player, fireballKey, Duration.ofSeconds(30));

// Register another persistent cooldown (explicitly persistent)
cooldownManager.setCooldown(player, teleportKey, Duration.ofHours(1), true);

// Register a non-persistent cooldown (in-memory only)
cooldownManager.setCooldown(player, sprintKey, Duration.ofSeconds(5), false);

// Check if player is in cooldown
if(cooldownManager.inCooldown(player, fireballKey)){
    long remainingSeconds = cooldownManager.remainingTime(player, fireballKey).getSeconds();
    player.sendMessage(Component.text("You must wait " + remainingSeconds + " seconds to use this ability again!", NamedTextColor.RED));
    return;
}
```
</details>

## Contributing
Contributions are welcome !
Please feel free to submit a pull request or open an issue for any bugs or feature requests :3

## License
This project is licensed under the GNU General Public License v3.0. See the [LICENSE](LICENSE) file for details.

## Acknowledgements
- Thanks to [Nirbose](https://github.com/Nirbose) for helping me to make the MockBukkit unit tests work with NiveriaAPI.
