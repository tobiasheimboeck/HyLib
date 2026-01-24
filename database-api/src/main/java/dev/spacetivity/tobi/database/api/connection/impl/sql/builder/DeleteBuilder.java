package dev.spacetivity.tobi.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.database.api.connection.impl.sql.Table;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for DELETE queries with type-safe column and table identifiers.
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
     * Can be called multiple times to add AND conditions.
     * @param column the column to compare
     * @param value the value to compare against
     * @return this builder for method chaining
     */
    public DeleteBuilder where(Column column, Object value) {
        whereConditions.add(column.toSql() + " = ?");
        params.add(value);
        return this;
    }

    /**
     * Builds the final DELETE query.
     * @return a BuiltQuery with SQL and parameters
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
