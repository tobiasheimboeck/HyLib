package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql.builder;

/**
 * Enumeration of SQL JOIN types.
 * 
 * <p>This enum represents the different types of JOIN operations available in SQL:
 * <ul>
 *   <li>{@link #INNER} - Returns only rows with matching values in both tables</li>
 *   <li>{@link #LEFT} - Returns all rows from the left table and matching rows from the right table</li>
 *   <li>{@link #RIGHT} - Returns all rows from the right table and matching rows from the left table</li>
 * </ul>
 * 
 * <h3>Usage</h3>
 * 
 * <p>Join types are used internally by {@link Join} records and can be accessed via
 * the {@link SelectBuilder} join methods.
 * 
 * @see Join
 * @see SelectBuilder#innerJoin(dev.spacetivity.tobi.database.api.connection.impl.sql.Table, dev.spacetivity.tobi.database.api.connection.impl.sql.Column, dev.spacetivity.tobi.database.api.connection.impl.sql.Column)
 * @since 1.0
 */
public enum JoinType {
    
    /**
     * INNER JOIN - Returns only rows that have matching values in both tables.
     * SQL: {@code INNER JOIN}
     */
    INNER("INNER JOIN"),
    
    /**
     * LEFT JOIN - Returns all rows from the left table and matching rows from the right table.
     * If there's no match, NULL values are returned for right table columns.
     * SQL: {@code LEFT JOIN}
     */
    LEFT("LEFT JOIN"),
    
    /**
     * RIGHT JOIN - Returns all rows from the right table and matching rows from the left table.
     * If there's no match, NULL values are returned for left table columns.
     * SQL: {@code RIGHT JOIN}
     */
    RIGHT("RIGHT JOIN");

    private final String sqlKeyword;

    /**
     * Creates a JoinType with the specified SQL keyword.
     * 
     * @param sqlKeyword the SQL keyword for this join type
     */
    JoinType(String sqlKeyword) {
        this.sqlKeyword = sqlKeyword;
    }

    /**
     * Gets the SQL keyword for this join type.
     * 
     * @return the SQL keyword (e.g., "INNER JOIN", "LEFT JOIN", "RIGHT JOIN")
     */
    public String getSqlKeyword() {
        return sqlKeyword;
    }
}
