package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for constructing DELETE queries with type-safe column and table identifiers.
 * 
 * <p>This builder provides a fluent API for building DELETE queries. For safety,
 * at least one WHERE condition is required to prevent accidental deletion of all rows.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * Table usersTable = Table.of("users");
 * Column idCol = Column.of("id");
 * 
 * BuiltQuery query = SqlBuilder.deleteFrom(usersTable)
 *     .where(idCol, 123)
 *     .build();
 * }</pre>
 * 
 * <h3>Multiple WHERE Conditions</h3>
 * 
 * <p>You can add multiple WHERE conditions (combined with AND):
 * 
 * <pre>{@code
 * .where(idCol, 123)
 * .where(nameCol, "John")  // Adds AND condition
 * }</pre>
 * 
 * <h3>Safety Requirement</h3>
 * 
 * <p><strong>At least one WHERE condition is required</strong> to prevent accidental
 * deletion of all rows in the table. This is enforced at build time.
 * 
 * <h3>SQL Injection Protection</h3>
 * 
 * <p>All table and column names use validated identifiers, and all values are
 * parameterized using PreparedStatement placeholders ({@code ?}).
 * 
 * @see SqlBuilder#deleteFrom(Table)
 * @see BuiltQuery
 * @since 1.0
 */
public class DeleteBuilder {
    private final Table table;
    private final List<String> whereConditions = new ArrayList<>();
    private final List<Object> params = new ArrayList<>();

    DeleteBuilder(Table table) {
        this.table = table;
    }

    /**
     * Adds a WHERE condition (column = value).
     * 
     * <p>This method can be called multiple times to add multiple AND conditions.
     * All conditions are combined with AND logic.
     * 
     * <p><strong>At least one WHERE condition is required</strong> for safety.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .where(idCol, 123)
     * .where(nameCol, "John")  // Adds AND condition
     * }</pre>
     * 
     * <p>This generates: {@code WHERE `id` = ? AND `name` = ?}
     * 
     * @param column the column to compare (validated identifier)
     * @param value  the value to compare against (will be parameterized)
     * @return this builder for method chaining
     * @throws NullPointerException if column is null
     */
    public DeleteBuilder where(Column column, Object value) {
        whereConditions.add(column.toSql() + " = ?");
        params.add(value);
        return this;
    }

    /**
     * Builds the final DELETE query.
     * 
     * <p>This method constructs the complete SQL DELETE statement with parameterized values.
     * At least one WHERE condition must be specified.
     * 
     * @return a {@link BuiltQuery} containing the SQL string and parameters
     * @throws IllegalStateException if no WHERE conditions are specified
     */
    public BuiltQuery build() {
        if (whereConditions.isEmpty()) {
            throw new IllegalStateException("At least one WHERE condition must be specified for safety");
        }

        String sql = "DELETE FROM " + table.toSql() + " WHERE " + String.join(" AND ", whereConditions);

        return new BuiltQuery(sql, new ArrayList<>(params));
    }
}
