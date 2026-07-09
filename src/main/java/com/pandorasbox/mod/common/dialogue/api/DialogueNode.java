package com.pandorasbox.mod.common.dialogue.api;

import net.minecraft.network.chat.Component;
import java.util.List;

public class DialogueNode {
    private final String id;
    private final String speaker;
    private final Component text;
    private final int duration; // В секундах
    private final DialogueType type;
    private final List<DialogueChoice> choices;
    private final String nextNodeId;

    public DialogueNode(String id, String speaker, Component text, int duration, DialogueType type, List<DialogueChoice> choices, String nextNodeId) {
        this.id = id;
        this.speaker = speaker;
        this.text = text;
        this.duration = duration;
        this.type = type;
        this.choices = choices;
        this.nextNodeId = nextNodeId;
    }

    public String id() { return id; }
    public String speaker() { return speaker; }
    public Component text() { return text; }
    public int duration() { return duration; }
    public DialogueType type() { return type; }
    public List<DialogueChoice> choices() { return choices; }
    public String nextNodeId() { return nextNodeId; }

    public boolean hasChoices() {
        return choices != null && !choices.isEmpty();
    }
}