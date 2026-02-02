package cam.pele.sgm.event;

import cam.pele.sgm.SGM;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = SGM.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ChunkTracker {

    private static final Set<LevelChunk> LOADED_CHUNKS = ConcurrentHashMap.newKeySet();

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!event.getLevel().isClientSide() && event.getChunk() instanceof LevelChunk chunk) {
            LOADED_CHUNKS.add(chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!event.getLevel().isClientSide() && event.getChunk() instanceof LevelChunk chunk) {
            LOADED_CHUNKS.remove(chunk);
        }
    }

    public static Set<LevelChunk> getLoadedChunks() {
        return Collections.unmodifiableSet(LOADED_CHUNKS);
    }
}
