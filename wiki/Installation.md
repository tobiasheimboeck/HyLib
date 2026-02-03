# Installation

Diese Anleitung erklärt, wie du HyLib in deinem Projekt einrichtest und verwendest.

## Voraussetzungen

- **Java 21+**
- **Gradle 8.0+**
- **GitHub Personal Access Token** (für GitHub Packages Zugriff)
- **Hytale Server** (für Hytale Module)

## Schritt 1: GitHub Packages Repository hinzufügen

Füge das GitHub Packages Repository zu deiner `build.gradle.kts` hinzu:

```kotlin
repositories {
    mavenCentral()
    
    // Hytale Maven Repositories
    maven {
        name = "hytale-release"
        url = uri("https://maven.hytale.com/release")
    }
    maven {
        name = "hytale-pre-release"
        url = uri("https://maven.hytale.com/pre-release")
    }
    
    // GitHub Packages Repository
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Spacetivity/HyLib")
        credentials {
            username = project.findProperty("github.username") as String? 
                ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("github.token") as String? 
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

**Wichtig:** Ersetze `Spacetivity/HyLib` mit dem tatsächlichen Owner/Repository-Namen, falls abweichend.

## Schritt 2: Authentifizierung einrichten

GitHub Packages erfordert Authentifizierung. Du hast zwei Optionen:

### Option A: gradle.properties (Empfohlen für lokale Entwicklung)

Erstelle eine `gradle.properties` Datei im Projekt-Root:

```properties
github.username=dein-github-username
github.token=ghp_dein_personal_access_token_hier
```

**Wichtig:** `gradle.properties` sollte in `.gitignore` sein und wird nicht committed!

### Option B: Umgebungsvariablen

```bash
# Linux/Mac
export GITHUB_ACTOR=dein-github-username
export GITHUB_TOKEN=ghp_dein_personal_access_token_hier

# Windows (PowerShell)
$env:GITHUB_ACTOR="dein-github-username"
$env:GITHUB_TOKEN="ghp_dein_personal_access_token_hier"

# Windows (CMD)
set GITHUB_ACTOR=dein-github-username
set GITHUB_TOKEN=ghp_dein_personal_access_token_hier
```

### GitHub Token erstellen

1. Gehe zu: https://github.com/settings/tokens
2. Klicke auf "Generate new token (classic)"
3. Wähle die Berechtigung `read:packages` (und optional `write:packages` für Publishing)
4. Kopiere den generierten Token (beginnt mit `ghp_`)

## Schritt 3: Dependencies hinzufügen

Füge die benötigten HyLib Module zu deiner `build.gradle.kts` hinzu:

```kotlin
dependencies {
    // Database API (Hytale-unabhängig)
    implementation("dev.spacetivity.tobi.hylib.database:database-api:VERSION")
    implementation("dev.spacetivity.tobi.hylib.database:database-common:VERSION")
    
    // Hytale API (für Hytale-spezifische Features)
    implementation("dev.spacetivity.tobi.hylib.hytale:hytale-api:VERSION")
    implementation("dev.spacetivity.tobi.hylib.hytale:hytale-common:VERSION")
    
    // Hytale Server (für BuilderCodec und Config)
    compileOnly("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
    
    // Lombok (optional, aber empfohlen)
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}
```

**Ersetze `VERSION`** mit der gewünschten Version (z.B. `1.0.0`).

Verfügbare Versionen findest du unter:
`https://github.com/Spacetivity/HyLib/packages`

## Schritt 4: Projekt synchronisieren

Nach dem Hinzufügen der Dependencies:

1. **Gradle Sync** durchführen (in deiner IDE)
2. Oder manuell: `./gradlew build --refresh-dependencies`

## Vollständiges Beispiel

Hier ist ein vollständiges `build.gradle.kts` Beispiel:

```kotlin
plugins {
    id("java")
    id("java-library")
}

group = "dein.plugin.package"
version = "1.0.0"

repositories {
    mavenCentral()
    
    maven {
        name = "hytale-release"
        url = uri("https://maven.hytale.com/release")
    }
    maven {
        name = "hytale-pre-release"
        url = uri("https://maven.hytale.com/pre-release")
    }
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/Spacetivity/HyLib")
        credentials {
            username = project.findProperty("github.username") as String? 
                ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("github.token") as String? 
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // HyLib Database Module
    implementation("dev.spacetivity.tobi.hylib.database:database-api:1.0.0")
    implementation("dev.spacetivity.tobi.hylib.database:database-common:1.0.0")
    
    // HyLib Hytale Module
    implementation("dev.spacetivity.tobi.hylib.hytale:hytale-api:1.0.0")
    implementation("dev.spacetivity.tobi.hylib.hytale:hytale-common:1.0.0")
    
    // Hytale Server
    compileOnly("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
    
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}
```

## Nächste Schritte

Nach der Installation:

1. **[Database Guide](Database-Guide)** - Datenbankverbindungen einrichten
2. **[Configuration Guide](Configuration-Guide)** - Erste Konfiguration erstellen
3. **[Localization Guide](Localization-Guide)** - Mehrsprachigkeit hinzufügen

## Troubleshooting

### "Could not resolve dependency"

- Stelle sicher, dass dein GitHub Token korrekt ist und die Berechtigung `read:packages` hat
- Überprüfe, ob der Repository-Name korrekt ist (`Spacetivity/HyLib`)
- Versuche `./gradlew build --refresh-dependencies`

### "Authentication failed"

- Überprüfe deine `gradle.properties` oder Umgebungsvariablen
- Stelle sicher, dass der Token nicht abgelaufen ist
- Token muss mit `ghp_` beginnen

### "Package not found"

- Überprüfe, ob die Version existiert: `https://github.com/Spacetivity/HyLib/packages`
- Stelle sicher, dass du Zugriff auf das Repository hast (bei privaten Repos)
