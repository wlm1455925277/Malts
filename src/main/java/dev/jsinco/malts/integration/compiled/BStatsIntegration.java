package dev.jsinco.malts.integration.compiled;

import dev.jsinco.malts.Malts;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.integration.Integration;
import dev.jsinco.malts.storage.DataSource;
import dev.jsinco.malts.utility.ClassUtil;
import dev.jsinco.malts.utility.Text;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;

public class BStatsIntegration implements Integration.Compiled {

    private static final int ID = 27527;

    private Metrics metrics;

    @Override
    public String name() {
        return "bStats";
    }

    @Override
    public void register() {
        if (isMockBukkit() || isUnitTest()) {
            Text.log("Skipping bStats integration registration (MockBukkit/Unit Test detected).");
            return;
        }

        this.metrics = new Metrics(Malts.getInstance(), ID);

        DataSource dataSource = DataSource.getInstance();
        Config config = ConfigManager.get(Config.class);

        dataSource.getTotalVaultCount().thenAccept(count ->
                metrics.addCustomChart(new SingleLineChart("vault_count", () -> count))
        );
        dataSource.getTotalWarehouseStockCount().thenAccept(count ->
                metrics.addCustomChart(new SingleLineChart("warehouse_stock", () -> count))
        );

        metrics.addCustomChart(new SimplePie("storage_driver", () -> config.storage().driver().toString()));
    }

    private static boolean isMockBukkit() {
        return ClassUtil.classExists("org.mockbukkit.MockBukkit");
    }

    private static boolean isUnitTest() {
        return System.getProperty("java.class.path").contains("test");
    }
}
