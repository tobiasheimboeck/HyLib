package dev.spacetivity.tobi.database.api.connection.impl.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SQLDataType {

    // Text Types
    VARCHAR("VARCHAR(255)"),
    CHAR("CHAR(1)"),
    TEXT("TEXT"),
    UUID("BINARY(16)"), // UUID stored as BINARY(16) - 16 bytes

    // Numeric Types
    INTEGER("INT"),
    BIGINT("BIGINT"),
    SMALLINT("SMALLINT"),
    DECIMAL("DECIMAL"),
    NUMERIC("NUMERIC"),
    REAL("REAL"),
    FLOAT("FLOAT"),
    DOUBLE("DOUBLE"),

    // Date and Time Types
    DATE("DATE"),
    TIME("TIME"),
    TIMESTAMP("TIMESTAMP"),

    // Boolean Type
    BOOLEAN("TINYINT"),

    // Constraints
    PRIMARY_KEY("PRIMARY KEY"),
    NOT_NULL("NOT NULL"),
    AUTO_INCREMENT("AUTO_INCREMENT");

    private final String queryText;

}
