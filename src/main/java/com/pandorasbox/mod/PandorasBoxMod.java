package com.pandorasbox.mod;

import com.pandorasbox.mod.common.config.ConfigLoader;
import com.pandorasbox.mod.common.config.EventConfigParser;
import com.pandorasbox.mod.common.dialogue.client.ClientDialogueHandler;
import com.pandorasbox.mod.common.dialogue.client.DialogueOverlay;
import com.pandorasbox.mod.common.dialogue.network.DialogueNetwork;
import com.pandorasbox.mod.common.event.ItemDropHandler;
import com.pandorasbox.mod.common.event.PandorasBoxEventHandler;
import com.pandorasbox.mod.registries.PandorasBoxCreativeTabs;
import com.pandorasbox.mod.registries.PandorasBoxItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(PandorasBoxMod.MODID)
public class PandorasBoxMod {

    public static final String MODID = "pandorasbox";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public PandorasBoxMod(IEventBus modEventBus) {
        // Регистрация Deferred Registers
        PandorasBoxItems.ITEMS.register(modEventBus);
        PandorasBoxCreativeTabs.TABS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        // Регистрация обработчиков событий игры (общая шина)
        NeoForge.EVENT_BUS.register(PandorasBoxEventHandler.class);
        NeoForge.EVENT_BUS.register(ItemDropHandler.class);

        // Инициализация диалоговой сети (без DataLoader)
        DialogueNetwork.init(modEventBus);

        // Регистрация клиентского оверлея
        modEventBus.addListener(this::registerGuiLayers);

        // Клиентский тик для анимации диалогов
        NeoForge.EVENT_BUS.addListener(this::onClientTick);

        LOGGER.info("Pandora's Box mod initializing...");
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // Создаём папки
            ConfigLoader.ensureConfigDirectoriesExist();
            ConfigLoader.copyDefaultConfigsIfMissing();

            // Загружаем квесты
            ConfigLoader.loadAllQuests();

            // Загружаем диалоги из config/pandoras_box/dialogues/
            ConfigLoader.loadAllDialogues();

            // Загружаем случайные фразы
            ConfigLoader.loadRandomPhrases();

            // Загружаем события
            EventConfigParser.loadAllEventConfigs();

            LOGGER.info("Pandora's Box config loading complete.");
        });
    }

    private void registerGuiLayers(RegisterGuiLayersEvent event) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MODID, "dialogue_layer");
        event.registerAboveAll(id, new DialogueOverlay());
    }

    private void onClientTick(ClientTickEvent.Post event) {
        ClientDialogueHandler.clientTick();
    }
}