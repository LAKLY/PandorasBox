package com.pandorasbox.mod.common.quest.tasks;

import com.pandorasbox.mod.common.quest.QuestTask;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public class InteractBlockTask extends QuestTask {

    private String blockType; // например "minecraft:chest"

    public void setBlockType(String blockType) { this.blockType = blockType; }
    public String getBlockType() { return blockType; }

    @Override
    public void onEvent(Event event) {
        if (isCompleted() || blockType == null) return;
        if (event instanceof PlayerInteractEvent.RightClickBlock clickEvent) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(clickEvent.getLevel().getBlockState(clickEvent.getPos()).getBlock());
            if (blockId != null && blockId.toString().equals(blockType)) {
                progress++;
            }
        }
    }
}