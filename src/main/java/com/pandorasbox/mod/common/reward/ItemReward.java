package com.pandorasbox.mod.common.reward;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemReward extends Reward {

    private final List<ItemStack> items = new ArrayList<>();

    public void addItem(ItemStack stack) { items.add(stack); }

    @Override
    public void giveReward(Player player) {
        for (ItemStack item : items) {
            ItemStack copy = item.copy();
            if (!player.getInventory().add(copy)) {
                player.drop(copy, false);
            }
        }
    }
}
