package com.pandorasbox.mod.common.quest;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerQuestProgress {

    private final UUID playerUUID;
    private final List<String> activeQuestIds = new ArrayList<>();
    private final List<String> completedQuestIds = new ArrayList<>();

    public PlayerQuestProgress(UUID playerUUID) {
        this.playerUUID = playerUUID;
    }

    public void startQuest(Quest quest) {
        if (!activeQuestIds.contains(quest.getQuestId())
                && !completedQuestIds.contains(quest.getQuestId())) {
            activeQuestIds.add(quest.getQuestId());
        }
    }

    public void completeQuest(String questId) {
        activeQuestIds.remove(questId);
        if (!completedQuestIds.contains(questId)) {
            completedQuestIds.add(questId);
        }
    }

    public boolean isActive(String questId) { return activeQuestIds.contains(questId); }
    public boolean isCompleted(String questId) { return completedQuestIds.contains(questId); }

    public List<String> getActiveQuestIds() { return activeQuestIds; }
    public List<String> getCompletedQuestIds() { return completedQuestIds; }
    public int getCompletedQuestsCount() { return completedQuestIds.size(); }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag active = new ListTag();
        for (String id : activeQuestIds) active.add(StringTag.valueOf(id));
        ListTag completed = new ListTag();
        for (String id : completedQuestIds) completed.add(StringTag.valueOf(id));
        tag.put("Active", active);
        tag.put("Completed", completed);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        activeQuestIds.clear();
        completedQuestIds.clear();
        for (Tag t : tag.getList("Active", 8)) activeQuestIds.add(t.getAsString());
        for (Tag t : tag.getList("Completed", 8)) completedQuestIds.add(t.getAsString());
    }
}
