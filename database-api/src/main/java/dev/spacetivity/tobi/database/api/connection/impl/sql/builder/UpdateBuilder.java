package dev.spacetivity.tobi.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.database.api.connection.impl.sql.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for UPDATE queries with type-safe column and table identifiers.
 */
public class UpdateBuilder {
    private final Table table;
    private final Map<Column, Object> setValues = new LinkedHashMap<>();
    private final List<String> whereConditions = new ArrayList<>();
    private final List<Object> whereParams = new ArrayList<>();

    UpdateBuilder(Table table) {
        this.table = table;
    }

    /**
     * Adds a SET clause (column = value).
     * Can be called multiple times to set multiple columns.
     * @param column the column to update
     * @param value the new value
     * @return this builder for method chaining
     */
    public UpdateBuilder set(Column column, Object value) {
        setValues.put(column, value);
        return this;
    }

    /**
     * Adds a WHERE condition (column = value).
     * Can be called multiple times to add AND conditions.
     * @param column the column to compare
     * @param value the value to compare against
     * @return this builder for method chaining
     */
    public UpdateBuilder where(Column column, Object value) {
        whereConditions.add(column.toSql() + " = ?");
        whereParams.add(value);
        return this;
    }

    /**
     * Builds the final UPDATE query.
     * @return a BuiltQuery with SQL and parameters
     * @throws IllegalStateException if no SET values or WHERE conditions are specified
     */
    public BuiltQuery build() {
        if (setValues.isEmpty()) {
            throw new IllegalStateException("At least one SET clause must be specified");
        }
        if (whereConditions.isEmpty()) {
            throw new IllegalStateException("At least one WHERE condition must be specified for safety");
        }

        StringBuilder sql = new StringBuilder("UPDATE ");
        sql.append(table.toSql()).append(" SET ");

        List<String> setClauses = new ArrayList<>();
        List<Object> setParams = new ArrayList<>();
        for (Map.Entry<Column, Object> entry : setValues.entrySet()) {
            setClauses.add(entry.getKey().toSql() + " = ?");
            setParams.add(entry.getValue());
        }
        sql.append(String.join(", ", setClauses));

        sql.append(" WHERE ").append(String.join(" AND ", whereConditions));

        List<Object> allParams = new ArrayList<>(setParams);
        allParams.addAll(whereParams);

        return new BuiltQuery(sql.toString(), allParams);
    }
}
