package cam.pele.sgm.event;

import cam.pele.sgm.SGM;
import cam.pele.sgm.data.DecayingBlock;
import cam.pele.sgm.data.RestoringBlock;
import cam.pele.sgm.data.SgmCapEvents;
import cam.pele.sgm.logic.RuleEvaluationService;
import cam.pele.sgm.logic.RuleResult;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.ListIterator;

@Mod.EventBusSubscriber(modid = SGM.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExplosionHandler {

    @SubscribeEvent
    public static void onExplosionDetonate(ExplosionEvent.Detonate event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        ServerLevel level = (ServerLevel) event.getLevel();
        List<BlockPos> affectedBlocks = event.getAffectedBlocks();
        ListIterator<BlockPos> iterator = affectedBlocks.listIterator();

        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            BlockState state = level.getBlockState(pos);

            // Avoid processing blocks that might have been removed by dependency handling
            // of previous blocks
            if (state.isAir()) {
                continue;
            }

            // Evaluate rules (no player involved)
            RuleResult result = RuleEvaluationService.evaluateBreak(level, null, pos, state);

            if (result.type() == RuleResult.Type.DENIED) {
                // Remove from affected list so vanilla explosion ignores it (it stays intact)
                iterator.remove();
            } else if (result.type() == RuleResult.Type.ALLOW_RESPAWN) {
                // Remove from affected list to prevent vanilla drop/destruction
                iterator.remove();

                // Handle dependents
                cam.pele.sgm.util.DependencyHelper.handleDependentBlocks(level, pos, result.timer());

                // Handle Respawn Logic via Helper
                cam.pele.sgm.logic.RestorationHelper.scheduleRestoration(level, pos, state, result.timer());
            }
        }
    }
}
