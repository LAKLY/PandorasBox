package com.pandorasbox.mod.common.event;

import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.config.ConfigLoader;
import com.pandorasbox.mod.common.data.PlayerDataManager;
import com.pandorasbox.mod.common.dialogue.server.DialogueManager;
import com.pandorasbox.mod.common.quest.QuestManager;
import com.pandorasbox.mod.registries.PandorasBoxItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.List;
import java.util.Random;

public class PandorasBoxEventHandler {

    private static final Random RANDOM = new Random();

    private PandorasBoxEventHandler() {}

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();

        PlayerDataManager.loadPlayerData(player);

        if (!PlayerDataManager.hasPermission(player, "received_pandora_box")) {
            ItemStack pandoraBox = new ItemStack(PandorasBoxItems.PANDORA_BOX.get());
            if (!player.getInventory().add(pandoraBox)) {
                player.drop(pandoraBox, false);
            }
            PlayerDataManager.addPermission(player, "received_pandora_box");

            String initialQuest = ConfigLoader.getInitialQuest();
            if (initialQuest != null && !initialQuest.isEmpty()) {
                QuestManager.getInstance().startQuest(player, initialQuest);
            } else {
                PandorasBoxMod.LOGGER.warn("Initial quest not configured in settings.json");
            }

            PlayerDataManager.savePlayerData(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PlayerDataManager.savePlayerData(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        QuestManager.getInstance().updateActiveQuests(player, event);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // Обработка для убийцы (если убийца — игрок)
        Entity killer = event.getSource().getEntity();
        if (killer instanceof Player player) {
            // Игрок убил моба или другого игрока — обновляем квесты убийцы
            PandorasBoxMod.LOGGER.info("Player {} killed entity {}", player.getName().getString(),
                    BuiltInRegistries.ENTITY_TYPE.getKey(event.getEntity().getType()));
            QuestManager.getInstance().updateActiveQuests(player, event);
        }

        // Если убит игрок (для диалогов смерти и квестов на смерть)
        if (event.getEntity() instanceof Player player) {
            if (player.level().isClientSide) return;

            // Обновляем квесты самого игрока (если есть квесты на смерть)
            QuestManager.getInstance().updateActiveQuests(player, event);

            // Диалоги смерти
            List<String> deathDialogues = ConfigLoader.getDeathDialogues();
            if (!deathDialogues.isEmpty()) {
                String dialogue = deathDialogues.get(RANDOM.nextInt(deathDialogues.size()));
                if (player instanceof ServerPlayer serverPlayer) {
                    if (DialogueManager.getTree(dialogue) != null) {
                        DialogueManager.startDialogue(serverPlayer, dialogue);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (player == null || player.level().isClientSide) return;

        QuestManager.getInstance().updateActiveQuests(player, event);

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock());
        if (blockId != null) {
            ActionCommentaryHandler.checkAction(player, "block_broken", blockId.toString());
        }
    }
}