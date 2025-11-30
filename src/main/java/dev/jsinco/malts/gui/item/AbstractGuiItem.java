package dev.jsinco.malts.gui.item;

import dev.jsinco.malts.utility.Util;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

public interface AbstractGuiItem {

    // todo: should maybe be abstract class so this can be private
    ItemStack itemStack();

    void onClick(InventoryClickEvent event, ItemStack clickedItem);

    default NamespacedKey key() {
        String name = this.getClass().getSimpleName() + "_" + System.identityHashCode(this);
        return Util.namespacedKey(name);
    }

    @Nullable
    default Integer index() {
        return null;
    }

    default ItemStack guiItemStack() {
        return Util.setPersistentKey(itemStack(), key(), PersistentDataType.BOOLEAN, true);
    }
}
