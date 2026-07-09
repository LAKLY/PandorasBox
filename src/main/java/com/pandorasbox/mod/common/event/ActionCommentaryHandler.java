package com.pandorasbox.mod.common.event;

import com.pandorasbox.mod.common.data.PlayerDataManager;
import com.pandorasbox.mod.common.dialogue.server.DialogueManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class ActionCommentaryHandler {

    private static final List<ActionCommentaryEntry> ENTRIES = new ArrayList<>();

    private ActionCommentaryHandler() {}

    public static void register(ActionCommentaryEntry entry) {
        ENTRIES.add(entry);
    }

    public static void checkAction(Player player, String triggerType, String matchValue) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        for (ActionCommentaryEntry entry : ENTRIES) {
            if (!entry.getTriggerType().equals(triggerType)) continue;
            if (entry.getMatchValue() != null && !entry.getMatchValue().equals(matchValue)) continue;

            String seenKey = "seen_action:" + entry.getEventId();
            if (entry.isOneTime() && PlayerDataManager.hasPermission(player, seenKey)) continue;

            // Используем новую систему диалогов
            DialogueManager.startDialogue(serverPlayer, entry.getDialogueId());

            if (entry.isOneTime()) {
                PlayerDataManager.addPermission(player, seenKey);
            }
        }
    }

    public static void registerSimple(String eventId, String triggerType, String matchValue,
                                      String dialogueId, boolean oneTime) {
        ActionCommentaryEntry entry = new ActionCommentaryEntry();
        entry.setEventId(eventId);
        entry.setTriggerType(triggerType);
        entry.setMatchValue(matchValue);
        entry.setDialogueId(dialogueId);
        entry.setOneTime(oneTime);
        register(entry);
    }
}