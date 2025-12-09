package dev.jsinco.malts.gui.item;

import dev.jsinco.malts.utility.ItemStacks;
import dev.jsinco.malts.utility.Util;
import lombok.Builder;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

@Builder
public class UncontainedGuiItem extends AbstractGuiItem {

    @FunctionalInterface
    public interface UncontainedGuiItemAction {
        void onClick(InventoryClickEvent event, UncontainedGuiItem self, boolean isClicked);
    }

    private final GuiItemStackBuilder itemStack;
    private final UncontainedGuiItemAction action;
    private final GuiIndex index;

    public UncontainedGuiItem(GuiItemStackBuilder itemStack, UncontainedGuiItemAction action, @Nullable GuiIndex index) {
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
        boolean isClicked = Util.hasPersistentKey(clickedItem, key());
        action.onClick(event, this, isClicked);
    }

    @Override
    public Integer index() {
        if (index == null) {
            return null;
        }
        return index.getIndex();
    }
}
