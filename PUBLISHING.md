# Publishing zu GitHub Packages

Diese Anleitung erklärt, wie du die HyLib Module zu GitHub Packages veröffentlichst.

## Voraussetzungen

1. **GitHub Personal Access Token (PAT)** mit folgenden Berechtigungen:
   - `write:packages` - Zum Veröffentlichen von Packages
   - `read:packages` - Zum Lesen von Packages (optional, für lokale Entwicklung)

   Erstelle ein Token unter: https://github.com/settings/tokens

2. **Repository Information**: 
   - GitHub Owner/Organisation
   - Repository Name

## Lokales Publishing

### 1. Konfiguration einrichten

Kopiere `gradle.properties.example` zu `gradle.properties` und fülle die Werte aus:

```bash
cp gradle.properties.example gradle.properties
```

Bearbeite `gradle.properties`:

```properties
github.owner=dein-username-oder-org
github.repo=hylib
github.username=dein-username
github.token=dein-personal-access-token
```

**Wichtig:** `gradle.properties` ist in `.gitignore` und wird nicht committed!

### 2. Version setzen

Bearbeite `build.gradle.kts` und setze die gewünschte Version:

```kotlin
allprojects {
    version = "1.0.0"  // Statt "1.0-SNAPSHOT"
}
```

### 3. Packages veröffentlichen

```bash
# Alle Module veröffentlichen
./gradlew :database-api:publish :database-common:publish :hytale-api:publish :hytale-common:publish
```

### 4. Packages verwenden

