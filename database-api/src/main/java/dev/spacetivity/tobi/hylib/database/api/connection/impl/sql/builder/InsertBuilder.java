package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for constructing INSERT queries with type-safe column and table identifiers.
 * 
 * <p>This builder provides a fluent API for building INSERT queries. Values are
 * parameterized using PreparedStatement placeholders for SQL injection protection.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * Table usersTable = Table.of("users");
 * Column idCol = Column.of("id");
 * Column nameCol = Column.of("name");
 * Column emailCol = Column.of("email");
 * 
 * BuiltQuery query = SqlBuilder.insertInto(usersTable)
 *     .value(idCol, 123)
 *     .value(nameCol, "John")
 *     .value(emailCol, "john@example.com")
 *     .build();
 * }</pre>
 * 
 * <h3>Batch Insert</h3>
 * 
 * <p>You can also add multiple values at once:
 * 
 * <pre>{@code
 * BuiltQuery query = SqlBuilder.insertInto(usersTable)
 *     .values(
 *         new Column[]{idCol, nameCol, emailCol},
 *         new Object[]{123, "John", "john@example.com"}
 *     )
 *     .build();
 * }</pre>
 * 
 * <h3>SQL Injection Protection</h3>
 * 
 * <p>All table and column names use validated identifiers, and all values are
 * parameterized using PreparedStatement placeholders ({@code ?}).
 * 
 * @see SqlBuilder#insertInto(Table)
 * @see BuiltQuery
 * @since 1.0
 */
public class InsertBuilder {
    private final Table table;
    private final List<Column> columns = new ArrayList<>();
    private final List<Object> values = new ArrayList<>();

    InsertBuilder(Table table) {
        this.table = table;
    }

    /**
     * Adds a column-value pair to the INSERT statement.
     * 
     * <p>This method can be called multiple times to add multiple columns.
     * Columns are added in the order they are specified.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .value(idCol, 123)
     * .value(nameCol, "John")
     * .value(emailCol, "john@example.com")
     * }</pre>
     * 
     * @param column the column name (validated identifier)
     * @param value  the value to insert (will be parameterized)
     * @return this builder for method chaining
     * @throws NullPointerException if column is null
     */
    public InsertBuilder value(Column column, Object value) {
        columns.add(column);
        values.add(value);
        return this;
    }

    /**
     * Adds multiple column-value pairs at once.
     * 
     * <p>This is a convenience method for adding multiple columns and values in a single call.
     * The columns and values arrays must have the same length.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .values(
     *     new Column[]{idCol, nameCol, emailCol},
     *     new Object[]{123, "John", "john@example.com"}
     * )
     * }</pre>
     * 
     * @param columns the columns to insert into
     * @param values  the corresponding values (must match column count)
     * @return this builder for method chaining
     * @throws IllegalArgumentException if columns and values arrays have different lengths
     * @throws NullPointerException if columns or values is null
     */
    public InsertBuilder values(Column[] columns, Object[] values) {
        if (columns.length != values.length) {
            throw new IllegalArgumentException("Column count must match value count");
        }
        this.columns.addAll(Arrays.asList(columns));
        this.values.addAll(Arrays.asList(values));
        return this;
    }

    /**
     * Builds the final INSERT query.
     * 
     * <p>This method constructs the complete SQL INSERT statement with parameterized values.
     * At least one column-value pair must be specified.
     * 
     * @return a {@link BuiltQuery} containing the SQL string and parameters
     * @throws IllegalStateException if no columns are specified
     */
    public BuiltQuery build() {
        if (columns.isEmpty()) {
            throw new IllegalStateException("At least one column-value pair must be specified");
        }

        String columnList = String.join(", ", columns.stream().map(Column::toSql).toList());
        String placeholders = "?, ".repeat(columns.size());
        placeholders = placeholders.substring(0, placeholders.length() - 2); // Remove trailing ", "

        String sql = "INSERT INTO " + table.toSql() + " (" + columnList + ") VALUES (" + placeholders + ")";

        return new BuiltQuery(sql, new ArrayList<>(values));
    }
}
