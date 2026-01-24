package dev.spacetivity.tobi.database.api.connection.impl.sql;

import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
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
     */
    public void generate() {
        StringJoiner fieldsString = new StringJoiner(", ");
        values.forEach(sqlColumn -> fieldsString.add(sqlColumn.getColumn().toSql() + " " + sqlColumn.getValue()));
        try (PreparedStatement statement = this.connection.prepareStatement(
                "CREATE TABLE IF NOT EXISTS " + table.toSql() + " (" + fieldsString + ")")) {
            boolean execute = statement.execute();
            if (execute) Logger.getGlobal().log(Level.INFO, "SUCCESSFULLY CREATED TABLE (" + table.name() + ")");
        } catch (SQLException e) {
            e.printStackTrace();
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
