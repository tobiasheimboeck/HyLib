package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql;

import java.util.regex.Pattern;

/**
 * Represents a safe SQL table identifier with validation.
 * 
 * <p>This class provides SQL injection protection by validating table names and ensuring
 * only safe identifiers are used in SQL queries. Table names are validated to contain only
 * alphanumeric characters and underscores.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * // Create a table identifier
 * Table usersTable = Table.of("users");
 * 
 * // Use in SQL queries
 * String sql = "SELECT * FROM " + usersTable.toSql();  // "SELECT * FROM `users`"
 * }</pre>
 * 
 * <h3>Validation Rules</h3>
 * 
 * <p>Table names must:
 * <ul>
 *   <li>Not be null or blank</li>
 *   <li>Contain only alphanumeric characters (A-Z, a-z, 0-9) and underscores (_)</li>
 *   <li>Not contain spaces, special characters, or SQL keywords</li>
 * </ul>
 * 
 * <h3>SQL Injection Protection</h3>
 * 
 * <p>By using validated table identifiers instead of raw strings, SQL injection attacks
 * are prevented. Invalid table names throw {@code IllegalArgumentException} at construction time.
 * 
 * @param name the validated table name
 * @see Column
 * @see dev.spacetivity.tobi.database.api.connection.impl.sql.builder.SqlBuilder
 * @since 1.0
 */
public record Table(String name) {
    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");

    /**
     * Validates the table name during construction.
     * 
     * @param name the table name to validate
     * @throws IllegalArgumentException if the name is null, blank, or contains invalid characters
     */
    public Table {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Table name cannot be null or blank");
        }
        if (!VALID_IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid table name: " + name + ". Only alphanumeric characters and underscores are allowed.");
        }
    }

    /**
     * Creates a {@code Table} instance from a string name.
     * 
     * <p>This factory method validates the table name and creates a safe table identifier.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * Table usersTable = Table.of("users");
     * Table postsTable = Table.of("user_posts");
     * }</pre>
     * 
     * @param name the table name (will be validated)
     * @return a validated {@code Table} instance
     * @throws IllegalArgumentException if the name is invalid
     */
    public static Table of(String name) {
        return new Table(name);
    }

    /**
     * Returns the SQL-safe table name formatted for use in SQL queries.
     * 
     * <p>The returned string wraps the table name in backticks, making it safe to use
     * directly in SQL queries. This format is compatible with MariaDB/MySQL.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * Table table = Table.of("users");
     * String sql = "SELECT * FROM " + table.toSql();  // "SELECT * FROM `users`"
     * }</pre>
     * 
     * @return the SQL-safe table name wrapped in backticks (e.g., "`users`")
     */
    public String toSql() {
        return "`" + name + "`";
    }
}
