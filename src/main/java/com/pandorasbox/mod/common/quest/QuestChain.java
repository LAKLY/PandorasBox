package com.pandorasbox.mod.common.quest;

import java.util.ArrayList;
import java.util.List;

public class QuestChain {

    private final String chainId;
    private final List<Quest> quests = new ArrayList<>();
    private int currentIndex = 0;

    public QuestChain(String chainId) {
        this.chainId = chainId;
    }

    public void addQuest(Quest quest) { quests.add(quest); }

    public String getChainId() { return chainId; }
    public List<Quest> getQuests() { return quests; }

    public Quest getCurrentQuest() {
        if (currentIndex >= quests.size()) return null;
        return quests.get(currentIndex);
    }

    public void advanceChain() { currentIndex++; }

    public boolean isChainComplete() { return currentIndex >= quests.size(); }
}
