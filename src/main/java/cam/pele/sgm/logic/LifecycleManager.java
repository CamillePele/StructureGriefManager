package cam.pele.sgm.logic;

import cam.pele.sgm.SGM;
import cam.pele.sgm.config.model.DropStrategy;
import cam.pele.sgm.data.DecayingBlock;
import cam.pele.sgm.data.RestoringBlock;
import cam.pele.sgm.data.SgmCapEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.Iterator;

public class LifecycleManager {

    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY_TICKS = 20;

    public static void tickChunk(LevelChunk chunk, ServerLevel level) {
        chunk.getCapability(SgmCapEvents.CHUNK_DATA).ifPresent(data -> {
            long gameTime = level.getGameTime();
            boolean dirty = false;

            // Process Restoring Blocks (Respawn)
            Iterator<RestoringBlock> restoringIterator = data.getRestoringBlocks().iterator();
            while (restoringIterator.hasNext()) {
                RestoringBlock block = restoringIterator.next();
                if (block.targetTime <= gameTime) {
                    if (block.state.canSurvive(level, block.pos)) {
                        net.minecraft.world.level.block.state.BlockState current = level.getBlockState(block.pos);
                        if (!current.isAir() && !current.is(block.state.getBlock())) {
                            level.destroyBlock(block.pos, true);
                        }

                        // Support OK - Respawn
                        // Flag 18 = 16 (NO_NEIGHBOR_REACTIONS) + 2 (UPDATE_CLIENTS)
                        // This prevents multi-blocks (doors) from self-destructing if their partner is
                        // missing
                        level.setBlock(block.pos, block.state, 18);
                        data.removeDecayingBlock(block.pos);
                        level.destroyBlockProgress(block.pos.hashCode(), block.pos, -1);
                        SGM.LOGGER.debug("Respawned block at {}", block.pos);
                        restoringIterator.remove();
                        dirty = true;
                    } else {
                        // Support Missing - Retry
                        block.retryCount++;
                        if (block.retryCount > MAX_RETRIES) {
                            SGM.LOGGER.debug("Abandoned respawn at {} after {} retries", block.pos, MAX_RETRIES);
                            restoringIterator.remove();
                            dirty = true;
                        } else {
                            block.targetTime += RETRY_DELAY_TICKS;
                            dirty = true;
                        }
                    }
                }
            }

            // Process Decaying Blocks (Destruction)
            Iterator<DecayingBlock> decayingIterator = data.getDecayingBlocks().iterator();
            while (decayingIterator.hasNext()) {
                DecayingBlock block = decayingIterator.next();
                if (block.targetTime <= gameTime) {
                    // Destroy
                    if (block.strategy == DropStrategy.NORMAL) {
                        level.destroyBlock(block.pos, true);
                    } else {
                        level.setBlock(block.pos, Blocks.AIR.defaultBlockState(), 18);
                    }
                    // Clear visual cracks
                    level.destroyBlockProgress(block.pos.hashCode(), block.pos, -1);
                    SGM.LOGGER.debug("Decayed block at {}", block.pos);
                    decayingIterator.remove();
                    dirty = true;
                } else {
                    // Visualize Decay (Cracks)
                    // Configurable? No, hardcoded for now.
                    long timeLeft = block.targetTime - gameTime;
                    long totalDuration = 100;
                    // Let's assume 100 ticks for normalization
                    // Progress 0 to 9. 0 = slight crack, 9 = almost broken.
                    // We want cracks to GROW as time decreases.
                    // timeLeft: 100 -> 0. Progress: 0 -> 9.
                    int progress = (int) (9 - (timeLeft * 9 / totalDuration));
                    if (progress < 0)
                        progress = 0;
                    if (progress > 9)
                        progress = 9;

                    // Send packet to all nearby players
                    level.destroyBlockProgress(block.pos.hashCode(), block.pos, progress);
                }
            }

            if (dirty) {
                chunk.setUnsaved(true);
            }
        });
    }
}
