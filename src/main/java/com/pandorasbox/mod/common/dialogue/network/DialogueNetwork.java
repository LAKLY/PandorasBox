package com.pandorasbox.mod.common.dialogue.network;

import com.pandorasbox.mod.common.dialogue.client.ClientDialogueHandler;
import com.pandorasbox.mod.common.dialogue.server.DialogueManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class DialogueNetwork {
    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(DialogueNetwork::registerPackets);
    }

    private static void registerPackets(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1.0.0");

        // Сервер -> Клиент
        registrar.playToClient(
                S2CDialoguePacket.TYPE,
                S2CDialoguePacket.CODEC,
                (payload, context) -> context.enqueueWork(() -> ClientDialogueHandler.handleDialogue(payload))
        );

        // Клиент -> Сервер (выбор варианта)
        registrar.playToServer(
                C2SDialogueChoicePacket.TYPE,
                C2SDialogueChoicePacket.CODEC,
                (payload, context) -> context.enqueueWork(() -> DialogueManager.selectChoice(context.player(), payload.treeId(), payload.choiceId()))
        );

        // Клиент -> Сервер (продвижение диалога кликом)
        registrar.playToServer(
                C2SDialogueAdvancePacket.TYPE,
                C2SDialogueAdvancePacket.CODEC,
                (payload, context) -> context.enqueueWork(() -> DialogueManager.advanceDialogue(context.player(), payload.treeId()))
        );
    }
}