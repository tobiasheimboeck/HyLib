package dev.spacetivity.tobi.database.api.connection.impl.sql;

import java.util.regex.Pattern;

/**
 * Represents a safe SQL table identifier with validation.
 * Prevents SQL injection by ensuring only valid table names are used.
 */
public record Table(String name) {
    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");

    public Table {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Table name cannot be null or blank");
        }
        if (!VALID_IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid table name: " + name + ". Only alphanumeric characters and underscores are allowed.");
        }
    }

    /**
     * Creates a Table instance from a string name.
     * @param name the table name
     * @return a validated Table instance
     * @throws IllegalArgumentException if the name is invalid
     */
    public static Table of(String name) {
        return new Table(name);
    }

    /**
     * Returns the SQL-safe table name (can be used directly in queries).
     * @return the table name
     */
    public String toSql() {
        return "`" + name + "`";
    }
}
