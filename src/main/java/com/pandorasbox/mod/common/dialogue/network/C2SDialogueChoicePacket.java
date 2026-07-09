package com.pandorasbox.mod.common.dialogue.network;

import com.pandorasbox.mod.PandorasBoxMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SDialogueChoicePacket(String treeId, int choiceId) implements CustomPacketPayload {
    public static final Type<C2SDialogueChoicePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PandorasBoxMod.MODID, "c2s_choice"));

    // Здесь FriendlyByteBuf подходит, так как строковый кодек работает с ByteBuf
    public static final StreamCodec<FriendlyByteBuf, C2SDialogueChoicePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2SDialogueChoicePacket::treeId,
            ByteBufCodecs.VAR_INT, C2SDialogueChoicePacket::choiceId,
            C2SDialogueChoicePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}