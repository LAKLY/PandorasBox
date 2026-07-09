package com.pandorasbox.mod.common.reward;

import com.pandorasbox.mod.common.data.PlayerDataManager;
import net.minecraft.world.entity.player.Player;

public class PermissionReward extends Reward {

    private String permissionId;

    public void setPermissionId(String permissionId) { this.permissionId = permissionId; }

    @Override
    public void giveReward(Player player) {
        PlayerDataManager.addPermission(player, permissionId);
    }
}
