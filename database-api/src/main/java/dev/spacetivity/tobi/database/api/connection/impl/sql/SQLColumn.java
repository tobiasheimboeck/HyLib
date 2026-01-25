package dev.spacetivity.tobi.database.api.connection.impl.sql;

import lombok.Getter;

@Getter
public class SQLColumn {

    private Column column;
    private String value;

    public SQLColumn(Column column, String value) {
        this.column = column;
        this.value = value;
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
}
