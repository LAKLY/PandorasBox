package com.pandorasbox.mod.common.data;

import com.pandorasbox.mod.common.quest.PlayerQuestProgress;
import com.pandorasbox.mod.common.quest.QuestManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public class PlayerDataManager {

    private static final String DATA_KEY = "PandoraBoxData";

    private PlayerDataManager() {}

    public static void savePlayerData(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        CompoundTag tag = serverPlayer.getPersistentData().getCompound(DATA_KEY);

        PlayerQuestProgress progress = QuestManager.getInstance().getPlayerProgress(player);
        if (progress != null) {
            tag.put("QuestProgress", progress.serializeNBT());
        }

        serverPlayer.getPersistentData().put(DATA_KEY, tag);
    }

    public static void loadPlayerData(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        CompoundTag tag = serverPlayer.getPersistentData().getCompound(DATA_KEY);
        if (tag.isEmpty()) return;

        if (tag.contains("QuestProgress")) {
            PlayerQuestProgress progress = new PlayerQuestProgress(player.getUUID());
            progress.deserializeNBT(tag.getCompound("QuestProgress"));
            QuestManager.getInstance().setPlayerProgress(player, progress);
        }
    }

    public static void addPermission(Player player, String permissionId) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        CompoundTag tag = serverPlayer.getPersistentData().getCompound(DATA_KEY);
        CompoundTag permissions = tag.getCompound("Permissions");
        permissions.putBoolean(permissionId, true);
        tag.put("Permissions", permissions);
        serverPlayer.getPersistentData().put(DATA_KEY, tag);
    }

    public static boolean hasPermission(Player player, String permissionId) {
        if (!(player instanceof ServerPlayer serverPlayer)) return false;

        CompoundTag tag = serverPlayer.getPersistentData().getCompound(DATA_KEY);
        CompoundTag permissions = tag.getCompound("Permissions");
        return permissions.getBoolean(permissionId);
    }

    public static boolean hasQuestStarted(Player player, String questId) {
        PlayerQuestProgress progress = QuestManager.getInstance().getPlayerProgress(player);
        return progress != null && (progress.isActive(questId) || progress.isCompleted(questId));
    }
}
