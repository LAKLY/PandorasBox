package com.pandorasbox.mod.common.quest;

import net.neoforged.bus.api.Event;

public abstract class QuestTask {

    protected String taskId;
    protected String description;
    protected int progress = 0;
    protected int required = 1;

    public abstract void onEvent(Event event);

    public boolean isCompleted() {
        return progress >= required;
    }

    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getProgress() { return progress; }
    public int getRequired() { return required; }
    public void setRequired(int required) { this.required = required; }
}
