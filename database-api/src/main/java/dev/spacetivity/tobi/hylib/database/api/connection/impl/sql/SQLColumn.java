package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql;

import lombok.Getter;

/**
 * Represents a SQL column definition for table creation.
 * 
 * <p>This class encapsulates a column definition including its name, data type,
 * constraints (NOT NULL, PRIMARY KEY, AUTO_INCREMENT), and optional foreign key
 * relationships. It is used with {@link TableDefinition} to create database tables.
 * 
 * <h3>Usage Examples</h3>
 * 
 * <h4>Basic Column</h4>
 * <pre>{@code
 * SQLColumn nameCol = SQLColumn.from("name", SQLDataType.VARCHAR);
 * SQLColumn emailCol = SQLColumn.fromNullable("email", SQLDataType.VARCHAR);
 * }</pre>
 * 
 * <h4>Primary Key</h4>
 * <pre>{@code
 * SQLColumn idCol = SQLColumn.fromPrimary("id", SQLDataType.INTEGER);
 * SQLColumn uuidCol = SQLColumn.fromPrimaryAutoIncrement("uuid", SQLDataType.UUID);
 * }</pre>
 * 
 * <h4>Foreign Key</h4>
 * <pre>{@code
 * Table usersTable = Table.of("users");
 * Table postsTable = Table.of("posts");
 * Column userIdCol = Column.of("user_id");
 * Column idCol = Column.of("id");
 * 
 * SQLColumn fkCol = SQLColumn.fromForeignKey(
 *     userIdCol,
 *     SQLDataType.INTEGER,
 *     usersTable,
 *     idCol
 * );
 * }</pre>
 * 
 * <h3>Factory Methods</h3>
 * 
 * <p>This class provides numerous factory methods for common column patterns:
 * <ul>
 *   <li>{@link #from(Column, SQLDataType)} - Basic NOT NULL column</li>
 *   <li>{@link #fromNullable(Column, SQLDataType)} - Nullable column</li>
 *   <li>{@link #fromPrimary(Column, SQLDataType)} - Primary key column</li>
 *   <li>{@link #fromPrimaryAutoIncrement(Column, SQLDataType)} - Auto-increment primary key</li>
 *   <li>{@link #fromForeignKey(Column, SQLDataType, Table, Column)} - Foreign key column</li>
 *   <li>{@link #fromPrimaryForeignKey(Column, SQLDataType, Table, Column)} - Primary key with foreign key</li>
 * </ul>
 * 
 * <p>Most factory methods have overloads that accept either a {@link Column} object
 * or a {@code String} column name (which is automatically validated).
 * 
 * @see TableDefinition
 * @see Column
 * @see SQLDataType
 * @see ForeignKey
 * @since 1.0
 */
@Getter
public class SQLColumn {

    private final Column column;
    private final String value;
    private final ForeignKey foreignKey;

    public SQLColumn(Column column, String value) {
        this.column = column;
        this.value = value;
        this.foreignKey = null;
    }

    public SQLColumn(Column column, String value, ForeignKey foreignKey) {
        this.column = column;
        this.value = value;
        this.foreignKey = foreignKey;
    }

    public static SQLColumn from(Column column, String value) {
        return new SQLColumn(column, value);
    }

    public static SQLColumn from(String key, String value) {
        return new SQLColumn(Column.of(key), value);
    }

    public static SQLColumn fromPrimary(Column column, SQLDataType value, boolean notNull) {
        String valueString = value.getQueryText();
        if (notNull) valueString += " " + SQLDataType.NOT_NULL.getQueryText();
        valueString += " " + SQLDataType.PRIMARY_KEY.getQueryText();
        return new SQLColumn(column, valueString);
    }

    public static SQLColumn fromPrimary(Column column, SQLDataType value) {
        return fromPrimary(column, value, true);
    }

    public static SQLColumn fromPrimary(String key, SQLDataType value, boolean notNull) {
        return fromPrimary(Column.of(key), value, notNull);
    }

    public static SQLColumn fromPrimary(String key, SQLDataType value) {
        return fromPrimary(Column.of(key), value, true);
    }

    public static SQLColumn fromPrimaryNullable(String key, SQLDataType value) {
        return fromPrimary(Column.of(key), value, false);
    }

    public static SQLColumn fromPrimaryAutoIncrement(Column column, SQLDataType value, boolean notNull) {
        String valueString = value.getQueryText();
        if (notNull) valueString += " " + SQLDataType.NOT_NULL.getQueryText();
        valueString += " " + SQLDataType.AUTO_INCREMENT.getQueryText();
        valueString += " " + SQLDataType.PRIMARY_KEY.getQueryText();
        return new SQLColumn(column, valueString);
    }

