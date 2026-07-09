package com.pandorasbox.mod.common.quest;

import java.util.HashMap;
import java.util.Map;

public class QuestRegistry {

    private final Map<String, Quest> quests = new HashMap<>();
    private final Map<String, QuestChain> chains = new HashMap<>();

    public void register(String questId, Quest quest) {
        quests.put(questId, quest);
    }

    public void registerChain(String chainId, QuestChain chain) {
        chains.put(chainId, chain);
    }

    public Quest getQuest(String questId) {
        return quests.get(questId);
    }

    public QuestChain getChain(String chainId) {
        return chains.get(chainId);
    }

    public Map<String, Quest> getAllQuests() {
        return quests;
    }
}
