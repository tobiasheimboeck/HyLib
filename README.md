<div align="center">

![HyLib Banner](assets/hylib-banner.png)

</div>

## Overview

HyLib is a comprehensive utility library for Hytale server plugin development. It provides type-safe database operations, flexible configuration management, and a powerful localization system—eliminating boilerplate code and common pitfalls.

## Features

### **Database Module** (Hytale-Independent)

- **Type-Safe SQL Queries** - Fluent builder API with compile-time safety
- **SQL Injection Protection** - Validated identifiers prevent common vulnerabilities
- **Flexible Query Builder** - Support for SELECT, INSERT, UPDATE, DELETE with JOINs, WHERE, ORDER BY
- **RowMapper-Based Mapping** - Clean separation of SQL and domain logic
- **Connection Pooling** - Built on HikariCP for MariaDB
- **In-Memory Caching** - Simple cache API for frequently accessed data

### **Hytale Module**

- **Type-Safe Configuration API** - Fluent DSL codec builder with method references
- **Multi-Language Localization** - Translation system supporting multiple languages and plugins
- **Rich Message Formatting** - Parse and render formatted messages (colors, gradients, formatting)
- **Player Management** - Integrated player service with database integration

## Architecture

HyLib follows a modular architecture:

- **Database Modules**: Independent of Hytale, usable in any Java application
- **Hytale Modules**: Built on database modules, adding Hytale-specific functionality
- **Provider Pattern**: Central registries (`DatabaseProvider`, `HytaleProvider`) for dependency injection
- **Repository Pattern**: Base repository class with automatic CRUD operations

## Modules

- **`database-api`**: Core interfaces, query builder, RowMapper (no Hytale dependencies)
- **`database-common`**: Default implementations (MariaDB connector, connection handler)
- **`hytale-api`**: Hytale-specific API interfaces (codec builder, localization, messages)
- **`hytale-common`**: Implementations (codec builder, JSON language loader, message parser)
- **`hytale-plugin`**: Complete Hytale plugin combining both APIs

## Getting Started

HyLib is distributed via GitHub Packages. Configure the Maven repository, set up authentication with a GitHub Personal Access Token, and add dependencies for the modules you need.

For detailed installation instructions, see the [Installation Guide](https://github.com/Spacetivity/HyLib/wiki/Installation) in our Wiki.

## Documentation

📚 **Complete documentation is available in our [GitHub Wiki](https://github.com/Spacetivity/HyLib/wiki)**.

### Quick Links

- **[Installation](https://github.com/Spacetivity/HyLib/wiki/Installation)**: Setup and configuration guide
- **[Database Guide](https://github.com/Spacetivity/HyLib/wiki/Database-Guide)**: Complete guide to using the database API
- **[SQL Query Builder](https://github.com/Spacetivity/HyLib/wiki/SQL-Query-Builder)**: Type-safe SQL query building
- **[Repository Pattern](https://github.com/Spacetivity/HyLib/wiki/Repository-Pattern)**: Repository implementation guide
- **[Configuration Guide](https://github.com/Spacetivity/HyLib/wiki/Configuration-Guide)**: Creating and managing configurations
- **[Localization Guide](https://github.com/Spacetivity/HyLib/wiki/Localization-Guide)**: Multi-language support
- **[Message Formatting](https://github.com/Spacetivity/HyLib/wiki/Message-Formatting)**: Formatting messages with tags
- **[Player Management](https://github.com/Spacetivity/HyLib/wiki/Player-Management)**: Player data and language preferences
- **[Cache System](https://github.com/Spacetivity/HyLib/wiki/Cache-System)**: In-memory caching strategies

## Requirements

- **Java 21+**
- **Hytale Server** (for Hytale modules)
- **MariaDB** (for database functionality)
- **Gradle 8.0+** (for building)

## License

This project is licensed under the terms specified in the LICENSE file.

## Support

- **GitHub Issues**: [Report bugs or request features](https://github.com/Spacetivity/HyLib/issues)
- **Discord**: Join our [Discord server](https://discord.spacetivity.dev) for community support

## Credits

Developed by **Spacetivity** — Tobias Heimböck-Kramesberger (DeveloperTobi)

