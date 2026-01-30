package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builder for constructing UPDATE queries with type-safe column and table identifiers.
 * 
 * <p>This builder provides a fluent API for building UPDATE queries. For safety,
 * at least one WHERE condition is required to prevent accidental updates of all rows.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * Table usersTable = Table.of("users");
 * Column idCol = Column.of("id");
 * Column nameCol = Column.of("name");
 * Column emailCol = Column.of("email");
 * 
 * BuiltQuery query = SqlBuilder.update(usersTable)
 *     .set(nameCol, "Jane")
 *     .set(emailCol, "jane@example.com")
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
 * updates of all rows in the table. This is enforced at build time.
 * 
 * <h3>SQL Injection Protection</h3>
 * 
 * <p>All table and column names use validated identifiers, and all values are
 * parameterized using PreparedStatement placeholders ({@code ?}).
 * 
 * @see SqlBuilder#update(Table)
 * @see BuiltQuery
 * @since 1.0
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
     * 
     * <p>This method can be called multiple times to set multiple columns.
     * If the same column is set multiple times, the last value is used.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * .set(nameCol, "Jane")
     * .set(emailCol, "jane@example.com")
     * }</pre>
     * 
     * @param column the column to update (validated identifier)
     * @param value  the new value (will be parameterized)
     * @return this builder for method chaining
     * @throws NullPointerException if column is null
     */
    public UpdateBuilder set(Column column, Object value) {
        setValues.put(column, value);
        return this;
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
    public UpdateBuilder where(Column column, Object value) {
        whereConditions.add(column.toSql() + " = ?");
        whereParams.add(value);
        return this;
    }

    /**
     * Builds the final UPDATE query.
     * 
     * <p>This method constructs the complete SQL UPDATE statement with parameterized values.
     * At least one SET clause and one WHERE condition must be specified.
     * 
     * @return a {@link BuiltQuery} containing the SQL string and parameters
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
