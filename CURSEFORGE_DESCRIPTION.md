![HyLib Banner](https://raw.githubusercontent.com/tobiasheimboeck/HyLib/main/assets/hylib-banner.png)

**HyLib** is a powerful utility library for Hytale server plugin development. It provides type-safe database operations, comprehensive configuration management, and a robust localization system that works with or without a database.

## Features

### **Localization System**
- **Multi-language support** with JSON-based language files
- **Type-safe translation keys** using `LangKey`
- **Placeholder system** for dynamic content (`{player}`, `{count}`, etc.)
- **Message formatting** with Hytale's native formatting tags (`<red>`, `<gradient:red:blue>`, etc.)
- **Works without database** - perfect for plugins that only need translations
- **Configurable default language** via GlobalConfig

### **Database Operations** (Optional)
- **MariaDB support** with connection pooling
- **Optional database** - enable/disable via config

### **Configuration Management**
- **Type-safe configs** using `CodecBuilder`
- **Automatic serialization/deserialization**
- **Default values** support
- **Easy config file generation**

### **Player Management** (Optional)
- **HyPlayer service** for player data and language preferences
- **Online/offline player handling**
- **Player language preferences** stored in database
- **Automatic caching** for fast access

### **Message Parsing**
- **Rich text formatting** support
- **Color codes** and gradients
- **Easy message creation** from strings

## Installation

### As a Dependency

**Important:** GitHub Packages requires authentication even for public packages. **Each user needs to create their own GitHub token** - tokens cannot be shared.

**Step 1: Create Your GitHub Token**

1. Go to [GitHub Settings > Developer settings > Personal access tokens](https://github.com/settings/tokens)
2. Click "Generate new token (classic)"
3. Give it a name (e.g., "HyLib Packages")
4. Select the `read:packages` permission
5. Click "Generate token"
6. Copy the token (starts with `ghp_`) - you won't see it again!

**Step 2: Configure Your Project**

Create a `gradle.properties` file in your project root (add it to `.gitignore`):

```properties
github.username=your-github-username
github.token=ghp_your_personal_access_token
```

**Important:** 
- `gradle.properties` should be in `.gitignore` and **never committed**!
- Each developer needs their own token

**Step 3: Add Repository and Dependencies**

Add to your `build.gradle.kts`:

```kotlin
repositories {
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/tobiasheimboeck/HyLib")
        credentials {
            username = project.findProperty("github.username") as String?
            password = project.findProperty("github.token") as String?
        }
    }
}

dependencies {
    // Hytale API (for Hytale-specific features)
    implementation("dev.spacetivity.tobi.hylib:hytale-api:VERSION")
    
    // Note: If hylib-plugin plugin is installed, you only need hytale-api
    // The plugin provides all implementations
}
```

**Important:** 
- If you have the `hylib-plugin` plugin installed on your server, you **only need** `hytale-api`. The plugin already provides all implementations.

**Note:** Even though the repository is public, GitHub Packages requires authentication for all package downloads. This is a GitHub design decision. Each developer needs their own token with `read:packages` permission.

### Runtime Plugin

Install the `hylib-plugin` plugin on your server to enable:
- Language command (`/language`) - Change your language preference
- Player event handling
- Global configuration
- Database connection management (if enabled)

![Language Command Feature](https://raw.githubusercontent.com/tobiasheimboeck/HyLib/main/assets/language_command_feature.png)

*Example: Using the `/language` command to change language preferences*

## Quick Start

### Example 1: Localization (No Database Required)

```java
import dev.spacetivity.tobi.hylib.hytale.api.HytaleProvider;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LocalizationService;
import dev.spacetivity.tobi.hylib.hytale.api.localization.LangKey;
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;
import dev.spacetivity.tobi.hylib.hytale.api.player.HyPlayerService;
import dev.spacetivity.tobi.hymessage.api.placeholder.Placeholder;

// Define translation keys
public class Messages {
    public static final LangKey WELCOME = LangKey.of("welcome");
    public static final LangKey PLAYER_JOINED = LangKey.of("player.joined");
}

// Send translated message to player
LocalizationService localizationService = HytaleProvider.getApi().getLocalizationService();
HyPlayerService playerService = HytaleProvider.getApi().getHyPlayerService();

// Get player's language (or default)
Lang playerLang = playerService != null 
    ? playerService.getOnlineHyPlayer(playerRef).getLanguage()
    : localizationService.getDefaultLanguage();

Message message = localizationService.translate(Messages.PLAYER_JOINED, playerLang, 
    Placeholder.of("player", player.getName())
);
playerRef.sendMessage(message);
```

**Language file** (`lang/en/player.json`):
```json
{
  "welcome": "<green>Welcome to the server!",
  "player.joined": "<yellow>{player} <gray>joined the game"
}
```

### Message Formatting

HyLib integrates with **HyMessage** for rich text formatting with colors, gradients, and styling:

```java
import dev.spacetivity.tobi.hymessage.api.HyMessages;

// Parse formatted strings (using HyMessage API)
Message msg1 = HyMessages.parse("<gradient:red:blue>Welcome to HyLib!</gradient>");
Message msg2 = HyMessages.parse("<green>Success!</green> <yellow>Player joined</yellow>");
Message msg3 = HyMessages.parse("<blue>[<gold>HyLib<blue>]<gray> Your language has been changed!");
```

**Note:** HyMessage is automatically included as a dependency. The message parsing functionality is provided by the HyMessage library.

![Message Formatting Example](https://raw.githubusercontent.com/tobiasheimboeck/HyLib/main/assets/hymessage_feature.png)

*Example: Formatted messages with colors and gradients in-game*

### Example 2: Player Service (Database Required)

```java
import dev.spacetivity.tobi.hylib.hytale.api.localization.Lang;

HyPlayerService playerService = HytaleProvider.getApi().getHyPlayerService();
if (playerService != null) {
    playerService.createHyPlayer(player.getUuid(), player.getName());
    playerService.changeLanguage(player.getUuid(), Lang.of("de"));
}
```

## Configuration

### GlobalConfig

```yaml
LanguageCommandEnabled: true
DefaultLanguage: "en"
```

## Key Benefits

- **No Database Required** - Use localization features standalone  
- **Type-Safe** - Compile-time safety for translations and configs  
- **Easy to Use** - Simple API with static methods  
- **Performance** - Built-in caching  
- **Flexible** - Works with or without database  
- **Modern** - Uses virtual threads for async operations  

## API Overview

### LocalizationService
- `translate()` - Translate keys to messages
- `getRawTranslation()` - Get translated string without parsing
- `getDefaultLanguage()` - Get default language
- `getAvailableLanguages()` - Get all available languages

### HytaleApi
- `getLocalizationService()` - Access localization features
- `getHyPlayerService()` - Access player management (null if DB disabled)
- `newCodec()` - Create type-safe config codecs

### HyMessage API (External Library)
HyLib integrates with **HyMessage** for message parsing. The HyMessage library is automatically included as a dependency:
- `HyMessages.parse()` - Parse formatted strings to messages
- `HyMessages.strip()` - Remove formatting tags  
- `HyMessages.builder()` - Create custom message parsers
- `Placeholder.of()` - Create placeholders for dynamic content

**Import:** `dev.spacetivity.tobi.hymessage.api.HyMessages` and `dev.spacetivity.tobi.hymessage.api.placeholder.Placeholder`

## Requirements

- **Hytale Server** (Early Access)
- **Java 21+**
- **MariaDB** (optional, only if using database features)

## License

This project is licensed under the **MIT License** - see the [LICENSE](https://github.com/tobiasheimboeck/HyLib/blob/main/LICENSE) file for details.

## Links

- **GitHub**: [https://github.com/tobiasheimboeck/HyLib](https://github.com/tobiasheimboeck/HyLib)
- **Wiki**: [https://github.com/tobiasheimboeck/HyLib/wiki](https://github.com/tobiasheimboeck/HyLib/wiki)
- **Issues**: [https://github.com/tobiasheimboeck/HyLib/issues](https://github.com/tobiasheimboeck/HyLib/issues)

## Support

- **Discord**: [discord.developertobi.net](https://discord.developertobi.net)
- **GitHub Issues**: [Report bugs or request features](https://github.com/tobiasheimboeck/HyLib/issues)
- **Wiki Documentation**: [Complete API documentation and guides](https://github.com/tobiasheimboeck/HyLib/wiki)

For support, questions, or feature requests, please join our Discord community, check the Wiki, or open an issue on GitHub.

---

**Made with love for the Hytale modding community**
