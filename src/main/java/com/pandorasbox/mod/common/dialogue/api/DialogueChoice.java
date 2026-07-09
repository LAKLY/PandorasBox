package com.pandorasbox.mod.common.dialogue.api;

import net.minecraft.network.chat.Component;

public class DialogueChoice {
    private final int id;
    private final Component text;
    private final String nextNodeId;
    private final String onSelectQuestTaskId; // новое поле

    public DialogueChoice(int id, Component text, String nextNodeId, String onSelectQuestTaskId) {
        this.id = id;
        this.text = text;
        this.nextNodeId = nextNodeId;
        this.onSelectQuestTaskId = onSelectQuestTaskId;
    }

    public int id() { return id; }
    public Component text() { return text; }
    public String nextNodeId() { return nextNodeId; }
    public String onSelectQuestTaskId() { return onSelectQuestTaskId; }
}