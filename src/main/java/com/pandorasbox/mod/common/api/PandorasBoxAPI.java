package com.pandorasbox.mod.common.api;

import com.google.gson.JsonObject;
import com.pandorasbox.mod.common.data.PlayerDataManager;
import com.pandorasbox.mod.common.dialogue.server.DialogueManager;
import com.pandorasbox.mod.common.event.ActionCommentaryHandler;
import com.pandorasbox.mod.common.quest.Quest;
import com.pandorasbox.mod.common.quest.QuestChain;
import com.pandorasbox.mod.common.quest.QuestManager;
import com.pandorasbox.mod.common.quest.QuestTask;
import com.pandorasbox.mod.common.reward.Reward;
import com.pandorasbox.mod.common.reward.RewardHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.function.Function;

public final class PandorasBoxAPI {

    private static final PandorasBoxAPI INSTANCE = new PandorasBoxAPI();

    private PandorasBoxAPI() {}

    public static PandorasBoxAPI get() { return INSTANCE; }

    public void registerQuest(Quest quest) {
        QuestManager.getInstance().registerQuest(quest);
    }

    public void registerQuestChain(QuestChain chain) {
        QuestManager.getInstance().registerQuestChain(chain);
    }

    public void giveQuestToPlayer(Player player, String questId) {
        QuestManager.getInstance().startQuest(player, questId);
    }

    public void completeQuestForPlayer(Player player, String questId) {
        QuestManager.getInstance().completeQuest(player, questId);
    }

    public void registerTaskType(String typeId, Function<JsonObject, QuestTask> parser) {
        com.pandorasbox.mod.common.config.ConfigLoader.registerTaskType(typeId, parser);
    }

    public void registerRewardType(String typeId, Function<JsonObject, Reward> parser) {
        RewardHandler.registerRewardType(typeId, parser);
    }

    // Запуск диалога через новую систему
    public void playDialogue(Player player, String dialogueId) {
        if (player instanceof ServerPlayer serverPlayer) {
            DialogueManager.startDialogue(serverPlayer, dialogueId);
        }
    }

    // Выбор ответа в диалоге
    public void onDialogueChoiceMade(Player player, String dialogueId, int choiceId) {
        if (player instanceof ServerPlayer serverPlayer) {
            DialogueManager.selectChoice(serverPlayer, dialogueId, choiceId);
        }
    }

    public void addActionCommentary(String eventId, String triggerType, String matchValue,
                                    String dialogueId, boolean oneTime) {
        ActionCommentaryHandler.registerSimple(eventId, triggerType, matchValue, dialogueId, oneTime);
    }

    public void grantPermission(Player player, String permissionId) {
        PlayerDataManager.addPermission(player, permissionId);
    }

    public boolean hasPermission(Player player, String permissionId) {
        return PlayerDataManager.hasPermission(player, permissionId);
    }
}