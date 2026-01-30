package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Builder for constructing SELECT queries with type-safe column and table identifiers.
 * 
 * <p>This builder provides a fluent API for building SELECT queries with support for:
 * <ul>
 *   <li>Column selection</li>
 *   <li>Table specification (FROM clause)</li>
 *   <li>JOINs (INNER, LEFT, RIGHT)</li>
 *   <li>WHERE conditions (multiple AND conditions)</li>
 *   <li>ORDER BY clause</li>
 *   <li>LIMIT clause</li>
 * </ul>
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * Table usersTable = Table.of("users");
 * Table postsTable = Table.of("posts");
 * Column idCol = Column.of("id");
 * Column nameCol = Column.of("name");
 * Column titleCol = Column.of("title");
 * 
 * BuiltQuery query = SqlBuilder.select(idCol, nameCol, titleCol)
 *     .from(usersTable)
 *     .innerJoin(postsTable, Column.of("user_id"), idCol)
 *     .where(idCol, 123)
 *     .orderBy(nameCol, true)
 *     .limit(10)
 *     .build();
 * }</pre>
 * 
 * <h3>SQL Injection Protection</h3>
 * 
 * <p>All table and column names use validated identifiers, and all values are
 * parameterized using PreparedStatement placeholders ({@code ?}).
 * 
 * @see SqlBuilder#select(Column...)
 * @see BuiltQuery
 * @since 1.0
 */
public class SelectBuilder {
    private final List<Column> columns;
    private Table table;
    private final List<Join> joins = new ArrayList<>();
    private final List<String> whereConditions = new ArrayList<>();
    private final List<Object> params = new ArrayList<>();
    private String orderByColumn;
    private boolean orderAsc = true;
    private Integer limitValue;

    SelectBuilder(Column... columns) {
        this.columns = Arrays.asList(columns);
    }

    /**
     * Specifies the table to select from (FROM clause).
     * 
     * <p>This method is required and must be called before building the query.
     * 
     * @param table the table to select from
     * @return this builder for method chaining
     * @throws NullPointerException if table is null
     */
    public SelectBuilder from(Table table) {
        this.table = table;
        return this;
    }

    /**
     * Adds an INNER JOIN clause.
     * 
     * <p>An INNER JOIN returns only rows that have matching values in both tables.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .from(usersTable)
     * .innerJoin(postsTable, Column.of("user_id"), Column.of("id"))
     * }</pre>
     * 
     * @param table       the table to join
     * @param leftColumn  the column from the current table or previous join
     * @param rightColumn the column from the joined table
     * @return this builder for method chaining
     * @throws NullPointerException if any parameter is null
     */
    public SelectBuilder innerJoin(Table table, Column leftColumn, Column rightColumn) {
        joins.add(new Join(JoinType.INNER, table, leftColumn, rightColumn));
        return this;
    }

    /**
     * Adds a LEFT JOIN clause.
     * 
     * <p>A LEFT JOIN returns all rows from the left table and matching rows from the right table.
     * If there's no match, NULL values are returned for right table columns.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .from(usersTable)
     * .leftJoin(postsTable, Column.of("id"), Column.of("user_id"))
     * }</pre>
     * 
     * @param table       the table to join
     * @param leftColumn  the column from the current table or previous join
     * @param rightColumn the column from the joined table
     * @return this builder for method chaining
     * @throws NullPointerException if any parameter is null
     */
    public SelectBuilder leftJoin(Table table, Column leftColumn, Column rightColumn) {
        joins.add(new Join(JoinType.LEFT, table, leftColumn, rightColumn));
        return this;
    }

