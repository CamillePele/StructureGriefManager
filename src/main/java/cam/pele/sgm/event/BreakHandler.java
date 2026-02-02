package cam.pele.sgm.event;

import cam.pele.sgm.SGM;
import cam.pele.sgm.logic.RuleEvaluationService;
import cam.pele.sgm.logic.RuleResult;
import cam.pele.sgm.data.SgmCapEvents;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SGM.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BreakHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide() || !(event.getPlayer() instanceof ServerPlayer))
            return;

        ServerPlayer player = (ServerPlayer) event.getPlayer();
        net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) event.getLevel();

        SGM.LOGGER.debug("BreakEvent fired for {}", event.getState().getBlock());

        LevelChunk chunk = level.getChunkAt(event.getPos());
        java.util.concurrent.atomic.AtomicBoolean wasDecaying = new java.util.concurrent.atomic.AtomicBoolean(false);
        chunk.getCapability(SgmCapEvents.CHUNK_DATA).ifPresent(data -> {
            if (data.removeDecayingBlock(event.getPos())) {
                wasDecaying.set(true);
                chunk.setUnsaved(true);
            }
        });

        if (wasDecaying.get()) {
            SGM.LOGGER.debug("Break ALLOWED: Decaying block removed");
            return;
        }

        // Evaluate rules for this event
        RuleResult result = RuleEvaluationService.evaluateBreak(level, player, event.getPos(), event.getState());

        if (result.type() == RuleResult.Type.DENIED) {
            SGM.LOGGER.debug("Break DENIED");
            event.setCanceled(true);
            player.displayClientMessage(Component.literal("§c[SGM] Break Denied"), true);
        } else if (result.type() == RuleResult.Type.ALLOW_RESPAWN) {
            // Handle dependents (torches, etc.) first
            cam.pele.sgm.util.DependencyHelper.handleDependentBlocks(level, event.getPos(), result.timer());

            // Save block to memory via Helper
            cam.pele.sgm.logic.RestorationHelper.scheduleRestoration(level, event.getPos(), event.getState(),
                    result.timer());

            // Cancel vanilla break to prevent drops (block is already set to AIR by helper)
            event.setCanceled(true);
        } else {
            SGM.LOGGER.debug("Break ALLOWED: {}", result);
        }
    }
}
