package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder;

import java.util.List;

/**
 * Represents a built SQL query with its parameters, ready for execution.
 * 
 * <p>This record encapsulates a complete SQL query string and its parameters in the
 * correct order for use with {@link java.sql.PreparedStatement}. The SQL string uses
 * placeholders ({@code ?}) for parameters, and the parameters list contains the values
 * in the order they should be bound.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * BuiltQuery query = SqlBuilder.select(idCol, nameCol)
 *     .from(usersTable)
 *     .where(idCol, 123)
 *     .build();
 * 
 * // Execute with PreparedStatement
 * try (PreparedStatement stmt = connection.prepareStatement(query.sql())) {
 *     for (int i = 0; i < query.params().size(); i++) {
 *         stmt.setObject(i + 1, query.params().get(i));
 *     }
 *     ResultSet rs = stmt.executeQuery();
 *     // Process results...
 * }
 * }</pre>
 * 
 * <h3>Parameter Binding</h3>
 * 
 * <p>The parameters list contains values in the order they appear in the SQL string.
 * Each {@code ?} placeholder in the SQL corresponds to one parameter in the list.
 * Parameters are bound using 1-based indexing (first parameter is index 1).
 * 
 * <h3>SQL Injection Safety</h3>
 * 
 * <p>The SQL string is built using validated identifiers (Table, Column) and all
 * user-provided values are stored as parameters, not concatenated into the SQL string.
 * This ensures protection against SQL injection attacks.
 * 
 * @param sql    the SQL query string with {@code ?} placeholders for parameters
 * @param params the list of parameter values in the order they appear in the SQL
 * @see java.sql.PreparedStatement
 * @see SqlBuilder
 * @since 1.0
 */
public record BuiltQuery(String sql, List<Object> params) {
    
    /**
     * Validates the BuiltQuery during construction.
     * 
     * @param sql    the SQL query string
     * @param params the parameter list
     * @throws IllegalArgumentException if sql is null/blank or params is null
     */
    public BuiltQuery {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL query cannot be null or blank");
        }
        if (params == null) {
            throw new IllegalArgumentException("Parameters list cannot be null");
        }
    }
}
