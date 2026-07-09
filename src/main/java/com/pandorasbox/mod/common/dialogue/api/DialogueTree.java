package com.pandorasbox.mod.common.dialogue.api;

import java.util.Map;

public class DialogueTree {
    private final String treeId;
    private final String startNodeId;
    private final Map<String, DialogueNode> nodes;

    public DialogueTree(String treeId, String startNodeId, Map<String, DialogueNode> nodes) {
        this.treeId = treeId;
        this.startNodeId = startNodeId;
        this.nodes = nodes;
    }

    public String treeId() { return treeId; }
    public String startNodeId() { return startNodeId; }
    public Map<String, DialogueNode> nodes() { return nodes; }

    public DialogueNode getNode(String id) {
        return nodes.get(id);
    }
}