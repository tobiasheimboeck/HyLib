package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql;

import lombok.Getter;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a complete table definition including columns and generation logic.
 * 
 * <p>This class manages the creation and migration of database tables. It encapsulates
 * a table name, column definitions, and provides methods to create the table in the
 * database if it doesn't exist, and to migrate existing tables (e.g., adding AUTO_INCREMENT).
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * Connection connection = dataSource.getConnection();
 * Table usersTable = Table.of("users");
 * 
 * TableDefinition tableDef = TableDefinition.create(connection, usersTable,
 *     SQLColumn.fromPrimary("id", SQLDataType.INTEGER),
 *     SQLColumn.from("name", SQLDataType.VARCHAR),
 *     SQLColumn.fromNullable("email", SQLDataType.VARCHAR)
 * );
 * 
 * tableDef.generate();  // Creates the table if it doesn't exist
 * }</pre>
 * 
 * <h3>Table Creation</h3>
 * 
 * <p>The {@link #generate()} method creates the table using {@code CREATE TABLE IF NOT EXISTS},
 * so it's safe to call multiple times. The table will only be created if it doesn't already exist.
 * 
 * <h3>Migration Support</h3>
 * 
 * <p>This class includes automatic migration support for adding AUTO_INCREMENT to existing columns.
 * When {@link #generate()} is called, it checks existing columns and adds AUTO_INCREMENT if
 * the column definition requires it but the existing column doesn't have it.
 * 
 * <h3>Foreign Keys</h3>
 * 
 * <p>Foreign key constraints are automatically added to the CREATE TABLE statement when
 * columns have associated {@link ForeignKey} objects.
 * 
 * @see SQLColumn
 * @see Table
 * @see ForeignKey
 * @since 1.0
 */
@Getter
public class TableDefinition {

    private final Connection connection;
    private final Table table;
    private final List<SQLColumn> values;

    private final List<Column> tableFields;

    public TableDefinition(Connection connection, Table table, List<SQLColumn> values) {
        this.connection = connection;
        this.table = table;
        this.values = values;
        this.tableFields = this.values.stream().map(SQLColumn::getColumn).toList();
    }

    /**
     * Generates the table in the database if it doesn't exist.
     * 
     * <p>This method creates the table with all defined columns and constraints using
     * {@code CREATE TABLE IF NOT EXISTS}. It's safe to call multiple times - the table
     * will only be created if it doesn't already exist.
     * 
     * <p>After creating the table (or if it already exists), this method also performs
     * migration checks to add AUTO_INCREMENT to existing columns if needed.
     * 
     * <h3>What Gets Created</h3>
     * 
     * <ul>
     *   <li>All columns with their data types and constraints (NOT NULL, PRIMARY KEY, AUTO_INCREMENT)</li>
     *   <li>Foreign key constraints (if specified in column definitions)</li>
     * </ul>
     * 
     * <h3>Migration</h3>
     * 
     * <p>If the table already exists, this method checks each column definition for AUTO_INCREMENT
     * requirements. If a column definition includes AUTO_INCREMENT but the existing column doesn't
     * have it, an {@code ALTER TABLE MODIFY COLUMN} statement is executed to add it.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * TableDefinition tableDef = TableDefinition.create(connection, Table.of("users"),
     *     SQLColumn.fromPrimaryAutoIncrement("id", SQLDataType.INTEGER),
     *     SQLColumn.from("name", SQLDataType.VARCHAR)
     * );
     * 
     * tableDef.generate();  // Creates table or migrates if needed
     * }</pre>
     * 
     * @throws SQLException if a database access error occurs
     * @see #migrateAutoIncrementColumns()
     */
    public void generate() {
        StringJoiner fieldsString = new StringJoiner(", ");
        values.forEach(sqlColumn -> fieldsString.add(sqlColumn.getColumn().toSql() + " " + sqlColumn.getValue()));
        
        // Add foreign key constraints
        for (SQLColumn sqlColumn : values) {
            if (sqlColumn.getForeignKey() != null) {
                ForeignKey fk = sqlColumn.getForeignKey();
                fieldsString.add("FOREIGN KEY (" + sqlColumn.getColumn().toSql() + ") REFERENCES " + 
                    fk.referencedTable().toSql() + "(" + fk.referencedColumn().toSql() + ")");
            }
        }
        
        try (PreparedStatement statement = this.connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + table.toSql() + " (" + fieldsString + ")")) {
            statement.execute();
            Logger.getGlobal().log(Level.INFO, "Table checked/created: " + table.name());
            // Always check if migration is needed (for existing tables that may need AUTO_INCREMENT added)
            migrateAutoIncrementColumns();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Migrates existing columns to add AUTO_INCREMENT if needed.
     * 
     * <p>This private method is called by {@link #generate()} to check each column definition
     * and add AUTO_INCREMENT to existing columns if the definition requires it but the
     * existing column doesn't have it.
     * 
     * <p>The migration uses {@code ALTER TABLE MODIFY COLUMN} to update the column definition.
     * 
     * @throws SQLException if a database access error occurs (logged as warning, not thrown)
     */
    private void migrateAutoIncrementColumns() {
        try {
            DatabaseMetaData metaData = connection.getMetaData();
            String catalog = connection.getCatalog();
            // Schema can be null for MariaDB/MySQL, which is fine - pass null
            String schema = connection.getSchema();
            
            // Check each column definition for AUTO_INCREMENT requirement
            for (SQLColumn sqlColumn : values) {
                String columnValue = sqlColumn.getValue();
                // Check if this column definition includes AUTO_INCREMENT
                if (columnValue.contains(SQLDataType.AUTO_INCREMENT.getQueryText())) {
                    Column column = sqlColumn.getColumn();
                    String columnName = column.name();
                    
                    // Check if the column exists and if it has AUTO_INCREMENT
                    // Use null for schema if it's null (common in MariaDB/MySQL)
                    try (ResultSet columns = metaData.getColumns(catalog, schema, table.name(), columnName)) {
                        if (columns.next()) {
                            String isAutoIncrement = columns.getString("IS_AUTOINCREMENT");
                            // "NO" or null means it doesn't have AUTO_INCREMENT, we need to add it
                            if (isAutoIncrement == null || "NO".equalsIgnoreCase(isAutoIncrement)) {
                                alterColumnToAutoIncrement(column, columnValue);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "Failed to migrate AUTO_INCREMENT columns for table " + table.name() + ": " + e.getMessage());
        }
    }

    /**
     * Alters a column to add AUTO_INCREMENT.
     * 
     * <p>This private method executes an {@code ALTER TABLE MODIFY COLUMN} statement to
     * add AUTO_INCREMENT to an existing column. The full column definition (including
     * data type and all constraints) is used to ensure the column definition matches
     * the expected schema.
     * 
     * @param column                the column to alter
     * @param fullColumnDefinition the complete column definition including data type and constraints
     * @throws SQLException if a database access error occurs (logged as warning, not thrown)
     */
    private void alterColumnToAutoIncrement(Column column, String fullColumnDefinition) {
        try {
            // Extract the data type and constraints from the full definition
            // Format: "INT NOT NULL AUTO_INCREMENT PRIMARY KEY"
            // We need to construct: "ALTER TABLE table MODIFY COLUMN column INT NOT NULL AUTO_INCREMENT PRIMARY KEY"
            String alterSql = "ALTER TABLE " + table.toSql() + " MODIFY COLUMN " + column.toSql() + " " + fullColumnDefinition;
            
            try (PreparedStatement statement = connection.prepareStatement(alterSql)) {
                statement.execute();
                Logger.getGlobal().log(Level.INFO, "Successfully migrated column " + column.name() + " to AUTO_INCREMENT in table " + table.name());
            }
        } catch (SQLException e) {
            Logger.getGlobal().log(Level.WARNING, "Failed to alter column " + column.name() + " to AUTO_INCREMENT: " + e.getMessage());
        }
    }

    /**
     * Creates a {@code TableDefinition} with a {@link Table} instance.
     * 
     * <p>This factory method creates a new table definition with the specified connection,
     * table identifier, and column definitions.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * Table usersTable = Table.of("users");
     * TableDefinition tableDef = TableDefinition.create(connection, usersTable,
     *     SQLColumn.fromPrimary("id", SQLDataType.INTEGER),
     *     SQLColumn.from("name", SQLDataType.VARCHAR)
     * );
     * }</pre>
     * 
     * @param connection the database connection to use for table creation
     * @param table      the table identifier (validated)
     * @param values     the column definitions (varargs)
     * @return a new {@code TableDefinition} instance
     * @throws NullPointerException if connection, table, or any value is null
     */
    public static TableDefinition create(Connection connection, Table table, SQLColumn... values) {
        return new TableDefinition(connection, table, Arrays.stream(values).toList());
    }

    /**
     * Creates a {@code TableDefinition} with a table name string (convenience method).
     * 
     * <p>This factory method is a convenience overload that accepts a table name string,
     * which is automatically validated and converted to a {@link Table} instance.
     * 
     * <h3>Example</h3>
     * 
     * <pre>{@code
     * TableDefinition tableDef = TableDefinition.create(connection, "users",
     *     SQLColumn.fromPrimary("id", SQLDataType.INTEGER),
     *     SQLColumn.from("name", SQLDataType.VARCHAR)
     * );
     * }</pre>
     * 
     * @param connection the database connection to use for table creation
     * @param tableName  the table name (will be validated)
     * @param values     the column definitions (varargs)
     * @return a new {@code TableDefinition} instance
     * @throws IllegalArgumentException if tableName is invalid
     * @throws NullPointerException if connection or any value is null
     * @see Table#of(String)
     */
    public static TableDefinition create(Connection connection, String tableName, SQLColumn... values) {
        return new TableDefinition(connection, Table.of(tableName), Arrays.stream(values).toList());
    }

    /**
     * Gets the table name as a string.
     * 
     * <p>This is a convenience method that returns the name of the table without
     * the backticks used in SQL formatting.
     * 
     * @return the table name (e.g., "users")
     */
    public String getName() {
        return table.name();
    }
}
