package cam.pele.sgm.data;

import cam.pele.sgm.SGM;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SGM.MODID)
public class SgmCapEvents {

    public static final Capability<ISgmChunkData> CHUNK_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });

    @SubscribeEvent
    public static void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.register(ISgmChunkData.class);
    }

    @SubscribeEvent
    public static void onAttachChunkCapabilities(AttachCapabilitiesEvent<LevelChunk> event) {
        if (!event.getObject().getLevel().isClientSide) {
            event.addCapability(ResourceLocation.tryBuild(SGM.MODID, "chunk_data"), new SgmChunkDataProvider());
        }
    }
}
