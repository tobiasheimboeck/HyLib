package dev.spacetivity.tobi.database.api.connection.impl.sql;

import java.util.regex.Pattern;

/**
 * Represents a safe SQL column identifier with validation.
 * Prevents SQL injection by ensuring only valid column names are used.
 */
public record Column(String name) {
    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");

    public Column {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Column name cannot be null or blank");
        }
        if (!VALID_IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid column name: " + name + ". Only alphanumeric characters and underscores are allowed.");
        }
    }

    /**
     * Creates a Column instance from a string name.
     * @param name the column name
     * @return a validated Column instance
     * @throws IllegalArgumentException if the name is invalid
     */
    public static Column of(String name) {
        return new Column(name);
    }

    /**
     * Returns the SQL-safe column name (can be used directly in queries).
     * @return the column name with backticks
     */
    public String toSql() {
        return "`" + name + "`";
    }
}
