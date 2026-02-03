package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder;

import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Column;
import dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.Table;

/**
 * Factory class for creating SQL query builders.
 * 
 * <p>This class provides a fluent API for building type-safe SQL queries. All queries
 * are built using validated {@link Table} and {@link Column} identifiers, preventing
 * SQL injection attacks. All queries use parameterized statements (PreparedStatement)
 * for safe parameter binding.
 * 
 * <h3>Supported Query Types</h3>
 * 
 * <ul>
 *   <li>{@link #select(Column...)} - SELECT queries</li>
 *   <li>{@link #insertInto(Table)} - INSERT queries</li>
 *   <li>{@link #update(Table)} - UPDATE queries</li>
 *   <li>{@link #deleteFrom(Table)} - DELETE queries</li>
 * </ul>
 * 
 * <h3>Usage Examples</h3>
 * 
 * <pre>{@code
 * Table usersTable = Table.of("users");
 * Column idCol = Column.of("id");
 * Column nameCol = Column.of("name");
 * 
 * // SELECT query
 * BuiltQuery selectQuery = SqlBuilder.select(idCol, nameCol)
 *     .from(usersTable)
 *     .where(idCol, 123)
 *     .build();
 * 
 * // INSERT query
 * BuiltQuery insertQuery = SqlBuilder.insertInto(usersTable)
 *     .value(idCol, 123)
 *     .value(nameCol, "John")
 *     .build();
 * 
 * // UPDATE query
 * BuiltQuery updateQuery = SqlBuilder.update(usersTable)
 *     .set(nameCol, "Jane")
 *     .where(idCol, 123)
 *     .build();
 * 
 * // DELETE query
 * BuiltQuery deleteQuery = SqlBuilder.deleteFrom(usersTable)
 *     .where(idCol, 123)
 *     .build();
 * }</pre>
 * 
 * <h3>SQL Injection Protection</h3>
 * 
 * <p>All queries use:
 * <ul>
 *   <li>Validated table and column identifiers</li>
 *   <li>Parameterized statements (PreparedStatement) for all values</li>
 *   <li>No string concatenation for user input</li>
 * </ul>
 * 
 * <h3>Query Execution</h3>
 * 
 * <p>Built queries return a {@link BuiltQuery} object containing the SQL string and
 * parameters. These can be executed using repository methods or directly with PreparedStatement.
 * 
 * @see Table
 * @see Column
 * @see BuiltQuery
 * @see SelectBuilder
 * @see InsertBuilder
 * @see UpdateBuilder
 * @see DeleteBuilder
 * @since 1.0
 */
public final class SqlBuilder {

    private SqlBuilder() {
    }

    /**
     * Creates a SELECT query builder.
     * 
     * <p>This method starts building a SELECT query with the specified columns.
     * You must specify at least one column to select.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * BuiltQuery query = SqlBuilder.select(idCol, nameCol, emailCol)
     *     .from(usersTable)
     *     .where(idCol, 123)
     *     .build();
     * }</pre>
     * 
     * @param columns the columns to select (must not be empty)
     * @return a {@link SelectBuilder} instance for building the SELECT query
     * @throws IllegalArgumentException if columns is null or empty
     * @throws NullPointerException if any column is null
     * @see SelectBuilder
     */
    public static SelectBuilder select(Column... columns) {
        if (columns == null || columns.length == 0) {
            throw new IllegalArgumentException("At least one column must be specified");
        }
        return new SelectBuilder(columns);
    }

    /**
     * Creates an INSERT query builder.
     * 
     * <p>This method starts building an INSERT query for the specified table.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * BuiltQuery query = SqlBuilder.insertInto(usersTable)
     *     .value(idCol, 123)
     *     .value(nameCol, "John")
     *     .value(emailCol, "john@example.com")
     *     .build();
     * }</pre>
     * 
     * @param table the table to insert into
     * @return an {@link InsertBuilder} instance for building the INSERT query
     * @throws NullPointerException if table is null
     * @see InsertBuilder
     */
    public static InsertBuilder insertInto(Table table) {
        return new InsertBuilder(table);
    }

    /**
     * Creates an UPDATE query builder.
     * 
     * <p>This method starts building an UPDATE query for the specified table.
     * You must specify at least one SET clause and typically a WHERE clause.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * BuiltQuery query = SqlBuilder.update(usersTable)
     *     .set(nameCol, "Jane")
     *     .set(emailCol, "jane@example.com")
     *     .where(idCol, 123)
     *     .build();
     * }</pre>
     * 
     * @param table the table to update
     * @return an {@link UpdateBuilder} instance for building the UPDATE query
     * @throws NullPointerException if table is null
     * @see UpdateBuilder
     */
    public static UpdateBuilder update(Table table) {
        return new UpdateBuilder(table);
    }

    /**
     * Creates a DELETE query builder.
     * 
     * <p>This method starts building a DELETE query for the specified table.
     * You should typically specify a WHERE clause to avoid deleting all rows.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * BuiltQuery query = SqlBuilder.deleteFrom(usersTable)
     *     .where(idCol, 123)
     *     .build();
     * }</pre>
     * 
     * @param table the table to delete from
     * @return a {@link DeleteBuilder} instance for building the DELETE query
     * @throws NullPointerException if table is null
     * @see DeleteBuilder
     */
    public static DeleteBuilder deleteFrom(Table table) {
        return new DeleteBuilder(table);
    }
}
