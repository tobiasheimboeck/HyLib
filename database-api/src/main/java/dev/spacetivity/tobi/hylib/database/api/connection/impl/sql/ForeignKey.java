package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql;

/**
 * Represents a foreign key constraint referencing another table and column.
 * 
 * <p>This record encapsulates the information needed to create a foreign key constraint
 * in a database table. Foreign keys enforce referential integrity by ensuring that
 * values in one table reference valid values in another table.
 * 
 * <h3>Usage</h3>
 * 
 * <p>Foreign keys are typically created using {@link SQLColumn} factory methods:
 * 
 * <pre>{@code
 * Table usersTable = Table.of("users");
 * Table postsTable = Table.of("posts");
 * Column userIdCol = Column.of("user_id");
 * Column idCol = Column.of("id");
 * 
 * SQLColumn fkColumn = SQLColumn.fromForeignKey(
 *     userIdCol,
 *     SQLDataType.INTEGER,
 *     usersTable,
 *     idCol
 * );
 * }</pre>
 * 
 * <p>This creates a foreign key constraint: {@code FOREIGN KEY (`user_id`) REFERENCES `users`(`id`)}
 * 
 * @param referencedTable   the table being referenced
 * @param referencedColumn   the column being referenced (typically a primary key)
 * @see SQLColumn#fromForeignKey(Column, SQLDataType, Table, Column)
 * @see TableDefinition
 * @since 1.0
 */
public record ForeignKey(Table referencedTable, Column referencedColumn) {
    public ForeignKey {
        if (referencedTable == null) {
            throw new IllegalArgumentException("Referenced table cannot be null");
        }
        if (referencedColumn == null) {
            throw new IllegalArgumentException("Referenced column cannot be null");
        }
    }
}
