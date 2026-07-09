package com.pandorasbox.mod.common.quest.tasks;

import com.pandorasbox.mod.common.quest.QuestTask;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

public class ReachLevelTask extends QuestTask {

    private int targetLevel;

    public void setTargetLevel(int targetLevel) { this.targetLevel = targetLevel; }
    public int getTargetLevel() { return targetLevel; }

    @Override
    public void onEvent(Event event) {
        if (isCompleted()) return;
        if (event instanceof PlayerXpEvent.XpChange xpEvent) {
            int currentLevel = xpEvent.getEntity().experienceLevel;
            if (currentLevel >= targetLevel) {
                progress = required;
            }
        }
    }
}