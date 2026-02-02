package cam.pele.sgm;

import com.mojang.logging.LogUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(SGM.MODID)
public class SGM {
    public static final String MODID = "structure_grief_manager";
    public static final Logger LOGGER = LogUtils.getLogger();

    public SGM(FMLJavaModLoadingContext context) {
        // Register the commonSetup method for modloading
        context.getModEventBus().addListener(this::commonSetup);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        // Load configuration
        cam.pele.sgm.config.SgmConfigManager.load();
        // Register Networking
        cam.pele.sgm.network.SgmNetwork.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("Structure Grief Manager server starting");
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class DataGenEvents {
        @SubscribeEvent
        public static void onGatherData(net.minecraftforge.data.event.GatherDataEvent event) {
            net.minecraft.data.DataGenerator generator = event.getGenerator();
            net.minecraft.data.PackOutput output = generator.getPackOutput();

            generator.addProvider(event.includeClient(), new cam.pele.sgm.datagen.SchemaProvider(output));
        }
    }
}
