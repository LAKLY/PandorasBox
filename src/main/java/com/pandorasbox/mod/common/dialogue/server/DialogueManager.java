package com.pandorasbox.mod.common.dialogue.server;

import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.dialogue.DialogueHandler;
import com.pandorasbox.mod.common.dialogue.api.DialogueChoice;
import com.pandorasbox.mod.common.dialogue.api.DialogueNode;
import com.pandorasbox.mod.common.dialogue.api.DialogueTree;
import com.pandorasbox.mod.common.dialogue.api.DialogueType;
import com.pandorasbox.mod.common.dialogue.network.S2CDialoguePacket;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class DialogueManager {
    private static final Map<String, DialogueTree> DIALOGUE_TREES = new HashMap<>();
    private static final Map<UUID, DialogueState> activeSessions = new ConcurrentHashMap<>();

    private static final Map<UUID, Long> autoAdvanceTimes = new ConcurrentHashMap<>();
    private static final Map<UUID, String> autoAdvanceNodes = new ConcurrentHashMap<>();

    public static void registerTree(DialogueTree tree) {
        DIALOGUE_TREES.put(tree.treeId(), tree);
    }

    public static DialogueTree getTree(String treeId) {
        return DIALOGUE_TREES.get(treeId);
    }

    // Проверка, активен ли диалог у игрока
    public static boolean isDialogueActive(Player player) {
        return activeSessions.containsKey(player.getUUID());
    }

    public static void startDialogue(Player player, String treeId) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        DialogueTree tree = DIALOGUE_TREES.get(treeId);
        if (tree == null) {
            PandorasBoxMod.LOGGER.error("Dialogue tree not found: {}", treeId);
            return;
        }
        // Если уже есть активный диалог, завершаем его (это для случая, когда диалог стартует из квеста)
        if (activeSessions.containsKey(player.getUUID())) {
            endDialogue(serverPlayer);
        }
        DialogueState state = new DialogueState(treeId, tree.startNodeId());
        activeSessions.put(player.getUUID(), state);
        sendNodeToClient(serverPlayer, tree, tree.startNodeId());
    }

    public static void advanceDialogue(Player player, String treeId) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        DialogueState state = activeSessions.get(player.getUUID());
        if (state == null || !state.getTreeId().equals(treeId)) return;

        DialogueTree tree = DIALOGUE_TREES.get(treeId);
        if (tree == null) return;

        DialogueNode currentNode = tree.getNode(state.getCurrentNodeId());
        if (currentNode != null && !currentNode.hasChoices()) {
            advanceDialogue(serverPlayer, state, tree, currentNode.nextNodeId());
        }
    }

    private static void advanceDialogue(ServerPlayer player, DialogueState state, DialogueTree tree, String nextNodeId) {
        if (nextNodeId == null || nextNodeId.isEmpty() || tree.getNode(nextNodeId) == null) {
            endDialogue(player);
        } else {
            state.setCurrentNodeId(nextNodeId);
            sendNodeToClient(player, tree, nextNodeId);
        }
    }

    public static void selectChoice(Player player, String treeId, int choiceId) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        DialogueState state = activeSessions.get(player.getUUID());
        if (state == null || !state.getTreeId().equals(treeId)) return;

        DialogueTree tree = getTree(treeId);
        if (tree == null) return;

        DialogueNode currentNode = tree.getNode(state.getCurrentNodeId());
        if (currentNode == null) return;

        for (DialogueChoice choice : currentNode.choices()) {
            if (choice.id() == choiceId) {
                if (choice.onSelectQuestTaskId() != null && !choice.onSelectQuestTaskId().isEmpty()) {
                    DialogueHandler.markDialogueTaskCompleted(player, choice.onSelectQuestTaskId(), choiceId);
                }

                state.getVisitedFlags().put(currentNode.id(), true);
                cancelScheduledTask(player.getUUID());
                advanceDialogue(serverPlayer, state, tree, choice.nextNodeId());
                return;
            }
        }
    }

    private static void sendNodeToClient(ServerPlayer player, DialogueTree tree, String nodeId) {
        DialogueNode node = tree.getNode(nodeId);
        if (node == null) return;

        player.connection.send(new S2CDialoguePacket(
                tree.treeId(), node.id(), node.speaker(), node.text(), node.duration(), node.type(), node.choices()
        ));

        UUID playerId = player.getUUID();
        cancelScheduledTask(playerId);

        if (!node.hasChoices()) {
            int delaySeconds = Math.max(node.duration() + 1, 2);
            long triggerTime = System.currentTimeMillis() + (delaySeconds * 1000L);

            autoAdvanceTimes.put(playerId, triggerTime);
            autoAdvanceNodes.put(playerId, nodeId);

            player.server.execute(() -> {
                scheduleTickCheck(player, playerId, tree.treeId(), nodeId, triggerTime);
            });
        }
    }

    private static void scheduleTickCheck(ServerPlayer player, UUID playerId, String treeId, String nodeId, long triggerTime) {
        if (!activeSessions.containsKey(playerId)) return;

        long now = System.currentTimeMillis();
        if (now >= triggerTime) {
            DialogueState currentState = activeSessions.get(playerId);
            Long expectedTime = autoAdvanceTimes.get(playerId);
            String expectedNode = autoAdvanceNodes.get(playerId);

            if (currentState != null && currentState.getTreeId().equals(treeId)
                    && nodeId.equals(expectedNode) && expectedTime != null && now >= expectedTime) {

                DialogueTree tree = DIALOGUE_TREES.get(treeId);
                if (tree != null) {
                    DialogueNode node = tree.getNode(nodeId);
                    if (node != null) {
                        PandorasBoxMod.LOGGER.info("Safe auto-advancing from node {} for player {}", nodeId, player.getName().getString());
                        advanceDialogue(player, currentState, tree, node.nextNodeId());
                    }
                }
            }
        } else {
            if (activeSessions.containsKey(playerId) && autoAdvanceNodes.containsKey(playerId)) {
                player.server.execute(() -> scheduleTickCheck(player, playerId, treeId, nodeId, triggerTime));
            }
        }
    }

    private static void cancelScheduledTask(UUID playerId) {
        autoAdvanceTimes.remove(playerId);
        autoAdvanceNodes.remove(playerId);
    }

    public static void endDialogue(ServerPlayer player) {
        UUID playerId = player.getUUID();
        activeSessions.remove(playerId);
        cancelScheduledTask(playerId);
        player.connection.send(new S2CDialoguePacket("", "", "", Component.empty(), 0, DialogueType.AMBIENT, List.of()));
        PandorasBoxMod.LOGGER.info("Dialogue ended for player {}", player.getName().getString());
    }
}