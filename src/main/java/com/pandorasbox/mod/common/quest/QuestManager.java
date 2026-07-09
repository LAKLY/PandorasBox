package com.pandorasbox.mod.common.quest;

import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.dialogue.server.DialogueManager;
import com.pandorasbox.mod.common.reward.RewardHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class QuestManager {

    private static final QuestManager INSTANCE = new QuestManager();

    private final QuestRegistry questRegistry = new QuestRegistry();
    private final Map<UUID, PlayerQuestProgress> playerProgress = new ConcurrentHashMap<>();

    private QuestManager() {}

    public static QuestManager getInstance() { return INSTANCE; }

    public QuestRegistry getRegistry() { return questRegistry; }

    public void registerQuest(Quest quest) {
        questRegistry.register(quest.getQuestId(), quest);
    }

    public void registerQuestChain(QuestChain chain) {
        questRegistry.registerChain(chain.getChainId(), chain);
    }

    public PlayerQuestProgress getOrCreateProgress(Player player) {
        return playerProgress.computeIfAbsent(player.getUUID(),
                k -> new PlayerQuestProgress(player.getUUID()));
    }

    public PlayerQuestProgress getPlayerProgress(Player player) {
        return playerProgress.get(player.getUUID());
    }

    public void setPlayerProgress(Player player, PlayerQuestProgress progress) {
        playerProgress.put(player.getUUID(), progress);
    }

    public void startQuest(Player player, String questId) {
        Quest quest = questRegistry.getQuest(questId);
        if (quest == null) {
            PandorasBoxMod.LOGGER.warn("Quest not found: {}", questId);
            return;
        }

        PlayerQuestProgress progress = getOrCreateProgress(player);
        if (progress.isActive(questId) || progress.isCompleted(questId)) {
            PandorasBoxMod.LOGGER.debug("Quest {} already started or completed for player {}", questId, player.getName().getString());
            return;
        }

        progress.startQuest(quest);
        PandorasBoxMod.LOGGER.info("Started quest {} for player {}", questId, player.getName().getString());

        // Запуск стартового диалога, если он указан в квесте
        if (player instanceof ServerPlayer serverPlayer) {
            String startDialogue = quest.getStartDialogueId();
            if (startDialogue != null && !startDialogue.isEmpty()) {
                if (DialogueManager.getTree(startDialogue) != null) {
                    DialogueManager.startDialogue(serverPlayer, startDialogue);
                } else {
                    PandorasBoxMod.LOGGER.warn("Start dialogue '{}' for quest '{}' not found", startDialogue, questId);
                }
            }
        }
    }

    public void completeQuest(Player player, String questId) {
        Quest quest = questRegistry.getQuest(questId);
        if (quest == null) {
            PandorasBoxMod.LOGGER.warn("Quest not found for completion: {}", questId);
            return;
        }

        PlayerQuestProgress progress = getOrCreateProgress(player);
        progress.completeQuest(questId);
        PandorasBoxMod.LOGGER.info("Completed quest {} for player {}", questId, player.getName().getString());

        RewardHandler.giveRewards(player, quest.getRewards());

        if (quest.getOnCompletionDialogueId() != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                if (DialogueManager.getTree(quest.getOnCompletionDialogueId()) != null) {
                    DialogueManager.startDialogue(serverPlayer, quest.getOnCompletionDialogueId());
                } else {
                    PandorasBoxMod.LOGGER.warn("Completion dialogue '{}' for quest '{}' not found", quest.getOnCompletionDialogueId(), questId);
                }
            }
        }

        if (quest.getNextQuestId() != null) {
            startQuest(player, quest.getNextQuestId());
        }
    }

    public void updateActiveQuests(Player player, Event event) {
        PlayerQuestProgress progress = getPlayerProgress(player);
        if (progress == null) return;

        List<String> toComplete = new ArrayList<>();
        for (String questId : new ArrayList<>(progress.getActiveQuestIds())) {
            Quest quest = questRegistry.getQuest(questId);
            if (quest == null) continue;

            for (QuestTask task : quest.getTasks()) {
                task.onEvent(event);
            }

            if (quest.isCompleted()) {
                PandorasBoxMod.LOGGER.info("Quest {} is now completed for player {}", questId, player.getName().getString());
                toComplete.add(questId);
            }
        }

        for (String questId : toComplete) {
            completeQuest(player, questId);
        }
    }

    public List<Quest> getActiveQuests(Player player) {
        PlayerQuestProgress progress = getPlayerProgress(player);
        List<Quest> result = new ArrayList<>();
        if (progress == null) return result;
        for (String id : progress.getActiveQuestIds()) {
            Quest quest = questRegistry.getQuest(id);
            if (quest != null) result.add(quest);
        }
        return result;
    }
}