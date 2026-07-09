package com.pandorasbox.mod.common.quest.tasks;

import com.pandorasbox.mod.common.quest.QuestTask;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class UseItemTask extends QuestTask {

    private String itemType;

    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getItemType() { return itemType; }

    @Override
    public void onEvent(Event event) {
        if (isCompleted() || itemType == null) return;
        if (event instanceof PlayerInteractEvent.RightClickItem clickEvent) {
            ResourceLocation usedId = BuiltInRegistries.ITEM.getKey(clickEvent.getItemStack().getItem());
            if (usedId != null && usedId.toString().equals(itemType)) {
                progress++;
            }
        }
    }
}