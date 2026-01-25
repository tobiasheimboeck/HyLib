package dev.spacetivity.tobi.database.api;

public class DatabaseProvider {

    private static DatabaseApi api = null;

    public static DatabaseApi getApi() {
        DatabaseApi api = DatabaseProvider.api;
        if (api == null) throw new IllegalStateException("Api instance is null");
        return api;
    }

    public static void register(DatabaseApi api) {
        DatabaseProvider.api = api;
    }

}
