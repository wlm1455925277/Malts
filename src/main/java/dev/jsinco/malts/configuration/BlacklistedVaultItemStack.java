package dev.jsinco.malts.configuration;

import eu.okaeri.configs.OkaeriConfig;
import org.bukkit.Material;

import java.util.List;

/**
 * Represents a blacklisted item stack in a vault.
 */
public class BlacklistedVaultItemStack extends OkaeriConfig {
    // TODO: Implement

    private Material typePattern; // regex supported
    private String namePattern; // regex supported
    private List<String> lorePattern; // regex supported
    private String customModel;
    private int customModelData;
    private List<String> enchantments; // format: ENCHANTMENT_NAME:LEVEL

}
