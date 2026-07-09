package com.pandorasbox.mod.common.quest.tasks;

import com.pandorasbox.mod.common.quest.QuestTask;
import net.minecraft.core.BlockPos;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class LocationTask extends QuestTask {

    private BlockPos location;
    private int radius = 10;

    public void setLocation(BlockPos location) { this.location = location; }
    public void setRadius(int radius) { this.radius = radius; }

    @Override
    public void onEvent(Event event) {
        if (isCompleted() || location == null) return;
        if (event instanceof PlayerTickEvent.Post tickEvent) {
            double distSq = tickEvent.getEntity().blockPosition().distSqr(location);
            if (distSq <= (double) radius * radius) {
                progress = required;
            }
        }
    }
}
