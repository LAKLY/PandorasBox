package com.pandorasbox.mod.common.quest.tasks;

import com.pandorasbox.mod.common.quest.QuestTask;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public class BlockBreakTask extends QuestTask {

    private String blockType; // например "minecraft:oak_log"

    public void setBlockType(String blockType) { this.blockType = blockType; }
    public String getBlockType() { return blockType; }

    @Override
    public void onEvent(Event event) {
        if (isCompleted()) return;
        if (event instanceof BlockEvent.BreakEvent breakEvent) {
            ResourceLocation blockId = BuiltInRegistries.BLOCK
                    .getKey(breakEvent.getState().getBlock());
            if (blockId != null && blockId.toString().equals(blockType)) {
                progress++;
            }
        }
    }
}
