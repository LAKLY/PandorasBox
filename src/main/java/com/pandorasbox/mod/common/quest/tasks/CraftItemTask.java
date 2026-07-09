package com.pandorasbox.mod.common.quest.tasks;

import com.pandorasbox.mod.common.quest.QuestTask;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class CraftItemTask extends QuestTask {

    private String itemType;

    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getItemType() { return itemType; }

    @Override
    public void onEvent(Event event) {
        if (isCompleted() || itemType == null) return;
        if (event instanceof PlayerEvent.ItemCraftedEvent craftedEvent) {
            ResourceLocation craftedId = BuiltInRegistries.ITEM.getKey(craftedEvent.getCrafting().getItem());
            if (craftedId != null && craftedId.toString().equals(itemType)) {
                // Увеличиваем прогресс на количество скрафченных предметов (размер стека)
                progress += craftedEvent.getCrafting().getCount();
            }
        }
    }
}