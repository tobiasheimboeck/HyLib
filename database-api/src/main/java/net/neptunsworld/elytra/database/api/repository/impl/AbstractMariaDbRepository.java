package net.neptunsworld.elytra.database.api.repository.impl;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import lombok.SneakyThrows;
import net.neptunsworld.elytra.database.api.ElytraDatabaseProvider;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnectionHandler;
import net.neptunsworld.elytra.database.api.connection.DatabaseConnector;
import net.neptunsworld.elytra.database.api.connection.DatabaseType;
import net.neptunsworld.elytra.database.api.connection.credentials.DatabaseCredentials;
import net.neptunsworld.elytra.database.api.connection.impl.sql.QueryStatement;
import net.neptunsworld.elytra.database.api.connection.impl.sql.SQLTable;
import net.neptunsworld.elytra.database.api.repository.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class AbstractMariaDbRepository<T> implements Repository {

    private final DatabaseConnectionHandler db;
    private final SQLTable table;

    public AbstractMariaDbRepository(DatabaseConnectionHandler db, SQLTable table) {
        this.db = db;
        this.table = table;
        this.table.generate();
    }

    @SneakyThrows
    protected Connection readConnection() {
        DatabaseConnector<HikariDataSource, DatabaseCredentials> databaseConnector = this.db.getConnectorNullsafe(DatabaseType.MARIADB);
        return databaseConnector.getSafeConnection().getConnection();
    }

    protected String createInsertQueryStatement(String tableName, List<String> fields) {
        List<String> valueQuestionMarks = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            valueQuestionMarks.add("?");
        }
        return createRawInsertQueryStatement(tableName, fields, valueQuestionMarks);
    }

    protected String createRawInsertQueryStatement(String tableName, List<String> fields, List<String> values) {
        StringJoiner fieldsString = new StringJoiner(", ");
        StringJoiner valuesString = new StringJoiner(", ");
        for (String field : fields) fieldsString.add(field);
        for (String value : values) valuesString.add(value);
        return "INSERT INTO " + tableName + " (" + fieldsString + ") VALUES (" + valuesString + ")";
    }

    protected String createUpdateQueryStatement(String tableName, String key, List<String> fields) {
        StringJoiner fieldsString = new StringJoiner(",");
        for (String field : fields) fieldsString.add(field + "=?");
        return "UPDATE " + tableName + " SET " + fieldsString + " WHERE " + key + "=?";
    }

    protected String createUpdateQueryStatement(String tableName, List<String> keys, List<String> fields) {
        StringJoiner fieldsString = new StringJoiner(",");
        for (String field : fields) fieldsString.add(field + "=?");

        StringJoiner keysString = new StringJoiner(" AND ");
        for (String key : keys) keysString.add(key + "=?");

        return "UPDATE " + tableName + " SET " + fieldsString + " WHERE " + keysString;
    }

    public boolean isExist(String keyField, String key) {
        String queryStatement = MessageFormat.format("SELECT * FROM {0} WHERE {1}=?", this.table.getName(), keyField);
        boolean response;

        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            statement.setString(1, key);

            ResultSet resultSet = statement.executeQuery();
            statement.close();
            response = resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

    public CompletableFuture<T> getAsyncWithMultipleKeys(List<String> keyFields, List<String> keys) {
        CompletableFuture<T> asyncTask = new CompletableFuture<>();
        ElytraDatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(getSyncWithMultipleKeys(keyFields, keys)));
        return asyncTask;
    }

    public CompletableFuture<T> getAsync(String keyField, String key) {
        CompletableFuture<T> asyncTask = new CompletableFuture<>();
        ElytraDatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(getSync(keyField, key)));
        return asyncTask;
    }

    public CompletableFuture<List<T>> getAllAsyncWithMultipleKeys(List<String> keyFields, List<String> keys) {
        CompletableFuture<List<T>> asyncTask = new CompletableFuture<>();
        ElytraDatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(getAllSyncWithMultipleKeys(keyFields, keys)));
        return asyncTask;
    }

    public CompletableFuture<List<T>> getAllAsync() {
        CompletableFuture<List<T>> asyncTask = new CompletableFuture<>();
        ElytraDatabaseProvider.getApi().getExecutorService().execute(() -> asyncTask.complete(getAllSync()));
        return asyncTask;
    }

    public T getSyncWithMultipleKeys(List<String> keyFields, List<String> keys) {
        StringBuilder modifiedKeyFields = new StringBuilder();

        for (String keyField : keyFields) {
            modifiedKeyFields.append("`").append(keyField).append("`=?");
            if (keyFields.indexOf(keyField) != keyFields.size() - 1) modifiedKeyFields.append(" AND ");
        }

        String queryStatement = "SELECT * FROM " + this.table.getName() + " WHERE " + modifiedKeyFields;
        T result;

        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            int keyIndex = 1;

            for (String key : keys) {
                statement.setString(keyIndex, key);
                keyIndex++;
            }

            ResultSet resultSet = statement.executeQuery();
            boolean hasNext = resultSet.next();
            statement.close();
            result = hasNext ? deserializeResultSet(resultSet) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public T getSync(String keyField, String key) {
        String queryStatement = MessageFormat.format(QueryStatement.SELECT_ONE.getStatement(), this.table.getName(), keyField);
        T result;

        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            statement.setString(1, key);
            ResultSet resultSet = statement.executeQuery();
            boolean hasNext = resultSet.next();
            statement.close();
            result = hasNext ? deserializeResultSet(resultSet) : null;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public List<T> getAllSyncWithMultipleKeys(List<String> keyFields, List<String> keys) {
        StringBuilder modifiedKeyFields = new StringBuilder();

        for (String keyField : keyFields) {
            modifiedKeyFields.append("`").append(keyField).append("`=?");
            if (keyFields.indexOf(keyField) != keyFields.size() - 1) modifiedKeyFields.append(" AND ");
        }

        String queryStatement = "SELECT * FROM " + this.table.getName() + " WHERE " + modifiedKeyFields;
        List<T> result = new ArrayList<>();

        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            int keyIndex = 1;

            for (String key : keys) {
                statement.setString(keyIndex, key);
                keyIndex++;
            }

            ResultSet resultSet = statement.executeQuery();
            statement.close();
            while (resultSet.next()) result.add(deserializeResultSet(resultSet));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public List<T> getAllSync() {
        String queryStatement = MessageFormat.format(QueryStatement.SELECT_ALL.getStatement(), this.table.getName());
        List<T> result = new ArrayList<>();

        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            ResultSet resultSet = statement.executeQuery();
            statement.close();
            while (resultSet.next()) result.add(deserializeResultSet(resultSet));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public void deleteWithMultipleKeys(List<String> keyFields, List<String> keys, int... startKeyIndex) {
        StringBuilder modifiedKeyFields = new StringBuilder();

        for (String keyField : keyFields) {
            modifiedKeyFields.append("`").append(keyField).append("`=?");
            if (keyFields.indexOf(keyField) != keyFields.size() - 1) modifiedKeyFields.append(" AND ");
        }

        String queryStatement = "DELETE FROM `" + this.table.getName() + "` WHERE " + modifiedKeyFields;

        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            int keyIndex = startKeyIndex.length == 0 ? 1 : startKeyIndex[0];

            for (String key : keys) {
                statement.setString(keyIndex, key);
                keyIndex++;
            }

            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String keyField, String key) {
        String queryStatement = "DELETE FROM `" + this.table.getName() + "` WHERE `" + keyField + "`=?";
        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            statement.setString(1, key);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateField(Gson gson, String keyField, String key, String fieldToUpdate, Object newValue) {
        String queryStatement = "UPDATE " + this.table.getName() + " SET `" + fieldToUpdate + "`=? WHERE `" + keyField + "`=?";
        boolean success;

        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(queryStatement);

            if (newValue instanceof List<?>) statement.setString(1, gson.toJson(newValue));
            else if (newValue instanceof Map<?, ?>) statement.setString(1, gson.toJson(newValue));
            else statement.setObject(1, newValue);

            statement.setString(2, key);
            statement.executeUpdate();
            statement.close();
            success = true;
        } catch (SQLException e) {
            success = false;
        }

        return success;
    }

    public boolean updateFields(Gson gson, String keyField, String key, Map<String, Object> fieldsToUpdate) {
        String modifiedFields = fieldsToUpdate.keySet().stream()
                .map(field -> "`" + field + "`=?")
                .collect(Collectors.joining(", "));

        String queryStatement = "UPDATE " + this.table.getName() + " SET " + modifiedFields + " WHERE `" + keyField + "`=?";
        boolean success;

        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            int index = 1;

            for (Object value : fieldsToUpdate.values()) {
                if (value instanceof List<?>) statement.setString(index, gson.toJson(value));
                else statement.setObject(index, value);
                index++;
            }

            statement.setString(index, key);
            statement.executeUpdate();
            statement.close();
            success = true;
        } catch (SQLException e) {
            success = false;
        }

        return success;
    }

    public boolean updateFieldsWithMultipleKeys(Gson gson, List<String> keyFields, List<String> keys, Map<String, Object> fieldsToUpdate) {
        String modifiedFields = fieldsToUpdate.keySet().stream()
                .map(field -> "`" + field + "`=?")
                .collect(Collectors.joining(", "));
        StringBuilder modifiedKeyFields = new StringBuilder();

        for (String keyField : keyFields) {
            modifiedKeyFields.append("`").append(keyField).append("`=?");
            if (keyFields.indexOf(keyField) != keyFields.size() - 1) modifiedKeyFields.append(" AND ");
        }

        String queryStatement = "UPDATE " + this.table.getName() + " SET " + modifiedFields + " WHERE " + modifiedKeyFields;
        boolean success;

        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            int index = 1;

            for (Object value : fieldsToUpdate.values()) {
                if (value instanceof List<?>) statement.setString(index, gson.toJson(value));
                else statement.setObject(index, value);
                index++;
            }

            int keyIndex = index + 1;

            for (String key : keys) statement.setString(keyIndex, key);

            statement.executeUpdate();
            statement.close();
            success = true;
        } catch (SQLException e) {
            success = false;
        }

        return success;
    }

    public void prepareInsertStatement(Consumer<PreparedStatement> result) {
        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(createInsertQueryStatement(this.table.getName(), this.table.getTableFields()));
            result.accept(statement);
            statement.executeUpdate();
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void prepareUpdateStatement(String key, Consumer<PreparedStatement> result) {
        try (Connection connection = readConnection()) {
            PreparedStatement statement = connection.prepareStatement(createUpdateQueryStatement(this.table.getName(), key, this.table.getTableFields()));
            result.accept(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void prepareUpdateStatement(List<String> keys, Consumer<PreparedStatement> result) {
        try (Connection connection = readConnection()) {
            String queryStatement = createUpdateQueryStatement(this.table.getName(), keys, this.table.getTableFields());
            PreparedStatement statement = connection.prepareStatement(queryStatement);
            result.accept(statement);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public abstract T deserializeResultSet(ResultSet resultSet);

    public abstract void insert(T value);

}
