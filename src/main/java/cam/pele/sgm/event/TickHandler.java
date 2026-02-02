package cam.pele.sgm.event;

import cam.pele.sgm.SGM;
import cam.pele.sgm.logic.LifecycleManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SGM.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TickHandler {

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide())
            return;

        // Run every 20 ticks (1 second) to optimize performance
        if (event.level.getGameTime() % 20 != 0)
            return;

        ServerLevel level = (ServerLevel) event.level;

        // Iterate over loaded chunks
        for (net.minecraft.world.level.chunk.LevelChunk chunk : ChunkTracker.getLoadedChunks()) {
            // Ensure chunk belongs to this level (in case of multi-world)
            if (chunk.getLevel() == level) {
                LifecycleManager.tickChunk(chunk, level);
            }
        }
    }
}