    public static SQLColumn fromPrimaryAutoIncrement(Column column, SQLDataType value) {
        return fromPrimaryAutoIncrement(column, value, true);
    }

    public static SQLColumn fromPrimaryAutoIncrement(String key, SQLDataType value, boolean notNull) {
        return fromPrimaryAutoIncrement(Column.of(key), value, notNull);
    }

    public static SQLColumn fromPrimaryAutoIncrement(String key, SQLDataType value) {
        return fromPrimaryAutoIncrement(Column.of(key), value, true);
    }

    public static SQLColumn from(Column column, SQLDataType dataType) {
        return new SQLColumn(column, dataType.getQueryText() + " " + SQLDataType.NOT_NULL.getQueryText());
    }

    public static SQLColumn from(String key, SQLDataType dataType) {
        return from(Column.of(key), dataType);
    }

    public static SQLColumn fromNullable(Column column, SQLDataType dataType) {
        return new SQLColumn(column, dataType.getQueryText());
    }

    public static SQLColumn fromNullable(String key, SQLDataType dataType) {
        return fromNullable(Column.of(key), dataType);
    }

    /**
     * Creates a SQLColumn with a foreign key constraint.
     * @param column the column
     * @param dataType the data type
     * @param referencedTable the table being referenced
     * @param referencedColumn the column being referenced
     * @return a SQLColumn with foreign key constraint
     */
    public static SQLColumn fromForeignKey(Column column, SQLDataType dataType, Table referencedTable, Column referencedColumn) {
        String valueString = dataType.getQueryText() + " " + SQLDataType.NOT_NULL.getQueryText();
        ForeignKey foreignKey = new ForeignKey(referencedTable, referencedColumn);
        return new SQLColumn(column, valueString, foreignKey);
    }

    /**
     * Creates a SQLColumn with a foreign key constraint (nullable).
     * @param column the column
     * @param dataType the data type
     * @param referencedTable the table being referenced
     * @param referencedColumn the column being referenced
     * @return a nullable SQLColumn with foreign key constraint
     */
    public static SQLColumn fromNullableForeignKey(Column column, SQLDataType dataType, Table referencedTable, Column referencedColumn) {
        String valueString = dataType.getQueryText();
        ForeignKey foreignKey = new ForeignKey(referencedTable, referencedColumn);
        return new SQLColumn(column, valueString, foreignKey);
    }

    /**
     * Creates a SQLColumn with a foreign key constraint (using string column name).
     * @param key the column name
     * @param dataType the data type
     * @param referencedTable the table being referenced
     * @param referencedColumn the column being referenced
     * @return a SQLColumn with foreign key constraint
     */
    public static SQLColumn fromForeignKey(String key, SQLDataType dataType, Table referencedTable, Column referencedColumn) {
        return fromForeignKey(Column.of(key), dataType, referencedTable, referencedColumn);
    }

    /**
     * Creates a SQLColumn with a foreign key constraint (nullable, using string column name).
     * @param key the column name
     * @param dataType the data type
     * @param referencedTable the table being referenced
     * @param referencedColumn the column being referenced
     * @return a nullable SQLColumn with foreign key constraint
     */
    public static SQLColumn fromNullableForeignKey(String key, SQLDataType dataType, Table referencedTable, Column referencedColumn) {
        return fromNullableForeignKey(Column.of(key), dataType, referencedTable, referencedColumn);
    }

    /**
     * Creates a SQLColumn with a primary key and foreign key constraint.
     * @param column the column
     * @param dataType the data type
     * @param referencedTable the table being referenced
     * @param referencedColumn the column being referenced
     * @return a SQLColumn with primary key and foreign key constraint
     */
    public static SQLColumn fromPrimaryForeignKey(Column column, SQLDataType dataType, Table referencedTable, Column referencedColumn) {
        String valueString = dataType.getQueryText() + " " + SQLDataType.NOT_NULL.getQueryText();
        valueString += " " + SQLDataType.PRIMARY_KEY.getQueryText();
        ForeignKey foreignKey = new ForeignKey(referencedTable, referencedColumn);
        return new SQLColumn(column, valueString, foreignKey);
    }

    /**
     * Creates a SQLColumn with a primary key and foreign key constraint (using string column name).
     * @param key the column name
     * @param dataType the data type
     * @param referencedTable the table being referenced
     * @param referencedColumn the column being referenced
     * @return a SQLColumn with primary key and foreign key constraint
     */
    public static SQLColumn fromPrimaryForeignKey(String key, SQLDataType dataType, Table referencedTable, Column referencedColumn) {
        return fromPrimaryForeignKey(Column.of(key), dataType, referencedTable, referencedColumn);
    }
}