In anderen Projekten kannst du die Packages jetzt verwenden. Siehe [README.md - Installation](../README.md#installation) für eine vollständige Anleitung.

**Kurze Zusammenfassung:**

1. **Repository hinzufügen** in `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/OWNER/REPO")
        credentials {
            username = project.findProperty("github.username") as String? 
                ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("github.token") as String? 
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
}
```

**Wichtig:** Ersetze `OWNER` und `REPO` mit deinen GitHub Repository-Informationen.

2. **Authentifizierung einrichten:**

Erstelle eine `gradle.properties` Datei:
```properties
github.username=dein-github-username
github.token=dein-github-personal-access-token
```

Oder verwende Umgebungsvariablen:
```bash
export GITHUB_ACTOR=dein-github-username
export GITHUB_TOKEN=dein-github-personal-access-token
```

3. **Dependencies hinzufügen:**

```kotlin
dependencies {
    implementation("dev.spacetivity.tobi.hylib.database:database-api:1.0.0")
    
    // Weitere Dependencies...
}
```

**Vollständiges Beispiel:** Siehe [README.md - Installation](../README.md#installation)

## Automatisches Publishing via GitHub Actions

### Workflow-Trigger

Der GitHub Actions Workflow wird automatisch ausgelöst, wenn:

1. **Tag mit Version**: Ein Git-Tag mit dem Format `v*` wird gepusht (z.B. `v1.0.0`)
2. **Manuell**: Über GitHub Actions UI → "Run workflow"

### Manuelles Publishing über GitHub Actions

1. Gehe zu: `Actions` → `Publish to GitHub Packages` → `Run workflow`
2. Gib die gewünschte Version ein (z.B. `1.0.0`)
3. Klicke auf `Run workflow`

### Tag-basiertes Publishing

```bash
# Tag erstellen
git tag v1.0.0

# Tag pushen
git push origin v1.0.0
```

Der Workflow wird automatisch ausgelöst und veröffentlicht beide Packages mit der Version `1.0.0`.

**Hinweis:** Die Version wird automatisch aus dem Tag extrahiert (z.B. `v1.0.0` → `1.0.0`).

## Versionierung

- **SNAPSHOT-Versionen** (z.B. `1.0-SNAPSHOT`): Für Entwicklung, können überschrieben werden
- **Release-Versionen** (z.B. `1.0.0`): Für Production, sind immutable

## Troubleshooting

### "401 Unauthorized"

- Überprüfe, ob dein GitHub Token die richtigen Berechtigungen hat
- Stelle sicher, dass `github.token` in `gradle.properties` korrekt gesetzt ist
- Für GitHub Actions: `GITHUB_TOKEN` wird automatisch bereitgestellt

### "403 Forbidden"

- Überprüfe, ob dein Token `write:packages` Berechtigung hat
- Stelle sicher, dass du Zugriff auf das Repository hast

### "404 Not Found"

- Überprüfe, ob `github.owner` und `github.repo` korrekt sind
- Stelle sicher, dass das Repository existiert und du Zugriff hast

### Packages werden nicht gefunden

- Warte ein paar Minuten nach dem Publishing (GitHub Packages braucht Zeit zum Indexieren)
- Überprüfe, ob die Repository-Konfiguration im anderen Projekt korrekt ist
- Stelle sicher, dass die Version übereinstimmt

## Packages in anderen Projekten verwenden

### Vollständige Anleitung

#### Schritt 1: Repository konfigurieren

Füge das GitHub Packages Repository zu deiner `build.gradle.kts` hinzu:

```kotlin
repositories {
    mavenCentral()
    
    // GitHub Packages Repository
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/OWNER/REPO")
        credentials {
            username = project.findProperty("github.username") as String? 
                ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("github.token") as String? 
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
    
    // Weitere Repositories...
}
```

**Ersetze:**
- `OWNER`: Dein GitHub Username oder Organisation (z.B. `spacetivity`)
- `REPO`: Repository Name (z.B. `hylib`)

#### Schritt 2: Authentifizierung einrichten

Du benötigst einen GitHub Personal Access Token mit `read:packages` Berechtigung.

**Option A: gradle.properties (empfohlen)**

Erstelle eine `gradle.properties` Datei im Projekt-Root:

```properties
github.username=dein-github-username
github.token=ghp_dein_personal_access_token_hier
```

**Option B: Umgebungsvariablen**

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

**GitHub Token erstellen:**
1. Gehe zu: https://github.com/settings/tokens
2. Klicke auf "Generate new token (classic)"
3. Wähle die Berechtigung `read:packages`
4. Kopiere den generierten Token (beginnt mit `ghp_`)

#### Schritt 3: Dependencies hinzufügen

Füge die Dependencies zu deiner `build.gradle.kts` hinzu:

```kotlin
dependencies {
    // Database API
    implementation("dev.spacetivity.tobi.hylib.database:database-api:VERSION")
    
    // Hytale Server (für BuilderCodec und Config)
    compileOnly("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
    
    // Lombok (optional, aber empfohlen)
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}
```

**Ersetze `VERSION`** mit der gewünschten Version (z.B. `1.0.0`).

Verfügbare Versionen findest du unter:
`https://github.com/OWNER/REPO/packages`

#### Schritt 4: Projekt synchronisieren

Nach dem Hinzufügen der Dependencies:

1. **Gradle Sync** durchführen (IDE)
2. Oder manuell: `./gradlew build --refresh-dependencies`

### Vollständiges Beispiel

**build.gradle.kts:**

```kotlin
plugins {
    id("java")
    id("java-library")
}

repositories {
    mavenCentral()
    
    // GitHub Packages
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/spacetivity/hylib")
        credentials {
            username = project.findProperty("github.username") as String? 
                ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("github.token") as String? 
                ?: System.getenv("GITHUB_TOKEN")
        }
    }
    
    // Hytale Repositories
    maven {
        name = "hytale-release"
        url = uri("https://maven.hytale.com/release")
    }
    maven {
        name = "hytale-pre-release"
        url = uri("https://maven.hytale.com/pre-release")
    }
}

dependencies {
    // Database API
    implementation("dev.spacetivity.tobi.hylib.database:database-api:1.0.0")
    
    // Hytale Server
    compileOnly("com.hypixel.hytale:Server:2026.01.22-6f8bdbdc4")
    
    // Lombok
    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")
}
```

**gradle.properties:**

```properties
github.username=dein-github-username
github.token=ghp_dein_personal_access_token_hier
```

### Verwendung in GitHub Actions

Wenn du die Packages in GitHub Actions verwendest, wird `GITHUB_TOKEN` automatisch bereitgestellt:

```yaml
# .github/workflows/build.yml
- name: Build project
  run: ./gradlew build
  env:
    GITHUB_ACTOR: ${{ github.actor }}
    GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
```

## Package-URLs

Nach dem Publishing sind die Packages verfügbar unter:

- **database-api**: `https://github.com/{owner}/{repo}/packages/maven/dev.spacetivity.tobi.hylib.database/database-api`
- **database-common**: `https://github.com/{owner}/{repo}/packages/maven/dev.spacetivity.tobi.hylib.database/database-common`
- **hytale-api**: `https://github.com/{owner}/{repo}/packages/maven/dev.spacetivity.tobi.hylib.database/hytale-api`
- **hytale-common**: `https://github.com/{owner}/{repo}/packages/maven/dev.spacetivity.tobi.hylib.database/hytale-common`

## Weitere Informationen

- [GitHub Packages Dokumentation](https://docs.github.com/en/packages)
- [Maven mit GitHub Packages](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
- [README.md - Installation](../README.md#installation) - Vollständige Installationsanleitung
