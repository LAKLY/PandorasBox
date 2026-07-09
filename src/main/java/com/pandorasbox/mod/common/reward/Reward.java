package com.pandorasbox.mod.common.reward;

import net.minecraft.world.entity.player.Player;

public abstract class Reward {

    protected String rewardId;

    public String getRewardId() { return rewardId; }
    public void setRewardId(String rewardId) { this.rewardId = rewardId; }

    public abstract void giveReward(Player player);
}
