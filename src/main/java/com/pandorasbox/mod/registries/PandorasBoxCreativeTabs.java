package com.pandorasbox.mod.registries;

import com.pandorasbox.mod.PandorasBoxMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class PandorasBoxCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PandorasBoxMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB =
            TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.pandorasbox.main"))
                    .icon(() -> new ItemStack(PandorasBoxItems.PANDORA_BOX.get()))
                    .displayItems((params, output) -> {
                        output.accept(PandorasBoxItems.PANDORA_BOX.get());
                    })
                    .build());
}
