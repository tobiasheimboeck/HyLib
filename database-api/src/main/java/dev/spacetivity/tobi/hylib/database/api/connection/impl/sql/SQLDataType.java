package dev.spacetivity.tobi.hylib.database.api.connection.impl.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration of SQL data types and constraints for MariaDB/MySQL.
 * 
 * <p>This enum provides type-safe constants for SQL data types and constraints used
 * when defining table schemas. Each constant contains the SQL text representation
 * that will be used in CREATE TABLE statements.
 * 
 * <h3>Usage Example</h3>
 * 
 * <pre>{@code
 * SQLColumn idCol = SQLColumn.fromPrimary("id", SQLDataType.INTEGER);
 * SQLColumn nameCol = SQLColumn.from("name", SQLDataType.VARCHAR);
 * SQLColumn emailCol = SQLColumn.fromNullable("email", SQLDataType.VARCHAR);
 * }</pre>
 * 
 * <h3>Data Types</h3>
 * 
 * <p>The enum includes common SQL data types:
 * <ul>
 *   <li><strong>Text Types:</strong> VARCHAR, CHAR, TEXT, UUID</li>
 *   <li><strong>Numeric Types:</strong> INTEGER, BIGINT, SMALLINT, DECIMAL, NUMERIC, REAL, FLOAT, DOUBLE</li>
 *   <li><strong>Date/Time Types:</strong> DATE, TIME, TIMESTAMP</li>
 *   <li><strong>Boolean Type:</strong> BOOLEAN (stored as TINYINT)</li>
 * </ul>
 * 
 * <h3>Constraints</h3>
 * 
 * <p>The enum also includes constraint keywords:
 * <ul>
 *   <li>{@link #PRIMARY_KEY} - Marks a column as primary key</li>
 *   <li>{@link #NOT_NULL} - Marks a column as NOT NULL</li>
 *   <li>{@link #AUTO_INCREMENT} - Marks a column as AUTO_INCREMENT</li>
 * </ul>
 * 
 * <h3>UUID Storage</h3>
 * 
 * <p>UUIDs are stored as {@code BINARY(16)} (16 bytes) for efficient storage and indexing.
 * Use {@link dev.spacetivity.tobi.database.api.connection.impl.sql.UuidUtils} for
 * converting between UUID and byte arrays.
 * 
 * @see SQLColumn
 * @see TableDefinition
 * @see dev.spacetivity.tobi.database.api.connection.impl.sql.UuidUtils
 * @since 1.0
 */
@Getter
@AllArgsConstructor
public enum SQLDataType {

    /**
     * Variable-length string type, up to 255 characters.
     * SQL: {@code VARCHAR(255)}
     */
    VARCHAR("VARCHAR(255)"),
    
    /**
     * Fixed-length string type, 1 character.
     * SQL: {@code CHAR(1)}
     */
    CHAR("CHAR(1)"),
    
    /**
     * Large text type for longer strings.
     * SQL: {@code TEXT}
     */
    TEXT("TEXT"),
    
    /**
     * UUID stored as binary data (16 bytes).
     * SQL: {@code BINARY(16)}
     * 
     * @see dev.spacetivity.tobi.database.api.connection.impl.sql.UuidUtils
     */
    UUID("BINARY(16)"),

    /**
     * 32-bit integer type.
     * SQL: {@code INT}
     */
    INTEGER("INT"),
    
    /**
     * 64-bit integer type.
     * SQL: {@code BIGINT}
     */
    BIGINT("BIGINT"),
    
    /**
     * 16-bit integer type.
     * SQL: {@code SMALLINT}
     */
    SMALLINT("SMALLINT"),
    
    /**
     * Decimal number with fixed precision.
     * SQL: {@code DECIMAL}
     */
    DECIMAL("DECIMAL"),
    
    /**
     * Numeric type (alias for DECIMAL).
     * SQL: {@code NUMERIC}
     */
    NUMERIC("NUMERIC"),
    
    /**
     * Single-precision floating point.
     * SQL: {@code REAL}
     */
    REAL("REAL"),
    
    /**
     * Single-precision floating point.
     * SQL: {@code FLOAT}
     */
    FLOAT("FLOAT"),
    
    /**
     * Double-precision floating point.
     * SQL: {@code DOUBLE}
     */
    DOUBLE("DOUBLE"),

    /**
     * Date type (year, month, day).
     * SQL: {@code DATE}
     */
    DATE("DATE"),
    
    /**
     * Time type (hour, minute, second).
     * SQL: {@code TIME}
     */
    TIME("TIME"),
    
    /**
     * Timestamp type (date and time).
     * SQL: {@code TIMESTAMP}
     */
    TIMESTAMP("TIMESTAMP"),

    /**
     * Boolean type (stored as TINYINT: 0 = false, 1 = true).
     * SQL: {@code TINYINT}
     */
    BOOLEAN("TINYINT"),

    /**
     * Primary key constraint.
     * SQL: {@code PRIMARY KEY}
     */
    PRIMARY_KEY("PRIMARY KEY"),
    
    /**
     * NOT NULL constraint.
     * SQL: {@code NOT NULL}
     */
    NOT_NULL("NOT NULL"),
    
    /**
     * AUTO_INCREMENT constraint.
     * SQL: {@code AUTO_INCREMENT}
     */
    AUTO_INCREMENT("AUTO_INCREMENT");

    /**
     * The SQL text representation of this data type or constraint.
     */
    private final String queryText;

}
