package dev.spacetivity.tobi.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.database.api.connection.impl.sql.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for INSERT queries with type-safe column and table identifiers.
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
     * Can be called multiple times to add multiple columns.
     * @param column the column name
     * @param value the value to insert
     * @return this builder for method chaining
     */
    public InsertBuilder value(Column column, Object value) {
        columns.add(column);
        values.add(value);
        return this;
    }

    /**
     * Adds multiple column-value pairs at once.
     * @param columns the columns
     * @param values the corresponding values (must match column count)
     * @return this builder for method chaining
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
     * @return a BuiltQuery with SQL and parameters
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
