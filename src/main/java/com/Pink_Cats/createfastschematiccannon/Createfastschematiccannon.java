package com.Pink_Cats.createfastschematiccannon;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Createfastschematiccannon.MODID)
public class Createfastschematiccannon {

    public static final String MODID = "createfastschematiccannon";
    public static final Logger LOGGER = LogUtils.getLogger();


    public Createfastschematiccannon(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("Create:FasterSchematicCannon: SpeedMultiply:" +Config.SchematicSpeedupPerTick );

    }

}
