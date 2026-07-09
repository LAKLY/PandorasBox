package com.pandorasbox.mod.common.dialogue.server;

import java.util.HashMap;
import java.util.Map;

public class DialogueState {
    private final String treeId;
    private String currentNodeId;
    private final Map<String, Boolean> visitedFlags = new HashMap<>();

    public DialogueState(String treeId, String currentNodeId) {
        this.treeId = treeId;
        this.currentNodeId = currentNodeId;
    }

    public String getTreeId() { return treeId; }
    public String getCurrentNodeId() { return currentNodeId; }
    public void setCurrentNodeId(String nodeId) { this.currentNodeId = nodeId; }
    public Map<String, Boolean> getVisitedFlags() { return visitedFlags; }
}