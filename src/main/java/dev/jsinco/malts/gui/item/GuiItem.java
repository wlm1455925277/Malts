package dev.jsinco.malts.gui.item;

import dev.jsinco.malts.utility.ItemStacks;
import dev.jsinco.malts.utility.Util;
import lombok.Builder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Builder
public class GuiItem extends AbstractGuiItem {

    @FunctionalInterface
    public interface GuiItemAction {
        void onClick(InventoryClickEvent event);
    }

    private final GuiItemStackBuilder itemStack;
    private final GuiItemAction action;
    private final GuiIndex index;

    public GuiItem(GuiItemStackBuilder itemStack, GuiItemAction action, @Nullable GuiIndex index) {
        this.itemStack = itemStack;
        this.action = action;
        this.index = index;
    }

    @Override
    public ItemStack itemStack() {
        return itemStack.create(ItemStacks.builder()).build();
    }

    @Override
    public void onClick(InventoryClickEvent event, ItemStack clickedItem) {
        if (Util.hasPersistentKey(clickedItem, key())) {
            action.onClick(event);
        }
    }

    @Override
    public Integer index() {
        if (index == null) {
            return null;
        }
        return index.getIndex();
    }
}
