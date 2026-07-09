package com.pandorasbox.mod.common.quest;

import com.pandorasbox.mod.common.reward.Reward;

import java.util.ArrayList;
import java.util.List;

public class Quest {

    private final String questId;
    private final String title;
    private final String description;
    private final List<QuestTask> tasks = new ArrayList<>();
    private final List<Reward> rewards = new ArrayList<>();

    private boolean repeatable = false;
    private String onCompletionDialogueId;
    private String nextQuestId;
    private String startDialogueId; // новое поле

    public Quest(String questId, String title, String description) {
        this.questId = questId;
        this.title = title;
        this.description = description;
    }

    public void addTask(QuestTask task) { tasks.add(task); }
    public void addReward(Reward reward) { rewards.add(reward); }

    public String getQuestId() { return questId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<QuestTask> getTasks() { return tasks; }
    public List<Reward> getRewards() { return rewards; }

    public boolean isRepeatable() { return repeatable; }
    public void setRepeatable(boolean repeatable) { this.repeatable = repeatable; }

    public String getOnCompletionDialogueId() { return onCompletionDialogueId; }
    public void setOnCompletionDialogueId(String id) { this.onCompletionDialogueId = id; }

    public String getNextQuestId() { return nextQuestId; }
    public void setNextQuestId(String nextQuestId) { this.nextQuestId = nextQuestId; }

    public String getStartDialogueId() { return startDialogueId; }
    public void setStartDialogueId(String startDialogueId) { this.startDialogueId = startDialogueId; }

    public boolean isCompleted() {
        for (QuestTask task : tasks) {
            if (!task.isCompleted()) return false;
        }
        return true;
    }

    public int getProgress() {
        int sum = 0;
        for (QuestTask task : tasks) sum += task.getProgress();
        return sum;
    }

    public int getTotalProgress() {
        int sum = 0;
        for (QuestTask task : tasks) sum += task.getRequired();
        return Math.max(sum, 1);
    }
}