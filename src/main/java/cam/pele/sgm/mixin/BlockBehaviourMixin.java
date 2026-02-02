package cam.pele.sgm.mixin;

import cam.pele.sgm.client.ClientRuleCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockBehaviour.class)
public class BlockBehaviourMixin {

    @Inject(method = "getDestroyProgress", at = @At("HEAD"), cancellable = true)
    public void onGetDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos,
            CallbackInfoReturnable<Float> cir) {
        // Warning: This is a client-side (or shared) cache check.
        // On server it's empty, so it won't affect anything.
        // On client, it returns true if the block is currently denied block-breaking.
        if (ClientRuleCache.isDenied(state)) {
            // 0.0F means impossible to break (e.g. Bedrock)
            cir.setReturnValue(0.0F);
        }
    }
}
