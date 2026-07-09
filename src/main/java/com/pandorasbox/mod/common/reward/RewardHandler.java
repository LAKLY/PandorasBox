package com.pandorasbox.mod.common.reward;

import com.google.gson.JsonObject;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RewardHandler {

    private static final Map<String, Function<JsonObject, Reward>> REWARD_PARSERS = new HashMap<>();

    static {
        registerRewardType("item", RewardHandler::parseItemReward);
        registerRewardType("experience", RewardHandler::parseExperienceReward);
        registerRewardType("permission", RewardHandler::parsePermissionReward);
    }

    public static void registerRewardType(String typeId, Function<JsonObject, Reward> parser) {
        REWARD_PARSERS.put(typeId, parser);
    }

    public static Reward parseReward(String type, JsonObject json) {
        Function<JsonObject, Reward> parser = REWARD_PARSERS.get(type);
        if (parser == null) {
            throw new IllegalArgumentException("Unknown reward type: " + type);
        }
        return parser.apply(json);
    }

    public static void giveRewards(Player player, List<Reward> rewards) {
        for (Reward reward : rewards) {
            reward.giveReward(player);
            // Отправляем уведомление о награде в чат
            player.sendSystemMessage(
                    Component.literal("Reward received: " + reward.getRewardId())
                            .withStyle(ChatFormatting.GREEN)
            );
        }
    }

    private static Reward parseItemReward(JsonObject json) {
        ItemReward reward = new ItemReward();
        reward.setRewardId(json.has("rewardId") ? json.get("rewardId").getAsString() : "item_reward");
        return reward;
    }

    private static Reward parseExperienceReward(JsonObject json) {
        ExperienceReward reward = new ExperienceReward();
        reward.setAmount(json.get("amount").getAsInt());
        reward.setRewardId(json.has("rewardId") ? json.get("rewardId").getAsString() : "xp_reward");
        return reward;
    }

    private static Reward parsePermissionReward(JsonObject json) {
        PermissionReward reward = new PermissionReward();
        reward.setPermissionId(json.get("permissionId").getAsString());
        reward.setRewardId(json.has("rewardId") ? json.get("rewardId").getAsString() : "permission_reward");
        return reward;
    }
}