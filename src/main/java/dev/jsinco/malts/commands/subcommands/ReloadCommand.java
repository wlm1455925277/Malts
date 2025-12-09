package dev.jsinco.malts.commands.subcommands;

import dev.jsinco.malts.Malts;
import dev.jsinco.malts.commands.interfaces.SubCommand;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.OkaeriFile;
import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.enums.Driver;
import dev.jsinco.malts.integration.compiled.UpdateCheckIntegration;
import dev.jsinco.malts.registry.Registry;
import dev.jsinco.malts.storage.DataSource;
import dev.jsinco.malts.utility.Couple;
import dev.jsinco.malts.utility.Text;
import org.bukkit.command.CommandSender;

import java.util.Comparator;
import java.util.List;

public class ReloadCommand implements SubCommand {
    @Override
    public boolean execute(Malts plugin, CommandSender sender, String label, List<String> args) {
        boolean success = true;
        try {
            ConfigManager.createTranslationConfigs();
            Registry.CONFIGS.values()
                    .stream()
                    .sorted(Comparator.comparing(OkaeriFile::isDynamicFileName))
                    .forEach(OkaeriFile::reload);

            Config.Storage storage = ConfigManager.get(Config.class).storage();
            Driver setDriver = storage.driver();
            DataSource dataSource = DataSource.getInstance();
            if (setDriver.getIdentifyingClass() != dataSource.getClass()) {
                dataSource.close().whenComplete((unused, throwable) -> {
                    DataSource.createInstance(storage);
                    lng.entry(l -> l.command().reload().newDatabaseDriverSet(), sender, Couple.of("{driver}", setDriver.toString()));
                });
            }

            Malts.setInvalidatedCachedGuiItems(true);
        } catch (Throwable e) {
            Text.error("An exception/error occurred while reloading Malts configuration", e);
            success = false;
        }

        final boolean finalSuccess = success;
        lng.entry(l -> finalSuccess ? l.command().reload().success() : l.command().reload().failed(), sender);
        if (finalSuccess) {
            UpdateCheckIntegration updateCheck = Registry.INTEGRATIONS.get(UpdateCheckIntegration.class);
            if (updateCheck != null && updateCheck.isUpdateAvailable()) {
                updateCheck.sendUpdateMessage(sender);
            }
        }
        return true;
    }

    @Override
    public List<String> tabComplete(Malts plugin, CommandSender sender, String label, List<String> args) {
        return List.of();
    }

    @Override
    public String permission() {
        return "malts.command.reload";
    }

    @Override
    public boolean playerOnly() {
        return false;
    }

    @Override
    public String name() {
        return "reload";
    }
}
