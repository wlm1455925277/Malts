package dev.jsinco.malts.gui.item;

import dev.jsinco.malts.utility.Util;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public record ItemConfirmation(ItemStack itemStack) {

    private static final NamespacedKey KEY = Util.namespacedKey("confirmed");

    public boolean isConfirmed() {
        Boolean value = Util.getPersistentKey(itemStack, KEY, PersistentDataType.BOOLEAN);
        return value != null && value;
    }

    public void setConfirmation(boolean bool) {
        Util.setPersistentKey(itemStack, KEY, PersistentDataType.BOOLEAN, bool);
        Util.editMeta(itemStack, meta -> {
            if (bool) {
                meta.addEnchant(Enchantment.MENDING, 1, true);
            } else {
                meta.removeEnchant(Enchantment.MENDING);
            }
        });
    }
}