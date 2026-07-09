package com.pandorasbox.mod.common.event;

import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.item.PandoraBoxItem;
import com.pandorasbox.mod.registries.PandorasBoxItems;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class ItemDropHandler {

    private ItemDropHandler() {}

    /**
     * Запрещает выбрасывание Pandora's Box. Если предмет всё же пропал (например, из-за бага),
     * возвращает его в инвентарь.
     */
    @SubscribeEvent
    public static void onItemToss(ItemTossEvent event) {
        ItemStack stack = event.getEntity().getItem();
        if (stack.getItem() instanceof PandoraBoxItem) {
            event.setCanceled(true);
            // Проверяем, есть ли предмет в инвентаре (на случай, если он уже удалён)
            Player player = event.getPlayer();
            boolean hasBox = false;
            for (ItemStack item : player.getInventory().items) {
                if (item.getItem() instanceof PandoraBoxItem) {
                    hasBox = true;
                    break;
                }
            }
            if (!hasBox) {
                // Если ящика нет в инвентаре — выдаём новый
                ItemStack box = new ItemStack(PandorasBoxItems.PANDORA_BOX.get());
                if (!player.getInventory().add(box)) {
                    player.drop(box, false);
                }
                PandorasBoxMod.LOGGER.warn("Pandora's Box was missing after toss attempt, restored for {}", player.getName().getString());
            }
        }
    }

    /**
     * Удаляет Pandora's Box из выпадающих при смерти предметов.
     */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        event.getDrops().removeIf(entityItem ->
                entityItem.getItem().getItem() instanceof PandoraBoxItem);
    }

    /**
     * На респауне гарантируем, что предмет снова в инвентаре, если его нет.
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        Player player = event.getEntity();
        boolean hasBox = false;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.getItem() instanceof PandoraBoxItem) {
                hasBox = true;
                break;
            }
        }
        if (!hasBox) {
            ItemStack box = new ItemStack(PandorasBoxItems.PANDORA_BOX.get());
            if (!player.getInventory().add(box)) {
                player.drop(box, false);
            }
            PandorasBoxMod.LOGGER.info("Pandora's Box restored on respawn for {}", player.getName().getString());
        }
    }
}