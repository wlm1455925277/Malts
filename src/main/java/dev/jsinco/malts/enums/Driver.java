package dev.jsinco.malts.enums;

import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.storage.sources.MySQLDataSource;
import lombok.Getter;
import dev.jsinco.malts.storage.DataSource;
import dev.jsinco.malts.storage.sources.SQLiteDataSource;

@Getter
public enum Driver {

    SQLITE((config) -> new SQLiteDataSource(config), SQLiteDataSource.class, "SQLite"),
    MYSQL((config) -> new MySQLDataSource(config), MySQLDataSource.class, "MySQL");

    private final DriverSupplier supplier;
    private final Class<? extends DataSource> identifyingClass;
    private final String asString;

    Driver(DriverSupplier supplier, Class<? extends DataSource> identifyingClass, String asString) {
        this.supplier = supplier;
        this.identifyingClass = identifyingClass;
        this.asString = asString;
    }

    @Override
    public String toString() {
        return asString;
    }

    public <T extends DataSource> T supply(Config.Storage config) {
        return (T) supplier.supply(config);
    }

    public interface DriverSupplier {
        DataSource supply(Config.Storage config);
    }
}
