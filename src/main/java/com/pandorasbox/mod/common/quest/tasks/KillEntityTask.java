package com.pandorasbox.mod.common.quest.tasks;

import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.quest.QuestTask;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

public class KillEntityTask extends QuestTask {

    private String entityType; // например "minecraft:zombie"

    public void setEntityType(String entityType) { this.entityType = entityType; }
    public String getEntityType() { return entityType; }

    @Override
    public void onEvent(Event event) {
        if (isCompleted()) {
            PandorasBoxMod.LOGGER.debug("KillEntityTask already completed, ignoring event");
            return;
        }
        if (entityType == null) {
            PandorasBoxMod.LOGGER.warn("KillEntityTask entityType is null");
            return;
        }
        if (event instanceof LivingDeathEvent deathEvent) {
            ResourceLocation killedId = BuiltInRegistries.ENTITY_TYPE
                    .getKey(deathEvent.getEntity().getType());
            if (killedId != null) {
                PandorasBoxMod.LOGGER.info("KillEntityTask: killed entity: {}, required: {}, progress: {}/{}",
                        killedId.toString(), entityType, progress, required);
                if (killedId.toString().equals(entityType)) {
                    progress++;
                    PandorasBoxMod.LOGGER.info("KillEntityTask progress increased to {}/{} for {}",
                            progress, required, entityType);
                }
            } else {
                PandorasBoxMod.LOGGER.warn("KillEntityTask: killed entity has no registry name");
            }
        } else {
            PandorasBoxMod.LOGGER.debug("KillEntityTask: event is not LivingDeathEvent, it's {}", event.getClass().getSimpleName());
        }
    }
}