package net.neptunsworld.elytra.database.api;

public class ElytraDatabaseProvider {

    private static DatabaseApi api = null;

    public static DatabaseApi getApi() {
        DatabaseApi api = ElytraDatabaseProvider.api;
        if (api == null) throw new IllegalStateException("Api instance is null");
        return api;
    }

    public static void register(DatabaseApi api) {
        ElytraDatabaseProvider.api = api;
    }

}
