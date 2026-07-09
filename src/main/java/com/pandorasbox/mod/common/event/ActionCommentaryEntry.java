package com.pandorasbox.mod.common.event;

public class ActionCommentaryEntry {

    private String eventId;
    private String triggerType; // entity_killed, block_broken, player_death, dimension_change, time_change
    private String matchValue;  // например "minecraft:zombie" или "minecraft:the_nether"
    private String dialogueId;
    private boolean isOneTime;

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }

    public String getMatchValue() { return matchValue; }
    public void setMatchValue(String matchValue) { this.matchValue = matchValue; }

    public String getDialogueId() { return dialogueId; }
    public void setDialogueId(String dialogueId) { this.dialogueId = dialogueId; }

    public boolean isOneTime() { return isOneTime; }
    public void setOneTime(boolean oneTime) { isOneTime = oneTime; }
}
