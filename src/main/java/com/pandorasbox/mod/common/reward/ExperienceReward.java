package com.pandorasbox.mod.common.reward;

import net.minecraft.world.entity.player.Player;

public class ExperienceReward extends Reward {

    private int amount;

    public void setAmount(int amount) { this.amount = amount; }

    @Override
    public void giveReward(Player player) {
        player.giveExperiencePoints(amount);
    }
}
