package dev.spacetivity.tobi.database.api.connection.impl.sql;

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
 * This class manages the creation of database tables and provides metadata about the table structure.
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
     * This method creates the table with all defined columns and constraints.
     * Also migrates existing tables if columns need AUTO_INCREMENT added.
     */
    public void generate() {
        StringJoiner fieldsString = new StringJoiner(", ");
        values.forEach(sqlColumn -> fieldsString.add(sqlColumn.getColumn().toSql() + " " + sqlColumn.getValue()));
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
     * Checks each column definition and alters the table if AUTO_INCREMENT is missing.
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
     * Extracts the column definition and applies ALTER TABLE MODIFY COLUMN.
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
     * Creates a TableDefinition with a Table instance.
     * @param connection the database connection
     * @param table the table identifier
     * @param values the column definitions
     * @return a new TableDefinition instance
     */
    public static TableDefinition create(Connection connection, Table table, SQLColumn... values) {
        return new TableDefinition(connection, table, Arrays.stream(values).toList());
    }

    /**
     * Creates a TableDefinition with a table name string (convenience method).
     * @param connection the database connection
     * @param tableName the table name (will be validated)
     * @param values the column definitions
     * @return a new TableDefinition instance
     */
    public static TableDefinition create(Connection connection, String tableName, SQLColumn... values) {
        return new TableDefinition(connection, Table.of(tableName), Arrays.stream(values).toList());
    }

    /**
     * Gets the table name as a string.
     * @return the table name
     */
    public String getName() {
        return table.name();
    }
}
