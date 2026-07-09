package com.pandorasbox.mod.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * Утилита для открытия экрана журнала заданий с клиентской стороны.
 * В реальном моде вызывается через ClientOnly код или сетевой пакет с клиента.
 */
public final class QuestLogScreenOpener {

    private QuestLogScreenOpener() {}

    public static void open(Player player) {
        Minecraft.getInstance().setScreen(new QuestLogScreen(player));
    }
}
