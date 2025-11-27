package dev.jsinco.malts.commands.subcommands;

import dev.jsinco.malts.Malts;
import dev.jsinco.malts.commands.interfaces.SubCommand;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.files.Config;
import dev.jsinco.malts.obj.CachedObject;
import dev.jsinco.malts.obj.Vault;
import dev.jsinco.malts.storage.DataSource;
import dev.jsinco.malts.utility.Couple;
import dev.jsinco.malts.utility.Util;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class EditVaultCommand implements SubCommand {

    // Cache this for tab completion
    private static final List<String> ICON_MATERIAL_NAMES = Arrays.stream(Material.values())
            .filter(Material::isItem)
            .map(material -> material.name().toLowerCase())
            .toList();

    @Override
    public boolean execute(Malts plugin, CommandSender sender, String label, List<String> args) {
        if (args.size() < 2) return false;

        Player player = (Player) sender;
        DataSource dataSource = DataSource.getInstance();
        int vaultId = Util.getInteger(args.getFirst(), -1);
        ArgOption option = Util.getEnum(args.get(1), ArgOption.class);
        List<String> newArgs = args.subList(1, args.size());

        if (vaultId <= 0 || option == null) return false;

        dataSource.getVaultWithEconomy(player, vaultId, vault -> {
            boolean result = option.getExecutor().handle(dataSource, player, vault, newArgs);
            if (!result) {
                lng.entry(l -> l.command().base().invalidUsage(), sender);
            }
        });
        return true;
    }

    @Override
    public List<String> tabComplete(Malts plugin, CommandSender sender, String label, List<String> args) {
        return switch (args.size()) {
            case 1 -> Stream.of(ArgOption.values()).map(it -> it.toString().toLowerCase()).toList();
            case 2 -> {
                if (!(sender instanceof Player player)) yield List.of();
                ArgOption option = Util.getEnum(args.getFirst(), ArgOption.class);
                if (option == null) yield List.of();

                yield option.getTabCompleter().handle(player);
            }
            default -> List.of();
        };
    }

    @Override
    public String permission() {
        return "malts.command.editvault";
    }

    @Override
    public boolean playerOnly() {
        return true;
    }

    @Override
    public String name() {
        return "editvault";
    }

    @Getter
    @AllArgsConstructor
    private enum ArgOption {
        NAME((dataSource, sender, vault, args) -> {
            if (args.isEmpty()) return false;

            String newName = String.join(" ", args);
            int maxLength = ConfigManager.get(Config.class).vaults().maxNameCharacters();

            if (vault.setCustomName(newName)) {
                lng.entry(l -> l.vaults().nameChanged(), sender, Couple.of("{vaultName}", newName));
                dataSource.saveVault(vault);
            } else {
                lng.entry(l -> l.vaults().vaultNameTooLong(), sender, Couple.of("{maxLength}", maxLength));
            }
            return true;
        }, (sender) -> List.of()),
        ICON(((dataSource, sender, vault, args) -> {
            if (args.isEmpty()) return false;

            Material material = Util.getEnum(args.getFirst(), Material.class);
            if (material == null || !material.isItem()) return false;

            if (!vault.setIcon(material)) return false;

            dataSource.saveVault(vault);
            lng.entry(l -> l.vaults().iconChanged(), sender, Couple.of("{material}", Util.formatEnumerator(material)));
            return true;
        }), (sender) -> ICON_MATERIAL_NAMES),
        TRUSTED((dataSource, sender, vault, args) -> {
            if (args.isEmpty()) return false;

            String name = args.getFirst();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(name);
            UUID playerUUID = offlinePlayer.getUniqueId();
            int trustListCap = ConfigManager.get(Config.class).vaults().trustCap();
            if (!offlinePlayer.hasPlayedBefore()) {
                lng.entry(l -> l.vaults().playerNeverOnServer(), sender, Couple.of("{name}", name));
                return true;
            }

            if (vault.isTrusted(playerUUID)) {
                if (vault.removeTrusted(playerUUID)) {
                    lng.entry(l -> l.vaults().playerUntrusted(), sender, Couple.of("{name}", name));
                } else {
                    lng.entry(l -> l.vaults().playerNotTrusted(), sender, Couple.of("{name}", name));
                }
            } else if (vault.addTrusted(playerUUID)){
                lng.entry(l -> l.vaults().playerTrusted(), sender, Couple.of("{name}", name));
            } else {
                lng.entry(l -> l.vaults().trustListMaxed(), sender, Couple.of("{trustedListSize}", trustListCap));
            }
            return true;
        }, (sender) ->{
            UUID playerUUID = sender.getUniqueId();
            DataSource dataSource = DataSource.getInstance();
            CachedTrustedNames cachedVaultNames = dataSource.cachedObject(playerUUID, CachedTrustedNames.class);

            if (cachedVaultNames == null) {
                CompletableFuture<CachedTrustedNames> future = dataSource.getVaultNames(playerUUID)
                        .thenApply(vaultNames -> new CachedTrustedNames(playerUUID, vaultNames));
                // 7s expire time
                dataSource.cacheObject(future, 7000).thenAccept(cached -> {
                    // No action needed here for tab completion
                });
                return List.of("...");
            }
            return cachedVaultNames.getTrustedNames();
        });


        private final Handler executor;
        private final TabCompleter tabCompleter;

        private interface Handler {
            boolean handle(DataSource dataSource, Player sender, Vault vault, List<String> args);
        }

        private interface TabCompleter {
            List<String> handle(Player sender);
        }
    }

    private static class CachedTrustedNames implements CachedObject {

        private final UUID owner;
        @Getter
        private final List<String> trustedNames;

        @Getter
        @Setter
        private Long expire;

        public CachedTrustedNames(UUID owner, List<String> trustedNames) {
            this.owner = owner;
            this.trustedNames = trustedNames;
        }


        @Override
        public @NotNull UUID getUuid() {
            return owner;
        }


        @Override
        public @NotNull CompletableFuture<Void> save(DataSource dataSource) {
            return CompletableFuture.completedFuture(null);
        }
    }
}
