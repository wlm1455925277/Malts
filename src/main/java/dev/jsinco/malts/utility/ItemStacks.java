package dev.jsinco.malts.utility;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import dev.jsinco.malts.configuration.ConfigManager;
import dev.jsinco.malts.configuration.files.GuiConfig;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ItemStacks {

    private static final UUID STATIC_UUID = UUID.fromString("ffd467ff-d884-4ace-8732-72e0d8e476f2");

    public static ItemStack borderItem() {
        GuiConfig.BorderItem cfg = ConfigManager.get(GuiConfig.class).borderItem();
        return builder().displayName(cfg.name()).material(cfg.material()).lore(cfg.lore()).build();
    }


    public static ItemStackBuilder builder() {
        return new ItemStackBuilder();
    }


    public static class ItemStackBuilder {
        @FunctionalInterface
        public interface EditMeta {
            void editMeta(ItemMeta meta);
        }

        private Material material = Material.BARRIER;
        private int amount = 1;
        private TextColor colorIfAbsentDisplayName = NamedTextColor.AQUA;
        private TextColor colorIfAbsentLore = NamedTextColor.WHITE;
        private String displayName;
        private List<String> lore;
        private Map<Enchantment, Integer> enchantments;
        private ItemFlag[] itemFlags;
        private PlayerProfile playerProfile;
        private EditMeta editMeta;
        private String headOwner;
        private Couple<String, Object>[] replacements;

        public ItemStackBuilder material(Material material) {
            this.material = material;
            return this;
        }

        public ItemStackBuilder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public ItemStackBuilder colorIfAbsentDisplayName(TextColor color) {
            this.colorIfAbsentDisplayName = color;
            return this;
        }

        public ItemStackBuilder colorIfAbsentLore(TextColor color) {
            this.colorIfAbsentLore = color;
            return this;
        }

        public ItemStackBuilder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public ItemStackBuilder lore(String... lore) {
            this.lore = Arrays.stream(lore).toList();
            return this;
        }

        public ItemStackBuilder lore(List<String> lore) {
            this.lore = lore;
            return this;
        }


        public ItemStackBuilder enchantments(Map<Enchantment, Integer> enchantments) {
            this.enchantments = enchantments;
            return this;
        }

        public ItemStackBuilder glint(boolean glint) {
            if (glint) {
                this.enchantments = Map.of(Enchantment.LURE, 1);
                if (this.itemFlags != null) {
                    this.itemFlags = Arrays.copyOf(this.itemFlags, this.itemFlags.length + 1);
                    this.itemFlags[this.itemFlags.length - 1] = ItemFlag.HIDE_ENCHANTS;
                } else {
                    this.itemFlags = new ItemFlag[]{ItemFlag.HIDE_ENCHANTS};
                }
            } else {
                this.enchantments = null;
            }
            return this;
        }

        public ItemStackBuilder itemFlags(ItemFlag... itemFlags) {
            this.itemFlags = itemFlags;
            return this;
        }

        public ItemStackBuilder base64Head(String base64Head) {
            this.playerProfile = Bukkit.createProfile(STATIC_UUID);
            this.playerProfile.getProperties().add(new ProfileProperty("textures", base64Head));
            return this;
        }

        public ItemStackBuilder playerProfile(PlayerProfile playerProfile) {
            this.playerProfile = playerProfile;
            return this;
        }

        public ItemStackBuilder headOwner(String headOwner) {
            this.headOwner = headOwner;
            return this;
        }

        public ItemStackBuilder editMeta(EditMeta editMeta) {
            this.editMeta = editMeta;
            return this;
        }

        @SuppressWarnings("unchecked")
        public ItemStackBuilder  stringReplacements(Couple<String, Object>... replacements) {
            this.replacements = replacements;
            return this;
        }

        public ItemStack build() {
            ItemStack itemStack = ItemStack.of(material, amount);
            ItemMeta meta = itemStack.getItemMeta();
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);

            if (replacements != null) {
                displayName = Util.replace(displayName, replacements);
                lore = Util.replaceAll(lore, replacements);
                headOwner = Util.replace(headOwner, replacements);
            }

            if (displayName != null) {
                Component c = Text.mmNoItalic(displayName).colorIfAbsent(colorIfAbsentDisplayName);
                meta.displayName(c);
            }

            if (lore != null) {
                List<Component> l = Text.mmlNoItalic(lore).stream().map(it -> it.colorIfAbsent(colorIfAbsentLore)).toList();
                meta.lore(l);
            }

            if (enchantments != null) {
                enchantments.forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
            }

            if (itemFlags != null) {
                meta.addItemFlags(itemFlags);
            }

            if (playerProfile != null && meta instanceof SkullMeta skullMeta) {
                skullMeta.setPlayerProfile(playerProfile);
            }

            if (headOwner != null && meta instanceof SkullMeta skullMeta) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(headOwner));
            }


            if (editMeta != null) {
                editMeta.editMeta(meta);
            }



            itemStack.setItemMeta(meta);
            return itemStack;
        }
    }

}