    /**
     * Adds a RIGHT JOIN clause.
     * 
     * <p>A RIGHT JOIN returns all rows from the right table and matching rows from the left table.
     * If there's no match, NULL values are returned for left table columns.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .from(usersTable)
     * .rightJoin(postsTable, Column.of("id"), Column.of("user_id"))
     * }</pre>
     * 
     * @param table       the table to join
     * @param leftColumn  the column from the current table or previous join
     * @param rightColumn the column from the joined table
     * @return this builder for method chaining
     * @throws NullPointerException if any parameter is null
     */
    public SelectBuilder rightJoin(Table table, Column leftColumn, Column rightColumn) {
        joins.add(new Join(JoinType.RIGHT, table, leftColumn, rightColumn));
        return this;
    }

    /**
     * Adds a WHERE condition (column = value).
     * 
     * <p>This method can be called multiple times to add multiple AND conditions.
     * All conditions are combined with AND logic.
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
     * @param column the column to compare
     * @param value  the value to compare against (will be parameterized)
     * @return this builder for method chaining
     * @throws NullPointerException if column is null
     */
    public SelectBuilder where(Column column, Object value) {
        whereConditions.add(column.toSql() + " = ?");
        params.add(value);
        return this;
    }

    /**
     * Adds a WHERE condition with a qualified column (table.column = value).
     * 
     * <p>This method is useful when working with JOINs to specify which table's column
     * to use, avoiding ambiguity when multiple tables have columns with the same name.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .from(usersTable)
     * .innerJoin(postsTable, Column.of("id"), Column.of("user_id"))
     * .where(usersTable, Column.of("id"), 123)  // Explicitly use users.id
     * }</pre>
     * 
     * @param table  the table containing the column
     * @param column the column to compare
     * @param value  the value to compare against (will be parameterized)
     * @return this builder for method chaining
     * @throws NullPointerException if table or column is null
     */
    public SelectBuilder where(Table table, Column column, Object value) {
        whereConditions.add(table.toSql() + "." + column.toSql() + " = ?");
        params.add(value);
        return this;
    }

    /**
     * Adds an ORDER BY clause.
     * 
     * <p>This method specifies the column to sort by and the sort direction.
     * Only one ORDER BY clause is supported (calling this multiple times replaces the previous one).
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .orderBy(nameCol, true)   // ASC order
     * .orderBy(nameCol, false)  // DESC order
     * }</pre>
     * 
     * @param column    the column to order by
     * @param ascending {@code true} for ASC, {@code false} for DESC
     * @return this builder for method chaining
     * @throws NullPointerException if column is null
     */
    public SelectBuilder orderBy(Column column, boolean ascending) {
        this.orderByColumn = column.toSql();
        this.orderAsc = ascending;
        return this;
    }

    /**
     * Adds a LIMIT clause.
     * 
     * <p>This method limits the number of rows returned by the query.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .limit(10)  // Return maximum 10 rows
     * }</pre>
     * 
     * @param limit the maximum number of rows to return (must be positive)
     * @return this builder for method chaining
     * @throws IllegalArgumentException if limit is not positive
     */
    public SelectBuilder limit(int limit) {
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be positive");
        }
        this.limitValue = limit;
        return this;
    }

    /**
     * Builds the final SELECT query.
     * 
     * <p>This method constructs the complete SQL query string and parameter list.
     * The table must be specified using {@link #from(Table)} before calling this method.
     * 
     * @return a {@link BuiltQuery} containing the SQL string and parameters
     * @throws IllegalStateException if table is not set (from() was not called)
     */
    public BuiltQuery build() {
        if (table == null) {
            throw new IllegalStateException("Table must be specified using from()");
        }

        StringBuilder sql = new StringBuilder("SELECT ");
        sql.append(String.join(", ", columns.stream().map(Column::toSql).toList()));
        sql.append(" FROM ").append(table.toSql());

        // Add JOIN clauses
        for (Join join : joins) {
            sql.append(" ").append(join.type().getSqlKeyword());
            sql.append(" ").append(join.table().toSql());
            sql.append(" ON ").append(join.leftColumn().toSql());
            sql.append(" = ").append(join.rightColumn().toSql());
        }

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
