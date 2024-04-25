package net.neptunsworld.elytra.database.api.connection.impl.sql;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SQLColumn {

    private String key;
    private String value;

    public static SQLColumn from(String key, String value) {
        return new SQLColumn(key, value);
    }

    public static SQLColumn fromPrimary(String key, SQLDataType value, boolean notNull) {
        String valueString = value.getQueryText();
        if (notNull) valueString += " " + SQLDataType.NOT_NULL.getQueryText();
        valueString += " " + SQLDataType.PRIMARY_KEY.getQueryText();
        return new SQLColumn(key, valueString);
    }

    public static SQLColumn fromPrimary(String key, SQLDataType value) {
        return fromPrimary(key, value, true);
    }

    public static SQLColumn fromPrimaryNullable(String key, SQLDataType value) {
        return fromPrimary(key, value, false);
    }

    public static SQLColumn from(String key, SQLDataType dataType) {
        return new SQLColumn(key, dataType.getQueryText() + " " + SQLDataType.NOT_NULL.getQueryText());
    }

    public static SQLColumn fromNullable(String key, SQLDataType dataType) {
        return new SQLColumn(key, dataType.getQueryText());
    }
}
