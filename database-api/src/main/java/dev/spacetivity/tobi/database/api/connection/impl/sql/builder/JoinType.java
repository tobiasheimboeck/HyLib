package dev.spacetivity.tobi.database.api.connection.impl.sql.builder;

/**
 * Enum representing the type of SQL JOIN operation.
 */
public enum JoinType {
    INNER("INNER JOIN"),
    LEFT("LEFT JOIN"),
    RIGHT("RIGHT JOIN");

    private final String sqlKeyword;

    JoinType(String sqlKeyword) {
        this.sqlKeyword = sqlKeyword;
    }

    public String getSqlKeyword() {
        return sqlKeyword;
    }
}
