package cam.pele.sgm.event;

import cam.pele.sgm.SGM;
import cam.pele.sgm.logic.RuleEvaluationService;
import cam.pele.sgm.logic.RuleResult;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.BlockItem;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SGM.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class PlaceHandler {

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer))
            return;

        if (!(event.getItemStack().getItem() instanceof BlockItem))
            return; // Only filter block placement

        ServerPlayer player = (ServerPlayer) event.getEntity();
        net.minecraft.server.level.ServerLevel level = (net.minecraft.server.level.ServerLevel) event.getLevel();
        // Calculate the position where the block WOULD be placed
        net.minecraft.core.BlockPos targetPos = event.getPos().relative(event.getFace());

        // Pass level and player to evaluatePlace
        RuleResult result = RuleEvaluationService.evaluatePlace(level, player, targetPos, event.getItemStack());

        if (result.type() == RuleResult.Type.DENIED) {
            event.setCanceled(true);
            player.displayClientMessage(Component.literal("§c[SGM] Place Denied"), true);
        } else if (result.type() == RuleResult.Type.ALLOW_DECAY) {
            // Let the placement happen, but register it for decay
            // We need to fetch the chunk where the block IS being placed (targetPos)
            net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunk(targetPos.getX() >> 4,
                    targetPos.getZ() >> 4);

            chunk.getCapability(cam.pele.sgm.data.SgmCapEvents.CHUNK_DATA).ifPresent(data -> {
                // Use the timer from result (in ticks)
                long targetTime = level.getGameTime() + result.timer();

                data.addDecayingBlock(new cam.pele.sgm.data.DecayingBlock(targetPos, targetTime,
                        cam.pele.sgm.config.model.DropStrategy.NORMAL));
                // SGM.LOGGER.debug("Block scheduled for decay at {} in {} ticks", targetPos,
                // result.timer());
                chunk.setUnsaved(true);
            });
        }
    }
}
