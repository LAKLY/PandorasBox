package com.pandorasbox.mod.common.dialogue;

import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.quest.Quest;
import com.pandorasbox.mod.common.quest.QuestManager;
import com.pandorasbox.mod.common.quest.QuestTask;
import com.pandorasbox.mod.common.quest.tasks.DialogueChoiceTask;
import net.minecraft.world.entity.player.Player;

public class DialogueHandler {

    public static void markDialogueTaskCompleted(Player player, String taskId, int choiceId) {
        PandorasBoxMod.LOGGER.info("Marking dialogue task completed: taskId={}, choiceId={}", taskId, choiceId);
        for (Quest quest : QuestManager.getInstance().getActiveQuests(player)) {
            for (QuestTask task : quest.getTasks()) {
                if (taskId.equals(task.getTaskId()) && task instanceof DialogueChoiceTask choiceTask) {
                    choiceTask.onChoiceMade(choiceId);
                    PandorasBoxMod.LOGGER.info("Dialogue choice task completed for quest: {}", quest.getQuestId());
                }
            }
            if (quest.isCompleted()) {
                QuestManager.getInstance().completeQuest(player, quest.getQuestId());
            }
        }
    }
}