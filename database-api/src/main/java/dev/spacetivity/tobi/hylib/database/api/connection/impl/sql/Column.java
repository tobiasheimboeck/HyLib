package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql;

import java.util.regex.Pattern;

/**
 * Represents a safe SQL column identifier with validation.
 * 
 * <p>This class provides SQL injection protection by validating column names and ensuring
 * only safe identifiers are used in SQL queries. Column names are validated to contain only
 * alphanumeric characters and underscores.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * // Create column identifiers
 * Column idCol = Column.of("id");
 * Column nameCol = Column.of("user_name");
 * 
 * // Use in SQL queries
 * String sql = "SELECT " + idCol.toSql() + ", " + nameCol.toSql() + " FROM users";
 * // "SELECT `id`, `user_name` FROM users"
 * }</pre>
 * 
 * <h3>Validation Rules</h3>
 * 
 * <p>Column names must:
 * <ul>
 *   <li>Not be null or blank</li>
 *   <li>Contain only alphanumeric characters (A-Z, a-z, 0-9) and underscores (_)</li>
 *   <li>Not contain spaces, special characters, or SQL keywords</li>
 * </ul>
 * 
 * <h3>SQL Injection Protection</h3>
 * 
 * <p>By using validated column identifiers instead of raw strings, SQL injection attacks
 * are prevented. Invalid column names throw {@code IllegalArgumentException} at construction time.
 * 
 * <h3>Best Practices</h3>
 * 
 * <p>It's recommended to define columns as constants in your repository classes:
 * 
 * <pre>{@code
 * public class UserRepository {
 *     private static final Column ID_COL = Column.of("id");
 *     private static final Column NAME_COL = Column.of("name");
 *     private static final Column EMAIL_COL = Column.of("email");
 * }
 * }</pre>
 * 
 * @param name the validated column name
 * @see Table
 * @see dev.spacetivity.tobi.database.api.connection.impl.sql.builder.SqlBuilder
 * @since 1.0
 */
public record Column(String name) {
    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[A-Za-z0-9_]+$");

    /**
     * Validates the column name during construction.
     * 
     * @param name the column name to validate
     * @throws IllegalArgumentException if the name is null, blank, or contains invalid characters
     */
    public Column {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Column name cannot be null or blank");
        }
        if (!VALID_IDENTIFIER.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid column name: " + name + ". Only alphanumeric characters and underscores are allowed.");
        }
    }

    /**
     * Creates a {@code Column} instance from a string name.
     * 
     * <p>This factory method validates the column name and creates a safe column identifier.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * Column idCol = Column.of("id");
     * Column userNameCol = Column.of("user_name");
     * }</pre>
     * 
     * @param name the column name (will be validated)
     * @return a validated {@code Column} instance
     * @throws IllegalArgumentException if the name is invalid
     */
    public static Column of(String name) {
        return new Column(name);
    }

    /**
     * Returns the SQL-safe column name formatted for use in SQL queries.
     * 
     * <p>The returned string wraps the column name in backticks, making it safe to use
     * directly in SQL queries. This format is compatible with MariaDB/MySQL.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * Column col = Column.of("user_name");
     * String sql = "SELECT " + col.toSql() + " FROM users";  // "SELECT `user_name` FROM users"
     * }</pre>
     * 
     * @return the SQL-safe column name wrapped in backticks (e.g., "`id`")
     */
    public String toSql() {
        return "`" + name + "`";
    }
}
