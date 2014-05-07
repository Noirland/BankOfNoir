package nz.co.noirland.bankofnoir.database;

import nz.co.noirland.bankofnoir.config.PluginConfig;

public enum DatabaseTables {
    PLAYERS("players"),
    SCHEMA("schema");

    private final String name;

    private DatabaseTables(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return PluginConfig.inst().getPrefix() + name;
    }
}
