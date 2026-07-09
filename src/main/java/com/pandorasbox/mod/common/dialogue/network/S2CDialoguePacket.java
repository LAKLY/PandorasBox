package com.pandorasbox.mod.common.dialogue.network;

import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.dialogue.api.DialogueChoice;
import com.pandorasbox.mod.common.dialogue.api.DialogueType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record S2CDialoguePacket(
        String treeId,
        String nodeId,
        String speaker,
        Component text,
        int duration,
        DialogueType dialogueType,
        List<DialogueChoice> choices
) implements CustomPacketPayload {

    public static final Type<S2CDialoguePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PandorasBoxMod.MODID, "s2c_dialogue"));

    private static final StreamCodec<RegistryFriendlyByteBuf, String> STRING_CODEC = ByteBufCodecs.STRING_UTF8.cast();
    private static final StreamCodec<RegistryFriendlyByteBuf, Integer> INT_CODEC = ByteBufCodecs.VAR_INT.cast();
    private static final StreamCodec<RegistryFriendlyByteBuf, DialogueType> ENUM_CODEC = ByteBufCodecs.idMapper(id -> DialogueType.values()[id], DialogueType::ordinal).cast();

    // Ручной кодек для DialogueChoice (4 поля)
    private static final StreamCodec<RegistryFriendlyByteBuf, DialogueChoice> CHOICE_CODEC = StreamCodec.of(
            (buf, choice) -> {
                INT_CODEC.encode(buf, choice.id());
                ComponentSerialization.STREAM_CODEC.encode(buf, choice.text());
                STRING_CODEC.encode(buf, choice.nextNodeId());
                STRING_CODEC.encode(buf, choice.onSelectQuestTaskId() != null ? choice.onSelectQuestTaskId() : "");
            },
            buf -> {
                int id = INT_CODEC.decode(buf);
                Component text = ComponentSerialization.STREAM_CODEC.decode(buf);
                String nextNodeId = STRING_CODEC.decode(buf);
                String onSelectQuestTaskId = STRING_CODEC.decode(buf);
                return new DialogueChoice(id, text, nextNodeId, onSelectQuestTaskId.isEmpty() ? null : onSelectQuestTaskId);
            }
    );

    // Кодек для списка выборов
    private static final StreamCodec<RegistryFriendlyByteBuf, List<DialogueChoice>> CHOICES_LIST_CODEC = CHOICE_CODEC.apply(ByteBufCodecs.list());

    // Основной кодек пакета
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CDialoguePacket> CODEC = StreamCodec.of(
            S2CDialoguePacket::encode,
            S2CDialoguePacket::decode
    );

    private static void encode(RegistryFriendlyByteBuf buf, S2CDialoguePacket packet) {
        STRING_CODEC.encode(buf, packet.treeId());
        STRING_CODEC.encode(buf, packet.nodeId());
        STRING_CODEC.encode(buf, packet.speaker());
        ComponentSerialization.STREAM_CODEC.encode(buf, packet.text());
        INT_CODEC.encode(buf, packet.duration());
        ENUM_CODEC.encode(buf, packet.dialogueType());
        CHOICES_LIST_CODEC.encode(buf, packet.choices());
    }

    private static S2CDialoguePacket decode(RegistryFriendlyByteBuf buf) {
        return new S2CDialoguePacket(
                STRING_CODEC.decode(buf),
                STRING_CODEC.decode(buf),
                STRING_CODEC.decode(buf),
                ComponentSerialization.STREAM_CODEC.decode(buf),
                INT_CODEC.decode(buf),
                ENUM_CODEC.decode(buf),
                CHOICES_LIST_CODEC.decode(buf)
        );
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}