package cam.pele.sgm.logic;

import cam.pele.sgm.data.RestoringBlock;
import cam.pele.sgm.data.SgmCapEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

public class RestorationHelper {

    /**
     * Schedules a block for restoration and immediately replaces it with AIR.
     * Use this when a rule action is ALLOW_RESPAWN.
     *
     * @param level The server level
     * @param pos   The position of the block
     * @param state The original state of the block
     * @param timer The time in ticks until restoration
     */
    public static void scheduleRestoration(ServerLevel level, BlockPos pos, BlockState state, int timer) {
        LevelChunk chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);

        chunk.getCapability(SgmCapEvents.CHUNK_DATA).ifPresent(data -> {
            long targetTime = level.getGameTime() + timer;
            data.addRestoringBlock(new RestoringBlock(pos, state, targetTime));
            chunk.setUnsaved(true);
        });

        // Manually set to AIR to simulate destruction without drops
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
    }
}
