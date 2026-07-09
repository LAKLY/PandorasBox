package com.pandorasbox.mod.common.item;

import com.pandorasbox.mod.common.config.ConfigLoader;
import com.pandorasbox.mod.common.dialogue.server.DialogueManager;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class PandoraBoxItem extends Item {

    public PandoraBoxItem(Properties properties) {
        super(properties
                .stacksTo(1)
                .rarity(Rarity.EPIC)
        );
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            if (player.isShiftKeyDown()) {
                // Shift+ПКМ: открыть журнал заданий (клиент сам откроет)
                com.pandorasbox.mod.PandorasBoxMod.LOGGER.debug(
                        "{} requested quest log via Pandora's Box", player.getName().getString());
            } else {
                // Обычный ПКМ: случайная фраза — проверяем, не активен ли диалог
                if (DialogueManager.isDialogueActive(player)) {
                    // Если диалог уже идёт, не прерываем его
                    player.displayClientMessage(
                            Component.literal("Pandora is already talking..."),
                            true
                    );
                    return InteractionResultHolder.success(stack);
                }

                String phraseId = ConfigLoader.getRandomPhrase();
                if (phraseId != null && !phraseId.isEmpty()) {
                    if (player instanceof ServerPlayer serverPlayer) {
                        DialogueManager.startDialogue(serverPlayer, phraseId);
                    }
                } else {
                    player.displayClientMessage(
                            Component.literal("Pandora's Box is silent..."),
                            true
                    );
                }
            }
        } else {
            // Клиентская сторона: открываем GUI только при Shift+ПКМ
            if (player.isShiftKeyDown()) {
                com.pandorasbox.mod.client.screen.QuestLogScreenOpener.open(player);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.pandorasbox.pandora_box.tooltip")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("item.pandorasbox.pandora_box.tooltip.hint")
                .withStyle(ChatFormatting.GRAY));
    }
}