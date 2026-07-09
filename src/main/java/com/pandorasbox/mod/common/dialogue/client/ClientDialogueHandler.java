package com.pandorasbox.mod.common.dialogue.client;

import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.dialogue.network.S2CDialoguePacket;
import net.minecraft.client.Minecraft;

public class ClientDialogueHandler {
    public static S2CDialoguePacket currentDialogue = null;
    public static float alpha = 0.0f;
    private static int tickCounter = 0;
    private static int maxTicks = 0;
    private static boolean fadingOut = false;
    private static String lastPacketId = ""; // для защиты от дублей

    public static void handleDialogue(S2CDialoguePacket packet) {
        PandorasBoxMod.LOGGER.info("Received dialogue packet: treeId={}, nodeId={}, text={}, hasChoices={}",
                packet.treeId(), packet.nodeId(),
                packet.text().getString(),
                packet.choices() != null && !packet.choices().isEmpty());

        // Пустой пакет = конец диалога
        if (packet.treeId().isEmpty()) {
            if (currentDialogue != null) {
                fadingOut = true;
            }
            lastPacketId = "";
            return;
        }

        // Защита от дублирования — если пакет с тем же treeId+nodeId уже обработан, пропускаем
        String packetId = packet.treeId() + ":" + packet.nodeId();
        if (packetId.equals(lastPacketId)) {
            PandorasBoxMod.LOGGER.debug("Ignoring duplicate dialogue packet: {}", packetId);
            return;
        }
        lastPacketId = packetId;

        // Мгновенная замена сообщения
        currentDialogue = packet;
        alpha = 0.0f;               // начинаем с нуля
        tickCounter = 0;
        maxTicks = packet.duration() * 20;
        fadingOut = false;

        // Если есть выбор, открываем GUI
        if (packet.choices() != null && !packet.choices().isEmpty()) {
            PandorasBoxMod.LOGGER.info("Opening choice screen with {} options", packet.choices().size());
            Minecraft.getInstance().tell(() -> Minecraft.getInstance().setScreen(new DialogueChoiceScreen(packet)));
        }
    }

    public static void clientTick() {
        if (currentDialogue == null) {
            if (alpha > 0.0f) alpha = 0.0f;
            return;
        }

        if (!fadingOut) {
            // Появление — быстрее (0.12 за тик)
            if (alpha < 1.0f) {
                alpha = Math.min(1.0f, alpha + 0.12f);
            }
            tickCounter++;
            if (tickCounter >= maxTicks) {
                fadingOut = true;
            }
        } else {
            // Исчезновение — быстрее (0.06 за тик)
            if (alpha > 0.0f) {
                alpha = Math.max(0.0f, alpha - 0.06f);
            } else {
                // Полностью исчезли
                currentDialogue = null;
                fadingOut = false;
                tickCounter = 0;
                lastPacketId = "";
            }
        }
    }

    public static void forceHide() {
        currentDialogue = null;
        alpha = 0.0f;
        fadingOut = false;
        tickCounter = 0;
        lastPacketId = "";
    }
}