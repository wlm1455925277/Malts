package dev.jsinco.malts.obj;

import com.google.common.collect.ImmutableList;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.files.GuiConfig;
import dev.jsinco.malts.gui.EditVaultGui;
import dev.jsinco.malts.gui.item.AbstractGuiItem;
import dev.jsinco.malts.storage.DataSource;
import dev.jsinco.malts.utility.Couple;
import dev.jsinco.malts.utility.ItemStacks;
import dev.jsinco.malts.utility.Util;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static dev.jsinco.malts.obj.Vault.BYPASS_OPEN_VAULT_PERM;
import static dev.jsinco.malts.obj.Vault.LIST_UUID_TYPE_TOKEN;

/**
 * A snapshot of a vault's data, used for displaying in GUIs without loading the full vault.
 */
public class SnapshotVault implements AbstractGuiItem {

    private static final GuiConfig cfg = ConfigManager.get(GuiConfig.class);

    @Getter
    private final UUID owner;
    @Getter
    private final int id;
    @Getter
    private final String customName;
    @Getter
    private final Material icon;
    @Getter
    private final ImmutableList<UUID> trustedPlayers;


    @Nullable
    private Vault lazyVault;

    public SnapshotVault(UUID owner, int id, String customName, Material icon) {
        this(owner, id, customName, icon, ImmutableList.of());
    }

    public SnapshotVault(UUID owner, int id, String customName, Material icon, ImmutableList<UUID> trustedPlayers) {
        this.owner = owner;
        this.id = id;
        this.customName = customName != null && !customName.isEmpty() ? customName : "Vault #" + id;
        this.icon = icon != null && icon.isItem() ? icon : Material.CHEST;
        this.trustedPlayers = trustedPlayers;
    }


    public SnapshotVault(UUID owner, int id, String customName, Material icon, String trustedPlayers) {
        this.owner = owner;
        this.id = id;
        this.customName = customName != null && !customName.isEmpty() ? customName : "Vault #" + id;
        this.icon = icon != null && icon.isItem() ? icon : Material.CHEST;

        List<UUID> json = Util.GSON.fromJson(trustedPlayers, LIST_UUID_TYPE_TOKEN);
        this.trustedPlayers = json != null ? ImmutableList.copyOf(json) : ImmutableList.of();
    }


    public CompletableFuture<Vault> toVault() {
        DataSource dataSource = DataSource.getInstance();
        return dataSource.getVault(owner, id);
    }

    public void toVaultWithEconomy(Player player, Consumer<Vault> consumer) {
        DataSource dataSource = DataSource.getInstance();
        dataSource.getVaultWithEconomy(player, id, consumer);
    }

    public boolean isTrusted(UUID uuid) {
        return trustedPlayers.contains(uuid);
    }

    public boolean canAccess(Player player) {
        return player.getUniqueId() == this.owner || this.trustedPlayers.contains(player.getUniqueId()) || player.hasPermission(BYPASS_OPEN_VAULT_PERM);
    }

    @SuppressWarnings("unchecked")
    @Override
    public ItemStack itemStack() {
        return ItemStacks.builder()
                .stringReplacements(
                        Couple.of("{vaultName}", customName),
                        Couple.of("{id}", id)
                )
                .displayName(cfg.yourVaultsGui().vaultItem().name())
                .material(icon)
                .lore(cfg.yourVaultsGui().vaultItem().lore())
                .build();
    }

    public CompletableFuture<Vault> lazyVault() {
        if (this.lazyVault == null) {
            return toVault().thenApply(vault -> {
                this.lazyVault = vault;
                return vault;
            });
        }
        return CompletableFuture.completedFuture(lazyVault);
    }

    public void lazyVaultWithEconomy(Player player, Consumer<Vault> consumer) {
        toVaultWithEconomy(player, consumer);
    }

    @Override
    public void onClick(InventoryClickEvent event, ItemStack clickedItem) {
        if (!Util.hasPersistentKey(clickedItem, key())) {
            return;
        }

        final boolean isLeftClick = event.isLeftClick();
        Player player = (Player) event.getWhoClicked();

        lazyVaultWithEconomy(player, vault -> {
            if (isLeftClick) {
                vault.open(player);
            } else {
                MaltsPlayer maltsPlayer = DataSource.getInstance().cachedObject(player.getUniqueId(), MaltsPlayer.class);
                EditVaultGui editVaultGui = new EditVaultGui(vault, maltsPlayer, player);
                editVaultGui.open(player);
            }
        });
    }
}
