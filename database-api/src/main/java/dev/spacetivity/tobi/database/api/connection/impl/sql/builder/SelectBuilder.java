package dev.spacetivity.tobi.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.database.api.connection.impl.sql.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for SELECT queries with type-safe column and table identifiers.
 */
public class SelectBuilder {
    private final List<Column> columns;
    private Table table;
    private final List<String> whereConditions = new ArrayList<>();
    private final List<Object> params = new ArrayList<>();
    private String orderByColumn;
    private boolean orderAsc = true;
    private Integer limitValue;

    SelectBuilder(Column... columns) {
        this.columns = Arrays.asList(columns);
    }

    /**
     * Specifies the table to select from.
     * @param table the table
     * @return this builder for method chaining
     */
    public SelectBuilder from(Table table) {
        this.table = table;
        return this;
    }

    /**
     * Adds a WHERE condition (column = value).
     * Can be called multiple times to add AND conditions.
     * @param column the column to compare
     * @param value the value to compare against
     * @return this builder for method chaining
     */
    public SelectBuilder where(Column column, Object value) {
        whereConditions.add(column.toSql() + " = ?");
        params.add(value);
        return this;
    }

    /**
     * Adds an ORDER BY clause.
     * @param column the column to order by
     * @param ascending true for ASC, false for DESC
     * @return this builder for method chaining
     */
    public SelectBuilder orderBy(Column column, boolean ascending) {
        this.orderByColumn = column.toSql();
        this.orderAsc = ascending;
        return this;
    }

    /**
     * Adds a LIMIT clause.
     * @param limit the maximum number of rows to return
     * @return this builder for method chaining
     */
    public SelectBuilder limit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        this.limitValue = limit;
        return this;
    }

    /**
     * Builds the final query.
     * @return a BuiltQuery with SQL and parameters
     * @throws IllegalStateException if table is not set
     */
    public BuiltQuery build() {
        if (table == null) {
            throw new IllegalStateException("Table must be specified using from()");
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", columns.stream().map(Column::toSql).toList()));
        sql.append(" FROM ").append(table.toSql());

        if (!whereConditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", whereConditions));
        }

        if (orderByColumn != null) {
            sql.append(" ORDER BY ").append(orderByColumn);
            sql.append(orderAsc ? " ASC" : " DESC");
        }

        if (limitValue != null) {
            sql.append(" LIMIT ").append(limitValue);
        }

        return new BuiltQuery(sql.toString(), new ArrayList<>(params));
    }
}
