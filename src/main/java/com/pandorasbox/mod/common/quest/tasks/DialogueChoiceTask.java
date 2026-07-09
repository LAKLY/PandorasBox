package com.pandorasbox.mod.common.quest.tasks;

import com.pandorasbox.mod.common.quest.QuestTask;
import net.neoforged.bus.api.Event;

/**
 * Задача завершается не через игровое событие, а напрямую вызовом onChoiceMade()
 * из DialogueHandler, когда игрок делает выбор в диалоге.
 */
public class DialogueChoiceTask extends QuestTask {

    private int requiredChoiceId = -1; // -1 значит "любой выбор подходит"

    public void setRequiredChoiceId(int requiredChoiceId) { this.requiredChoiceId = requiredChoiceId; }

    public void onChoiceMade(int choiceId) {
        if (requiredChoiceId == -1 || choiceId == requiredChoiceId) {
            progress = required;
        }
    }

    @Override
    public void onEvent(Event event) {
        // Эта задача не реагирует на общие игровые события
    }
}
