package com.pandorasbox.mod.common.dialogue.network;

import com.pandorasbox.mod.PandorasBoxMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record C2SDialogueAdvancePacket(String treeId) implements CustomPacketPayload {
    public static final Type<C2SDialogueAdvancePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(PandorasBoxMod.MODID, "c2s_advance"));

    public static final StreamCodec<FriendlyByteBuf, C2SDialogueAdvancePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, C2SDialogueAdvancePacket::treeId,
            C2SDialogueAdvancePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}