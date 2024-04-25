package net.neptunsworld.elytra.database.api.connection.impl.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QueryStatement {

    // Data Retrieval
    SELECT_ALL("SELECT * FROM {0}"),
    SELECT_ONE("SELECT * FROM {0} WHERE {1}=?"),
    SELECT_TWO("SELECT * FROM {0} WHERE {1}=? AND {2}=?"),

    // Data Modification
    UPDATE("UPDATE {0} SET {1}=? WHERE {2}=?"),
    UPDATE_TWO("UPDATE {0} SET {1}=? WHERE {2}=? AND {3}=?"),
    INSERT("INSERT INTO {0} ({1}) VALUES ({2})"),
    DELETE("DELETE FROM {0} WHERE {1}=?"),
    DELETE_TWO("DELETE FROM {0} WHERE {1}=? AND {2}=?"),

    // Counting Records
    COUNT("SELECT COUNT(*) FROM {0}"),
    COUNT_WHERE("SELECT COUNT(*) FROM {0} WHERE {1}=?"),
    COUNT_TWO_WHERE("SELECT COUNT(*) FROM {0} WHERE {1}=? AND {2}=?"),

    // Checking Existence
    EXISTS("SELECT EXISTS (SELECT 1 FROM {0} WHERE {1}=?)"),
    EXISTS_TWO("SELECT EXISTS (SELECT 1 FROM {0} WHERE {1}=? AND {2}=?)"),

    // Table Operations
    CREATE_TABLE("CREATE TABLE IF NOT EXISTS {0} ({1})"),
    DROP_TABLE("DROP TABLE IF EXISTS {0}");

    private final String statement;

}
