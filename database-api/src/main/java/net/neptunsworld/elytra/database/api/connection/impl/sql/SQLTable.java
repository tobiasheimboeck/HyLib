package net.neptunsworld.elytra.database.api.connection.impl.sql;

import lombok.Getter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class SQLTable {

    private final Connection connection;
    private final String name;
    private final List<SQLColumn> values;

    private final List<String> tableFields;

    public SQLTable(Connection connection, String name, List<SQLColumn> values) {
        this.connection = connection;
        this.name = name;
        this.values = values;
        this.tableFields = this.values.stream().map(SQLColumn::getKey).toList();
    }

    public void generate() {
        StringJoiner fieldsString = new StringJoiner(", ");
        values.forEach(SQLColumn -> fieldsString.add(SQLColumn.getKey() + " " + SQLColumn.getValue()));
        try (Connection connection = this.connection) {
            String queryStatement = "CREATE TABLE IF NOT EXISTS " + name + " (" + fieldsString + ")";
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            boolean execute = statement.execute();
            if (execute) Logger.getGlobal().log(Level.INFO, "SUCCESSFULLY CREATED TABLE (" + name + ")");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static SQLTable create(Connection connection, String name, SQLColumn... values) {
        return new SQLTable(connection, name, Arrays.stream(values).toList());
    }

}
