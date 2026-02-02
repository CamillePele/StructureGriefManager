package cam.pele.sgm.util;

import cam.pele.sgm.logic.RuleEvaluationService;
import cam.pele.sgm.logic.RuleResult;
import cam.pele.sgm.logic.RestorationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility class to handle block dependencies during SGM operations.
 * Simulates block removal to detect and handle dependent blocks (e.g., torches,
 * banners)
 * before the actual event occurs, ensuring they are properly protected or
 * restored.
 */
public class DependencyHelper {

    /**
     * Main entry point.
     * Starts the recursive dependency check.
     */
    public static void handleDependentBlocks(ServerLevel level, BlockPos sourcePos) {
        // Use a Set to avoid circular processing
        handleRecursive(level, sourcePos, -1, new HashSet<>());
    }

    /**
     * Overload to pass an existing timer.
     */
    public static void handleDependentBlocks(ServerLevel level, BlockPos sourcePos, int sourceTimer) {
        handleRecursive(level, sourcePos, sourceTimer, new HashSet<>());
    }

    private static void handleRecursive(ServerLevel level, BlockPos sourcePos, int sourceTimer, Set<BlockPos> visited) {
        if (visited.contains(sourcePos))
            return;
        visited.add(sourcePos);

        // 1. Save current state
        BlockState originalState = level.getBlockState(sourcePos);

        // 0. Pre-check: Handle explict multi-block partners (Beds, Doors)
        BlockPos partnerPos = getMultiBlockPartner(level, sourcePos, originalState);
        if (partnerPos != null && !visited.contains(partnerPos)) {
            // Force visit partner BEFORE touching current block
            BlockState partnerState = level.getBlockState(partnerPos);
            processDependentBlock(level, partnerPos, partnerState, sourceTimer, visited);
        }

        // 2. Simulation: Temporarily remove block to trigger dependency checks.
        // Flags: 16 (prevent neighbor updates), 4 (prevent rerender).
        // This allows checking dependent blocks without causing visual glitches or
        // physics updates.
        level.setBlock(sourcePos, Blocks.AIR.defaultBlockState(), 16 | 4);

        try {
            // 3. Scan all 6 neighbors
            for (Direction dir : Direction.values()) {
                BlockPos neighborPos = sourcePos.relative(dir);

                // Skip if already visited
                if (visited.contains(neighborPos))
                    continue;

                BlockState neighborState = level.getBlockState(neighborPos);

                if (neighborState.isAir())
                    continue;

                // 4. Verify structural integrity: Check if neighbor block can validly exist in
                // new state.
                // internal canSurvive() check (e.g. Torches need support).
                if (!neighborState.canSurvive(level, neighborPos)) {

                    // It's a dependent block! It wouldn't survive.
                    processDependentBlock(level, neighborPos, neighborState, sourceTimer, visited);
                }
            }
        } finally {
            // 5. Restoration: Restore original block state to allow standard event
            // processing
            // by the calling handler.
            level.setBlock(sourcePos, originalState, 16 | 4);
        }
    }

    private static void processDependentBlock(ServerLevel level, BlockPos pos, BlockState state, int sourceTimer,
            Set<BlockPos> visited) {
        // Evaluate SGM rules for this dependent block
        RuleResult result = RuleEvaluationService.evaluateBreak(level, null, pos, state);

        boolean shouldSave = false;
        int timer = sourceTimer;

        // If the dependent block is protected (DENY) or configured to RESPAWN
        if (result.type() == RuleResult.Type.ALLOW_RESPAWN || result.type() == RuleResult.Type.DENIED) {
            shouldSave = true;

            // If we didn't inherit a timer, use default or block config
            if (timer == -1) {
                // Here we could fetch the rule-specific timer if we had access to
                // RuleDefinition
                // For now, use a safety default
                timer = result.timer();
            }
        }

        if (shouldSave) {
            // 1. Recursion: Does this dependent block ITSELF have dependents?
            handleRecursive(level, pos, timer, visited);

            RestorationHelper.scheduleRestoration(level, pos, state, timer);
        }
    }

    private static BlockPos getMultiBlockPartner(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
            net.minecraft.world.level.block.state.properties.BedPart part = state
                    .getValue(net.minecraft.world.level.block.BedBlock.PART);
            Direction facing = state.getValue(net.minecraft.world.level.block.BedBlock.FACING);
            return (part == net.minecraft.world.level.block.state.properties.BedPart.FOOT) ? pos.relative(facing)
                    : pos.relative(facing.getOpposite());
        }

        if (state
                .hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            net.minecraft.world.level.block.state.properties.DoubleBlockHalf half = state
                    .getValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.DOUBLE_BLOCK_HALF);
            return (half == net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER) ? pos.above()
                    : pos.below();
        }

        return null;
    }
}