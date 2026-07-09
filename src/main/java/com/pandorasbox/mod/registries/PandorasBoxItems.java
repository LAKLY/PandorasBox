package com.pandorasbox.mod.registries;

import com.pandorasbox.mod.PandorasBoxMod;
import com.pandorasbox.mod.common.item.PandoraBoxItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class PandorasBoxItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(PandorasBoxMod.MODID);

    public static final DeferredHolder<Item, PandoraBoxItem> PANDORA_BOX =
            ITEMS.registerItem("pandora_box", PandoraBoxItem::new,
                    new Item.Properties());
}
